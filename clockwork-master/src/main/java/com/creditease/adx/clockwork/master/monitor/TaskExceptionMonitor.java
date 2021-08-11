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

import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.client.service.*;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.TaskRunCellRoutine;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.enums.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.master.service.ITaskDelayService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 监控异常状态的作业
 * <p>
 * 标记（task_exeception_monitor独立线程做标记工作）：
 * 程序监听5分钟/次，异常态任务（例行任务长时间停留在submit、master_has_received、worker_has_received状态5任务）
 * 1. kill掉异常态任务（并从队列中移除）
 * 2. 结束任务，状态修改为：异常结束exception
 * 3. 延迟状态标记为：异常延迟（exception_delay）
 * 4. 并记录此次异常态日志
 *
 * <p>
 * 恢复（routine线程）：
 * 程序1分钟/次，使用现有下发线程
 * 1. 获取到延迟状态为异常延迟（exception_delay）的所有任务
 * 2. 并且状态为结束态
 * 3. 延迟状态修改为：异常延迟恢复（exception_delay_recovery）
 * 4. 合并任务到下发任务列表，并去重
 * 5. 运行成功后下发子任务
 */
@Service
public class TaskExceptionMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskExceptionMonitor.class);

    @Resource(name = "nodeClientService")
    private NodeClientService nodeClientService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskLogClientService")
    private TaskLogClientService taskLogClientService;

    @Resource(name = "taskStateClientService")
    private TaskStateClientService taskStateClientService;

    @Autowired
    private ITaskDelayService taskDelayService;

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    // 超过这个分钟数的task标记为：异常延迟（exception_delay）
    @Value("${task.exception.residence.time.minutes}")
    private int taskExceptionResidenceTimeMinutes;

    private static List<String> statusList;

    static {
        statusList = Arrays.asList(
                TaskStatus.SUBMIT.getValue(),
                TaskStatus.MASTER_HAS_RECEIVE.getValue()
        );
    }

    /**
     * 轮询异常状态的作业
     * 异常态作业：例行任务长时间停留在submit、master_has_received状态的任务
     */
    @Scheduled(cron = "${monitor.task.exception.cron.exp}")
    public void exceptionResidenceTimeTask() {
        try {
            // 获取执行权限
            boolean toLaunchTaskCurrentTime = lockRecordClientService.getLockAndRecord(
                    UniqueValueRecordType.MASTER_TASK_EXCEPTION_MONITOR.getValue(), nodeIp, Integer.parseInt(nodePort));

            // 检查是否当前master执行此次下发任务
            if (!toLaunchTaskCurrentTime) {
                return;
            }

            Date now = new Date();

            // 获取开始状态的task
            List<TbClockworkTaskPojo> taskPojoList = taskClientService.getTaskListByStatusList(statusList);
            if (CollectionUtils.isEmpty(taskPojoList)) {
                return;
            }

            LOG.info("[TaskExceptionMonitor-getTaskListByStatusList] find current status = {} , task size = {}. ",
                    statusList, taskPojoList.size());

            // 检测任务是否超过系统预定的时间（未超过即正常的作业，移除，无需做处理）
            checkTaskIsResidenceTime(taskPojoList, now);

            // 检测任务是否是例行执行任务类型（不是例行执行任务，移除，无需做处理）
            checkTaskIsRoutineExecuteType(taskPojoList, now);

            // 是否存在异常状态的作业, 不存在则直接返回
            if (taskPojoList.isEmpty()) {
                LOG.info("[TaskExceptionMonitor-getTaskListByStatusList] not find current task is exception status!");
                return;
            }

            // 结束掉异常状态的作业
            // 1. 从master、worker队列中移除掉任务
            // 2. 结束任务，状态修改为异常结束exception
            removeFromQueueAndStopTask(taskPojoList);

            // 标记延迟状态为：EXCEPTION_DELAY 异常延迟
            taskDelayService.handleTasksDelayStatusAndRecordDelayLog(taskPojoList, TaskDelayStatus.EXCEPTION_DELAY.getCode());

        } catch (Exception e) {
            LOG.error("[TaskExceptionMonitor] exceptionResidenceTimeTask Error {}.", e.getMessage(), e);
        }
        LOG.info("[TaskExceptionMonitor] exceptionResidenceTimeTask Cycle finished!");
    }

    /**
     * 检测任务是否超过系统预定的时间，如果没有超时，则无需处理（从list中移除）
     *
     * @param taskPojoList task list
     * @param now          当前时间
     */
    private void checkTaskIsResidenceTime(List<TbClockworkTaskPojo> taskPojoList, Date now) {
        if (CollectionUtils.isEmpty(taskPojoList)) {
            return;
        }
        Iterator<TbClockworkTaskPojo> iterator = taskPojoList.iterator();
        while (iterator.hasNext()) {
            TbClockworkTaskPojo task = iterator.next();
            if (task == null || task.getLastStartTime() == null) {
                iterator.remove();
                continue;
            }

            long runningTime = now.getTime() - task.getLastStartTime().getTime();

            // 是否超过系统预定时间
            if (runningTime > taskExceptionResidenceTimeMinutes * 60 * 1000) {
                LOG.info("[TaskExceptionMonitor][time out]task's running timeout that System preset is {} "
                                + "minutes, but has running time is {} minutes, task id = {}, task status = {}",
                        taskExceptionResidenceTimeMinutes, runningTime / 60 / 1000, task.getId(), task.getStatus());
            } else {
                LOG.info("[TaskExceptionMonitor][Not time out]task's running not timeout that System preset is {} "
                                + "minutes, but has running time is {} minutes, task id = {}, task status = {}",
                        taskExceptionResidenceTimeMinutes, runningTime / 60 / 1000, task.getId(), task.getStatus());
                iterator.remove();
            }
        }
    }

    /**
     * 检测任务是否是例行执行任务类型
     *
     * @param taskPojoList task list
     * @param now          当前时间
     */
    private void checkTaskIsRoutineExecuteType(List<TbClockworkTaskPojo> taskPojoList, Date now) {
        if (CollectionUtils.isEmpty(taskPojoList)) {
            return;
        }

        List<Integer> taskIds = taskPojoList.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList());
        if (taskIds.isEmpty()) {
            return;
        }

        // 执行时间在开始时间-结束时间范围内的任务,
        // 如果taskExceptionResidenceTimeMinutes为1小时，当前时间为8点
        // 即：startTime为当前时间2小时以前，6点
        // 即：endTime  为当前时间1小时以前，7点向右偏移5分钟（7点零5分）
        long startTime = now.getTime() - taskExceptionResidenceTimeMinutes * 60 * 1000 * 2;
        long endTime = now.getTime() - taskExceptionResidenceTimeMinutes * 60 * 1000 + 5 * 60 * 1000;
        List<TbClockworkTaskLogPojo> taskLogList = taskLogClientService
                .getTaskLogByTaskIdsAndStatusList(taskIds, statusList, new Date(startTime), new Date(endTime));
        if (CollectionUtils.isEmpty(taskLogList)) {
            return;
        }

        // taskIdAndLogId
        Map<Integer, Integer> taskIdAndLogId = taskLogList.stream()
                .collect(Collectors.toMap(TbClockworkTaskLogPojo::getTaskId, TbClockworkTaskLogPojo::getId, (key1, key2) -> key2));

        // 设置taskLogId
        for (TbClockworkTaskPojo next : taskPojoList) {
            next.setTaskLogId(taskIdAndLogId.get(next.getId()));
        }

        // 获取需要移除的任务id, 过滤到不是ROUTINE的任务
        HashMap<Integer, Integer> removeTaskIdsMap = new HashMap<>();
        for (TbClockworkTaskLogPojo taskLogPojo : taskLogList) {
            if (!taskLogPojo.getExecuteType().equals(TaskExecuteType.ROUTINE.getCode())) {
                removeTaskIdsMap.put(taskLogPojo.getId(), taskLogPojo.getExecuteType());
            }
        }

        // 存在需要移除的任务，则remove
        if (removeTaskIdsMap.isEmpty()) {
            return;
        }
        taskPojoList.removeIf(taskPojo -> removeTaskIdsMap.containsKey(taskPojo.getId()));
    }

    /**
     * 结束掉任务
     *
     * @param taskPojoList task list
     */
    private void removeFromQueueAndStopTask(List<TbClockworkTaskPojo> taskPojoList) {
        if (CollectionUtils.isEmpty(taskPojoList)) {
            return;
        }
        // 活着的master、worker节点
        List<TbClockworkNode> allEnableMasterNode = nodeClientService.getAllEnableNodeByRole(NodeType.MASTER.getValue());
        List<TbClockworkNode> allEnableWorkerNode = nodeClientService.getAllEnableNodeByRole(NodeType.WORKER.getValue());

        for (TbClockworkTaskPojo taskPojo : taskPojoList) {
            // 此时 - 还没有启动进程 有可能存在taskRunService队列中, 需要去所有队列中kill
            int count = 0;
            if (CollectionUtils.isNotEmpty(allEnableMasterNode))
                for (TbClockworkNode tbClockMasterNode : allEnableMasterNode) {
                    String url = null;
                    try {
                        url = String.format("http://%s:%s/clockwork/master/task/stop/removeTaskFromWaitForDistributeQueue",
                                tbClockMasterNode.getIp(), tbClockMasterNode.getPort());
                        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                        map.add("taskId", String.valueOf(taskPojo.getId()));
                        map.add("isAuto", "true");
                        Map<String, Object> result = restTemplateClient.getResult(url, map);
                        if (result.containsKey(Constant.CODE)
                                && result.get(Constant.CODE).equals(Constant.SUCCESS_CODE)
                                && (count = Integer.parseInt(String.valueOf(result.get(Constant.DATA)))) > 0) {
                            LOG.info("[TaskExceptionMonitor-removeTaskFromWaitForDistributeQueue]remove success. " +
                                            "url = {}, needTobeKilledAndRemoveTaskId = {}, nodeId = {}, nodePort = {}",
                                    url, taskPojo.getId(), tbClockMasterNode.getId(), tbClockMasterNode.getPort());
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("[TaskExceptionMonitor-stopTaskListAndRemoveFromQueue]" +
                                "removeTaskFromExecutedQueue url error. url = {}, count = {}", url, count);
                    }
                }

            // 最终不存在队列中
            if (count > 0) {
                LOG.info("[removeTaskFromWaitForDistributeQueue]exist in the master queue, taskId = {}", taskPojo.getId());
                continue;
            }

            if (CollectionUtils.isNotEmpty(allEnableWorkerNode))
                for (TbClockworkNode tbClockworkNode : allEnableWorkerNode) {
                    String url = null;
                    try {
                        url = String.format("http://%s:%s/clockwork/worker/task/stop/removeTaskFromExecutedQueue",
                                tbClockworkNode.getIp(), tbClockworkNode.getPort());
                        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                        map.add("taskId", String.valueOf(taskPojo.getId()));
                        map.add("isAuto", "true");
                        Map<String, Object> result = restTemplateClient.getResult(url, map);
                        if (result.containsKey(Constant.CODE)
                                && result.get(Constant.CODE).equals(Constant.SUCCESS_CODE)
                                && (count = Integer.parseInt(String.valueOf(result.get(Constant.DATA)))) > 0) {
                            LOG.info("[TaskExceptionMonitor-stopTaskListAndRemoveFromQueue]" +
                                            "not found taskLog, but found task in needBeExecutedQueue and remove success. " +
                                            "url = {}, needTobeKilledAndRemoveTaskId = {}, nodeId = {}, nodePort = {}",
                                    url, taskPojo.getId(), tbClockworkNode.getId(), tbClockworkNode.getPort());
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("[TaskExceptionMonitor-stopTaskListAndRemoveFromQueue]" +
                                "removeTaskFromExecutedQueue url error. url = {}, count = {}", url, count);
                    }
                }

            TaskRunCellRoutine taskRunCellRoutine = new TaskRunCellRoutine(
                    null, null, TaskExecuteType.ROUTINE.getCode(), taskPojo);

            // 结束掉任务
            LOG.info("[TaskExceptionMonitor-taskStateFinished]set status = {}, needTobeKilledAndRemoveTaskId = {}, count = {}",
                    TaskStatus.EXCEPTION.getValue(), taskPojo.getId(), count);
            taskStateClientService.taskStateFinished(
                    taskRunCellRoutine, taskPojo.getTaskLogId(), TaskStatus.EXCEPTION.getValue(), true, -1);
        }
    }

}
