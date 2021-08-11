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

import com.creditease.adx.clockwork.api.kafka.KafkaTaskLogSender;
import com.creditease.adx.clockwork.api.service.INodeService;
import com.creditease.adx.clockwork.api.service.ITaskLogService;
import com.creditease.adx.clockwork.api.service.base.impl.AbstractBaseRdmsService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogExample;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskLogMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskLogMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:13 2019-09-11
 * @ Description
 * @ Modified By：
 */
@Service(value = "taskLogService")
public class TaskLogService extends AbstractBaseRdmsService<TbClockworkTaskLog, TbClockworkTaskLogPojo,
        TbClockworkTaskLogExample, TbClockworkTaskLogMapper> implements ITaskLogService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogService.class);

    @Autowired
    private KafkaTaskLogSender kafkaTaskLogSender;

    @Autowired
    private TbClockworkTaskLogMapper tbClockworkTaskLogMapper;

    @Autowired
    private INodeService nodeService;

    @Autowired
    private TaskLogMapper taskLogMapper;

    @Value("${spring.kafka.task.record.log.enable}")
    private Boolean TASK_RECORD_LOG_ENABLE;

    @Override
    public TbClockworkTaskLogMapper getMapper() {
        return this.tbClockworkTaskLogMapper;
    }

    /**
     * 简单的添加到队列
     *
     * @param logPojoList list
     * @return
     */
    @Override
    public boolean addToKafkaQueue(List<TbClockworkTaskLogPojo> logPojoList) {
        if (CollectionUtils.isEmpty(logPojoList)) {
            return true;
        }
        // 添加到队列
        if (this.TASK_RECORD_LOG_ENABLE != null && this.TASK_RECORD_LOG_ENABLE) {
            kafkaTaskLogSender.getKafkaTaskLogQueue().addAll(logPojoList);
        }
        return true;
    }

    /**
     * 添加任务运行日志信息
     *
     * @param taskLogPojo record
     * @return
     */
    @Override
    public int addTbClockworkTaskLog(TbClockworkTaskLogPojo taskLogPojo) {
        long start = System.currentTimeMillis();
        long phaseStart = System.currentTimeMillis();

        TbClockworkTaskLog taskLog = PojoUtil.convert(taskLogPojo, TbClockworkTaskLog.class);
        Date nowTimeStampDate = DateUtil.getNowTimeStampDate();
        taskLog.setStartTime(nowTimeStampDate);
        taskLog.setCreateTime(nowTimeStampDate);
        taskLog.setIsEnd(false);
        tbClockworkTaskLogMapper.insertSelective(taskLog);
        LOG.info("[TaskLogService-addTbClockworkTaskLog-db-finished]taskId = {}, logId = {}, cost time = {}",
                taskLog.getTaskId(), taskLog.getId(), System.currentTimeMillis() - phaseStart);

        // 发送到Kafka
        if (this.TASK_RECORD_LOG_ENABLE != null && this.TASK_RECORD_LOG_ENABLE) {
            phaseStart = System.currentTimeMillis();
            try {
                // 加入准备往kafka发消息的队列
                kafkaTaskLogSender.getKafkaTaskLogQueue().add(taskLogPojo);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            LOG.info("[TaskLogService-addTbClockworkTaskLog-kafka-finished] taskId = {}, logId = {},cost time = {}",
                    taskLog.getTaskId(), taskLog.getId(), System.currentTimeMillis() - phaseStart);
        }

        LOG.info("[TaskLogService]add task process finished, taskId = {}, logId = {}, cost time = {}",
                taskLog.getTaskId(), taskLog.getId(), System.currentTimeMillis() - start);
        return taskLog.getId();
    }

    /**
     * 批量添加任务运行日志信息
     *
     * @param taskLogList list
     * @return
     */
    @Override
    public Map<Integer, Integer> addBatchTaskLog(List<TbClockworkTaskLog> taskLogList) {
        long start = System.currentTimeMillis();
        tbClockworkTaskLogMapper.batchInsert(taskLogList);

        LOG.info("[TaskLogService-addTbClockworkTaskLog-db-finished]taskLogList.size = {}, cost time = {}",
                taskLogList.size(), System.currentTimeMillis() - start);

        // 发送到Kafka
        if (this.TASK_RECORD_LOG_ENABLE != null && this.TASK_RECORD_LOG_ENABLE) {
            start = System.currentTimeMillis();
            try {
                // 加入准备往kafka发消息的队列
                kafkaTaskLogSender.getKafkaTaskLogQueue().addAll(PojoUtil.convertList(taskLogList, TbClockworkTaskLogPojo.class));
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            LOG.info("[TaskLogService-addTbClockworkTaskLog-kafka-finished] taskLogList.size = {}, cost time = {}",
                    taskLogList.size(), System.currentTimeMillis() - start);
        }
        HashMap<Integer, Integer> result = new HashMap<>();
        for (TbClockworkTaskLog taskLog : taskLogList) {
            result.put(taskLog.getTaskId(), taskLog.getId());
        }
        return result;
    }

    @Override
    public int updateTbClockworkTaskLog(TbClockworkTaskLogPojo TbClockworkTaskLogPojo) {
        int count = tbClockworkTaskLogMapper.updateByPrimaryKeySelective(
                PojoUtil.convert(TbClockworkTaskLogPojo, TbClockworkTaskLog.class));

        // 发送到Kafka
        if (TbClockworkTaskLogPojo.getStatus() != null
                && this.TASK_RECORD_LOG_ENABLE != null && this.TASK_RECORD_LOG_ENABLE) {

            TbClockworkTaskLogPojo taskLogPojo = getTbClockworkTaskLogById(TbClockworkTaskLogPojo.getId());
            LOG.info("[TaskLogService]add add taskLog info to kafka queue!");

            try {
                kafkaTaskLogSender.getKafkaTaskLogQueue().add(taskLogPojo);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        LOG.info("[TaskLogService]update task process finished,taskLog = {}", TbClockworkTaskLogPojo.toString());
        return count;
    }

    /**
     * 更新logName
     *
     * @param id      PrimaryKey
     * @param logName logName
     * @return
     */
    @Override
    public int updateTaskLogLogName(int id, String logName) {
        TbClockworkTaskLog record = new TbClockworkTaskLog();
        record.setId(id);
        record.setLogName(logName);
        return tbClockworkTaskLogMapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 更新pid
     *
     * @param id  PrimaryKey
     * @param pid pid
     * @return
     */
    @Override
    public int updateTaskLogPid(int id, int pid) {
        TbClockworkTaskLog record = new TbClockworkTaskLog();
        record.setId(id);
        record.setPid(pid);
        return tbClockworkTaskLogMapper.updateByPrimaryKeySelective(record);
    }


    /**
     * 批量更新任务状态【submit】
     *
     * @param batchUpdateTaskLogStatusParam param
     * @return
     */
    @Override
    public boolean updateBatchTaskLogStatus(BatchUpdateTaskLogStatusParam batchUpdateTaskLogStatusParam) {

        // 批量修改各种状态
        String status = batchUpdateTaskLogStatusParam.getStatus();
        List<Integer> logIds = batchUpdateTaskLogStatusParam.getLogIds();
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria().andIdIn(logIds);

        TbClockworkTaskLog taskLog = new TbClockworkTaskLog();
        taskLog.setStatus(status);
        if (TaskStatus.WORKER_HAS_RECEIVE.getValue().equals(status)) {
            taskLog.setNodeId(batchUpdateTaskLogStatusParam.getNodeId());
            taskLog.setExecuteTime(batchUpdateTaskLogStatusParam.getExecuteTime());
        } else if (TaskStatusUtil.isFinishedTaskStatus(status)) {
            taskLog.setReturnCode(batchUpdateTaskLogStatusParam.getReturnCode());
            taskLog.setEndTime(new Date());
            taskLog.setIsEnd(true);
        }
        tbClockworkTaskLogMapper.updateByExampleSelective(taskLog, example);
        return true;
    }


    /**
     * 根据日志ID获取日志记录信息
     *
     * @return
     */
    @Override
    public TbClockworkTaskLogPojo getTbClockworkTaskLogById(Integer id) {
        if (id == null || id < 1) {
            throw new RuntimeException("param id is null!,please check it.");
        }
        TbClockworkTaskLog TbClockworkTaskLog = tbClockworkTaskLogMapper.selectByPrimaryKey(id);
        if (TbClockworkTaskLog == null) {
            throw new RuntimeException("TaskLog info is null,id = " + id);
        }
        return PojoUtil.convert(TbClockworkTaskLog, TbClockworkTaskLogPojo.class);
    }

    /**
     * 获得指定任务，指定时间段的任务详细信息，
     *
     * @param taskId
     * @param taskGroupId
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<TbClockworkTaskLog> getTaskLogLogInfo(Integer taskId, Integer taskGroupId, String startDate, String endDate) {
        if (taskId < 1) {
            throw new RuntimeException("taskId is illeagle,please check it.");
        }
        if (taskGroupId < 1) {
            throw new RuntimeException("taskGroupId is illeagle,please check it.");
        }
        if (StringUtils.isEmpty(startDate)) {
            throw new RuntimeException("startDate is null,please check it.");
        }
        if (StringUtils.isEmpty(endDate)) {
            throw new RuntimeException("endDate is null,please check it.");
        }
        TbClockworkTaskLogExample TbClockworkTaskLogExample = new TbClockworkTaskLogExample();
        TbClockworkTaskLogExample.createCriteria()
                .andTaskIdEqualTo(taskId)
                .andGroupIdEqualTo(taskGroupId)
                .andStartTimeGreaterThanOrEqualTo(DateUtil.parse(startDate, DateUtil.DATE_FULL_STR))
                .andStartTimeLessThanOrEqualTo(DateUtil.parse(endDate, DateUtil.DATE_FULL_STR));
        return tbClockworkTaskLogMapper.selectByExample(TbClockworkTaskLogExample);
    }

    /**
     * 根据任务的状态获取任务的执行日志信息
     *
     * @param status
     * @return
     */
    @Override
    public List<TbClockworkTaskLogPojo> getTaskLogByTaskStatus(String status) {
        if (StringUtils.isEmpty(status)) {
            throw new RuntimeException("param status is null!,please check it.");
        }

        TbClockworkTaskLogExample TbClockworkTaskLogExample = new TbClockworkTaskLogExample();
        TbClockworkTaskLogExample.createCriteria().andStatusEqualTo(status);

        List<TbClockworkTaskLog> TbClockworkTaskLogList = tbClockworkTaskLogMapper
                .selectByExample(TbClockworkTaskLogExample);

        if (CollectionUtils.isEmpty(TbClockworkTaskLogList)) {
            return null;
        }
        return PojoUtil.convertList(TbClockworkTaskLogList, TbClockworkTaskLogPojo.class);
    }

    @Override
    public TbClockworkTaskLogPojo getTaskLogByTaskId(Integer taskId) {
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        example.setOrderByClause("id desc");
        example.setLimitEnd(1);

        List<TbClockworkTaskLog> tbClockworkTaskLogs = tbClockworkTaskLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTaskLogs)) {
            LOG.info("[TaskLogService]getTaskLogByTaskId, taskId = {}, tbClockworkTaskLogs.size = {}", taskId, tbClockworkTaskLogs.size());
            return PojoUtil.convert(tbClockworkTaskLogs.get(0), TbClockworkTaskLogPojo.class);
        }
        LOG.info("[TaskLogService].getTaskLogByTaskId, taskId = {}, tbClockworkTaskLogs.size = {}", taskId, 0);
        return null;
    }

    @Override
    public List<TbClockworkTaskLogPojo> getFillDataTaskLogByTaskIds(List<Integer> taskIds, long rerunBatchNumber, String fillDataTime) {
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber).andFillDataTimeEqualTo(fillDataTime)
                .andTaskIdIn(taskIds);

        List<TbClockworkTaskLog> tbClockworkTaskLogs = tbClockworkTaskLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTaskLogs)) {
            LOG.info("[TaskLogService]getFillDataTaskLogByTaskIds, taskIds = {}, taskLogs.size = {}", taskIds, tbClockworkTaskLogs.size());
            return PojoUtil.convertList(tbClockworkTaskLogs, TbClockworkTaskLogPojo.class);
        }
        LOG.info("[TaskLogService]getFillDataTaskLogByTaskIds, taskIds = {}, taskLogs.size = {}", taskIds, 0);
        return null;
    }

    @Override
    public TbClockworkTaskLogPojo getTaskLogByTaskIdAndRerunBatchNumber(Integer taskId, Long rerunBatchNumber) {
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria().andTaskIdEqualTo(taskId).andRerunBatchNumberEqualTo(rerunBatchNumber);
        example.setOrderByClause("id desc");
        example.setLimitEnd(1);

        List<TbClockworkTaskLog> tbClockworkTaskLogs = tbClockworkTaskLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTaskLogs)) {
            LOG.info("getTaskLogByTaskIdAndRerunBatchNumber taskId = {}, tbClockworkTaskLogs.size = {}",
                    taskId, tbClockworkTaskLogs.size());
            return PojoUtil.convert(tbClockworkTaskLogs.get(0), TbClockworkTaskLogPojo.class);
        }
        LOG.info("getTaskLogByTaskIdAndRerunBatchNumber taskId = {}, tbClockworkTaskLogs.size = {}", taskId, 0);
        return null;
    }

    @Override
    public List<TbClockworkTaskLogPojo> getTaskLogByTaskIdsAndStatusList(BatchGetTaskLogByTaskIdsParam param) {
        List<Integer> taskIds = param.getTaskIds();
        List<String> statusList = param.getStatusList();
        Date startTime = param.getStartTime();
        Date endTime = param.getEndTime();
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria().andTaskIdIn(taskIds).andStatusIn(statusList).andStartTimeBetween(startTime, endTime);

        List<TbClockworkTaskLog> tbClockworkTaskLogs = tbClockworkTaskLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTaskLogs)) {
            LOG.info("getTaskLogByTaskIdsAndStatusList, taskIds = {}, statusList = {}, startTime in {} - {}, taskLogs.size = {}",
                    taskIds, statusList, startTime, endTime, tbClockworkTaskLogs.size());
            return PojoUtil.convertList(tbClockworkTaskLogs, TbClockworkTaskLogPojo.class);
        }
        LOG.info("getTaskLogByTaskIdsAndStatusList, taskIds = {}, statusList = {}, startTime in {} - {}, taskLogs.size = {}",
                taskIds, statusList, startTime, endTime, 0);
        return null;
    }

    @Override
    public List<TbClockworkTaskLogPojo> getTaskLogByTaskLogInfo(TaskLogInfo taskLogInfo) {
        if (taskLogInfo == null) {
            return null;
        }
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria()
                .andGroupIdEqualTo(taskLogInfo.getTaskGroupId())
                .andExecuteTypeEqualTo(TaskExecuteType.ROUTINE.getCode())
                .andStartTimeGreaterThanOrEqualTo(taskLogInfo.getStartTime())
                .andEndTimeLessThanOrEqualTo(taskLogInfo.getEndTime());
        example.setOrderByClause("id");
        List<TbClockworkTaskLog> tbClockworkTaskLogs = tbClockworkTaskLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTaskLogs)) {
            LOG.info("getTaskLogByTaskLogInfo method:::::tbClockworkTaskLogs.size={}", tbClockworkTaskLogs.size());
            return PojoUtil.convertList(tbClockworkTaskLogs, TbClockworkTaskLogPojo.class);
        }
        return null;
    }

    @Override
    public List<TbClockworkTaskLogPojo> getTaskLogByBatchNum(Integer taskGroupId, Long batchNum) {
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria().andGroupIdEqualTo(taskGroupId).andBatchNumberEqualTo(batchNum);
        example.setOrderByClause("id desc");
        List<TbClockworkTaskLog> tbClockworkTaskLogs = tbClockworkTaskLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTaskLogs)) {
            LOG.info("getTaskLogByBatchNum method:::::tbClockworkTaskLogs.size={}", tbClockworkTaskLogs.size());
            return PojoUtil.convertList(tbClockworkTaskLogs, TbClockworkTaskLogPojo.class);
        }
        return null;
    }

    /**
     * 获取所有未结束的生命周期日志信息
     *
     * @return
     */
    @Override
    public List<TbClockworkTaskLog> getAllNotEndTaskLog() {
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria()
                .andIsEndEqualTo(false)
                .andStartTimeGreaterThan(DateUtil.getYesterday());
        return tbClockworkTaskLogMapper.selectByExample(example);
    }

    @Override
    public List<TbClockworkTaskLogPojo> getAllTaskLogByPageParam(
            TaskLogSearchPageEntity taskLogSearchPageEntity, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("id", taskLogSearchPageEntity.getId());
        param.put("taskId", taskLogSearchPageEntity.getTaskId());
        param.put("taskName", taskLogSearchPageEntity.getTaskName());
        param.put("taskGroupName", taskLogSearchPageEntity.getTaskGroupName());
        param.put("taskAliasName", taskLogSearchPageEntity.getTaskAliasName());
        param.put("taskGroupAliasName", taskLogSearchPageEntity.getTaskGroupAliasName());
        param.put("executeType", taskLogSearchPageEntity.getExecuteType());
        param.put("operatorName", taskLogSearchPageEntity.getOperatorName());
        param.put("startTime", DateUtil.dateToString(taskLogSearchPageEntity.getStartTime()));
        param.put("endTime", DateUtil.dateToString(taskLogSearchPageEntity.getEndTime()));
        param.put("status", taskLogSearchPageEntity.getStatus());
        param.put("source", taskLogSearchPageEntity.getSource());
        param.put("isEnd", taskLogSearchPageEntity.getIsEnd());
        param.put("externalId", taskLogSearchPageEntity.getExternalId());
        param.put("rerunBatchNumber", taskLogSearchPageEntity.getRerunBatchNumber());
        param.put("createUser", taskLogSearchPageEntity.getCreateUser());
        param.put("roleName", taskLogSearchPageEntity.getRoleName());
        return taskLogMapper.selectAllTaskLogByPageParam(param);
    }

    @Override
    public int getAllTaskLogByPageParamCount(TaskLogSearchPageEntity taskLogSearchPageEntity) {
        taskLogSearchPageEntity.setRoleName(StringUtil.spiltAndAppendSingleCitation(taskLogSearchPageEntity.getRoleName()));
        HashMap<String, Object> param = new HashMap<>();
        param.put("id", taskLogSearchPageEntity.getId());
        param.put("taskId", taskLogSearchPageEntity.getTaskId());
        param.put("taskName", taskLogSearchPageEntity.getTaskName());
        param.put("taskGroupName", taskLogSearchPageEntity.getTaskGroupName());
        param.put("taskAliasName", taskLogSearchPageEntity.getTaskAliasName());
        param.put("taskGroupAliasName", taskLogSearchPageEntity.getTaskGroupAliasName());
        param.put("executeType", taskLogSearchPageEntity.getExecuteType());
        param.put("operatorName", taskLogSearchPageEntity.getOperatorName());
        param.put("startTime", DateUtil.dateToString(taskLogSearchPageEntity.getStartTime()));
        param.put("endTime", DateUtil.dateToString(taskLogSearchPageEntity.getEndTime()));
        param.put("status", taskLogSearchPageEntity.getStatus());
        param.put("source", taskLogSearchPageEntity.getSource());
        param.put("isEnd", taskLogSearchPageEntity.getIsEnd());
        param.put("externalId", taskLogSearchPageEntity.getExternalId());
        param.put("rerunBatchNumber", taskLogSearchPageEntity.getRerunBatchNumber());
        param.put("createUser", taskLogSearchPageEntity.getCreateUser());
        param.put("roleName", taskLogSearchPageEntity.getRoleName());
        return taskLogMapper.countAllTaskLogByPageParam(param);
    }

    /**
     * 获取最新的日志文件参数
     *
     * @param taskId taskId
     * @return
     */
    @Override
    public LogFileParam getLatestTaskLogFileParamByTaskId(Integer taskId) {
        TbClockworkTaskLogPojo taskLogPojo = getLatestTaskLogByTaskId(taskId);
        if (taskLogPojo == null) {
            LOG.error("TaskLogService-getLatestTaskLogFileParamByTaskId, taskLog does not exist");
            return null;
        }

        if (taskLogPojo.getNodeId() == null || taskLogPojo.getNodeId() < 1) {
            LOG.error("TaskLogService-getLatestTaskLogFileParamByTaskId, taskLog It doesn't exist yet！");
            return null;
        }
        TbClockworkNode tbClockworkNode = nodeService.getNodeById(taskLogPojo.getNodeId());
        LogFileParam logFileParam = new LogFileParam();
        logFileParam.setLogName(taskLogPojo.getLogName());
        logFileParam.setCreateTime(DateUtil.formatDateTime(taskLogPojo.getCreateTime()));
        logFileParam.setNodeIp(tbClockworkNode.getIp());
        logFileParam.setNodePort(tbClockworkNode.getPort());
        LOG.info("TaskLogService-getLatestTaskLogFileParamByTaskId, logFileParam={}", logFileParam);
        return logFileParam;
    }

    /**
     * 获取最新的日志记录
     *
     * @param taskId taskId
     * @return
     */
    private TbClockworkTaskLogPojo getLatestTaskLogByTaskId(Integer taskId) {
        TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        example.setOrderByClause("id desc");
        example.setLimitEnd(1);

        List<TbClockworkTaskLog> tbClockworkTaskLogs = tbClockworkTaskLogMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTaskLogs)) {
            LOG.info("TaskLogService-getLatestTaskLogByTaskId, taskId = {}, tbClockworkTaskLogs.size = {}",
                    taskId, tbClockworkTaskLogs.size());
            return PojoUtil.convert(tbClockworkTaskLogs.get(0), TbClockworkTaskLogPojo.class);
        }
        LOG.info("TaskLogService-getLatestTaskLogByTaskId, taskId = {}, tbClockworkTaskLogs.size = {}", taskId, 0);
        return null;
    }
}
