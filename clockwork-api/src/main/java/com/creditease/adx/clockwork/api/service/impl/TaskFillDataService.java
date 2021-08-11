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

import com.creditease.adx.clockwork.api.service.ITaskFillDataService;
import com.creditease.adx.clockwork.api.service.base.impl.AbstractBaseRdmsService;
import com.creditease.adx.clockwork.client.service.TaskFillDataClientService;
import com.creditease.adx.clockwork.common.entity.TaskFillDataEntity;
import com.creditease.adx.clockwork.common.entity.TaskFillDataSearchPageEntity;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillDataExample;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskFillDataMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskFillDataMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:34 2019-12-26
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskFillDataService")
public class TaskFillDataService extends AbstractBaseRdmsService<TbClockworkTaskFillData, TbClockworkTaskFillDataPojo,
        TbClockworkTaskFillDataExample, TbClockworkTaskFillDataMapper> implements ITaskFillDataService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskFillDataService.class);

    @Autowired
    private TaskFillDataMapper taskFillDataMapper;

    @Autowired
    private TbClockworkTaskFillDataMapper tbClockworkTaskFillDataMapper;

    @Resource(name = "taskFillDataClientService")
    private TaskFillDataClientService taskFillDataClientService;

    @Override
    public TbClockworkTaskFillDataMapper getMapper() {
        return this.tbClockworkTaskFillDataMapper;
    }

    public TbClockworkTaskFillData addTbClockworkTaskFillDataRecord(TaskFillDataEntity entity, long rerunBatchNumber) {
        List<Integer> taskIds = entity.getTaskIds();
        List<String> fillDataTimes = entity.getFillDataTimes();
        String operatorName = entity.getOperatorName();

        /*
         * 数据处理
         * 当前补数时间 - 为补数时间的第一个
         * 当前补数时间序号 - 为1
         */
        String currFillDataTime = fillDataTimes.get(0);
        int currFillDataTimeSort = 1;
        String taskIdsStr = StringUtils.join(taskIds, ",");
        String fillDataTimeStr = StringUtils.join(fillDataTimes, ",");
        LOG.info("[TaskSubmitService][addTbClockworkTaskFillDataRecord] fill data info. " +
                        "rerunBatchNumber = {}, taskIdsStr = {}, datesStr = {} , currFillDataTime = {}",
                rerunBatchNumber, taskIdsStr, fillDataTimeStr, currFillDataTime);

        // 补数记录信息入库
        TbClockworkTaskFillData record = new TbClockworkTaskFillData();
        Date date = new Date();
        record.setTaskIds(taskIdsStr);
        record.setTaskGroupAliasName(entity.getTaskGroupAliasName());
        record.setTaskCount(taskIds.size());
        record.setTaskCountSuccess(0);
        record.setFillDataType(entity.getFillDataType());
        record.setFillDataTime(fillDataTimeStr);
        record.setFillDataTimeCount(fillDataTimes.size());
        record.setStatus(TaskStatus.SUBMIT.getValue());
        record.setCurrFillDataTime(currFillDataTime);
        record.setCurrFillDataTimeSort(currFillDataTimeSort);
        record.setOperatorName(operatorName);
        record.setRerunBatchNumber(rerunBatchNumber);
        record.setStartTime(date);
        record.setIsEnd(false);
        record.setDescription(entity.getDescription());
        record.setCreateTime(date);
        record.setUpdateTime(date);
        int count = tbClockworkTaskFillDataMapper.insert(record);
        if (count < 1) {
            throw new RuntimeException("TaskFillData tbClockworkTaskFillDataMapper insert error.");
        }

        // 补数时间队列入库
        boolean timeQueueBatch = taskFillDataClientService.
                insertTaskFillDataTimeQueueBatch(String.valueOf(rerunBatchNumber), entity.getFillDataType(), fillDataTimes);
        if (!timeQueueBatch) {
            throw new RuntimeException("TaskFillData insertTaskFillDataTimeQueueBatch insert error.");
        }
        return record;
    }

    /**
     * 补数列表信息
     *
     * @param dataSearchPageEntity entity
     * @param pageNumber           pageNumber
     * @param pageSize             pageSize
     * @return
     */
    @Override
    public List<TbClockworkTaskFillDataPojo> getAllTaskFillDataByPageParam(
            TaskFillDataSearchPageEntity dataSearchPageEntity, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("taskIds", dataSearchPageEntity.getTaskIds());
        param.put("taskGroupId", dataSearchPageEntity.getTaskGroupId());
        param.put("taskGroupAliasName", dataSearchPageEntity.getTaskGroupAliasName());
        param.put("fillDataType", dataSearchPageEntity.getFillDataType());
        param.put("fillDataTime", dataSearchPageEntity.getFillDataTime());
        param.put("operatorName", dataSearchPageEntity.getOperatorName());
        param.put("externalId", dataSearchPageEntity.getExternalId());
        param.put("source", dataSearchPageEntity.getSource());
        param.put("rerunBatchNumber", dataSearchPageEntity.getRerunBatchNumber());
        param.put("createTimeStart", DateUtil.dateToString(dataSearchPageEntity.getCreateTimeStart()));
        param.put("createTimeEnd", DateUtil.dateToString(dataSearchPageEntity.getCreateTimeEnd()));
        param.put("description", dataSearchPageEntity.getDescription());
        param.put("createUser", dataSearchPageEntity.getCreateUser());
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(dataSearchPageEntity.getRoleName()));

        return taskFillDataMapper.selectAllTaskFillDataByPageParam(param);
    }

    @Override
    public int getAllTaskFillDataByPageParamCount(TaskFillDataSearchPageEntity dataSearchPageEntity) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("taskIds", dataSearchPageEntity.getTaskIds());
        param.put("taskGroupId", dataSearchPageEntity.getTaskGroupId());
        param.put("taskGroupAliasName", dataSearchPageEntity.getTaskGroupAliasName());
        param.put("fillDataType", dataSearchPageEntity.getFillDataType());
        param.put("fillDataTime", dataSearchPageEntity.getFillDataTime());
        param.put("operatorName", dataSearchPageEntity.getOperatorName());
        param.put("rerunBatchNumber", dataSearchPageEntity.getRerunBatchNumber());
        param.put("externalId", dataSearchPageEntity.getExternalId());
        param.put("source", dataSearchPageEntity.getSource());
        param.put("createTimeStart", DateUtil.dateToString(dataSearchPageEntity.getCreateTimeStart()));
        param.put("createTimeEnd", DateUtil.dateToString(dataSearchPageEntity.getCreateTimeEnd()));
        param.put("description", dataSearchPageEntity.getDescription());
        param.put("createUser", dataSearchPageEntity.getCreateUser());
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(dataSearchPageEntity.getRoleName()));
        return taskFillDataMapper.countAllTaskFillDataByPageParam(param);
    }

    @Override
    public List<TbClockworkTask4PagePojo> getTasksByReRunBatchNumber(Long rerunBatchNumber) {
        return taskFillDataMapper.selectTasksByReRunBatchNumber(rerunBatchNumber);
    }

    @Override
    public List<TbClockworkTaskLogPojo> getTaskLogsByReRunBNAndTaskId(Long rerunBatchNumber, Integer taskId) {
        HashMap<String, Object> param = new HashMap<>();
        param.put("rerunBatchNumber", rerunBatchNumber);
        param.put("taskId", taskId);
        return taskFillDataMapper.selectTaskLogsByReRunBNAndTaskId(param);
    }

    @Override
    public TbClockworkTaskFillData getTaskFillDataByRerunBatchNumber(Long rerunBatchNumber) {

        TbClockworkTaskFillDataExample example = new TbClockworkTaskFillDataExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber);
        List<TbClockworkTaskFillData> taskFillData = tbClockworkTaskFillDataMapper.selectByExample(example);
        if (taskFillData != null && taskFillData.size() == 1) {
            return taskFillData.get(0);
        }
        LOG.error("[TaskFillDataService-getTaskFillDataByRerunBatchNumber] get task fill data size != 1. " +
                "rerunBatchNumber = {}", rerunBatchNumber);
        return null;
    }

    @Override
    public int updateTaskFillDataByRerunBatchNumber(TbClockworkTaskFillDataPojo record) {
        // 参数校验
        if (record == null || record.getRerunBatchNumber() == null) {
            LOG.error("[TaskFillDataService-updateTaskFillDataByRerunBatchNumber] update fill data info Error, " +
                    " parameter is null. ");
            return 0;
        }
        Long rerunBatchNumber = record.getRerunBatchNumber();
        LOG.info("[TaskFillDataService-updateTaskFillDataByRerunBatchNumber] update fill data info. " +
                        "rerunBatchNumber = {}, status = {}",
                rerunBatchNumber, record.getStatus());

        // 构建条件，更新数据
        TbClockworkTaskFillDataExample example = new TbClockworkTaskFillDataExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber);
        record.setRerunBatchNumber(null);
        return tbClockworkTaskFillDataMapper.updateByExampleSelective(record, example);
    }

    @Override
    public boolean updateTaskFillDataIsRan(Long rerunBatchNumber) {
        // 参数校验
        if (rerunBatchNumber == null) {
            LOG.error("[TaskFillDataService-updateTaskFillDataRanByRerunBatchNumber] update fill data info Error, " +
                    " parameter rerunBatchNumber is null. ");
            return false;
        }
        LOG.info("[TaskFillDataService-updateTaskFillDataRanByRerunBatchNumber] update fill data info. " +
                "rerunBatchNumber = {}, status = running", rerunBatchNumber);

        // 构建条件，更新数据(设置条件即每批次只更新一个)
        TbClockworkTaskFillDataExample example = new TbClockworkTaskFillDataExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber)
                .andIsEndEqualTo(false)
                .andStatusEqualTo(TaskStatus.SUBMIT.getValue());

        // 需要更新的内容
        TbClockworkTaskFillDataPojo record = new TbClockworkTaskFillDataPojo();
        record.setExecuteTime(new Date());
        record.setStatus(TaskStatus.RUNNING.getValue());
        tbClockworkTaskFillDataMapper.updateByExampleSelective(record, example);
        return true;
    }

    @Override
    public int updateTaskFillDataSuccessCount(Long rerunBatchNumber) {
        return taskFillDataMapper.updateTaskFillDataSuccessCount(rerunBatchNumber);
    }

    @Override
    public int updateTaskFillDataCurrFillDataTime(Long rerunBatchNumber, String CurrFillDataTime, int CurrFillDataTimeSort) {
        LOG.info("[TaskFillDataService-updateTaskFillDataCurrFillDataTime] update fill data curr fill data time. " +
                        "rerunBatchNumber = {}, CurrFillDataTime = {}, CurrFillDataTimeSort = {}",
                rerunBatchNumber, CurrFillDataTime, CurrFillDataTimeSort);
        TbClockworkTaskFillData record = new TbClockworkTaskFillData();
        record.setCurrFillDataTimeSort(CurrFillDataTimeSort);
        record.setCurrFillDataTime(CurrFillDataTime);

        TbClockworkTaskFillDataExample example = new TbClockworkTaskFillDataExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber);
        return tbClockworkTaskFillDataMapper.updateByExampleSelective(record, example);
    }

}
