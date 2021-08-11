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

package com.creditease.adx.clockwork.api.controller;

import io.swagger.annotations.Api;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.api.service.ITaskFillDataTimeQueueService;
import com.creditease.adx.clockwork.common.entity.Response;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:07 2020-04-01
 * @ Description：补数时间队列相关逻辑
 * @ Modified By：
 */
@Api("补数时间队列相关逻辑")
@RestController
@RequestMapping("/clockwork/api/task/fillData/timeQueue")
public class TaskFillDataTimeQueueController {

    private final Logger LOG = LoggerFactory.getLogger(TaskFillDataTimeQueueController.class);

    @Autowired
    private ITaskFillDataTimeQueueService taskFillDataTimeQueueService;

    /**
     * 批量添加
     *
     * @param rerunBatchNumber 批次号
     * @param fillDataType     补数时间类型
     * @param fillDataTimes    补数时间
     * @return
     */
    @PostMapping(value = "/addTaskFillDataTimeQueueBatch")
    public Map<String, Object> addTaskFillDataTimeQueueBatch(
            @RequestParam(value = "rerunBatchNumber") String rerunBatchNumber,
            @RequestParam(value = "fillDataType") String fillDataType,
            @RequestParam(value = "fillDataTimes") List<String> fillDataTimes) {
        try {
            if (rerunBatchNumber == null || fillDataType == null || CollectionUtils.isEmpty(fillDataTimes)) {
                throw new RuntimeException("parameter is Error. rerunBatchNumber or fillDataType or fillDataTimes is null");
            }
            int count = taskFillDataTimeQueueService.
                    insertTaskFillDataTimeQueueBatch(Long.valueOf(rerunBatchNumber), fillDataType, fillDataTimes);
            LOG.info("[TaskFillDataTimeQueueController-addTaskFillDataTimeQueueBatch] rerunBatchNumber = {}, count = {}",
                    rerunBatchNumber, count);
            return Response.success(count);
        } catch (Exception e) {
            LOG.error("[TaskFillDataTimeQueueController-addTaskFillDataTimeQueueBatch]Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取下一个补数时间周期
     *
     * @param rerunBatchNumber 批次号
     * @param fillDataTime     补数时间
     * @return
     */
    @GetMapping(value = "/getNextTaskFillDataTimeQueue")
    public Map<String, Object> getNextTaskFillDataTimeQueue(@RequestParam("rerunBatchNumber") String rerunBatchNumber,
                                                            @RequestParam("fillDataTime") String fillDataTime) {
        try {
            if (rerunBatchNumber == null || fillDataTime == null) {
                throw new RuntimeException("parameter rerunBatchNumber or fillDataTime is null.");
            }
            LOG.info("[TaskFillDataTimeQueueController-getNextTaskFillDataTimeQueue]param, " +
                    "rerunBatchNumber = {}, fillDataTime = {}", rerunBatchNumber, fillDataTime);

            return Response.success(taskFillDataTimeQueueService.getNextTaskFillDataTimeQueue(Long.valueOf(rerunBatchNumber), fillDataTime));
        } catch (Exception e) {
            LOG.error("[TaskFillDataTimeQueueController-getNextTaskFillDataTimeQueue]Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
