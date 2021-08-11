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

import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.client.service.TaskLogClientService;
import com.creditease.adx.clockwork.client.service.TaskStateClientService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.enums.KillOperationInitiator;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 已经执行杀死命令的任务，检测状态是否已经完结
 */
@Service
public class TaskKilledService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TaskKilledService.class);

    // 存储已经执行了杀死逻辑，但是是否状态已经完结的作业
    private final BlockingQueue<KillTask> hasBeenKilledTasks = new LinkedBlockingQueue<KillTask>();

    @Value("${task.killed.check.time}")
    protected Integer checkKilledTaskCycleTime;

    @Value("${task.killed.timeout.time}")
    protected Integer killedTaskTimeoutTime;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskLogClientService")
    private TaskLogClientService taskLogClientService;

    @Resource(name = "taskStateClientService")
    private TaskStateClientService taskStateClientService;

    @PostConstruct
    public void setup() {
        if (checkKilledTaskCycleTime == null || checkKilledTaskCycleTime < 1) {
            // 单位秒
            checkKilledTaskCycleTime = 60;
        }

        if (killedTaskTimeoutTime == null || killedTaskTimeoutTime < 1) {
            // 单位秒
            killedTaskTimeoutTime = 120;
        }

        new Thread(this).start();
        LOG.info("Slave task TaskKilledService thread pool started, checkKilledTaskCycleTime : {}", checkKilledTaskCycleTime);
    }

    @Override
    public void run() {
        while (true) {
            if (hasBeenKilledTasks.isEmpty()) {
                LOG.info("[TaskKilledService]Has been killed tasks queue is null,wait a moment...");
                try {
                    Thread.sleep(checkKilledTaskCycleTime * 1000);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                continue;
            }
            LOG.info("[TaskKilledService]Has been killed tasks queue has tasks,begin check if task time out, "
                    + "queue size = {}", hasBeenKilledTasks.size());

            Iterator<KillTask> iterator = hasBeenKilledTasks.iterator();
            while (iterator.hasNext()) {
                try {
                    KillTask killTask = iterator.next();
                    TbClockworkTaskLogPojo taskLog;
                    if ((taskLog = killTask.getTaskLog()) == null) {
                        LOG.info("[TaskKilledService]taskLog is null, skip and remove. ");
                        iterator.remove();
                        continue;
                    }

                    Integer taskLogId = taskLog.getId();
                    TbClockworkTaskPojo currentTask = taskClientService.getTaskById(taskLog.getTaskId());
                    LOG.info("[TaskKilledService]task info is null, " +                 // 当任务被删除时
                            "skip and remove. taskLogId = {}, taskId = {}", taskLogId, taskLog.getTaskId());
                    if (taskLogId == null || currentTask == null) {
                        iterator.remove();
                        continue;
                    }

                    LOG.info("[TaskKilledService]begin check, task id = {},task current status = {}, " +
                                    "task process id = {},task process status = {}, " +
                                    "kill scene flag value = {},killed operate time = {},real run script = {}",
                            currentTask.getId(), currentTask.getStatus(), taskLogId, taskLog.getStatus(),
                            killTask.getKillSceneFlag(),
                            DateUtil.formatDate(killTask.getKilledOperateTime(), DateUtil.DATE_FULL_STR),
                            taskLog.getRealCommand());

                    // 如果当前的作业已经处于完结状态，则直接移除
                    if (TaskStatusUtil.isFinishedTaskStatus(currentTask.getStatus())) {
                        LOG.info("[TaskKilledService]the killed task has finished and removed directly，" +
                                        "task id = {},task current status = {}," +
                                        "task process id = {},task process status = {}," +
                                        "kill scene flag value = {},killed operate time = {},real run script = {}",
                                currentTask.getId(), currentTask.getStatus(),
                                taskLogId, taskLog.getStatus(), killTask.getKillSceneFlag(),
                                DateUtil.formatDate(killTask.getKilledOperateTime(), DateUtil.DATE_FULL_STR),
                                taskLog.getRealCommand());
                        // 任务移除
                        if (!currentTask.getStatus().equals(taskLog.getStatus())) {
                            taskLogClientService.updateTaskLogEnd(taskLog.getId(), currentTask.getStatus(), -1);
                        }
                        iterator.remove();
                        continue;
                    }

                    // 获得间隔时间
                    long timeInterval = DateUtil.getTimeIntervalTargetTimeAndNow(killTask.getKilledOperateTime().getTime());
                    if (timeInterval < 0) {
                        LOG.info("[TaskKilledService]time interval is not illegal that value is {}," +
                                        "task id = {},task current status = {}," +
                                        "task process id = {},task process status = {}," +
                                        "kill scene flag value = {},killed operate time = {},real run script = {}",
                                timeInterval, currentTask.getId(), currentTask.getStatus(),
                                taskLogId, taskLog.getStatus(), killTask.getKillSceneFlag(),
                                DateUtil.formatDate(killTask.getKilledOperateTime(), DateUtil.DATE_FULL_STR),
                                taskLog.getRealCommand());

                        throw new RuntimeException("[TaskKilledService]time interval is not illegal that value is " + timeInterval);
                    }

                    // 如果没有超时则跳过
                    if (timeInterval <= (killedTaskTimeoutTime * 1000)) {
                        LOG.info("[TaskKilledService]the killed task not timeout,skip it," +
                                        "timeInterval = {},task id = {},task current status = {}," +
                                        "task process id = {},task process status = {}," +
                                        "kill scene flag value = {},killed operate time = {},real run script = {}",
                                timeInterval, currentTask.getId(), currentTask.getStatus(),
                                taskLogId, taskLog.getStatus(), killTask.getKillSceneFlag(),
                                DateUtil.formatDate(killTask.getKilledOperateTime(), DateUtil.DATE_FULL_STR),
                                taskLog.getRealCommand());
                        continue;
                    }

                    // 如果超时了，则将状态强制设置为已经杀死的完结状态
                    TaskRunCell cell;
                    if (TaskExecuteType.ROUTINE.getCode().equals(taskLog.getExecuteType())) {
                        cell = new TaskRunCellRoutine(taskLog.getNodeId(), null, taskLog.getExecuteType(), currentTask);
                    } else if (TaskExecuteType.RERUN.getCode().equals(taskLog.getExecuteType())) {
                        cell = new TaskRunCellReRun(taskLog.getNodeId(), null, taskLog.getExecuteType(), currentTask,
                                taskLog.getRerunBatchNumber());
                    } else {
                        cell = new TaskRunCellFillData(taskLog.getNodeId(), null, taskLog.getExecuteType(), currentTask,
                                taskLog.getRerunBatchNumber(), taskLog.getFillDataTime());
                    }
                    int returnCode = taskLog.getReturnCode() == null ? -1 : taskLog.getReturnCode();
                    if (KillOperationInitiator.SYS_RUNNING_TASK_MONITOR.getValue().equals(killTask.getKillSceneFlag())) {
                        // 更新任务结束
                        taskStateClientService.taskStateFinished(cell, taskLogId, TaskStatus.RUN_TIMEOUT_KILLED.getValue(), true, returnCode);

                        // 然后将此任务移除
                        iterator.remove();

                        LOG.info("[TaskKilledService]update task status to run_timeout_killed" +
                                        "task id = {},task current status = {}," +
                                        "task process id = {},task process status = {}," +
                                        "kill scene flag value = {},killed operate time = {},real run script = {}",
                                currentTask.getId(), currentTask.getStatus(),
                                taskLogId, taskLog.getStatus(), killTask.getKillSceneFlag(),
                                DateUtil.formatDate(killTask.getKilledOperateTime(), DateUtil.DATE_FULL_STR),
                                taskLog.getRealCommand());
                    } else if (KillOperationInitiator.EXTERNAL_CLIENT.getValue().equals(killTask.getKillSceneFlag())) {
                        // 更新任务结束
                        taskStateClientService.taskStateFinished(cell, taskLogId, TaskStatus.KILLED.getValue(), true, returnCode);

                        // 然后将此任务移除
                        iterator.remove();

                        LOG.info("[TaskKilledService]update task status to killed" +
                                        "task id = {},task current status = {}," +
                                        "task process id = {},task process status = {}," +
                                        "kill scene flag value = {},killed operate time = {},real run script = {}",
                                currentTask.getId(), currentTask.getStatus(),
                                taskLogId, taskLog.getStatus(), killTask.getKillSceneFlag(),
                                DateUtil.formatDate(killTask.getKilledOperateTime(), DateUtil.DATE_FULL_STR),
                                taskLog.getRealCommand());
                    } else {
                        LOG.info("[TaskKilledService]" +
                                        "task kill scene flag is not illegal that kill scene flag value is {}," +
                                        "task id = {},task current status = {}," +
                                        "task process id = {},task process status = {}," +
                                        "kill scene flag value = {},killed operate time = {},real run script = {}",
                                killTask.getKillSceneFlag(), currentTask.getId(), currentTask.getStatus(),
                                taskLogId, taskLog.getStatus(), killTask.getKillSceneFlag(),
                                DateUtil.formatDate(killTask.getKilledOperateTime(), DateUtil.DATE_FULL_STR),
                                taskLog.getRealCommand());
                        throw new RuntimeException("[TaskKilledService]" +
                                "task kill scene flag is not illegal that kill scene flag value =  "
                                + killTask.getKillSceneFlag() + ",task id = " + taskLog.getTaskId()
                                + ", task process id = " + taskLog.getId());
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    try {
                        Thread.sleep(checkKilledTaskCycleTime * 1000);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }

        }
    }

    public BlockingQueue<KillTask> getHasBeenKilledTasks() {
        return hasBeenKilledTasks;
    }

}
