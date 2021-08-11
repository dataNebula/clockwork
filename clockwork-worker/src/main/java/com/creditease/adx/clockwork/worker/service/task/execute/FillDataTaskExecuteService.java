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

import com.creditease.adx.clockwork.client.service.TaskFillDataClientService;
import com.creditease.adx.clockwork.client.service.TaskRerunClientService;
import com.creditease.adx.clockwork.client.service.TaskSubmitClientService;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusSubmit;
import com.creditease.adx.clockwork.common.entity.TaskRunCell;
import com.creditease.adx.clockwork.common.entity.TaskRunCellFillData;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoFillData;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillDataTimeQueue;
import com.creditease.adx.clockwork.common.enums.RedisLockKey;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 作业运行类 - 处理补数的任务(执行队列中)
 * <p>
 * 1. 执行任务前
 * 2. 执行任务中
 * 3. 执行任务后
 * <p>
 * 1。 执行任务前所做的准备：
 * a. 判断是否可以执行,
 * b. 获取下载依赖相关的脚本，
 * c. 记录补数日志信息
 * d. 构建依赖关系
 * e. 下发根任务到队列中 -> [Queue]
 * <p>
 * 2。 执行队列中：单纯的运行队列中的任务，只关心不断的去执行
 * a. 从队列Queue中获取任务对象[TaskFillData]
 * b. 预处理task[preFillDateTask], [status=submit] And initTaskLogAndTaskLogFlow
 * c. 运行任务[taskExecute], 检查是否有依赖的文件如果有, 则需要下载并替换参数
 * d. runTaskFillData
 * e. 日志记录
 * f. 运行状态的记录
 * f. 运行结果的返回
 * <p>
 * 3。 执行任务后：任务失败直接返回，任务成功记录统计信息，判断是否需要发送新周期的任务，还是下发子任务，任务结束
 * a. 任务运行成功，更新补数记录表
 * b.
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:56 2019-10-14
 * @ Description：补数执行类
 * @ Modified By：
 */
