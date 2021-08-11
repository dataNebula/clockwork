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

package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.TaskLogFlowClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.enums.TaskLifeCycleOpType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogFlowPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:03 2019-12-04
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskLogFlowClientService")
public class TaskLogFlowClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogFlowClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected TaskLogFlowClient taskLogFlowClient;

    public TaskLogFlowClient getTaskLogFlowClient(){
        return taskLogFlowClient;
    }

    /**
     * 添加生命周期
     *
     * @param task   task
     * @param status 状态
     * @param logId  生命周期标识(日志ID)
     * @return
     */
    public boolean addTaskLogFlow(TbClockworkTaskPojo task, String status, Integer logId, Integer nodeId) {
        if (task == null || logId == null || status == null) {
            return false;
        }
        TbClockworkTaskLogFlowPojo cycleRecord = new TbClockworkTaskLogFlowPojo();
        cycleRecord.setStatus(status);
        cycleRecord.setGroupId(task.getGroupId());
        cycleRecord.setTaskId(task.getId());
        cycleRecord.setTaskName(task.getName());
        cycleRecord.setOperationType(TaskLifeCycleOpType.BASE.getValue());
        cycleRecord.setTriggerMode(task.getTriggerMode());
        cycleRecord.setLogId(logId);
        cycleRecord.setNodeId(nodeId);
        cycleRecord.setCreateTime(DateUtil.getNowTimeStampDate());
        cycleRecord.setStartTime(DateUtil.getNowTimeStampDate());
        cycleRecord.setDuration(-1);
        cycleRecord.setIsLast(true);
        cycleRecord.setIsEnd(TaskStatusUtil.isFinishedTaskStatus(status));

        return HttpUtil.checkInterfaceCodeSuccess(taskLogFlowClient.addTaskLogFlow(cycleRecord));
    }

    public boolean addBatchTaskLogFlow(List<TbClockworkTaskPojo> tasks, String status, Integer nodeId) {
        if (tasks == null || tasks.size() == 0 || status == null) {
            return false;
        }

        List<TbClockworkTaskLogFlowPojo> cycleRecordPojos = new ArrayList<>();
        TbClockworkTaskLogFlowPojo cycleRecord;
        for (TbClockworkTaskPojo task : tasks) {
            // 如果此处获得对应任务的日志ID等于空，说明添加日志记录的时候有问题，此条记录跳过，并记录
            if (task.getTaskLogId() == null) {
                LOG.error("[MetaClientService-addBatchTaskLogFlow]" +
                        "task process id is null and skit it,task id = {}", task.getId());
                continue;
            }
            cycleRecord = new TbClockworkTaskLogFlowPojo();
            cycleRecord.setStatus(status);
            cycleRecord.setGroupId(task.getGroupId());
            cycleRecord.setTaskId(task.getId());
            cycleRecord.setTaskName(task.getName());
            cycleRecord.setOperationType(TaskLifeCycleOpType.BASE.getValue());
            cycleRecord.setTriggerMode(task.getTriggerMode());
            cycleRecord.setLogId(task.getTaskLogId());
            if (nodeId != null) cycleRecord.setNodeId(nodeId);
            cycleRecord.setCreateTime(DateUtil.getNowTimeStampDate());
            cycleRecord.setStartTime(DateUtil.getNowTimeStampDate());
            cycleRecord.setDuration(-1);
            cycleRecord.setIsLast(true);
            cycleRecord.setIsEnd(TaskStatusUtil.isFinishedTaskStatus(status));
            cycleRecordPojos.add(cycleRecord);
        }
        LOG.info("[TaskLogClientService-initTaskLogAndTaskLogFlow]add taskLogFlow, " +
                "tasks size = {}", tasks.size()
        );
        return HttpUtil.checkInterfaceCodeSuccess(taskLogFlowClient.addBatchTaskLogFlow(cycleRecordPojos));
    }


    /**
     * 获取未结束的生命周期记录
     */
    public List<TbClockworkTaskLogFlowPojo> getAllNotEndTaskLogFlow() {
        Map<String, Object> interfaceResult = taskLogFlowClient.getAllNotEndTaskLogFlow();
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA),
                new TypeReference<List<TbClockworkTaskLogFlowPojo>>() {
                });

    }


}
