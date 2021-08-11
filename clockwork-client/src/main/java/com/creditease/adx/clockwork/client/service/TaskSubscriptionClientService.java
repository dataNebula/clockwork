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

import com.creditease.adx.clockwork.client.TaskSubscriptionClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskSubscription;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:23 下午 2020/12/8
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskSubscriptionClientService")
public class TaskSubscriptionClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSubscriptionClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected TaskSubscriptionClient taskSubscriptionClient;

    public TaskSubscriptionClient getTaskSubscriptionClient() {
        return taskSubscriptionClient;
    }


    /**
     * 获取当前时间的订阅信息
     *
     * @param subscriptionTime date
     * @return
     */
    public List<TbClockworkTaskSubscription> getTaskSubscriptionBySubscriptionTime(String subscriptionTime) {
        try {
            Map<String, Object> interfaceResult = taskSubscriptionClient.getTaskSubscriptionBySubscriptionTime(subscriptionTime);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                LOG.info("[TaskSubscriptionClientService-getTaskSubscriptionBySubscriptionTime] DATA is null");
                return null;
            }
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskSubscription>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }


}
