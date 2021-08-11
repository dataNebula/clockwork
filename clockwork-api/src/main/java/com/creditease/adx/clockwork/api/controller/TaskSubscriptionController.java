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

import com.creditease.adx.clockwork.api.service.ITaskSubscriptionService;
import com.creditease.adx.clockwork.common.entity.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:08 下午 2020/12/8
 * @ Description：
 * @ Modified By：
 */

@RestController
@RequestMapping("/clockwork/api/task/subscription")
public class TaskSubscriptionController {

    private final Logger LOG = LoggerFactory.getLogger(TaskSubscriptionController.class);

    @Autowired
    private ITaskSubscriptionService taskSubscriptionService;


    /**
     * 获取当前时间的订阅信息
     *
     * @param subscriptionTime string date
     * @return
     */
    @GetMapping(value = "/getTaskSubscriptionBySubscriptionTime")
    public Map<String, Object> getTaskSubscriptionBySubscriptionTime(@RequestParam(value = "subscriptionTime") String subscriptionTime) {
        try {
            if (subscriptionTime == null) {
                return Response.fail("subscriptionTime is null.");
            }
            return Response.success(taskSubscriptionService.getTaskSubscriptionBySubscriptionTime(subscriptionTime));
        } catch (Exception e) {
            LOG.error("[TaskSubscriptionController-getTaskSubscriptionBySubscriptionTime] Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }

    }

}
