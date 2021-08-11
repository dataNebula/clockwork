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

import com.creditease.adx.clockwork.client.service.TaskRelationClientService;
import com.creditease.adx.clockwork.client.service.TaskSubmitClientService;
import com.creditease.adx.clockwork.common.entity.TaskRunCell;
import com.creditease.adx.clockwork.common.entity.TaskRunCellRoutine;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoRouTine;
import com.creditease.adx.clockwork.common.enums.RedisLockKey;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
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
 * Routine 作业运行类
 */
@Service
public class RoutineTaskExecuteService extends TaskExecuteService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(RoutineTaskExecuteService.class);

    private final BlockingQueue<TaskRunCellRoutine> routineTaskExecuteServiceQueue
            = new LinkedBlockingQueue<TaskRunCellRoutine>();

    @Value("${task.thread.pool.num.routine}")
    protected int routineThreadPoolNumThreads;

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Resource(name = "taskRelationClientService")
    private TaskRelationClientService taskRelationClientService;

    @Resource(name = "taskSubmitClientService")
    private TaskSubmitClientService taskSubmitClientService;

    public BlockingQueue<TaskRunCellRoutine> getRoutineTaskExecuteServiceQueue() {
        return routineTaskExecuteServiceQueue;
    }

    @PostConstruct
    public void setup() {
        executeType = TaskExecuteType.ROUTINE.getCode();
        executeName = TaskExecuteType.ROUTINE.getName();

        // set name of consuming threads
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("ROUTINE-Task-Execute-Thread-%d")
                .build();

        // the number of max and core threads are equal, because the fixed number of consuming threads
        ThreadPoolExecutor workerRoutineTaskExecuteThreadPool =
                new ThreadPoolExecutor(
                        routineThreadPoolNumThreads,
                        routineThreadPoolNumThreads,
                        THREAD_POOL_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>(),
                        threadFactory
                );

        // start consuming threads, also, it could be called launching thread
        for (int i = 0; i < routineThreadPoolNumThreads; i++) {
            workerRoutineTaskExecuteThreadPool.execute(this);
        }
        LOG.info("ROUTINE-Task execute thread pool started, work thread number is : {}", routineThreadPoolNumThreads);
    }

    @Override
    public void run() {
        while (true) {
            try {
                TaskRunCellRoutine cell = routineTaskExecuteServiceQueue.poll(5, TimeUnit.SECONDS);
                //check parameter basic operation unit cell is not empty
                if (cell == null || cell.getTask() == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[RoutineTaskExecuteService-run]Don't have tasks need to routine, skip current loop!");
                    }
                    continue;
                }

                //execute task core process
                boolean result = executeCore(cell, executeType, executeName);
                LOG.info("[RoutineTaskExecuteService-run]routine execution finished ! result {}, executeType = {}, "
                                + "q.size = {}, task = {}", result, executeType, routineTaskExecuteServiceQueue.size(),
                        cell.getTask() != null ? cell.getTask().getId() : null);
            } catch (Exception e) {
                LOG.error("[RoutineTaskExecuteService-run]routine execution finished ! msg：{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public <T extends TaskRunCell> boolean beforeRunTaskHandle(T runTaskCell, String logName) {
        // 任务开始前做一些处理逻辑，如果有需要
        return true;
    }

    @Override
    public <T extends TaskRunCell> boolean afterRunTaskHandle(T runTaskCell, String logName, boolean runTaskState) {
        // 任务运行完后做一些处理逻辑，如果有需要
        return true;
    }

    /**
     * 下发依赖的子任务，需要再锁环境下操作
     *
     * @param runTaskCell runTaskCell
     * @param logName     log name
     * @param <T>         RunTaskRoutineCell
     * @return
     */
    public <T extends TaskRunCell> boolean launchChildTasksHandle(T runTaskCell, String logName) {
        long startTime = System.currentTimeMillis();
        TaskRunCellRoutine cell = (TaskRunCellRoutine) runTaskCell;
        TbClockworkTaskPojo parentTask = cell.getTask();
        Integer parentId = parentTask.getId();
        ArrayList<TbClockworkTaskPojo> childTaskList = new ArrayList<>();

        try {
            MDC.put("logFileName", logName);
            LOG.info("#########################################");
            LOG.info("########## launchChildTasks #############");

            // 获取子任务
            List<TbClockworkTaskPojo> childTasks = taskRelationClientService.getTaskDirectlyChildrenNotIncludeSelf(parentId);
            if (CollectionUtils.isEmpty(childTasks)) {
                LOG.info("[Launch]parent task {} has 0 child tasks in total.", parentId);
                LOG.info("[END!]");
                return true;
            }

            LOG.info("[Launch]Parent task {} has {} child tasks in total.", parentId, childTasks.size());
            for (TbClockworkTaskPojo childTask : childTasks) {
                LOG.info("----------------- {} --------------------", childTask.getId());

                // 检查必须是依赖触发，0代表依赖触发
                Integer childrenId = childTask.getId();
                if (childrenId == null
                        || childTask.getTriggerMode() == null
                        || childTask.getTriggerMode().intValue() != TaskTriggerModel.DEPENDENCY.getValue().intValue()) {
                    LOG.info("[Check]children triggerMode not 0 and skip it, childId = {}, triggerMode = {}", childrenId, childTask.getTriggerMode());
                    continue;
                }

                // 检查cron表达式，如果有则不进行下发处理，这里只下发单存依赖父任务的子任务【只有例行才会检测】
                if (StringUtils.isNotBlank(childTask.getCronExp())) {
                    LOG.info("[Check]children task has cron exp and skip it, childId = {}", childrenId);
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
                    isGetLock = redisService.tryLockForLaunchSubTask(childrenId, 30, TimeUnit.SECONDS);
                    if (!isGetLock) {
                        LOG.error("[Lock]parent task {} launch child. get lock false, cost time = {} ms.", parentId, System.currentTimeMillis() - startExecute);
                        LOG.info("[END!]");
                        return false;
                    }

                    // 获取锁成功，获取所有子节点
                    LOG.info("[Lock]parent task {} launch child. get lock true, cost time = {} ms.", parentId, System.currentTimeMillis() - startExecute);

                    // 检查当前任务是否可以被提交
                    String childrenStatus = taskClientService.getTaskStatusById(childrenId);
                    if (!TaskStatusUtil.canBeSubmitCurrentTaskStatus(childrenStatus)) {
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
        return taskSubmitClientService.submitChildTask(parentId, logName, new TaskSubmitInfoRouTine(childTaskList));
    }

    /**
     * 检查当前任务依赖的父任务是否都是成功的状态
     *
     * @param runTaskCell
     * @return
     */
    @Override
    public <T extends TaskRunCell> boolean checkParentsSuccess(T runTaskCell, TbClockworkTaskPojo childTask) {
        return taskOperationClientService.checkParentsSuccess(childTask);
    }

}
