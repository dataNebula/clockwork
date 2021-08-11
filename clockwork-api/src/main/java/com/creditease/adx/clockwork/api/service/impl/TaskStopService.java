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

package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.*;
import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.StopRunningTaskParam;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogExample;
import com.creditease.adx.clockwork.common.enums.KillOperationInitiator;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkNodeMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskLogMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:48 下午 2020/4/8
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskStopService")
public class TaskStopService implements ITaskStopService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskStopService.class);

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Autowired
    private TbClockworkTaskLogMapper tbClockworkTaskLogMapper;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private ITaskOperationService taskOperationService;

    @Autowired
    private ITaskFillDataService taskFillDataService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private ITaskLogService taskLogService;

    @Autowired
    private TbClockworkNodeMapper tbClockworkNodeMapper;


    /**
     * 停止运行的作业，（已知该任务是Running）
     *
     * @param stopRunningTaskParam param
     */
    @Override
    public void stopRunningTask(StopRunningTaskParam stopRunningTaskParam) {

        if (stopRunningTaskParam == null) {
            throw new RuntimeException("stopRunningTaskParam is null.");
        }
        TbClockworkTaskLogPojo needTobeKilledTaskLog = stopRunningTaskParam.getTaskLogPojo();
        Integer needTobeKilledTaskId = needTobeKilledTaskLog.getTaskId();
        Integer nodeId = needTobeKilledTaskLog.getNodeId();

        // 到worker上去stop正在运行的任务
        TbClockworkNode tbClockworkNode = tbClockworkNodeMapper.selectByPrimaryKey(nodeId);
        String URL = String.format("http://%s:%s/clockwork/worker/task/stop/stopRunningTask",
                tbClockworkNode.getIp(), tbClockworkNode.getPort());

        restTemplateClient.getResult(URL, stopRunningTaskParam);
        LOG.info("[TaskStopService-stopRunningTask]send stop task msg to worker, url = {}, taskId = {}, Pid = {}, " +
                        "executeType = {}, BatchNum = {}, RealCommand = {}。",
                URL, needTobeKilledTaskId,
                needTobeKilledTaskLog.getPid(), needTobeKilledTaskLog.getExecuteType(),
                needTobeKilledTaskLog.getBatchNumber(), needTobeKilledTaskLog.getRealCommand());

    }


    /**
     * 查找出正在running的pid，kill掉 然后根据查询引擎查出异步骤任务的，kill掉（hive的任务直接查日志）
     *
     * @param needTobeKilledAndRemoveTaskIdList taskIds
     */
    @Override
    public void stopTaskListAndRemoveFromQueue(List<Integer> needTobeKilledAndRemoveTaskIdList) {
        // 参数校验
        if (CollectionUtils.isEmpty(needTobeKilledAndRemoveTaskIdList)) {
            LOG.info("[TaskStopService-stopTaskListAndRemoveFromQueue] needTobeKilledTaskIdList = {}, " +
                    "needTobeKilledTaskIdList.size = 0", needTobeKilledAndRemoveTaskIdList);
            throw new RuntimeException("taskId list is null,please check it.");
        }
        LOG.info("[TaskStopService-stopTaskListAndRemoveFromQueue] needTobeStopTaskIdList = {}, " +
                        "needTobeKilledTaskIdList.size = {}",
                needTobeKilledAndRemoveTaskIdList, needTobeKilledAndRemoveTaskIdList.size());

        // 1, 找出还在运行的任务的LOG：runningTaskLogList List && 转为 Map
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria()
                .andTaskIdIn(needTobeKilledAndRemoveTaskIdList)
                .andStatusEqualTo(TaskStatus.RUNNING.getValue());
        List<TbClockworkTaskLog> needTobeKilledTaskLogIsRunningList = tbClockworkTaskLogMapper.selectByExample(example);

        Map<Integer, TbClockworkTaskLog> needTobeKilledTaskLogIsRunningMap = needTobeKilledTaskLogIsRunningList.stream()
                .collect(
                        Collectors.toMap(
                                TbClockworkTaskLog::getTaskId, taskLog -> taskLog,
                                (e1, e2) -> {
                                    return e1.getId() > e2.getId() ? e1 : e2;
                                })
                );

        // 2, 活着的worker节点：TbClockworkNode List, 然后根据NodeGroupId分组
        List<TbClockworkNode> allEnableNode = nodeService.getAllEnableNodeByRole(NodeType.WORKER.getValue());
        Map<Integer, List<TbClockworkNode>> nodeGroupByNodeGroupId = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allEnableNode)) {
            nodeGroupByNodeGroupId = allEnableNode.stream().collect(Collectors.groupingBy(TbClockworkNode::getNodeGroupId));
        }

        /*
         * 遍历所有需要kill的taskId
         * a. 如果没有对应的taskLog 则表示可能还在缓存队列中，需要去缓存队列中kill掉
         * b. 如果存在对应的taskLog 则表示已经运行，需要去对应的节点kill掉
         */
        for (Integer needTobeKilledAndRemoveTaskId : needTobeKilledAndRemoveTaskIdList) {
            TbClockworkTaskLog needTobeKilledTaskLogIsRunning;
            //a. 没有对应的taskLog 则表示可能还在缓存队列中，需要去缓存队列中kill掉
            if ((needTobeKilledTaskLogIsRunning = needTobeKilledTaskLogIsRunningMap.get(needTobeKilledAndRemoveTaskId)) == null) {

                // 如果该任务已经为success, 则跳过(避免用户误操作)
                TbClockworkTaskPojo taskPojo = taskService.getTaskById(needTobeKilledAndRemoveTaskId);
                if (TaskStatus.SUCCESS.getValue().equals(taskPojo.getStatus())) {
                    LOG.info("[stopTaskListAndRemoveFromQueue]skip stop task, Because his state is success. task id = {}, killSceneFlag = {}",
                            needTobeKilledAndRemoveTaskId, KillOperationInitiator.EXTERNAL_CLIENT.getValue());
                    continue;
                }

                // 此时 - 还没有启动进程 有可能存在taskRunService队列中, 需要去所有队列中kill
                int count = 0;
                List<TbClockworkNode> nodes = nodeGroupByNodeGroupId.get(taskPojo.getNodeGid());
                if (CollectionUtils.isNotEmpty(nodes)) for (TbClockworkNode tbClockworkNode : nodes) {
                    String url = null;
                    try {
                        url = String.format("http://%s:%s/clockwork/worker/task/stop/removeTaskFromExecutedQueue",
                                tbClockworkNode.getIp(), tbClockworkNode.getPort());
                        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                        map.add("taskId", String.valueOf(needTobeKilledAndRemoveTaskId));
                        map.add("isAuto", "false");
                        Map<String, Object> result = restTemplateClient.getResult(url, map);
                        if (result.containsKey(Constant.CODE)
                                && result.get(Constant.CODE).equals(Constant.SUCCESS_CODE)
                                && (count = Integer.parseInt(String.valueOf(result.get(Constant.DATA)))) > 0) {
                            LOG.info("[TaskStopService-stopTaskListAndRemoveFromQueue]" +
                                            "not found taskLog, but found task in needBeExecutedQueue and remove success. " +
                                            "url = {}, " +
                                            "needTobeKilledAndRemoveTaskId = {}, " +
                                            "nodeId = {}, " +
                                            "nodePort = {}",
                                    url, needTobeKilledAndRemoveTaskId, tbClockworkNode.getId(), tbClockworkNode.getPort());
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("[TaskStopService-stopTaskListAndRemoveFromQueue]" +
                                        "removeTaskFromExecutedQueue url error. url = {}, count = {}",
                                url, count);
                    }
                }

                TbClockworkTaskPojo updateTask = new TbClockworkTaskPojo();
                updateTask.setId(needTobeKilledAndRemoveTaskId);
                updateTask.setStatus(TaskStatus.KILLED.getValue());
                updateTask.setLastEndTime(new Date());
                taskOperationService.updateTaskInfo(updateTask);

                LOG.info("[TaskStopService-stopTaskListAndRemoveFromQueue]" +
                                "not found taskLog, and remove in needBeExecutedQueue. " +
                                "set status={}, needTobeKilledAndRemoveTaskId = {}, count = {}",
                        TaskStatus.KILLED.getValue(), needTobeKilledAndRemoveTaskId, count);
                continue;
            }

            //b. 存在对应的taskLog 则表示已经运行，需要去对应的节点kill掉
            // 根据状态判断是否可以被kill掉
            if (!TaskStatusUtil.canBeKilledCurrentTaskStatus(needTobeKilledTaskLogIsRunning.getStatus())) {
                LOG.info("[TaskStopService-stopTaskListAndRemoveFromQueue] task can not be stopped,task id = {},task status = {}",
                        needTobeKilledTaskLogIsRunning.getId(), needTobeKilledTaskLogIsRunning.getStatus());
                continue;
            }

            // 获取进程信息，然后再指定的worker服务器上将进程杀死。
            StopRunningTaskParam stopRunningTaskParam = new StopRunningTaskParam();
            stopRunningTaskParam.setTaskLogPojo(PojoUtil.convert(needTobeKilledTaskLogIsRunning, TbClockworkTaskLogPojo.class));
            stopRunningTaskParam.setKillSceneFlag(KillOperationInitiator.EXTERNAL_CLIENT.getValue());
            stopRunningTask(stopRunningTaskParam);
        }
    }

    /**
     * 停止补数任务，通过批次号
     *
     * @param rerunBatchNumber 批次号
     */
    @Override
    public void stopFillDataTask(String rerunBatchNumber) {
        Long rerunBatchNumberL = Long.valueOf(rerunBatchNumber);
        // 1, 通过批次号获取所有该批次下的任务
        List<TbClockworkTask4PagePojo> tasksByReRunBatchNumber
                = taskFillDataService.getTasksByReRunBatchNumber(rerunBatchNumberL);

        // 2, 活着的worker节点：TbClockworkNode List, 然后根据NodeGroupId分组
        List<TbClockworkNode> allEnableNode = nodeService.getAllEnableNodeByRole(NodeType.WORKER.getValue());
        Map<Integer, List<TbClockworkNode>> nodeGroupByNodeGroupId = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allEnableNode)) {
            nodeGroupByNodeGroupId = allEnableNode.stream().collect(Collectors.groupingBy(TbClockworkNode::getNodeGroupId));
        }

        // 遍历任务
        Integer needTobeKilledAndRemoveTaskId;
        int runningTaskCount = 0;
        for (TbClockworkTask4PagePojo task : tasksByReRunBatchNumber) {
            needTobeKilledAndRemoveTaskId = task.getId();
            // 不是Running的状态就有可能存在队列中
            if (!TaskStatusUtil.canBeKilledCurrentTaskStatus(task.getStatus())) {
                // 此时 - 还没有启动进程 有可能存在taskRunService队列中, 需要去所有队列中kill
                int count = 0;
                List<TbClockworkNode> nodes = nodeGroupByNodeGroupId.get(task.getNodeGid());
                if (CollectionUtils.isNotEmpty(nodes)) for (TbClockworkNode tbClockworkNode : allEnableNode) {
                    String url = null;
                    try {
                        url = String.format("http://%s:%s/clockwork/worker/task/stop/removeFillDataTaskFromExecutedQueue",
                                tbClockworkNode.getIp(), tbClockworkNode.getPort());
                        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                        map.add("taskId", String.valueOf(needTobeKilledAndRemoveTaskId));
                        map.add("isAuto", "false");
                        Map<String, Object> result = restTemplateClient.getResult(url, map);
                        if (result.containsKey(Constant.CODE)
                                && result.get(Constant.CODE).equals(Constant.SUCCESS_CODE)
                                && (count = Integer.parseInt(String.valueOf(result.get(Constant.DATA)))) > 0) {
                            LOG.info("[TaskStopService-stopFillDataTaskListAndRemoveFromQueue]" +
                                            "not found taskLog, but found task in needBeExecutedQueue and remove success. " +
                                            "url = {}, " +
                                            "needTobeKilledAndRemoveTaskId = {}, " +
                                            "nodeId = {}, " +
                                            "nodePort = {}",
                                    url, needTobeKilledAndRemoveTaskId, tbClockworkNode.getId(), tbClockworkNode.getPort());
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("[TaskStopService-stopFillDataTaskListAndRemoveFromQueue]" +
                                "removeTaskFromExecutedQueue url error. url = {}, count = {}", url, count);
                    }
                }
                TbClockworkTaskPojo updateTask = new TbClockworkTaskPojo();
                updateTask.setId(needTobeKilledAndRemoveTaskId);
                updateTask.setStatus(TaskStatus.KILLED.getValue());
                taskOperationService.updateTaskInfo(updateTask);

                LOG.info("[TaskStopService-stopFillDataTaskListAndRemoveFromQueue]" +
                                "not found taskLog, and remove in needBeExecutedQueue. " +
                                "set status={}, needTobeKilledAndRemoveTaskId = {}, count = {}",
                        TaskStatus.KILLED.getValue(), needTobeKilledAndRemoveTaskId, count);
                continue;
            }

            runningTaskCount++;
            // b. 存在对应的taskLog 状态为Running 则表示已经运行，需要去对应的节点kill掉
            // 获取该批次该任务最新一次的运行日志
            TbClockworkTaskLogPojo needTobeKilledTaskLogIsRunning =
                    taskLogService.getTaskLogByTaskIdAndRerunBatchNumber(needTobeKilledAndRemoveTaskId, rerunBatchNumberL);
            if (needTobeKilledTaskLogIsRunning == null) {
                continue;
            }

            // 获取进程信息，然后再指定的worker服务器上将进程杀死。
            StopRunningTaskParam stopRunningTaskParam = new StopRunningTaskParam();
            stopRunningTaskParam.setTaskLogPojo(PojoUtil.convert(needTobeKilledTaskLogIsRunning, TbClockworkTaskLogPojo.class));
            stopRunningTaskParam.setKillSceneFlag(KillOperationInitiator.EXTERNAL_CLIENT.getValue());
            stopRunningTask(stopRunningTaskParam);
        }

        // 如果全部存在内存中，还需要修改补数状态
        if (runningTaskCount == 0) {
            // 更新状态End
            TbClockworkTaskFillDataPojo record = new TbClockworkTaskFillDataPojo();
            record.setIsEnd(true);
            record.setEndTime(new Date());
            record.setStatus(TaskStatus.KILLED.getValue());
            record.setRerunBatchNumber(rerunBatchNumberL);
            taskFillDataService.updateTaskFillDataByRerunBatchNumber(record);
            LOG.info("stopTaskListAndRemoveFromQueue runningTaskCount is 0, update fillData status = killed, " +
                    "rerunBatchNumber = {}", rerunBatchNumber);
        }
    }


}