@Service
public class FillDataTaskExecuteService extends TaskExecuteService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FillDataTaskExecuteService.class);

    private final BlockingQueue<TaskRunCellFillData> fillDataTaskExecuteServiceQueue = new LinkedBlockingQueue<>();

    @Value("${task.thread.pool.num.rerun}")
    protected int rerunThreadPoolNumThreads;

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Resource(name = "taskFillDataClientService")
    private TaskFillDataClientService taskFillDataClientService;

    @Resource(name = "taskRerunClientService")
    private TaskRerunClientService taskRerunClientService;

    @Resource(name = "taskSubmitClientService")
    private TaskSubmitClientService taskSubmitClientService;

    public BlockingQueue<TaskRunCellFillData> getFillDataTaskExecuteServiceQueue() {
        return fillDataTaskExecuteServiceQueue;
    }

    @PostConstruct
    public void setup() {
        executeType = TaskExecuteType.FILL_DATA.getCode();
        executeName = TaskExecuteType.FILL_DATA.getName();

        // set name of consuming threads
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("FILL_DATA-Task-Execute-Thread-%d")
                .build();

        // the number of max and core threads are equal, because the fixed number of consuming threads
        ThreadPoolExecutor workerTaskFillDataExecuteThreadPool =
                new ThreadPoolExecutor(
                        rerunThreadPoolNumThreads,
                        rerunThreadPoolNumThreads,
                        THREAD_POOL_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(),
                        threadFactory
                );

        // start consuming threads, also, it could be called launching thread
        for (int i = 0; i < rerunThreadPoolNumThreads; i++) {
            workerTaskFillDataExecuteThreadPool.execute(this);
        }
        LOG.info("FILL_DATA-Task execute thread pool started, work thread number is : {}", rerunThreadPoolNumThreads);
    }

    @Override
    public void run() {
        while (true) {
            try {
                TaskRunCellFillData cell = fillDataTaskExecuteServiceQueue.poll(5, TimeUnit.SECONDS);
                // check parameter basic operation unit cell is not empty
                if (cell == null || cell.getTask() == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[FillDataTaskExecuteService-run]Don't have tasks need to rerun, skip current loop!");
                    }
                    continue;
                }

                //execute task core process
                boolean result = executeCore(cell, executeType, executeName);
                LOG.info("[FillDataTaskExecuteService-run]fillData execution finished ! result {}, executeType = {}, "
                                + "q.size = {}, task = {}", result, executeType, fillDataTaskExecuteServiceQueue.size(),
                        cell.getTask() != null ? cell.getTask().getId() : null);
            } catch (Exception e) {
                LOG.error("[FillDataTaskExecuteService-run]fillData execution finished ! msg：{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public <T extends TaskRunCell> boolean beforeRunTaskHandle(T runTaskCell, String logName) {
        return true;
    }

    @Override
    public <T extends TaskRunCell> boolean afterRunTaskHandle(T runTaskCell, String logName, boolean runTaskState) {
        try {
            long startTime = System.currentTimeMillis();
            MDC.put("logFileName", logName);
            TaskRunCellFillData cell = (TaskRunCellFillData) runTaskCell;
            Long reRunBatchNumber = cell.getRerunBatchNumber();
            String fillDataTime = cell.getFillDataTime();
            Integer taskId = cell.getTask().getId();

            if (!runTaskState) {
                LOG.error("Task fill data run error. task id = {}, runTaskState is false", taskId);
                return false;
            }

            // 任务运行成功，更新补数记录表
            String rerunBatchNumberStr = String.valueOf(reRunBatchNumber);
            boolean result = taskFillDataClientService.updateTaskFillDataSuccessCount(rerunBatchNumberStr);
            if (!result) {
                LOG.error("Update task fill data success task count + 1 Error! reRunBatchNum ={}", rerunBatchNumberStr);
                return false;
            }
            LOG.info("Update task fill data success task count + 1 ! reRunBatchNum = {}", rerunBatchNumberStr);

            TbClockworkTaskFillData taskFillData = taskFillDataClientService.getTaskFillDataByRerunBatchNumber(rerunBatchNumberStr);
            if (taskFillData == null) {
                return false;
            }

            // 检测是否需要下发下一个周期还是下发子任务
            Integer taskCount = taskFillData.getTaskCount();
            Integer taskCountSuccess = taskFillData.getTaskCountSuccess();
            String currFillDataTime = taskFillData.getCurrFillDataTime();
            Integer currFillDataTimeSort = taskFillData.getCurrFillDataTimeSort();
            LOG.info("Task fill data info. taskCount = {}, taskCountSuccess = {}, fillDataTime = {}, currFillDataTime = {}, "
                    + "currFillDataTimeSort = {}", taskCount, taskCountSuccess, fillDataTime, currFillDataTime, currFillDataTimeSort);

            // 下发下一个周期的任务
            if (taskCountSuccess % taskCount == 0 && currFillDataTime.equals(fillDataTime)) {
                // 获取下一次需要补数的日期
                TbClockworkTaskFillDataTimeQueue nextTaskFillDataTimeQueue
                        = taskFillDataClientService.getNextTaskFillDataTimeQueue(rerunBatchNumberStr, fillDataTime);

                if (nextTaskFillDataTimeQueue == null) {
                    // 已经没有需要补数的日期，更新补数记录为End
                    LOG.info("Task next fill data info is null, all task is success! ");
                    taskFillDataClientService.updateTaskFillDataIsEnd(rerunBatchNumberStr, TaskStatus.SUCCESS.getValue(), true);
                    return true;
                }

                // 还存在需要下发的任务, 获取下一次的补数时间
                String nextFillDataTime = nextTaskFillDataTimeQueue.getFillDataTime();
                LOG.info("Task next fill data info. taskCount = {}, taskCountSuccess = {}, nextFillDataTime = {}, "
                        + "nextSort = {}", taskCount, taskCountSuccess, nextFillDataTime, nextTaskFillDataTimeQueue.getSort());
                taskFillDataClientService.updateTaskFillDataCurrFillDataTime(rerunBatchNumberStr, nextFillDataTime,
                        nextTaskFillDataTimeQueue.getSort());

                // 获取新周期的数据
                List<Integer> rootTaskIds = taskRerunClientService.getTaskRerunRootTaskIds(reRunBatchNumber);
                if (CollectionUtils.isEmpty(rootTaskIds)) {
                    LOG.error("RootTaskIds is null, rerunBatchNumberStr = {}", rerunBatchNumberStr);
                    return false;
                }

                // 下发新的周期数据
                List<TbClockworkTaskPojo> taskPojoList = null;
                boolean getLock = false;
                try {
                    getLock = redisService.tryLockForSubmitTask(30, TimeUnit.SECONDS);
                    if (getLock) {
                        taskPojoList = taskClientService.getTaskByTaskIds(rootTaskIds);
                        LOG.info("[update]Children task status to submit, taskIds = {}, update status = submit", taskPojoList);
                        boolean updateStatusResult = taskOperationClientService.updateTaskStatusSubmitBatch(new BatchUpdateTaskStatusSubmit(rootTaskIds));
                        if (!updateStatusResult) {
                            LOG.error("[update-error]Children task status update to submit error.");
                        }
                    } else {
                        throw new RuntimeException("Same transaction for update task is running,please try again later.");
                    }
                } catch (Exception e) {
                    LOG.error("[launchChildTasksHandle] Error And Update fillData status = FAILED, {}.", e.getMessage(), e);
                } finally {
                    try {
                        if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                if (CollectionUtils.isEmpty(taskPojoList)) return false;
                TaskSubmitInfoFillData taskSubmitInfoRerun = new TaskSubmitInfoFillData(taskPojoList, reRunBatchNumber, nextFillDataTime);
                if (!taskSubmitClientService.submitChildTask(taskId, logName, taskSubmitInfoRerun)) {
                    LOG.error("Task submit next fillData info failed! taskCount = {}, taskCountSuccess = {}, " +
                                    "nextFillDataTime = {}, rootTaskIds.size = {}, cost time = {} ms.", taskCount,
                            taskCountSuccess, nextFillDataTime, rootTaskIds.size(), System.currentTimeMillis() - startTime);
                } else {
                    LOG.info("Task submit next fillData info success! taskCount = {}, taskCountSuccess = {}, " +
                                    "nextFillDataTime = {}, rootTaskIds.size = {}, cost time = {} ms.",
                            taskCount, taskCountSuccess, nextFillDataTime, rootTaskIds.size(), System.currentTimeMillis() - startTime);
                }
                // 这里下发下一周期数据，即就不会再去下发子任务，所以这里返回false
                return false;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        } finally {
            MDC.remove("logFileName");
        }
        return true;
    }

    /**
     * 下发依赖的子任务（重启依赖表），需要再锁环境下操作
     *
     * @param runTaskCell cell
     * @param logName     log name
     * @param <T>
     * @return
     */
    public <T extends TaskRunCell> boolean launchChildTasksHandle(T runTaskCell, String logName) {
        long startTime = System.currentTimeMillis();
        TaskRunCellFillData cell = (TaskRunCellFillData) runTaskCell;
        Long reRunBatchNumber = cell.getRerunBatchNumber();
        String fillDataTime = cell.getFillDataTime();
        TbClockworkTaskPojo task = cell.getTask();
        Integer parentId = task.getId();
        ArrayList<TbClockworkTaskPojo> childTaskList = new ArrayList<>();

        try {
            // 下发子任务
            MDC.put("logFileName", logName);
            LOG.info("#########################################");
            LOG.info("########## launchChildTasks #############");

            LOG.info("fillDataTime = {}", fillDataTime);
            LOG.info("LaunchChildTasks, parentId = {}, reRunBatchNumber = {}", parentId, reRunBatchNumber);

            // 获取所有子节点
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
        return taskSubmitClientService.submitChildTask(parentId, logName, new TaskSubmitInfoFillData(childTaskList, reRunBatchNumber, fillDataTime));

    }

    /**
     * 检查当前任务依赖的父任务是否都是成功的状态(有批次的概念)
     *
     * @param runTaskCell
     * @return
     */
    @Override
    public <T extends TaskRunCell> boolean checkParentsSuccess(T runTaskCell, TbClockworkTaskPojo childTask) {
        TaskRunCellFillData cell = (TaskRunCellFillData) runTaskCell;
        Long reRunBatchNumber = cell.getRerunBatchNumber();
        String fillDataTime = cell.getFillDataTime();

        // 检查当前任务依赖的父任务是否都是成功的状态
        List<Integer> fartherTaskIds = taskRerunClientService.getTaskRerunFatherIds(childTask.getId(), reRunBatchNumber);
        // 没有父亲直接返回成功
        if (CollectionUtils.isEmpty(fartherTaskIds)) {
            LOG.info("Not found parents,so return success directly, childTask.taskId = {}", childTask.getId());
            return true;
        } else {
            LOG.info("Found parents size is = {}, fartherTaskIds = {}, childTask.taskId = {}", fartherTaskIds.size(),
                    fartherTaskIds, childTask.getId());
        }

        // 获得状态不是成功的父任务信息
        List<TbClockworkTaskLogPojo> childFatherTaskLogs =
                taskLogClientService.getFillDataTaskLogByTaskIds(fartherTaskIds, reRunBatchNumber, fillDataTime);

        Map<Integer, TbClockworkTaskLogPojo> childFatherTaskLogsMap =
                childFatherTaskLogs.stream().collect(Collectors.toMap(TbClockworkTaskLogPojo::getTaskId, taskLog -> taskLog,
                        (v1, v2) -> {
                            return v2;
                        }));

        List<String> fathersNoSuccess = new ArrayList<>();
        TbClockworkTaskLogPojo childFatherTaskLog;
        for (Integer fartherTaskId : fartherTaskIds) {
            childFatherTaskLog = childFatherTaskLogsMap.get(fartherTaskId);
            if (childFatherTaskLog == null || !childFatherTaskLog.getStatus().equals(TaskStatus.SUCCESS.getValue())) {
                LOG.info("Found father is not success status, father current status = {},father id = {}, " +
                                "childTask.taskId = {},childTask.task current status = {}",
                        fartherTaskId, childFatherTaskLog == null ? null : childFatherTaskLog.getStatus(),
                        childTask.getId(), childTask.getStatus());
                fathersNoSuccess.add(fartherTaskId + ":" + ((childFatherTaskLog == null) ? null : childFatherTaskLog.getStatus()));
            }
        }

        // 如果有父亲任务没有成功，则返回false
        if (!fathersNoSuccess.isEmpty()) {
            LOG.info("TbClockworkTaskPojo can't be submit to worker，because has father is not success status," +
                            "fathers status = {}, childTask.taskId = {}, childTask.task current status = {}",
                    org.apache.commons.lang.StringUtils.join(fathersNoSuccess, ","),
                    childTask.getId(), childTask.getStatus());
            return false;
        }

        return true;
    }

}
