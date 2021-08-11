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

package com.creditease.adx.clockwork.client;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogFlowPojo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:25 2019-12-04
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface TaskLogFlowClient {

    /**
     * 添加到kafka队列
     *
     * @param logFlowPojoList record
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/flow/addToKafkaQueue")
    Map<String, Object> addToKafkaQueue(@RequestBody List<TbClockworkTaskLogFlowPojo> logFlowPojoList);

    /**
     * 添加task生命周期记录
     *
     * @param logFlowPojo record
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/flow/addTaskLogFlow")
    Map<String, Object> addTaskLogFlow(@RequestBody TbClockworkTaskLogFlowPojo logFlowPojo);

    /**
     * 批量添加task生命周期记录
     *
     * @param logFlowPojoList record
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/flow/addBatchTaskLogFlow")
    Map<String, Object> addBatchTaskLogFlow(@RequestBody List<TbClockworkTaskLogFlowPojo> logFlowPojoList);

    /**
     * 获取未结束的生命周期记录
     */
    @GetMapping(value = "/clockwork/api/task/log/flow/getAllNotEndTaskLogFlow")
    Map<String, Object> getAllNotEndTaskLogFlow();


}
