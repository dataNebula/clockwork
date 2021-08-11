/*-
 *  
 * Clockwork
 *  
 * Copyright (C) 2019 - 2020 adx
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 */

package com.creditease.adx.clockwork.worker.service.task.execute;

import com.creditease.adx.clockwork.client.service.TaskRerunClientService;
import com.creditease.adx.clockwork.client.service.TaskSubmitClientService;
import com.creditease.adx.clockwork.common.entity.TaskRunCell;
import com.creditease.adx.clockwork.common.entity.TaskRunCellReRun;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoRerun;
import com.creditease.adx.clockwork.common.enums.RedisLockKey;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 作业运行类 - 处理重启的任务
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:56 2019-10-14
 * @ Description：重启执行类
 * @ Modified By：
 */
@Service
public class ReRunTaskExecuteService extends TaskExecuteService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ReRunTaskExecuteService.class);

    private final BlockingQueue<TaskRunCellReRun> reRunTaskExecuteServiceQueue = new LinkedBlockingQueue<TaskRunCellReRun>();

    @Value("${task.thread.pool.num.rerun}")
    protected int rerunThreadPoolNumThreads;

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Resource(name = "taskRerunClientService")
    private TaskRerunClientService taskRerunClientService;

    @Resource(name = "taskSubmitClientService")
    private TaskSubmitClientService taskSubmitClientService;

    public BlockingQueue<TaskRunCellReRun> getReRunTaskExecuteServiceQueue() {
        return reRunTaskExecuteServiceQueue;
    }

    @PostConstruct
    public void setup() {
        executeType = TaskExecuteType.RERUN.getCode();
        executeName = TaskExecuteType.RERUN.getName();

        // set name of consuming threads
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("RERUN-Task-Execute-Thread-%d")
                .build();

        // the number of max and core threads are equal, because the fixed number of consuming threads
        ThreadPoolExecutor workerReRunTaskExecuteThreadPool =
                new ThreadPoolExecutor(
                        rerunThreadPoolNumThreads,
                        rerunThreadPoolNumThreads,
                        THREAD_POOL_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(),
                        threadFactory
                );

        // start consuming threads, also, it could be called launching thread
        for (int i = 0; i < rerunThreadPoolNumThreads; i++) {
            workerReRunTaskExecuteThreadPool.execute(this);
        }
        LOG.info("RERUN-Task execute thread pool started, work thread number is : {}", rerunThreadPoolNumThreads);
    }

    @Override
    public void run() {
        while (true) {
            try {
                TaskRunCellReRun cell = reRunTaskExecuteServiceQueue.poll(5, TimeUnit.SECONDS);
                if (cell == null || cell.getTask() == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[ReRunTaskExecuteService-run]Don't have tasks need to rerun, skip current loop!");
                    }
                    continue;
                }

                //execute task core process
                boolean result = executeCore(cell, executeType, executeName);
                LOG.info("[ReRunTaskExecuteService-run]rerun execution finished ! result {}, executeType = {}, "
                                + "q.size = {}, task = {}", result, executeType, reRunTaskExecuteServiceQueue.size(),
                        cell.getTask() != null ? cell.getTask().getId() : null);
            } catch (Exception e) {
                LOG.error("[ReRunTaskExecuteService-run]rerun execution finished ! msg：{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public <T extends TaskRunCell> boolean beforeRunTaskHandle(T runTaskCell, String logName) {
        return true;
    }

    @Override
    public <T extends TaskRunCell> boolean afterRunTaskHandle(T runTaskCell, String logName, boolean runTaskState) {
        return runTaskState;
    }

    /**
     * 下发依赖的子任务（重启依赖表），需要再锁环境下操作
     *
     * @param runTaskCell cell
     * @param <T>         RunTaskReRunCell
     * @return
     */
    @Override
    public <T extends TaskRunCell> boolean launchChildTasksHandle(T runTaskCell, String logName) {
        long startTime = System.currentTimeMillis();
        TaskRunCellReRun cell = (TaskRunCellReRun) runTaskCell;
        Long reRunBatchNumber = cell.getRerunBatchNumber();
        TbClockworkTaskPojo task = cell.getTask();
        Integer parentId = task.getId();
        ArrayList<TbClockworkTaskPojo> childTaskList = new ArrayList<>();

        try {
            MDC.put("logFileName", logName);
            LOG.info("#########################################");
            LOG.info("########## launchChildTasks #############");

            LOG.info("[Launch]LaunchChildTasks, parentId = {}, reRunBatchNumber = {}", parentId, reRunBatchNumber);

            // 获取锁成功，获取所有子节点
            List<TbClockworkTaskPojo> childTasks = taskRerunClientService.getTaskRerunChild(parentId, reRunBatchNumber);
            if (CollectionUtils.isEmpty(childTasks)) {
                LOG.info("[Launch]parent task {} has 0 child tasks in total.", parentId);
                LOG.info("[END!]");
                return true;
            }

            LOG.info("[Launch]Parent task {} has {} child tasks in total.", parentId, childTasks.size());
            for (TbClockworkTaskPojo childTask : childTasks) {
                LOG.info("----------------- {} --------------------", childTask.getId());

                // 检查不能是时间触发的任务
                Integer childrenId = childTask.getId();
                if (childrenId == null
                        || childTask.getTriggerMode() == null
                        || childTask.getTriggerMode().intValue() == TaskTriggerModel.TIME.getValue().intValue()) {
                    LOG.info("[Check]children triggerMode is {} and skip it, childId = {}", childTask.getTriggerMode(), childrenId);
                    continue;
                }

                // 检查当前任务依赖的父任务是否都是成功的状态
                if (!checkParentsSuccess(cell, childTask)) {
                    LOG.info("[Check]children task not all parent are success and skip it, childId = {}", childrenId);
                    continue;
                }

                boolean isGetLock = false;
                try {
                    // 获取锁
                    long startExecute = System.currentTimeMillis();
                    isGetLock = redisService.tryLockForLaunchSubTask(childrenId, 10, TimeUnit.SECONDS);
                    if (!isGetLock) {
                        LOG.error("[Lock]parent task {} launch child. get lock false, cost time = {} ms.", parentId, System.currentTimeMillis() - startExecute);
                        LOG.info("[END!]");
                        return false;
                    }

                    // 获取锁成功，获取所有子节点
                    LOG.info("[Lock]parent task {} launch child. get lock true, cost time = {} ms.", parentId, System.currentTimeMillis() - startExecute);

                    // 检查当前任务是否可以被提交
                    String childrenStatus = taskClientService.getTaskStatusById(childrenId);
                    if (!TaskStatusUtil.canBeLaunchReRunTasksStatus(childrenStatus)) {
                        LOG.warn("[Lock]can't be submit to worker, because children status = {}, childrenId = {}, parentId = {}",
                                childrenStatus, childrenId, parentId);
                        continue;
                    }

                    /*
                     * 更新可以提交的任务的状态为已提交
                     */
                    LOG.info("[Lock]update status to submit, taskId = {}, update status = submit", childrenId);
                    boolean updateStatusResult = taskOperationClientService.updateTaskStatusSubmit(childrenId);
                    if (!updateStatusResult) {
                        LOG.error("[Lock]update status to submit error. holding lock cost time = {} ms.", System.currentTimeMillis() - startExecute);
                    }
                    LOG.info("[Lock]update status to submit success, holding lock cost time = {} ms.", System.currentTimeMillis() - startExecute);

                    // 添加到执行数组
                    childTaskList.add(childTask);
                } catch (Exception e) {
                    LOG.error("[Lock]Error {}.", e.getMessage(), e);
                    LOG.info("[END!]");
                    return false;
                } finally {
                    try {
                        if (isGetLock) {
                            LOG.info("[releaseLock]");
                            redisService.releaseLock(RedisLockKey.LAUNCH_SUB_TASK_TRANSACTION.getValue() + childrenId);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }// for end

            if (childTaskList.isEmpty()) {
                LOG.info("[Submit]submit children tasks is null, parentId = {}. total cost time = {} ms.", parentId, System.currentTimeMillis() - startTime);
                LOG.info("[END!]");
                return true;
            }
            LOG.info("[Submit]submit children tasks to worker size = {}, total children = {}.", childTaskList.size(), childTasks.size());
        } finally {
            MDC.remove("logFileName");
        }
        return taskSubmitClientService.submitChildTask(parentId, logName, new TaskSubmitInfoRerun(childTaskList, reRunBatchNumber));
    }

    /**
     * 检查当前任务依赖的父任务是否都是成功的状态(有批次的概念)
     *
     * @param runTaskCell cell
     * @return
     */
    @Override
    public <T extends TaskRunCell> boolean checkParentsSuccess(T runTaskCell, TbClockworkTaskPojo childTask) {
        // 检查当前任务依赖的父任务是否都是成功的状态
        TaskRunCellReRun cell = (TaskRunCellReRun) runTaskCell;
        Long reRunBatchNumber = cell.getRerunBatchNumber();
        List<TbClockworkTaskPojo> fartherTasks =
                taskRerunClientService.getTaskRerunFather(childTask.getId(), reRunBatchNumber);
        // 没有父亲直接返回成功
        if (CollectionUtils.isEmpty(fartherTasks)) {
            LOG.info("Not found parents,so return success directly, childTask.taskId = {}", childTask.getId());
            return true;
        } else {
            LOG.info("Found parents size is = {}, childTask.taskId = {}", fartherTasks.size(), childTask.getId());
        }

        // 获得状态不是成功的父任务信息
        List<String> fathersNoSuccess = new ArrayList<>();
        for (TbClockworkTaskPojo fartherTask : fartherTasks) {
            if (fartherTask.getOnline() == null || !fartherTask.getOnline()) {
                LOG.info("Found father is offline, skip. father current status = {}, father id = {}, task id = {}, "
                                + "task current status = {}", fartherTask.getStatus(), fartherTask.getId(),
                        childTask.getId(), childTask.getStatus());
                continue;
            }
            if (!fartherTask.getStatus().equals(TaskStatus.SUCCESS.getValue())) {
                LOG.info("Found father is not success status, father current status = {},father id = {}, " +
                                "childTask.taskId = {},childTask.task current status = {}",
                        fartherTask.getStatus(), fartherTask.getId(), childTask.getId(), childTask.getStatus());
                fathersNoSuccess.add(fartherTask.getId() + ":" + fartherTask.getStatus());
            }
        }

        // 如果有父亲任务没有成功，则返回false
        if (!fathersNoSuccess.isEmpty()) {
            LOG.info("TbClockworkTaskPojo can't be submit to worker, because has father is not success status," +
                            "fathers status = {}, childTask.taskId = {}, childTask.task current status = {}",
                    org.apache.commons.lang.StringUtils.join(fathersNoSuccess, ","),
                    childTask.getId(), childTask.getStatus());
            return false;
        }
        return true;
    }

}
