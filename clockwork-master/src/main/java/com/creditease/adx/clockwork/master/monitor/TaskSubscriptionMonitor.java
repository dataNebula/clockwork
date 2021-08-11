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

package com.creditease.adx.clockwork.master.monitor;

import com.creditease.adx.clockwork.client.service.LockRecordClientService;
import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.client.service.TaskSubscriptionClientService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskSubscription;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.enums.UniqueValueRecordType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:24 2019-09-04
 * @ Description：定时
 * @ Modified By：
 */
@Service
public class TaskSubscriptionMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSubscriptionMonitor.class);

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskSubscriptionClientService")
    private TaskSubscriptionClientService taskSubscriptionClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    @Value("${send.notify.service.phone.url}")
    private String sendNotifyServicePhoneUrl;

    @Scheduled(cron = "0 */1 * * * ?")
    public void taskSubscriptionMonitor() {

        Date currentDate = new Date();          // 当前时间

        // 有该服务的control权限才能执行
        boolean hasServiceControlAuthority = lockRecordClientService.getLockAndRecord(
                UniqueValueRecordType.MASTER_TASK_SUBSCRIPTION_MONITOR.getValue(), nodeIp, Integer.parseInt(nodePort));

        if (!hasServiceControlAuthority) {
            LOG.debug("[TaskSubscriptionMonitor-hasServiceControlAuthority]The current thread authority {}.", false);
            return;
        }

        // 获取当前时间的订阅信息
        String subscriptionTimeStr = DateUtil.formatDateToString(currentDate, DateUtil.DATE_TIME_M_STR) + ":00";

        List<TbClockworkTaskSubscription> subscription = taskSubscriptionClientService.getTaskSubscriptionBySubscriptionTime(subscriptionTimeStr);
        if (CollectionUtils.isEmpty(subscription)) {
            LOG.info("[TaskSubscriptionMonitor-getTaskSubscriptionBySubscriptionTime] skip currentDate = {}, " +
                    "subscriptionTime = {}, subscription size 0.", subscriptionTimeStr, currentDate);
            return;
        }
        LOG.info("[TaskSubscriptionMonitor-getTaskSubscriptionBySubscriptionTime] currentDate = {}, subscriptionTime = {}, " +
                "subscription size {}.", currentDate, subscriptionTimeStr, subscription.size());

        // taskId, List<TbClockworkTaskSubscription>
        Map<Integer, List<TbClockworkTaskSubscription>> collect
                = subscription.stream().collect(Collectors.groupingBy(TbClockworkTaskSubscription::getTaskId));

        // 获取信息
        List<Integer> taskIds = new ArrayList<Integer>(collect.keySet());
        List<TbClockworkTaskPojo> taskList = taskClientService.getTaskByTaskIds(taskIds);
        if (CollectionUtils.isEmpty(taskList)) {
            LOG.info("[TaskSubscriptionMonitor-getTaskSubscriptionBySubscriptionTime] skip currentDate = {}, taskList is null, " +
                    "subscriptionTime = {}, taskList size 0.", subscriptionTimeStr, currentDate);
            return;
        }
        Map<Integer, TbClockworkTaskPojo> maps = taskList.stream().collect(Collectors.toMap(TbClockworkTaskPojo::getId, Function.identity(), (key1, key2) -> key2));

        for (Map.Entry<Integer, List<TbClockworkTaskSubscription>> entry : collect.entrySet()) {

            Integer taskId = entry.getKey();                            // 任务ID（该任务被一个人或则多个人订阅）
            List<TbClockworkTaskSubscription> list = entry.getValue();  // 订阅信息
            StringBuilder sbMobileNumber = new StringBuilder();
            if (taskId != null) for (TbClockworkTaskSubscription taskSubscription : list) {
                if (StringUtils.isNotBlank(taskSubscription.getMobileNumber())) {
                    sbMobileNumber.append(",").append(taskSubscription.getMobileNumber());
                }
            }
            String phones = sbMobileNumber.length() > 0 ? sbMobileNumber.substring(1) : null;
            if (StringUtils.isBlank(phones)) {
                continue;
            }
            TbClockworkTaskPojo task = maps.get(taskId);
            String content = String.format("[任务状态订阅]：taskId：%s, taskName：%s, 在您订阅的时间：%s时，状态为：%s.",
                    task.getId(),
                    task.getName(),
                    DateUtil.formatDateToString(currentDate, DateUtil.DATE_FULL_STR),
                    TaskStatus.getDescByValue(task.getStatus())
            );

            Map<String, Object> params = new HashMap<>();
            params.put("phones", phones);
            params.put("content", content);
            String result = HttpUtil.post(sendNotifyServicePhoneUrl, params);
            LOG.info("[TaskSubscriptionMonitor-getTaskSubscriptionBySubscriptionTime] currentDate = {}, phones = {}, content = {} result = {}.",
                    currentDate, phones, content, result);
        }

    }
}
