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

package com.creditease.adx.clockwork.worker.service;

import com.creditease.adx.clockwork.client.service.LoopClockClientService;
import com.creditease.adx.clockwork.client.service.TaskRerunClientService;
import com.creditease.adx.clockwork.client.service.TaskStateClientService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.worker.service.task.execute.FillDataTaskExecuteService;
import com.creditease.adx.clockwork.worker.service.task.execute.ReRunTaskExecuteService;
import com.creditease.adx.clockwork.worker.service.task.execute.RoutineTaskExecuteService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class TaskRunService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRunService.class);

    // 等待执行的队列
    private final BlockingQueue<TaskDistributeTuple> taskDistributeTupleQueue = new LinkedBlockingQueue<>();

    @Resource(name = "taskStateClientService")
    private TaskStateClientService taskStateClientService;

    @Resource(name = "taskRerunClientService")
    private TaskRerunClientService taskRerunClientService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RoutineTaskExecuteService routineTaskExecuteService;

    @Autowired
    private ReRunTaskExecuteService reRunTaskExecuteService;

    @Autowired
    private FillDataTaskExecuteService fillDataTaskExecuteService;

    @Autowired
    private TaskLifecycleService taskLifecycleService;

    @Autowired
    private LoopClockClientService loopClockClientService;

    @PostConstruct
    public void setup() {
        Thread thread = new Thread(this);
        thread.start();
        LOG.info("[TaskRunService-setup]The thread that process need be executed task is started");
    }

    @Override
    public void run() {
        while (true) {
            TaskDistributeTuple taskDistributeTuple = null;
            try {
                // 获取 等待队列中的任务
                taskDistributeTuple = taskDistributeTupleQueue.poll(2, TimeUnit.SECONDS);

                if (taskDistributeTuple == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[TaskRunService-run] There were no tasks need to be executed,skip current loop!");
                    }
                    continue;
                }
                LOG.info("[TaskRunService-run]process task begin, task size = {}, node id = {}, executeType = {}",
                        taskDistributeTuple.getTaskPojoList().size(), taskDistributeTuple.getNodeId(),
                        taskDistributeTuple.getExecuteType());

                // 执行
                if (taskDistributeTuple instanceof TaskDistributeTupleRoutine) {
                    addRoutineTaskToExecuteQueue((TaskDistributeTupleRoutine) taskDistributeTuple);
                } else if (taskDistributeTuple instanceof TaskDistributeTupleReRun) {
                    addReRunTaskToExecuteQueue((TaskDistributeTupleReRun) taskDistributeTuple);
                } else if (taskDistributeTuple instanceof TaskDistributeTupleFillData) {
                    addFillDateTaskToExecuteQueue((TaskDistributeTupleFillData) taskDistributeTuple);
                } else if (taskDistributeTuple instanceof TaskDistributeTupleSignal) {
                    addSignalTaskToExecuteQueue((TaskDistributeTupleSignal) taskDistributeTuple);
                } else {
                    LOG.error("needBeExecutedTuple type is Error.");
                }

                LOG.info("[TaskRunService-run]process task success, task size = {}, node id = {}, executeType = {}",
                        taskDistributeTuple.getTaskPojoList().size(), taskDistributeTuple.getNodeId(),
                        taskDistributeTuple.getExecuteType());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                if ((taskDistributeTuple != null)) {
                    LOG.error("[TaskRunService-run]process task exception, task size = {} {}, node id = {}, executeType = {}",
                            taskDistributeTuple.getTaskIds().size(), taskDistributeTuple.getTaskPojoList().size(),
                            taskDistributeTuple.getNodeId(), taskDistributeTuple.getExecuteType());
                }
            }
        }
    }

    /**
     * 添加到执行例行任务队列
     *
     * @param taskDistributeTupleRoutine 例行任务tuple
     */
    private void addRoutineTaskToExecuteQueue(TaskDistributeTupleRoutine taskDistributeTupleRoutine) {
        try {
            long startTime = System.currentTimeMillis();
            List<TbClockworkTaskPojo> taskPojoList = taskDistributeTupleRoutine.getTaskPojoList();
            if (CollectionUtils.isEmpty(taskPojoList)) {
                return;
            }
            // 设置此次请求所有任务的下一次执行槽位信息和界面显示时间信息
            loopClockClientService.addTaskToLoopClockSlotByBatch(taskPojoList);

            // 检查是否有需要重置生命周期的任务，如果有则重置
            long phaseStartTime = System.currentTimeMillis();
            taskLifecycleService.checkAndResetTaskLifecycle2(taskPojoList);
            LOG.info("[TaskRunService-processTask-routine]check and reset task life cycle,phase cost time = {} ms.",
                    System.currentTimeMillis() - phaseStartTime);

            // 预处理-将需要执行的任务提交给任务执行队列
            taskDistributeTupleRoutine.setTaskPojoList(taskPojoList);
            List<TbClockworkTaskPojo> tasks = taskDistributeTupleRoutine.getTaskPojoList();
            int nodeId = taskDistributeTupleRoutine.getNodeId();
            for (TbClockworkTaskPojo task : tasks) {
                // 组装执行任务TaskTask对象, 加入执行等待队列
                TaskRunCellRoutine runTaskRoutineCell = new TaskRunCellRoutine(
                        nodeId, taskService.getRuntimeDirClientUrl(), taskDistributeTupleRoutine.getExecuteType(), task);

                // 加入执行等待队列
                routineTaskExecuteService.getRoutineTaskExecuteServiceQueue().put(runTaskRoutineCell);
                LOG.info("[TaskRunService-addRoutineTaskExecuteService]task to queue, " +
                        "taskId = {}, logId = {}, ", task.getId(), task.getTaskLogId());
            }

            LOG.info("[TaskRunService-processTask-routine]add tasks to execute queue, original task size = {}, "
                            + "queue task size = {}, cost time = {} ms.", taskDistributeTupleRoutine.getTaskPojoList().size(),
                    tasks.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 添加到执行重启任务队列
     *
     * @param taskDistributeTupleReRun 重启元祖
     */
    private void addReRunTaskToExecuteQueue(TaskDistributeTupleReRun taskDistributeTupleReRun) {
        try {
            // 获取参数
            long startTime = System.currentTimeMillis();
            int executeType = taskDistributeTupleReRun.getExecuteType();
            Long rerunBatchNumber = taskDistributeTupleReRun.getRerunBatchNumber();

            List<TbClockworkTaskPojo> tasks = taskDistributeTupleReRun.getTaskPojoList();
            for (TbClockworkTaskPojo task : tasks) {
                // 从TaskRerun表中读取对象
                TbClockworkTaskPojo data = taskRerunClientService.getTaskRerunByTaskId(task.getId(), rerunBatchNumber);
                if (data == null) {
                    LOG.error("[TaskRunService-addReRunTaskToExecuteQueue] getTaskRerunByTaskId Error data is null. ");
                    continue;
                }
                data.setTaskLogId(task.getTaskLogId());
                LOG.info("[TaskRunService-addReRunTaskToExecuteQueue] getTaskRerunByTaskId Error data is null. taskId = {}, parameter = {}",
                        data.getId(), data.getParameter());
                // 组装执行任务单元RunTaskReRunCell
                TaskRunCellReRun taskRunCellReRun = new TaskRunCellReRun(
                        taskDistributeTupleReRun.getNodeId(), taskService.getRuntimeDirClientUrl(), executeType, data, rerunBatchNumber);

                // 加入执行等待队列
                reRunTaskExecuteService.getReRunTaskExecuteServiceQueue().put(taskRunCellReRun);
                LOG.info("[TaskRunService-processTask-rerun]update tasks status to submit, taskId = {}, " +
                        "logId = {}, rerunBatchNumber = {}", data.getId(), data.getTaskLogId(), rerunBatchNumber);
            }

            LOG.info("[TaskRunService-processTask-rerun]add tasks to submit queue, rerun " +
                            "task size = {}, executeType = {}, rerunBatchNumber = {}, cost time = {} ms.",
                    taskDistributeTupleReRun.getTaskPojoList().size(), executeType, rerunBatchNumber,
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 添加到执行补数任务队列
     *
     * @param taskDistributeTupleFillData 补数元祖
     */
    private void addFillDateTaskToExecuteQueue(TaskDistributeTupleFillData taskDistributeTupleFillData) {
        try {
            long startTime = System.currentTimeMillis();
            Long rerunBatchNumber = taskDistributeTupleFillData.getRerunBatchNumber();
            String fillDataTime = taskDistributeTupleFillData.getFillDataTime();
            int executeType = taskDistributeTupleFillData.getExecuteType();

            // 预处理tasks然后携带logId返回 - 入队等待执行
            List<TbClockworkTaskPojo> tasks = taskDistributeTupleFillData.getTaskPojoList();
            for (TbClockworkTaskPojo task : tasks) {
                // 从TaskRerun表中读取对象
                TbClockworkTaskPojo data = taskRerunClientService.getTaskRerunByTaskId(task.getId(), rerunBatchNumber);
                if (data == null) {
                    LOG.error("[TaskRunService-addFillDateTaskToExecuteQueue] getTaskRerunByTaskId Error data is null. ");
                    continue;
                }
                data.setTaskLogId(task.getTaskLogId());
                LOG.info("[TaskRunService-addReRunTaskToExecuteQueue] getTaskRerunByTaskId Error data is null. taskId = {}, parameter = {}",
                        data.getId(), data.getParameter());

                // 组装执行任务TaskFillData对象
                TaskRunCellFillData runTaskFillDataCell = new TaskRunCellFillData(
                        taskDistributeTupleFillData.getNodeId(), taskService.getRuntimeDirClientUrl(),
                        executeType, data, rerunBatchNumber, fillDataTime);

                // 加入执行等待队列
                fillDataTaskExecuteService.getFillDataTaskExecuteServiceQueue().put(runTaskFillDataCell);
                LOG.info("[TaskRunService-processTask-fillData]update tasks status to submit, " +
                        "taskId = {}, logId = {}, rerunBatchNumber = {}", data.getId(), data.getTaskLogId(), rerunBatchNumber);
            }

            LOG.info("[TaskRunService-addFillDateTaskToExecuteQueue]add tasks to submit queue, rerun " +
                            "task size = {}, rerunBatchNumber = {}, executeType = {}, fillDataTime = {}, cost time = {} ms.",
                    taskDistributeTupleFillData.getTaskPojoList().size(), rerunBatchNumber,
                    executeType, fillDataTime, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 添加到执行例行任务队列
     *
     * @param taskDistributeTupleSignal 例行的任务
     */
    private void addSignalTaskToExecuteQueue(TaskDistributeTupleSignal taskDistributeTupleSignal) {
        try {
            long startTime = System.currentTimeMillis();
            List<TbClockworkTaskPojo> taskPojoList = taskDistributeTupleSignal.getTaskPojoList();

            // 检查是否有需要重置生命周期的任务，如果有则重置
            taskLifecycleService.checkAndResetTaskLifecycle2(taskPojoList);
            LOG.info("[TaskRunService-processTask-signal]check and reset task life cycle,phase cost time = {} ms.",
                    System.currentTimeMillis() - startTime);

            // 预处理-将需要执行的任务提交给任务执行队列
            taskDistributeTupleSignal.setTaskPojoList(taskPojoList);
            List<TbClockworkTaskPojo> tasks = taskDistributeTupleSignal.getTaskPojoList();
            for (TbClockworkTaskPojo task : tasks) {
                // 组装执行任务TaskTask对象, 加入执行等待队列
                TaskRunCellRoutine runTaskRoutineCell = new TaskRunCellRoutine(
                        taskDistributeTupleSignal.getNodeId(), taskService.getRuntimeDirClientUrl(),
                        taskDistributeTupleSignal.getExecuteType(), task);

                // 加入到例行执行等待队列中
                routineTaskExecuteService.getRoutineTaskExecuteServiceQueue().put(runTaskRoutineCell);
                LOG.info("[TaskRunService-addSignalTaskToNeedBeExecutedQueue]task to queue, taskId = {}, logId = {}",
                        task.getId(), task.getTaskLogId());
            }

            LOG.info("[TaskRunService-processTask-signal]add task to submit queue, " +
                            "original task size = {}, queue task size = {}, cost time = {} ms.",
                    taskDistributeTupleSignal.getTaskPojoList().size(), tasks.size(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 移除 NeedBeExecutedTaskQueue by ID（排除补数任务）
     *
     * @param taskId
     * @return
     */
    public int removeTaskFromNeedBeExecutedQueue(int taskId) {
        int count = 0;
        Iterator<TaskDistributeTuple> executedTaskIterator = taskDistributeTupleQueue.iterator();
        while (executedTaskIterator.hasNext()) {
            TaskDistributeTuple next = executedTaskIterator.next();
            // 排除补数任务
            if (TaskExecuteType.FILL_DATA.getCode().intValue() == next.getExecuteType()) {
                continue;
            }
            Iterator<TbClockworkTaskPojo> tasksIterator = next.getTaskPojoList().iterator();
            while (tasksIterator.hasNext()) {
                TbClockworkTaskPojo taskPojo = tasksIterator.next();
                if (taskPojo.getId() == taskId) {
                    LOG.info("[removeNeedBeExecutedTaskQueue]existence needBeExecutedTaskQueue, taskId id = {}", taskId);
                    tasksIterator.remove();
                    count++;
                    break;
                }
            }
        }
        if (count == 0) {
            LOG.info("[removeNeedBeExecutedTaskQueue]notExistence needBeExecutedTaskQueue, taskId id = {}", taskId);
        }
        return count;
    }

    /**
     * 移除 NeedBeExecutedTaskQueue by ID (补数任务)
     *
     * @param taskId
     * @return
     */
    public int removeTaskFillDataFromNeedBeExecutedQueue(int taskId) {
        int count = 0;
        Iterator<TaskDistributeTuple> executedTaskIterator = taskDistributeTupleQueue.iterator();
        while (executedTaskIterator.hasNext()) {
            TaskDistributeTuple next = executedTaskIterator.next();
            int executeType = next.getExecuteType();
            // 指针对补数任务
            if (TaskExecuteType.FILL_DATA.getCode() == executeType) {
                Iterator<TbClockworkTaskPojo> tasksIterator = next.getTaskPojoList().iterator();
                while (tasksIterator.hasNext()) {
                    TbClockworkTaskPojo taskPojo = tasksIterator.next();
                    if (taskPojo.getId() == taskId) {
                        LOG.info("[removeTaskFillDataFromNeedBeExecutedQueue]existence needBeExecutedTaskQueue, taskId id = {}", taskId);
                        tasksIterator.remove();
                        count++;
                        break;
                    }
                }
            }
        }
        if (count == 0) {
            LOG.info("[removeTaskFillDataFromNeedBeExecutedQueue]notExistence needBeExecutedTaskQueue, taskId id = {}", taskId);
        }
        return count;
    }

    /**
     * 添加到队列，并修改状态
     *
     * @param tuple tuple
     * @return boolean
     */
    public boolean addTupleToNeedBeExecutedQueue(TaskDistributeTuple tuple) {
        if (tuple == null || tuple.getTaskIds() == null) {
            throw new RuntimeException("params tuple or taskIds Error.");
        }
        taskStateClientService.taskStateWorkerHasReceive(tuple, tuple.getNodeId());
        return taskDistributeTupleQueue.add(tuple);
    }

}
