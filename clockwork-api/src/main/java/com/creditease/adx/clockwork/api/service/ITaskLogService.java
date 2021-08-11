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

package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.api.service.base.IBaseRdmsService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskLogMapper;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:03 2019-09-11
 * @ Description：
 * @ Modified By：
 */
public interface ITaskLogService extends
        IBaseRdmsService<TbClockworkTaskLog, TbClockworkTaskLogPojo,
                TbClockworkTaskLogExample, TbClockworkTaskLogMapper> {

    /**
     * 添加到kafka队列
     *
     * @param logPojoList list
     * @return
     */
    boolean addToKafkaQueue(List<TbClockworkTaskLogPojo> logPojoList);

    int addTbClockworkTaskLog(TbClockworkTaskLogPojo TbClockworkTaskLogPojo);

    Map<Integer, Integer> addBatchTaskLog(List<TbClockworkTaskLog> list);

    int updateTbClockworkTaskLog(TbClockworkTaskLogPojo TbClockworkTaskLogPojo);

    int updateTaskLogLogName(int id, String logName);

    int updateTaskLogPid(int id, int pid);

    boolean updateBatchTaskLogStatus(BatchUpdateTaskLogStatusParam batchUpdateTaskLogStatusParam);

    TbClockworkTaskLogPojo getTbClockworkTaskLogById(Integer id);

    List<TbClockworkTaskLogPojo> getTaskLogByTaskStatus(String status);

    List<TbClockworkTaskLog> getTaskLogLogInfo(Integer taskId, Integer taskGroupId, String startDate, String endDate);

    TbClockworkTaskLogPojo getTaskLogByTaskId(Integer taskId);

    List<TbClockworkTaskLogPojo> getFillDataTaskLogByTaskIds(List<Integer> taskIds, long rerunBatchNumber, String fillDataTime);

    TbClockworkTaskLogPojo getTaskLogByTaskIdAndRerunBatchNumber(Integer taskId, Long rerunBatchNumber);

    List<TbClockworkTaskLogPojo> getTaskLogByTaskIdsAndStatusList(BatchGetTaskLogByTaskIdsParam param);

    List<TbClockworkTaskLogPojo> getTaskLogByTaskLogInfo(TaskLogInfo taskLogInfo);

    List<TbClockworkTaskLogPojo> getTaskLogByBatchNum(Integer taskGroupId, Long batchNum);


    /**
     * 获取所有未结束的任务日志信息
     *
     * @return
     */
    List<TbClockworkTaskLog> getAllNotEndTaskLog();

    List<TbClockworkTaskLogPojo> getAllTaskLogByPageParam(TaskLogSearchPageEntity pojo, int pageNumber, int pageSize);

    int getAllTaskLogByPageParamCount(TaskLogSearchPageEntity task);

    /**
     * 获取最新的日志文件参数
     *
     * @param taskId taskId
     * @return
     */
    LogFileParam getLatestTaskLogFileParamByTaskId(Integer taskId);
}
