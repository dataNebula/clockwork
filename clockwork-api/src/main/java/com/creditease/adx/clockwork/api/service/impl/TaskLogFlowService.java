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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.api.kafka.KafkaTaskLogFlowSender;
import com.creditease.adx.clockwork.api.service.ITaskLogFlowService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogFlow;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogFlowExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogFlowPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskLogFlowBatchMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskLogFlowMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskLogMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:58 2019-10-14
 * @ Description：
 * @ Modified By：
 */
@Service
public class TaskLogFlowService implements ITaskLogFlowService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogFlowService.class);

    @Autowired
    private KafkaTaskLogFlowSender kafkaTaskLogFlowSender;

    @Autowired
    private TbClockworkTaskLogFlowMapper tbClockworkTaskLogFlowMapper;

    @Autowired
    private TaskLogFlowBatchMapper taskLogFlowBatchMapper;

    @Autowired
    private TbClockworkTaskLogMapper TbClockworkTaskLogMapper;

    @Value("${spring.kafka.task.record.lifecycle.enable}")
    private Boolean TASK_RECORD_LIFECYCLE_ENABLE;

    /**
     * 简单的添加到队列
     *
     * @param logFlowPojoList list
     * @return
     */
    @Override
    public boolean addToKafkaQueue(List<TbClockworkTaskLogFlowPojo> logFlowPojoList) {
        if (CollectionUtils.isEmpty(logFlowPojoList)) {
            return true;
        }
        // 添加到队列
        if (this.TASK_RECORD_LIFECYCLE_ENABLE != null && this.TASK_RECORD_LIFECYCLE_ENABLE) {
            kafkaTaskLogFlowSender.getKafkaTaskLogFlowQueue().addAll(logFlowPojoList);
        }
        return true;
    }


    /**
     * 添加生命周期记录到队列
     *
     * @param logFlowPojo record
     * @return
     */
    @Override
    public boolean addTaskLogFlow(TbClockworkTaskLogFlowPojo logFlowPojo) {
        if (logFlowPojo == null) {
            return true;
        }
        // 入库
        tbClockworkTaskLogFlowMapper.insert(PojoUtil.convert(logFlowPojo, TbClockworkTaskLogFlow.class));
        if (this.TASK_RECORD_LIFECYCLE_ENABLE != null && this.TASK_RECORD_LIFECYCLE_ENABLE) {
            // 添加到队列
            kafkaTaskLogFlowSender.getKafkaTaskLogFlowQueue().add(logFlowPojo);
        }
        return true;
    }

    /**
     * 批量添加记录
     *
     * @param logFlowPojoList record
     * @return
     */
    @Override
    public boolean addBatchTaskLogFlow(List<TbClockworkTaskLogFlowPojo> logFlowPojoList) {
        if (CollectionUtils.isEmpty(logFlowPojoList)) {
            return true;
        }
        // 入库
        taskLogFlowBatchMapper.addBatchTaskLogFlow(
                PojoUtil.convertList(logFlowPojoList, TbClockworkTaskLogFlow.class));

        // 添加到队列
        if (this.TASK_RECORD_LIFECYCLE_ENABLE != null && this.TASK_RECORD_LIFECYCLE_ENABLE) {
            kafkaTaskLogFlowSender.getKafkaTaskLogFlowQueue().addAll(logFlowPojoList);
        }
        LOG.info("[TaskLogFlowService-addBatchTaskLogFlow, record size = {}", logFlowPojoList.size());
        return true;
    }

    /**
     * 获取所有未结束的生命周期日志信息
     *
     * @return
     */
    @Override
    public List<TbClockworkTaskLogFlow> getAllNotEndTaskLogFlow() {
        TbClockworkTaskLogFlowExample example = new TbClockworkTaskLogFlowExample();
        example.createCriteria()
                .andIsEndEqualTo(false)
                .andIsLastEqualTo(true)
                .andStartTimeGreaterThan(DateUtil.getYesterday());
        return tbClockworkTaskLogFlowMapper.selectByExample(example);
    }


    /**
     * 通过groupId，获取时间范围内的生命周期信息,瀑布图
     *
     * @param groupId   groupId
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @Override
    public String getTaskLogFlowInfo(Integer groupId, Date startTime, Date endTime) {
        JSONArray taskInfos = new JSONArray();
        if (groupId == null) {
            return null;
        }

        try {
            // 获取任务运行状态
            TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
            example.createCriteria().andGroupIdEqualTo(groupId).andCreateTimeBetween(startTime, endTime);
            List<TbClockworkTaskLog> taskLogs = TbClockworkTaskLogMapper.selectByExample(example);

            // 更具开始时间排序
            try {
                Comparator<TbClockworkTaskLog> comparator = new Comparator<TbClockworkTaskLog>() {
                    @Override
                    public int compare(TbClockworkTaskLog s1, TbClockworkTaskLog s2) {
                        if (s1.getStartTime() == null && s2.getStartTime() == null) {
                            return 0;
                        }
                        if (s1.getStartTime() == null) {
                            return 1;
                        }
                        if (s2.getStartTime() == null) {
                            return -1;
                        }
                        return (int) (s1.getStartTime().getTime() - s2.getStartTime().getTime());
                    }
                };
                Collections.sort(taskLogs, comparator);
            } catch (Exception e) {
                LOG.warn("警告：taskLog排序异常！", e);
            }

            // 数据转换
            HashMap<String, List<TbClockworkTaskLog>> stringListHashMap = new HashMap<>();
            for (TbClockworkTaskLog taskLog : taskLogs) {
                String taskName = taskLog.getTaskName();
                List<TbClockworkTaskLog> processList = stringListHashMap.get(taskName);
                if (processList == null) {
                    processList = new ArrayList<>();
                }
                processList.add(taskLog);
                stringListHashMap.put(taskName, processList);
            }

            JSONObject task = null;
            JSONArray logs = null;
            JSONObject log = null;
            JSONArray flows = null;
            JSONObject flow = null;
            Date nowDate = DateUtil.getNowTimeStampDate();
            for (Map.Entry<String, List<TbClockworkTaskLog>> entry : stringListHashMap.entrySet()) {
                String taskName = entry.getKey();
                List<TbClockworkTaskLog> processList = entry.getValue();
                if (!processList.isEmpty()) {
                    task = new JSONObject();
                    task.put("groupId", processList.get(0).getGroupId());
                    task.put("taskId", processList.get(0).getTaskId());
                    // 正对[taskName]特殊处理
                    int index = taskName.indexOf("[");
                    int lastIndex = taskName.lastIndexOf("]");
                    if (index != -1 && lastIndex != -1 && index < lastIndex && lastIndex < taskName.length()) {
                        taskName = taskName.substring(taskName.indexOf("[") + 1, taskName.lastIndexOf("]"));
                    }
                    task.put("taskName", taskName);

                    logs = new JSONArray();
                    for (TbClockworkTaskLog taskLog : processList) {
                        Integer logId = taskLog.getId();
                        if (logId == null) {
                            continue;
                        }

                        if (taskLog.getExecuteTime() == null) {
                            taskLog.setExecuteTime(nowDate);
                        }

                        if (taskLog.getEndTime() == null) {
                            taskLog.setEndTime(nowDate);
                        }

                        log = new JSONObject();
                        log.put("logId", logId);
                        log.put("status", taskLog.getStatus());
                        log.put("returnCode", taskLog.getReturnCode());
                        log.put("startTime", taskLog.getStartTime());
                        log.put("executeTime", taskLog.getExecuteTime());
                        log.put("runningTime", taskLog.getRunningTime());
                        log.put("endTime", taskLog.getEndTime());
                        log.put("logName", taskLog.getLogName());
                        log.put("isEnd", taskLog.getIsEnd());

                        // 获取记录 - cycles
                        TbClockworkTaskLogFlowExample recordExample = new TbClockworkTaskLogFlowExample();
                        recordExample.createCriteria().andLogIdEqualTo(logId);
                        recordExample.setOrderByClause("id asc");

                        List<TbClockworkTaskLogFlow> records = tbClockworkTaskLogFlowMapper.selectByExample(recordExample);
                        flows = new JSONArray();
                        for (int i = 0; i < records.size(); i++) {
                            TbClockworkTaskLogFlow record = records.get(i);
                            flow = new JSONObject();
                            flow.put("id", record.getId());
                            flow.put("startTime", record.getStartTime());
                            if (record.getEndTime() == null) {
                                if (i + 1 < records.size()) {
                                    record.setEndTime(records.get(i + 1).getStartTime());
                                } else {
                                    record.setEndTime(record.getStartTime());
                                }
                            }
                            flow.put("status", record.getStatus());
                            flow.put("endTime", record.getEndTime());
                            flows.add(flow);
                        }
                        log.put("flows", flows);
                        logs.add(log);
                    }
                    task.put("logs", logs);
                }
                taskInfos.add(task);
            }
        } catch (Exception e) {
            LOG.error("获取监控信息异常：", e);
            return null;
        }
        return JSONArray.toJSONString(taskInfos);
    }

    /**
     * 通过groupId，获取时间范围内的生命周期信息，列表
     *
     * @param groupId   groupId
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @Override
    public String getTaskLogFlowList(Integer groupId, Date startTime, Date endTime) {
        JSONArray taskInfos = new JSONArray();
        if (groupId == null) {
            return null;
        }

        try {
            // 获取任务运行状态
            TbClockworkTaskLogExample example = new TbClockworkTaskLogExample();
            example.createCriteria().andGroupIdEqualTo(groupId).andCreateTimeBetween(startTime, endTime);
            List<TbClockworkTaskLog> taskLogs = TbClockworkTaskLogMapper.selectByExample(example);

            // 数据转换
            HashMap<String, List<TbClockworkTaskLog>> stringListHashMap = new HashMap<>();
            for (TbClockworkTaskLog taskLog : taskLogs) {
                String taskName = taskLog.getTaskName();
                List<TbClockworkTaskLog> processList = stringListHashMap.get(taskName);
                if (processList == null) {
                    processList = new ArrayList<>();
                }
                processList.add(taskLog);
                stringListHashMap.put(taskName, processList);
            }

            JSONObject task = null;
            JSONObject process = null;
            JSONArray processes = null;
            for (Map.Entry<String, List<TbClockworkTaskLog>> entry : stringListHashMap.entrySet()) {
                String taskName = entry.getKey();
                List<TbClockworkTaskLog> processList = entry.getValue();
                if (!processList.isEmpty()) {
                    task = new JSONObject();
                    task.put("groupId", processList.get(0).getGroupId());
                    task.put("taskId", processList.get(0).getTaskId());
                    // 针对[taskName]特殊处理
                    int index = taskName.indexOf("[");
                    int lastIndex = taskName.lastIndexOf("]");
                    if (index != -1 && lastIndex != -1 && index < lastIndex && lastIndex < taskName.length()) {
                        taskName = taskName.substring(taskName.indexOf("[") + 1, taskName.lastIndexOf("]"));
                    }
                    task.put("taskName", taskName);

                    processes = new JSONArray();
                    for (TbClockworkTaskLog taskLog : processList) {
                        Integer logId = taskLog.getId();
                        if (logId == null) {
                            continue;
                        }
                        process = new JSONObject();
                        process.put("logId", logId);
                        process.put("status", taskLog.getStatus());
                        process.put("returnCode", taskLog.getReturnCode());
                        process.put("startTime", DateUtil.dateToString(taskLog.getStartTime()));
                        process.put("executeTime", DateUtil.dateToString(taskLog.getExecuteTime()));
                        process.put("runningTime", taskLog.getRunningTime());
                        process.put("endTime", DateUtil.dateToString(taskLog.getEndTime()));
                        process.put("logName", taskLog.getLogName());
                        process.put("isEnd", taskLog.getIsEnd());
                        processes.add(process);
                    }
                    task.put("logs", processes);
                }
                taskInfos.add(task);
            }
        } catch (Exception e) {
            LOG.error("获取监控信息异常：", e);
            return null;
        }
        return JSONArray.toJSONString(taskInfos);
    }

}
