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
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 处理状态是 failed 的作业
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:58 2019-09-29
 * @ Description：
 * @ Modified By：
 */
@Service
public class TaskFailedMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskFailedMonitor.class);

    @Value("${send.notify.service.email.url}")
    private String sendNotifyServiceEmailUrl;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    @Value("${monitor.task.failed.cron.exp}")
    protected String springCronExp;

    /**
     * 处理失败的作业
     */
    @Scheduled(cron = "${monitor.task.failed.cron.exp}")
    public void failedTaskNotification() {
        try {
            Date currentDate = new Date();          // 当前时间
            // 获取执行权限
            boolean toLaunchTaskCurrentTime = lockRecordClientService.getLockAndRecord(
                    UniqueValueRecordType.MASTER_TASK_FAILED_MONITOR.getValue(), nodeIp, Integer.parseInt(nodePort));

            // 检查是否当前master执行此次下发任务
            if (!toLaunchTaskCurrentTime) {
                LOG.debug("[TaskFailedMonitor-failedTaskNotification]The current thread authority {}.", false);
                return;
            }

            LOG.info("[TaskFailedMonitor-failedTaskNotification] start ...");

            // 获取当前时间，并且去掉秒（最小单位力度这里规定为1分钟）
            String currentDateStr = DateUtil.formatDate(currentDate, DateUtil.DATE_MIN_STR) + ":00";
            currentDate = DateUtil.parse(currentDateStr);

            if (!CronSequenceGenerator.isValidExpression(springCronExp)) {
                LOG.error("[TaskFailedMonitor-failedTaskNotification]The springCronExp is Valid Expression ");
                return;
            }
            CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(springCronExp);
            Date next = cronSequenceGenerator.next(currentDate);
            Date next2 = cronSequenceGenerator.next(next);
            long timeDifference = next2.getTime() - next.getTime();

            long beforeTime = currentDate.getTime() - timeDifference; // cronExp时间差
            Date beforeDate = new Date(beforeTime);
            String beforeDateStr = DateUtil.formatDate(beforeDate, DateUtil.DATE_FULL_STR);


            // 获取状态是 failed 的任务 online任务
            LOG.info("[TaskFailedMonitor-failedTaskNotification]getTaskOnlineByStatus {} - {}.", beforeDateStr, currentDateStr);
            List<String> status = new ArrayList<>(TaskStatusUtil.getFailedTaskStatus().keySet());
            List<TbClockworkTaskPojo> tasks = taskClientService.getTaskByRunFailedStatus(status, beforeDateStr, currentDateStr);

            if (CollectionUtils.isEmpty(tasks)) {
                LOG.info("[TaskFailedMonitor-failedTaskNotification] end. tasks.size = 0, {} - {}.", beforeDateStr, currentDateStr);
                return;
            }
            LOG.info("[TaskFailedMonitor-failedTaskNotification] task fail.size = {}", tasks.size());

            for (TbClockworkTaskPojo taskPojo : tasks) {
                if (!taskPojo.getOnline() || taskPojo.getLastEndTime() == null) {
                    continue;
                }
                String emailList = taskPojo.getEmailList();     // 邮件列表
                String createUser = taskPojo.getCreateUser();   // 所属用户
                if (StringUtils.isBlank(emailList)) {
                    if (StringUtils.isNotBlank(createUser)) {
                        // 添加邮箱后缀
                        int index = createUser.indexOf('@');
                        if (index == -1) emailList = createUser + "@clockwork.com";
                    } else {
                        LOG.info("[TaskFailedMonitor-failedTaskNotification] task failed to run, skip sendMail. " +
                                        "taskId = {}, taskName = {}, status = {}, endTime = {}, createUser = {}, emailList = {}",
                                taskPojo.getId(), taskPojo.getName(), taskPojo.getStatus(), taskPojo.getLastEndTime(), createUser, emailList);
                        continue;
                    }
                }

                // 发送邮件，构建邮件内容
                String content = String.format("[失败任务预警]：taskId：%s, taskName：%s, 状态为：%s, %s结束时间：%s.",
                        taskPojo.getId(),
                        taskPojo.getName(),
                        TaskStatus.getDescByValue(taskPojo.getStatus()),
                        TaskStatus.FATHER_NOT_SUCCESS.getValue().equals(taskPojo.getStatus()) ? "上次" : "",
                        DateUtil.formatDateToString(taskPojo.getLastEndTime(), DateUtil.DATE_FULL_STR)
                );

                LOG.info("[TaskFailedMonitor-failedTaskNotification] task failed to run, sendMail! " +
                                "currentDate = {}, emailList = {}, content = {},  createUser = {}",
                        currentDateStr, emailList, content, createUser);
                Map<String, Object> params = new HashMap<>();
                params.put("emails", emailList);
                params.put("content", content);
                String result = HttpUtil.post(sendNotifyServiceEmailUrl, params);
                LOG.info("[TaskFailedMonitor-failedTaskNotification] taskId = {}, result = {}, ", taskPojo.getId(), result);
            }
            LOG.info("[TaskFailedMonitor-failedTaskNotification] end.");
        } catch (Exception e) {
            LOG.error("[TaskFailedMonitor-failedTaskNotification]", e);
        }
    }

}
