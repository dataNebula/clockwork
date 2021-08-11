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

import com.creditease.adx.clockwork.client.TaskFillDataClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillDataTimeQueue;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:46 下午 2020/4/1
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskFillDataClientService")
public class TaskFillDataClientService {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public TaskFillDataClient getTaskFillDataClient() {
        return taskFillDataClient;
    }

    @Autowired
    private TaskFillDataClient taskFillDataClient;

    public TbClockworkTaskFillData getTaskFillDataByRerunBatchNumber(String rerunBatchNumber) {
        Map<String, Object> interfaceResult = taskFillDataClient.getTaskFillDataByRerunBatchNumber(rerunBatchNumber);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return null;
        }
        if (interfaceResult.get(Constant.DATA) == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA), new TypeReference<TbClockworkTaskFillData>() {
        });
    }

    public boolean updateTaskFillDataSuccessCount(String rerunBatchNumber) {
        Map<String, Object> interfaceResult = taskFillDataClient.updateTaskFillDataSuccessCount(rerunBatchNumber);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        Object data = interfaceResult.get(Constant.DATA);
        if (data == null) {
            return false;
        }
        return Integer.parseInt(String.valueOf(data)) == 1;
    }

    public boolean updateTaskFillDataCurrFillDataTime(String rerunBatchNumber, String CurrFillDataTime, int CurrFillDataTimeSort) {
        Map<String, Object> interfaceResult
                = taskFillDataClient.updateTaskFillDataCurrFillDataTime(rerunBatchNumber, CurrFillDataTime, CurrFillDataTimeSort);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        Object data = interfaceResult.get(Constant.DATA);
        if (data == null) {
            return false;
        }
        return Integer.parseInt(String.valueOf(data)) == 1;
    }

    /**
     * 更新补数状态为运行
     *
     * @param rerunBatchNumber 批次号
     * @return boolean
     */
    public boolean updateTaskFillDataIsRan(String rerunBatchNumber) {
        Map<String, Object> interfaceResult = taskFillDataClient.updateTaskFillDataIsRan(rerunBatchNumber);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        Object data = interfaceResult.get(Constant.DATA);
        if (data == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(data));
    }

    /**
     * 更新补数状态为Killing
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    public boolean updateTaskFillDataIsKilling(String rerunBatchNumber, String status) {
        TbClockworkTaskFillDataPojo record = new TbClockworkTaskFillDataPojo();
        record.setRerunBatchNumber(Long.parseLong(rerunBatchNumber));
        record.setStatus(status);
        record.setIsEnd(false);
        Map<String, Object> interfaceResult = taskFillDataClient.updateTaskFillDataByRerunBatchNumber(record);
        Object data = interfaceResult.get(Constant.DATA);
        return Integer.parseInt(String.valueOf(data)) == 1;
    }

    /**
     * 更新补数状态为结束
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    public boolean updateTaskFillDataIsEnd(String rerunBatchNumber, String status, boolean isStarted) {
        TbClockworkTaskFillDataPojo record = new TbClockworkTaskFillDataPojo();
        record.setRerunBatchNumber(Long.parseLong(rerunBatchNumber));
        record.setStatus(status);
        record.setEndTime(DateUtil.getNowTimeDate());
        record.setIsEnd(true);
        Map<String, Object> interfaceResult = taskFillDataClient.updateTaskFillDataByRerunBatchNumber(record);
        Object data = interfaceResult.get(Constant.DATA);
        return Integer.parseInt(String.valueOf(data)) == 1;
    }

    public boolean insertTaskFillDataTimeQueueBatch(String rerunBatchNumber, String fillDataType, List<String> fillDataTimes) {
        Map<String, Object> interfaceResult
                = taskFillDataClient.addTaskFillDataTimeQueueBatch(rerunBatchNumber, fillDataType, fillDataTimes);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        Object data = interfaceResult.get(Constant.DATA);
        if (data == null) {
            return false;
        }
        return Integer.parseInt(String.valueOf(data)) == fillDataTimes.size();
    }

    public TbClockworkTaskFillDataTimeQueue getNextTaskFillDataTimeQueue(String rerunBatchNumber, String fillDataTime) {
        Map<String, Object> interfaceResult = taskFillDataClient.getNextTaskFillDataTimeQueue(rerunBatchNumber, fillDataTime);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return null;
        }
        if (interfaceResult.get(Constant.DATA) == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA), new TypeReference<TbClockworkTaskFillDataTimeQueue>() {
        });
    }


}


