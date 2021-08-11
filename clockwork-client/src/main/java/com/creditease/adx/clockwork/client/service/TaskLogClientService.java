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

import com.creditease.adx.clockwork.client.TaskLogClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.BatchGetTaskLogByTaskIdsParam;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskLogStatusParam;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:52 2019-12-04
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskLogClientService")
public class TaskLogClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public TaskLogClient getTaskLogClient() {
        return taskLogClient;
    }

    @Autowired
    protected TaskLogClient taskLogClient;

    @Autowired
    protected TaskLogFlowClientService taskLogFlowClientService;


    public TbClockworkTaskLogPojo getTaskLogById(Integer taskLogId) {
        Map<String, Object> interfaceResult = taskLogClient.getTaskLogById(taskLogId);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) return null;
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<TbClockworkTaskLogPojo>() {
                });
    }

    public List<TbClockworkTaskLogPojo> getFillDataTaskLogByTaskIds(
            List<Integer> taskIds, long rerunBatchNumber, String fillDataTime) {
        Map<String, Object> interfaceResult
                = taskLogClient.getFillDataTaskLogByTaskIds(taskIds, rerunBatchNumber, fillDataTime);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) return null;
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskLogPojo>>() {
                });
    }

    public List<TbClockworkTaskLogPojo> getTaskLogByTaskIdsAndStatusList(
            List<Integer> taskIds, List<String> statusList, Date startTime, Date endTime) {
        BatchGetTaskLogByTaskIdsParam param = new BatchGetTaskLogByTaskIdsParam();
        param.setTaskIds(taskIds);
        param.setStatusList(statusList);
        param.setStartTime(startTime);
        param.setEndTime(endTime);
        Map<String, Object> interfaceResult
                = taskLogClient.getTaskLogByTaskIdsAndStatusList(param);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) return null;
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskLogPojo>>() {
                });
    }

    public TbClockworkTaskLogPojo getTaskLogByTaskId(Integer taskId) {
        Map<String, Object> interfaceResult = taskLogClient.getTaskLogByTaskId(taskId);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) return null;
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<TbClockworkTaskLogPojo>() {
                });
    }

    /**
     * 根据任务的状态获取任务的执行日志信息
     *
     * @param status
     * @return
     */
    public List<TbClockworkTaskLogPojo> getTaskLogByTaskStatus(String status) {
        Map<String, Object> interfaceResult = taskLogClient.getTaskLogByTaskStatus(status);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) return null;
        return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskLogPojo>>() {
        });
    }

    /**
     * 更新作业运行日志记录的状态
     *
     * @param taskLogId
     * @param status
     * @return
     */
    public boolean updateTaskLogStatus(Integer taskLogId, String status) {
        LOG.info("[TaskLogClientService-updateTaskLogStatus]update task logId = {},  status = {} ", taskLogId, status);
        TbClockworkTaskLogPojo updateTaskLog = new TbClockworkTaskLogPojo();
        updateTaskLog.setId(taskLogId);
        updateTaskLog.setStatus(status);
        return HttpUtil.checkInterfaceCodeSuccess(taskLogClient.updateTaskLog(updateTaskLog));

    }

    /**
     * 批量修改状态为MasterHasReceive
     *
     * @param logIds ids
     * @return
     */
    public boolean updateBatchTaskLogMasterHasReceive(List<Integer> logIds, String status) {
        BatchUpdateTaskLogStatusParam
                batchUpdateTaskLogStatusParam = new BatchUpdateTaskLogStatusParam(logIds, status);
        Map<String, Object> interfaceResult = taskLogClient.updateBatchTaskLogStatus(batchUpdateTaskLogStatusParam);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 批量修改状态为WorkerHasReceive
     *
     * @param logIds ids
     * @return
     */
    public boolean updateBatchTaskLogWorkerHasReceive(List<Integer> logIds, String status, Integer nodeId) {
        BatchUpdateTaskLogStatusParam batchUpdateTaskLogStatusParam
                = new BatchUpdateTaskLogStatusParam(logIds, status, nodeId, DateUtil.getNowTimeStampDate());
        Map<String, Object> interfaceResult = taskLogClient.updateBatchTaskLogStatus(batchUpdateTaskLogStatusParam);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 批量修改状态为[End]
     *
     * @param logIds ids
     * @return
     */
    public boolean updateBatchTaskLogEnd(List<Integer> logIds, String status, int returnCode) {
        BatchUpdateTaskLogStatusParam batchUpdateTaskLogStatusParam
                = new BatchUpdateTaskLogStatusParam(logIds, status, returnCode);
        Map<String, Object> interfaceResult = taskLogClient.updateBatchTaskLogStatus(batchUpdateTaskLogStatusParam);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 更新日志状态为Running
     *
     * @param taskLogId logId
     * @param pid       pid
     * @param LogName   logName
     * @return
     */
    public boolean updateTaskLogRunning(Integer taskLogId, Integer pid, String LogName) {
        TbClockworkTaskLogPojo TbClockworkTaskLogPojo = new TbClockworkTaskLogPojo();
        TbClockworkTaskLogPojo.setId(taskLogId);
        TbClockworkTaskLogPojo.setPid(pid);
        TbClockworkTaskLogPojo.setLogName(LogName);
        TbClockworkTaskLogPojo.setStatus(TaskStatus.RUNNING.getValue());
        TbClockworkTaskLogPojo.setRunningTime(DateUtil.getNowTimeStampDate());
        TbClockworkTaskLogPojo.setIsEnd(false);
        return HttpUtil.checkInterfaceCodeSuccess(taskLogClient.updateTaskLog(TbClockworkTaskLogPojo));
    }


    public boolean updateTaskLogLogName(Integer id, String logName) {
        return HttpUtil.checkInterfaceCodeSuccess(taskLogClient.updateTaskLogLogName(id, logName));
    }

    public boolean updateTaskLogPid(Integer id, Integer pid) {
        return HttpUtil.checkInterfaceCodeSuccess(taskLogClient.updateTaskLogPid(id, pid));
    }

    /**
     * 更新日志状态为Killing
     *
     * @param taskLogId  logId
     * @param status     status
     * @param returnCode returnCode
     * @return
     */
    public boolean updateTaskLogKilling(Integer taskLogId, String status, int returnCode) {
        TbClockworkTaskLogPojo updateTaskLog = new TbClockworkTaskLogPojo();
        updateTaskLog.setId(taskLogId);
        updateTaskLog.setReturnCode(returnCode);
        updateTaskLog.setStatus(status);
        updateTaskLog.setIsEnd(false);
        return HttpUtil.checkInterfaceCodeSuccess(taskLogClient.updateTaskLog(updateTaskLog));
    }

    /**
     * 更新作业运行日志结束时间
     *
     * @param taskLogId  logId
     * @param returnCode returnCode
     * @return
     */
    public boolean updateTaskLogEnd(Integer taskLogId, String status, int returnCode) {
        TbClockworkTaskLogPojo updateTaskLog = new TbClockworkTaskLogPojo();
        updateTaskLog.setId(taskLogId);
        updateTaskLog.setReturnCode(returnCode);
        updateTaskLog.setStatus(status);
        updateTaskLog.setEndTime(DateUtil.getNowTimeStampDate());
        updateTaskLog.setIsEnd(true);
        return HttpUtil.checkInterfaceCodeSuccess(taskLogClient.updateTaskLog(updateTaskLog));
    }

    /**
     * 修改command
     *
     * @param taskLogId 日志id
     * @param command   realCommand
     * @return
     */
    public boolean updateTaskLogRealCommand(Integer taskLogId, String command) {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        TbClockworkTaskLogPojo updateTaskLog = new TbClockworkTaskLogPojo();
        updateTaskLog.setId(taskLogId);
        updateTaskLog.setRealCommand(command);
        return HttpUtil.checkInterfaceCodeSuccess(taskLogClient.updateTaskLog(updateTaskLog));
    }


    /**
     * 批量添加日志信息
     *
     * @param taskList         tasks
     * @param executeType      execute Type
     * @param fillDataTime     fillDataTime
     * @param rerunBatchNumber rerunBatchNumber
     */
    public void addBatchTaskLog(List<TbClockworkTaskPojo> taskList, int executeType, String fillDataTime, Long rerunBatchNumber) {
        String status = TaskStatus.SUBMIT.getValue();
        long start = System.currentTimeMillis();
        Date date = new Date();
        TaskExecuteType taskExecuteType = TaskExecuteType.getEnumByCode(executeType);
        List<TbClockworkTaskLog> taskLogList = new ArrayList<>();
        TbClockworkTaskLog taskLog = null;
        for (TbClockworkTaskPojo task : taskList) {
            taskLog = new TbClockworkTaskLog();
            taskLog.setTaskId(task.getId());
            taskLog.setTaskName(task.getName());
            taskLog.setExecuteType(executeType);
            taskLog.setStartTime(DateUtil.getNowTimeStampDate());
            taskLog.setRealCommand(task.getCommand());
            taskLog.setParameter(task.getParameter());
            taskLog.setStatus(status);
            taskLog.setFillDataTime(fillDataTime);
            taskLog.setRerunBatchNumber(rerunBatchNumber);
            taskLog.setIsEnd(false);
            taskLog.setCreateTime(date);
            taskLog.setUpdateTime(date);
            if (task.getRunEngine() != null) {
                taskLog.setRunEngine(task.getRunEngine());
            }
            if (task.getBatchNumber() != null) {
                taskLog.setBatchNumber(task.getBatchNumber());
            }
            if (task.getGroupId() != null) {
                taskLog.setGroupId(task.getGroupId());
            }
            if (task.getNextTriggerTime() != null) {
                taskLog.setTriggerTime(task.getNextTriggerTime());
            }
            taskLogList.add(taskLog);
        }

        Map<String, Object> interfaceResult = taskLogClient.addBatchTaskLog(taskLogList);

        // 解析id并设置给task
        if (HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            HashMap<Integer, Integer> taskIdAndLogId = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<HashMap<Integer, Integer>>() {
                    });

            for (TbClockworkTaskPojo taskPojo : taskList) {
                taskPojo.setTaskLogId(taskIdAndLogId.get(taskPojo.getId()));
            }
        }
        LOG.info("[TaskLogClientService-addBatchTaskLog] " +
                        "result {}, taskLogList.size = {}, run type = {}, cost time = {} ms.",
                HttpUtil.checkInterfaceDataSuccess(interfaceResult),
                taskLogList.size(), taskExecuteType != null ? taskExecuteType.getName() : null,
                System.currentTimeMillis() - start);

    }

    /**
     * 获取未结束的生命周期记录
     */
    public List<TbClockworkTaskLogPojo> getAllNotEndTaskLog() {
        Map<String, Object> interfaceResult = taskLogClient.getAllNotEndTaskLog();
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA),
                new TypeReference<List<TbClockworkTaskLogPojo>>() {
                });

    }


}
