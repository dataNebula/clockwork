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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.api.service.ITaskFillDataTimeQueueService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillDataTimeQueue;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillDataTimeQueueExample;
import com.creditease.adx.clockwork.dao.mapper.TaskFillDataTimeQueueBatchMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskFillDataTimeQueueMapper;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:14 下午 2020/4/1
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskFillDataTimeQueueService")
public class TaskFillDataTimeQueueService implements ITaskFillDataTimeQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskFillDataTimeQueueService.class);

    @Autowired
    private TbClockworkTaskFillDataTimeQueueMapper tbClockworkTaskFillDataTimeQueueMapper;

    @Autowired
    private TaskFillDataTimeQueueBatchMapper taskFillDataTimeQueueBatchMapper;

    @Override
    public int insertTaskFillDataTimeQueueBatch(
            Long rerunBatchNumber, String fillDataType, List<String> fillDataTimes) {
        if (rerunBatchNumber == null || fillDataType == null || CollectionUtils.isEmpty(fillDataTimes)) {
            LOG.error("[TaskFillDataTimeQueueService-getNextTaskFillDataTimeQueue] parameter is Error. rerunBatchNumber "
            		+ "= {}, fillDataType = {}, fillDataTimes = {}", rerunBatchNumber, fillDataType, fillDataTimes);
            return 0;
        }
        LOG.info("[TaskFillDataTimeQueueService-getNextTaskFillDataTimeQueue] rerunBatchNumber = {}, fillDataType = {}, "
        		+ "fillDataTimes = {}", rerunBatchNumber, fillDataType, fillDataTimes);

        List<TbClockworkTaskFillDataTimeQueue> tbClockworkTaskFillDataTimeQueues = new ArrayList<>();
        TbClockworkTaskFillDataTimeQueue tbClockworkTaskFillDataTimeQueue = null;
        String upperFillDataTime = null;
        Date currentDate = new Date();
        for (int i = 0; i < fillDataTimes.size(); i++) {
            tbClockworkTaskFillDataTimeQueue = new TbClockworkTaskFillDataTimeQueue();
            tbClockworkTaskFillDataTimeQueue.setFillDataType(fillDataType);
            tbClockworkTaskFillDataTimeQueue.setFillDataTime(fillDataTimes.get(i));
            tbClockworkTaskFillDataTimeQueue.setRerunBatchNumber(rerunBatchNumber);
            tbClockworkTaskFillDataTimeQueue.setUpperFillDataTime(upperFillDataTime);
            tbClockworkTaskFillDataTimeQueue.setSort(i + 1);
            tbClockworkTaskFillDataTimeQueue.setUpdateTime(currentDate);
            tbClockworkTaskFillDataTimeQueue.setCreateTime(currentDate);

            // upperFillDataTime
            upperFillDataTime = fillDataTimes.get(i);
            tbClockworkTaskFillDataTimeQueues.add(tbClockworkTaskFillDataTimeQueue);
        }

        return taskFillDataTimeQueueBatchMapper.
        		addBatchTbClockworkTaskFillDataTimeQueue(tbClockworkTaskFillDataTimeQueues);
    }

    @Override
    public TbClockworkTaskFillDataTimeQueue getNextTaskFillDataTimeQueue(Long rerunBatchNumber, String fillDataTime) {
        LOG.info("[TaskFillDataTimeQueueService-getNextTaskFillDataTimeQueue] " +
                "rerunBatchNumber = {}, fillDataTime = {}", rerunBatchNumber, fillDataTime);

        TbClockworkTaskFillDataTimeQueueExample example = new TbClockworkTaskFillDataTimeQueueExample();
        example.createCriteria()
                .andRerunBatchNumberEqualTo(rerunBatchNumber)
                .andUpperFillDataTimeEqualTo(fillDataTime);

        List<TbClockworkTaskFillDataTimeQueue> taskFillDataTimeQueues
                = tbClockworkTaskFillDataTimeQueueMapper.selectByExample(example);
        if (taskFillDataTimeQueues != null && taskFillDataTimeQueues.size() == 1) {
            return taskFillDataTimeQueues.get(0);
        }
        return null;
    }
}
