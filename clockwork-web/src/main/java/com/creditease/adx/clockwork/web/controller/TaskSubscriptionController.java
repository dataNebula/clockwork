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

package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskSubscriptionPojo;
import com.creditease.adx.clockwork.web.service.ITaskSubscriptionService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:08 下午 2020/12/8
 * @ Description：
 * @ Modified By：
 */

@RestController
@RequestMapping("/clockwork/web/task/subscription")
public class TaskSubscriptionController {

    private final Logger LOG = LoggerFactory.getLogger(TaskSubscriptionController.class);

    @Autowired
    private ITaskSubscriptionService taskSubscriptionService;


    /**
     * 添加订阅信息
     *
     * @param pojo TbClockworkTaskSubscriptionPojo
     * @return
     */
    @PostMapping(value = "/addTaskSubscription")
    public Map<String, Object> addTaskSubscription(@RequestBody TbClockworkTaskSubscriptionPojo pojo) {
        try {
            if (pojo == null || pojo.getSubscriptionTime() == null || StringUtils.isBlank(pojo.getMobileNumber())
                    || StringUtils.isBlank(pojo.getUserName())) {
                return Response.fail("addTaskSubscription param subscriptionTime|mobileNumber|userName cannot be empty.");
            }
            return Response.success(taskSubscriptionService.addTaskSubscription(pojo));
        } catch (Exception e) {
            LOG.error("[TaskSubscriptionController-addTaskSubscription] Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 获取订阅信息，通过taskId
     *
     * @param taskId taskId
     * @return list
     */
    @GetMapping(value = "/getTaskSubscriptionByTaskId")
    public Map<String, Object> getTaskSubscriptionByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 0) {
                return Response.fail("getTaskSubscriptionByTaskId param taskId cannot be empty.");
            }
            return Response.success(taskSubscriptionService.getTaskSubscriptionByTaskId(taskId));
        } catch (Exception e) {
            LOG.error("[TaskSubscriptionController-getTaskSubscriptionByTaskId] Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 获取订阅信息，通过userName
     *
     * @param userName userName
     * @return list
     */
    @GetMapping(value = "/getTaskSubscriptionByUserName")
    public Map<String, Object> getTaskSubscriptionByUserName(@RequestParam(value = "userName") String userName) {
        try {
            if (StringUtils.isBlank(userName)) {
                return Response.fail("getTaskSubscriptionByUserName param userName cannot be empty.");
            }
            return Response.success(taskSubscriptionService.getTaskSubscriptionByUserName(userName));
        } catch (Exception e) {
            LOG.error("[TaskSubscriptionController-getTaskSubscriptionByUserName] Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
