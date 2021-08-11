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
import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.client.service.TaskStateClientService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.enums.KillOperationInitiator;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.worker.service.task.execute.ReRunTaskExecuteService;
import com.creditease.adx.clockwork.worker.service.task.execute.RoutineTaskExecuteService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 杀死任务服务类
 */
@Service
public class TaskKillService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TaskKillService.class);

    private final static Integer THREAD_POOL_KEEP_ALIVE_TIME = 5;

    // 存储需要被kill的作业
    private final BlockingQueue<KillTask> needToBeKilledTasks = new LinkedBlockingQueue<KillTask>();

    @Value("${task.thread.pool.num.kill}")
    protected Integer killTaskThreadNumber;

    @Resource(name = "killRemoteService")
    protected KillRemoteService killRemoteService;

    @Autowired
    protected TaskKilledService taskKilledService;

    @Resource(name = "taskStateClientService")
    private TaskStateClientService taskStateClientService;

    @Autowired
    protected LinuxService linuxService;

    @Autowired
    protected ReRunTaskExecuteService reRunTaskExecuteService;

    @Autowired
    protected RoutineTaskExecuteService routineTaskExecuteService;

    @Resource(name = "taskLogClientService")
    private TaskLogClientService taskLogClientService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskOperationClientService")
    private TaskOperationClientService taskOperationClientService;

    @PostConstruct
    public void setup() {
        if (killTaskThreadNumber == null || killTaskThreadNumber < 1) {
            // 单位秒
            killTaskThreadNumber = 20;
        }
        // set name of consuming threads
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("Worker-TaskKillService-Execute-Thread-%d")
                .build();
        // the number of max and core threads are equal, because the fixed number of consuming threads
        ThreadPoolExecutor killTaskExecuteThreadPool =
                new ThreadPoolExecutor(
                        killTaskThreadNumber,
                        killTaskThreadNumber,
                        THREAD_POOL_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                        new SynchronousQueue<Runnable>(),
                        threadFactory
                );
        // start consuming threads, also, it could be called launching thread
        for (int i = 0; i < killTaskThreadNumber; i++) {
            killTaskExecuteThreadPool.execute(this);
        }
        LOG.info("Worker task TaskKillService thread pool started, work thread number is : {}", killTaskThreadNumber);
    }

    @Override
    public void run() {
        while (true) {
            try {
                KillTask killTask = needToBeKilledTasks.poll(5, TimeUnit.SECONDS);
                if (killTask == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[TaskKillService]kill task object is null.");
                    }
                    continue;
                }

                TbClockworkTaskLogPojo taskLog = killTask.getTaskLog();
                if (taskLog == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[TaskKillService]task log object is null.");
                    }
                    continue;
                }

                /**
                 * 由于队列中等待被杀死操作的任务有可能在等待的过程中状态已经变为完结状态
                 * 所以进行真正杀死操作之前进行一次检查，如果进程不存在，依然将状态设置为
                 * 根据不同kill场景过来的kill完结状态，这样做的目的是避免用户发起了kill
                 * 操作最后看见了除kill之外的完结状态，造成困扰。
                 */
                TbClockworkTaskPojo task = taskClientService.getTaskById(taskLog.getTaskId());

                // 从日志中拿取真实运行的shell名称
                String realCommand = taskLog.getRealCommand();

                LOG.info("[TaskKillService]Find task need to be killed," +
                                "task id = {}," +
                                "task log id = {}," +
                                "task log status = {}," +
                                "task status = {}," +
                                "task run script = {}," +
                                "task real run script = {}",
                        taskLog.getTaskId(),
                        taskLog.getId(),
                        taskLog.getStatus(),
                        task.getStatus(),
                        task.getCommand(),
                        realCommand);

                Set<String> pids = linuxService.getPidsByCommand(realCommand);

                // 如果进程PID信息不存在则根据不同的配置kill场景，设置不同的killed状态
                if (CollectionUtils.isEmpty(pids)) {
                    LOG.info("[TaskKillService-logIsNotExist]the task that need to be killed already finished, skip it, " +
                                    "task id = {}, task status is = {}, task log id = {},realCommand = {}",
                            task.getId(), task.getStatus(), taskLog.getId(), realCommand);

                    TaskRunCell cell;
                    if (TaskExecuteType.ROUTINE.getCode().equals(taskLog.getExecuteType())) {
                        cell = new TaskRunCellRoutine(taskLog.getNodeId(), null, taskLog.getExecuteType(), task);
                    } else if (TaskExecuteType.RERUN.getCode().equals(taskLog.getExecuteType())) {
                        cell = new TaskRunCellReRun(taskLog.getNodeId(), null, taskLog.getExecuteType(), task,
                                taskLog.getRerunBatchNumber());
                    } else {
                        cell = new TaskRunCellFillData(taskLog.getNodeId(), null, taskLog.getExecuteType(), task,
                                taskLog.getRerunBatchNumber(), taskLog.getFillDataTime());
                    }

                    // 修改状态
                    int returnCode = taskLog.getReturnCode() == null ? -1 : taskLog.getReturnCode();
                    if (killTask.getKillSceneFlag().intValue() == KillOperationInitiator.EXTERNAL_CLIENT.getValue().intValue()) {
                        // 前端或者外部接口触发的kill操作
                        taskStateClientService.taskStateFinished(cell, taskLog.getId(), TaskStatus.KILLED.getValue(),true, returnCode);
                        LOG.info("[TaskKillService-logIsNotExist]update task status to killed, " +
                                        "kill scene flag = {}, task id = {}, returnCode = {}, realCommand = {}",
                                killTask.getKillSceneFlag(), task.getId(), returnCode, realCommand);
                    } else if (killTask.getKillSceneFlag().intValue()
                            == KillOperationInitiator.SYS_RUNNING_TASK_MONITOR.getValue().intValue()) {
                        // 调度系统内部监控运行任务的线程发出的kill操作
                        taskStateClientService.taskStateFinished(cell, taskLog.getId(), TaskStatus.RUN_TIMEOUT_KILLED.getValue(), true, returnCode);
                        LOG.info("[TaskKillService-logIsNotExist]update task status to run_timeout_killed, " +
                                        "kill scene flag = {}, task id = {}, returnCode = {}, realCommand = {}",
                                killTask.getKillSceneFlag(), task.getId(), returnCode, realCommand);
                    } else {
                        LOG.error("[TaskKillService-logIsNotExist]Kill scene flag not support that value is = {}, "
                                + "task id = {}, returnCode = {}, realCommand = {}", killTask.getKillSceneFlag(), task.getId(), returnCode, realCommand);
                    }
                    continue;
                }

                // 开始执行kill操作
                String pidsInfo = StringUtils.join(pids, ",");
                LOG.info("[TaskKillService-logIsExist]begin kill task, pids = {}, kill scene flag = {}, task id = {}, "
                        + "realCommand = {}", pidsInfo, killTask.getKillSceneFlag(), task.getId(), realCommand);
                killTask.setPids(pids);
                killTask.setPidsInfo(pidsInfo);
                killTask.setTask(PojoUtil.convert(task, TbClockworkTaskPojo.class));

                boolean result = executorKillTaskOperation(killTask);
                LOG.info("[TaskKillService]kill task result = {}, task id = {},task status is = {}," +
                                "task log id = {},realCommand = {},pids = {}",
                        result, task.getId(), task.getStatus(), taskLog.getId(), realCommand, pidsInfo);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 添加任务到等待杀死的队列
     *
     * @param taskLog
     * @param killSceneFlag
     * @return
     */
    public boolean addTaskWaitingToBeKilled(TbClockworkTaskLogPojo taskLog, Integer killSceneFlag) {
        try {
            int taskId = taskLog.getTaskId();
            int taskLogId = taskLog.getId();
            if (killSceneFlag.intValue() == KillOperationInitiator.EXTERNAL_CLIENT.getValue().intValue()) {

                // 如果该任务已经为success, 则跳过(避免用户误操作)
                if (TaskStatus.SUCCESS.getValue().equals(taskClientService.getTaskStatusById(taskId))) {
                    LOG.info("[addTaskWaitingToBeKilled]skip stop task, , Because his state is success. " +
                                    "task log id = {}, task id = {}, killSceneFlag = {}",
                            taskLog.getId(), taskLog.getTaskId(), killSceneFlag);
                    return true;
                }
                // 修改作业的状态到停止中（killing）
                taskOperationClientService.updateTaskStatus(taskId, TaskStatus.KILLING.getValue());
                taskLogClientService.updateTaskLogStatus(taskLogId, TaskStatus.KILLING.getValue());
                LOG.info("[addTaskWaitingToBeKilled]update task status to {}, task log id = {},task id = {}, killSceneFlag = {}",
                        TaskStatus.KILLING.getValue(), taskLog.getId(), taskLog.getTaskId(), killSceneFlag);
                taskLog.setStatus(TaskStatus.KILLING.getValue());

            } else if (killSceneFlag.intValue() == KillOperationInitiator.SYS_RUNNING_TASK_MONITOR.getValue().intValue()) {
                // 修改作业的状态到停止中（killing）
                taskOperationClientService.updateTaskStatus(taskId, TaskStatus.RUN_TIMEOUT_KILLING.getValue());
                taskLogClientService.updateTaskLogStatus(taskLogId, TaskStatus.RUN_TIMEOUT_KILLING.getValue());
                LOG.info("[addTaskWaitingToBeKilled]update task status to {}, task log id = {},task id = {}, killSceneFlag = {}",
                        TaskStatus.RUN_TIMEOUT_KILLING.getValue(), taskLog.getId(), taskLog.getTaskId(), killSceneFlag);
                taskLog.setStatus(TaskStatus.RUN_TIMEOUT_KILLING.getValue());
            } else {
                LOG.info("[addTaskWaitingToBeKilled]killSceneFlag is illegal,task log id = {}, task id = {}, killSceneFlag = {}",
                        taskLog.getId(), taskLog.getTaskId(), killSceneFlag);
                return false;
            }

            //组装kill对象 加入到队列 等待执行kill
            KillTask killTask = new KillTask();
            killTask.setTaskLog(taskLog);
            killTask.setKillSceneFlag(killSceneFlag);
            needToBeKilledTasks.put(killTask);
            return true;
        } catch (Exception e) {
            LOG.error("addTaskWaitingToBeKilled Error {}.", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 执行杀死进程操作，只有成功，没有杀不死的情况
     *
     * @param killTask
     * @return
     */
    private boolean executorKillTaskOperation(KillTask killTask) {
        TbClockworkTaskLogPojo taskLog = killTask.getTaskLog();
        TbClockworkTaskPojo task = killTask.getTask();
        if (task == null || task.getId() == null) {
            return false;
        }

        LOG.info("[TaskKillService-killTaskLog]begin kill task log, task id = {}, task status = {}, task log id = {}, "
                        + "task log status = {}, task real run script = {}, task log pid = {}, all pids = {}",
                task.getId(), task.getStatus(), taskLog.getId(), taskLog.getStatus(),
                taskLog.getRealCommand(), taskLog.getPid(), killTask.getPidsInfo());
        //kill -15 杀当前任务进程，以及当前任务的子进程
        for (String pid : killTask.getPids()) {
            boolean result = false;
            try {
                linuxService.kill15TaskByPid(Integer.parseInt(pid));
                result = true;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            LOG.info("[TaskKillService][kill-15]kill -15 task log result = {}, task id = {}, task status = {}," +
                            "task log id = {}, task log status = {}, " +
                            "task real run script = {}, task log pid = {}, kill pid = {}, all pids = {}",
                    result ? "finished" : "failure", task.getId(), task.getStatus(), taskLog.getId(), taskLog.getStatus(),
                    taskLog.getRealCommand(), taskLog.getPid(), pid, killTask.getPidsInfo());
        }

        //kill -15 杀完停顿2秒，检查进程是否还存在，存在则使用kill -9 杀死
        try {
            // 停2秒，然后再检查，如果还存在则kill -9 杀
            Thread.sleep(2000);
            Set<String> pidsOfTask = linuxService.getPidsByCommand(taskLog.getRealCommand());
            if (CollectionUtils.isNotEmpty(pidsOfTask)) {
                for (String pidOfTask : pidsOfTask) {
                    boolean result = false;
                    try {
                        linuxService.kill9TaskByPid(Integer.parseInt(pidOfTask));
                        result = true;
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                    LOG.info("[TaskKillService][kill-9]kill -9 task log result = {}, task id = {}, task status = {}," +
                                    "task log id = {}, task log status = {}, " +
                                    "task real run script = {}, task log pid = {}, kill pid = {}, all pids = {}",
                            result ? "finished" : "failure", task.getId(), task.getStatus(), taskLog.getId(), taskLog.getStatus(),
                            taskLog.getRealCommand(), taskLog.getPid(), pidOfTask, killTask.getPidsInfo());
                }
            } else {
                LOG.info("[TaskKillService]" +
                                "task log had killed by kill -15 command, There's not need kill by -9 again" +
                                "task id = {},task status = {}, task log id = {},task log status = {}," +
                                "task real run script = {},task log pid = {},all pids = {}",
                        task.getId(), task.getStatus(), taskLog.getId(), taskLog.getStatus(),
                        taskLog.getRealCommand(), taskLog.getPid(), killTask.getPidsInfo());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        try {
            // 根据任务类型，进行相关周边资源的释放，例如hive任务需要将yarn上的任务kill掉
            killRemoteService.killProcess(taskLog);
            LOG.info("[TaskKillService]end kill task other resources, task id = {}, task status = {}," +
                            "task log id = {}, task log status = {}, task real run script = {},pid = {}",
                    task.getId(), task.getStatus(), taskLog.getId(), taskLog.getStatus(),
                    taskLog.getRealCommand(), taskLog.getPid());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        try {
            // 放入已经执行杀死操作的队列，用于后面检查状态是否变为已杀死的终结状态。
            killTask.setKilledOperateTime(new Date());
            taskKilledService.getHasBeenKilledTasks().put(killTask);
            LOG.info("[TaskKillService]end kill task log and put into kill queue, task id = {},task status = {}, " +
                            "task log id = {}, task log status = {}, task real run script = {}, pid = {}",
                    task.getId(), task.getStatus(), taskLog.getId(), taskLog.getStatus(),
                    taskLog.getRealCommand(), taskLog.getPid());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return true;
    }

}
