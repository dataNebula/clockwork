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

package com.creditease.adx.clockwork.master.monitor;

import com.creditease.adx.clockwork.client.service.*;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.StopRunningTaskParam;
import com.creditease.adx.clockwork.common.entity.TaskRunCellRoutine;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.enums.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.master.service.ITaskLogService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 监控正在运行的作业
 */
@Service
public class TaskRunningMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRunningMonitor.class);

    @Resource(name = "nodeClientService")
    private NodeClientService nodeClientService;

    @Resource(name = "taskLogService")
    private ITaskLogService taskLogService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskOperationClientService")
    private TaskOperationClientService taskOperationClientService;

    @Resource(name = "taskLogClientService")
    private TaskLogClientService taskLogClientService;

    @Resource(name = "taskStateClientService")
    private TaskStateClientService taskStateClientService;

    @Value("${task.running.timeout.minutes}")
    private int taskRunningTimeoutMinutes;

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    /**
     * 轮询正在执行的作业
     */
    @Scheduled(cron = "${monitor.task.running.cron.exp}")
    public void runningTask() {
        try {

            LOG.info("[TaskRunningMonitor]RunningMonitor Cycle start ...");

            // 获取执行权限
            boolean toLaunchTaskCurrentTime = lockRecordClientService.getLockAndRecord(
                    UniqueValueRecordType.MASTER_TASK_RUNNING_MONITOR.getValue(), nodeIp, Integer.parseInt(nodePort));

            // 检查是否当前master执行此次下发任务
            if (!toLaunchTaskCurrentTime) {
                LOG.debug("TaskRunningMonitor.runningTask toLaunchTaskCurrentTime = {}", false);
                return;
            }

            // 获取开始状态的task日志
            List<TbClockworkTaskLogPojo> taskLogList = taskLogClientService.getTaskLogByTaskStatus(
                    TaskStatus.RUNNING.getValue());
            // 获取开始状态的task
            List<TbClockworkTaskPojo> tasks = taskClientService.getTaskByStatus(TaskStatus.RUNNING.getValue());
            // 获得可用的worker节点信息
            Map<Integer, TbClockworkNode> nodeMap = getAliveWorkerNode();
            if (nodeMap == null || nodeMap.size() < 1) {
                LOG.error("[TaskRunningMonitor]alive worker nodes is 0,please check it.");
                return;
            }

            // 从任务执行日志中检查是否有超时的任务
            checkTaskRunTimeoutFromTaskLog(taskLogList, nodeMap);

            // 检查是否存在有些作业没有对应日志的情况，主要考虑临时上线、未预见的异常情况造成作业状态不一致的场景。
            checkTaskRunTimeoutNotHasTaskLog(findNeedCheckTasks(taskLogList, tasks));

        } catch (Exception e) {
            LOG.error("TaskRunningMonitor Error {}.", e.getMessage(), e);
        } finally {
            LOG.info("[TaskRunningMonitor]RunningMonitor Cycle end !");
        }
    }

    /**
     * 从任务执行日志中检查是否有超时的任务
     * 0. 日志存在，不存在任务，设置日志状态为失败
     * 1. 没有超时，直接返回
     * 2. 超时了，进程不存在（莫名挂掉，节点挂掉），直接设置状态
     * 3. 超时了，进程存在，需要去kill
     *
     * @param taskLogList task log list (RUNNING)
     * @param nodeMap     node
     */
    private void checkTaskRunTimeoutFromTaskLog(List<TbClockworkTaskLogPojo> taskLogList, Map<Integer, TbClockworkNode> nodeMap) {
        if (CollectionUtils.isEmpty(taskLogList)) {
            LOG.info("[TaskRunningMonitor]No found running task log information.");
            return;
        }

        LOG.info("[TaskRunningMonitor]find running task log info,size = {}", taskLogList.size());

        Date now = new Date();

        for (TbClockworkTaskLogPojo taskLog : taskLogList) {
            // 获取作业的信息
            TbClockworkTaskPojo task = taskClientService.getTaskById(taskLog.getTaskId());
            if (task == null) {
                updateTaskLogEnd(taskLog.getId(), TaskStatus.EXCEPTION.getValue());
                continue;
            }

            // 根据每个日志的开始时间计算已经运行了多久
            long runningTime = now.getTime() - task.getLastStartTime().getTime();

            // 更新作业的状态到任务超时杀死
            TaskRunCellRoutine cell = new TaskRunCellRoutine(taskLog.getNodeId(), null, TaskExecuteType.ROUTINE.getCode(), task);

            // 如果用户配置超时时间，则按用户超时时间作为基准检查
            TimeoutEntity timeoutEntity = getTimeoutEntity(task.getRunTimeout());
            int runTimeout = timeoutEntity.getRunTimeout();
            String MAKE = timeoutEntity.getMake();

            // 任务是否超时，没有超时直接返回
            if (runningTime < runTimeout * 60 * 1000) {
                LOG.info("[TaskRunningMonitor][{}-Threshold][Not time out]" +
                                "task's running not timeout that {} expect threshold is {} minutes, " +
                                "but has running time is {} minutes, worker node id = {}, task id = {}, " +
                                "task status = {}, task log id = {}, task log status = {}, real run script = {}, pid = {}",
                        MAKE, MAKE, runTimeout, runningTime / 60 / 1000, taskLog.getNodeId(), task.getId(), task.getStatus(),
                        taskLog.getId(), taskLog.getStatus(), taskLog.getRealCommand(), taskLog.getPid());
                continue;
            } else {
                LOG.info("[TaskRunningMonitor][{}-Threshold][Time out]" +
                                "task's running timeout that {} expect threshold is {} minutes, " +
                                "but has running time is {} minutes, worker node id = {}, task id = {}, " +
                                "task status = {}, task log id = {}, task log status = {}, real run script = {}, pid = {}",
                        MAKE, MAKE, runTimeout, runningTime / 60 / 1000, taskLog.getNodeId(), task.getId(),
                        task.getStatus(), taskLog.getId(), taskLog.getStatus(), taskLog.getRealCommand(), taskLog.getPid());
            }

            // 超时任务
            // 进程不存在处理，直接设置状态后就返回
            if (!taskLogService.isExistRunningProcess(taskLog, nodeMap.get(taskLog.getNodeId()))) {
                taskStateClientService.taskStateFinished(cell, taskLog.getId(), TaskStatus.RUN_TIMEOUT_KILLED.getValue(),
                        true, taskLog.getReturnCode() == null ? -1 : taskLog.getReturnCode());
                LOG.info("[TaskRunningMonitor][{}-Threshold][Time out]" +
                                "task log already not exist at worker node, worker node id = {}, task id = {}, " +
                                "task status = {}, task log id = {}, task log status = {}, real run script = {}, pid = {}",
                        MAKE, taskLog.getNodeId(), task.getId(),
                        task.getStatus(), taskLog.getTaskId(), taskLog.getStatus(), taskLog.getRealCommand(), taskLog.getPid());
                continue;
            }

            // 超过了系统配置的超时时间，则进行杀死操作, (killTask: 修改状态为run_timeout_king & 加入到killedServer队列)
            StopRunningTaskParam stopRunningTaskParam = new StopRunningTaskParam();
            stopRunningTaskParam.setKillSceneFlag(KillOperationInitiator.SYS_RUNNING_TASK_MONITOR.getValue());
            stopRunningTaskParam.setTaskLogPojo(taskLog);
            boolean result = taskOperationClientService.stopRunningTask(stopRunningTaskParam);
            LOG.info("[TaskRunningMonitor][{}-Threshold][Time out]stop task sign launch to worker result = {}, " +
                            "worker node id = {}, task id = {}, task status = {}, task log id = {}, task log status = {}, " +
                            "real run script = {}, pid = {}",
                    MAKE, result, taskLog.getNodeId(), task.getId(), task.getStatus(),
                    taskLog.getId(), taskLog.getStatus(), taskLog.getRealCommand(), taskLog.getPid());
        }
    }

    /**
     * 从任务表中检查是否有超时的任务
     *
     * @param tasks
     */
    private void checkTaskRunTimeoutNotHasTaskLog(List<TbClockworkTaskPojo> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            LOG.info("[TaskRunningMonitor-ProcessNeedCheckTasks]No found need check running task information.");
            return;
        }
        Date now = new Date();
        for (TbClockworkTaskPojo task : tasks) {
            LOG.info("[TaskRunningMonitor-ProcessNeedCheckTasks]Found need check running task info, task id = {}", task.getId());

            // 根据每个作业的开始时间计算已经运行了多久
            long runningTime = now.getTime() - task.getLastStartTime().getTime();

            // 如果用户配置超时时间，则按用户超时时间作为基准检查
            TimeoutEntity timeoutEntity = getTimeoutEntity(task.getRunTimeout());
            int runTimeout = timeoutEntity.getRunTimeout();
            String MAKE = timeoutEntity.getMake();

            // 未超时跳过，超时结束
            if (runningTime < runTimeout * 60 * 1000) {
                LOG.info("[TaskRunningMonitor-ProcessNeedCheckTasks][{}-Threshold][Not time out]" +
                                "task's running not timeout that {} expect threshold is {} minutes, " +
                                "but has running time is {} minutes, task id = {}, task status = {}",
                        MAKE, MAKE, runTimeout, runningTime / 60 / 1000, task.getId(), task.getStatus());
                continue;
            } else {
                LOG.info("[TaskRunningMonitor-ProcessNeedCheckTasks][{}-Threshold][Time out]" +
                                "task's running not timeout that {} expect threshold is {} minutes, " +
                                "but has running time is {} minutes, task id = {}, task status = {}",
                        MAKE, MAKE, runTimeout, runningTime / 60 / 1000, task.getId(), task.getStatus());
            }
            // 更新作业的状态到任务超时杀死
            taskOperationClientService.updateTaskStatus(task.getId(), TaskStatus.RUN_TIMEOUT_KILLED.getValue());
            // 更新作业的最新结束时间
            updateTaskLastEndTime(task.getId(), now);

        }
    }


    private Map<Integer, TbClockworkNode> getAliveWorkerNode() {
        List<TbClockworkNode> nodes = nodeClientService.getAllEnableNodeByRole(NodeType.WORKER.getValue());
        if (CollectionUtils.isEmpty(nodes)) {
            return null;
        }
        // Map<key是nodeId, value是该Node>
        return nodes.stream().collect(Collectors.toMap(TbClockworkNode::getId, Function.identity(), (key1, key2) -> key2));
    }

    /**
     * 检查是否存在作业日志和作业状态不匹配的作业信息，也就是日志为完成，作业状态为开始的场景
     * 主要考虑临时上线、未预见的异常情况造成作业状态不一致的场景。
     *
     * @param taskLogList task log list
     * @param tasks       task list
     * @return
     */
    private List<TbClockworkTaskPojo> findNeedCheckTasks(List<TbClockworkTaskLogPojo> taskLogList, List<TbClockworkTaskPojo> tasks) {

        // 任务日志和任务的开始状态的都为空，无需比较，返回空
        if (CollectionUtils.isEmpty(taskLogList) && CollectionUtils.isEmpty(tasks)) {
            return null;
        }

        // 任务日志开始状态为空，发现有任务状态为开始，则直接返回
        if (CollectionUtils.isEmpty(taskLogList) && CollectionUtils.isNotEmpty(tasks)) {
            return tasks;
        }

        // 任务日志开始状态不为空，任务状态为开始的作业为空，则直接返回空
        if (CollectionUtils.isNotEmpty(taskLogList) && CollectionUtils.isEmpty(tasks)) {
            return null;
        }

        // 组装成map结构用于后续逻辑比较，key为作业ID，value为日志ID
        Map<Integer, Integer> taskLogMap = new HashMap<>();
        for (TbClockworkTaskLogPojo taskLog : taskLogList) {
            taskLogMap.put(taskLog.getTaskId(), taskLog.getId());
        }

        List<TbClockworkTaskPojo> result = new ArrayList<>();

        // 获得需要检查的任务, 任务日志为空
        for (TbClockworkTaskPojo task : tasks) {
            if (taskLogMap.get(task.getId()) == null) {
                result.add(task);
            }
        }
        return result;
    }


    /**
     * 更新作业运行日志结束
     *
     * @param taskLogId
     * @param status
     * @return
     */
    private boolean updateTaskLogEnd(Integer taskLogId, String status) {
        TbClockworkTaskLogPojo updateTaskLog = new TbClockworkTaskLogPojo();
        updateTaskLog.setId(taskLogId);
        updateTaskLog.setStatus(status);
        updateTaskLog.setEndTime(new Date());
        updateTaskLog.setIsEnd(true);
        Map<String, Object> interfaceResult = taskLogClientService.getTaskLogClient().updateTaskLog(updateTaskLog);
        return Constant.SUCCESS_CODE.equals(interfaceResult.get(Constant.CODE));
    }


    /**
     * 只更新作业最新的结束时间
     *
     * @param taskId
     * @param taskLastEndTime
     * @return
     */
    private boolean updateTaskLastEndTime(Integer taskId, Date taskLastEndTime) {
        try {
            TbClockworkTaskPojo updateTask = new TbClockworkTaskPojo();
            updateTask.setLastEndTime(taskLastEndTime);
            updateTask.setId(taskId);
            taskOperationClientService.getTaskOperationClient().updateTaskInfo(updateTask);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 获取超时时间
     *
     * @param taskRunTimeout timout
     * @return
     */
    private TimeoutEntity getTimeoutEntity(Integer taskRunTimeout) {
        TimeoutEntity timeoutEntity = null;
        if (taskRunTimeout != null && taskRunTimeout > 0) {
            // 用户自定义了超时时间
            timeoutEntity = new TimeoutEntity(taskRunTimeout, "User");
        } else {
            timeoutEntity = new TimeoutEntity(taskRunningTimeoutMinutes, "System");
        }
        return timeoutEntity;
    }

    static class TimeoutEntity {
        private int runTimeout;
        private String make;

        public int getRunTimeout() {
            return runTimeout;
        }

        public String getMake() {
            return make;
        }

        TimeoutEntity(int runTimeout, String make) {
            this.runTimeout = runTimeout;
            this.make = make;
        }
    }


}
