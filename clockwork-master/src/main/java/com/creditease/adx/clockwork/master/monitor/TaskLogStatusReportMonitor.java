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
import com.creditease.adx.clockwork.client.service.TaskLogClientService;
import com.creditease.adx.clockwork.common.enums.TaskLifeCycleOpType;
import com.creditease.adx.clockwork.common.enums.UniqueValueRecordType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:24 2019-09-04
 * @ Description：定时上报任务日志（进程）状态
 * @ Modified By：
 */
@Service
public class TaskLogStatusReportMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogStatusReportMonitor.class);

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Resource(name = "taskLogClientService")
    private TaskLogClientService taskLogClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    @Scheduled(cron = "${monitor.task.status.report.cron.exp}")
    public void taskLogStatusReportMonitor() {
        // 有该服务的control权限才能执行
        boolean hasServiceControlAuthority = lockRecordClientService.getLockAndRecord(
                UniqueValueRecordType.KAFKA_TASK_LOG_SEND.getValue(), nodeIp, Integer.parseInt(nodePort));

        if (!hasServiceControlAuthority) {
            LOG.debug("[TaskLogMonitor-hasServiceControlAuthority]The current thread authority {}.", false);
            return;
        }

        // 获取还没结束的任务周期
        List<TbClockworkTaskLogPojo> logPojoList = taskLogClientService.getAllNotEndTaskLog();
        if (CollectionUtils.isNotEmpty(logPojoList)) {
            LOG.info("[TaskLogMonitor-getAllNotEndTaskLog] logPojoList size {}.", logPojoList.size());
            // 批量发送记录，控制流量
            List<TbClockworkTaskLogPojo> batch = new ArrayList<>();
            for (TbClockworkTaskLogPojo taskLog : logPojoList) {
                taskLog.setOperationType(TaskLifeCycleOpType.TIMER.getValue());
                batch.add(PojoUtil.convert(taskLog, TbClockworkTaskLogPojo.class));
                // 批量发送100条记录
                if (batch.size() > 100) {
                    // 添加到kafka队列
                    taskLogClientService.getTaskLogClient().addToKafkaQueue(batch);
                    batch.clear();
                }
            }
            // 发送剩余记录到kafka
            if (!batch.isEmpty()) {
                // 添加到kafka队列
                taskLogClientService.getTaskLogClient().addToKafkaQueue(batch);
                batch.clear();
            }
        }
    }

}
