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

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.client.service.LockRecordClientService;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.common.enums.UniqueValueRecordType;
import com.creditease.adx.clockwork.common.util.DateUtil;

/**
 * 清除lockRecord日志记录（保留最近七天）
 * 清除lockRecord日志记录（保留最近七天）
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:13 2019-09-11
 * @ Description：CleanLog Monitor
 * @ Modified By：
 */
@Service
public class CleanLogMonitor {

    private final Logger LOG = LoggerFactory.getLogger(CleanLogMonitor.class);

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    // 保留最近多少天的lockRecord日志
    @Value("${kept.lately.lock.record.logs.day}")
    private int keptLatelyLockRecordLogsDay;

    /**
     * 清除日志
     */
    @Scheduled(cron = "${monitor.clean.log.cron.exp}")
    public void cleanMonitor() {
        try {
            LOG.info("[CleanLogMonitor.cleanMonitor] start... {}", keptLatelyLockRecordLogsDay);
            // 获取执行权限
            boolean toLaunchTaskCurrentTime = lockRecordClientService.getLockAndRecord(
                    UniqueValueRecordType.MASTER_CLEAN_LOG_MONITOR.getValue(), nodeIp, Integer.parseInt(nodePort));

            if (!toLaunchTaskCurrentTime) {
                LOG.info("[CleanLogMonitor][Try get authority]Not got authority to launch task, master node ip = {}, "
                		+ "master node port = {}, toLaunchTaskCurrentTime = {}", nodeIp, nodePort, false);
                LOG.info("[CleanLogMonitor.cleanMonitor] end.");
                return;
            }
            LOG.info("[CleanLogMonitor][Try get authority]Got authority to launch task, master node ip = {}, "
            		+ "master node port = {}, toLaunchTaskCurrentTime = {}", nodeIp, nodePort, true);

            if( keptLatelyLockRecordLogsDay < 1 ){
                keptLatelyLockRecordLogsDay = 7 ;
            }
            // 保留keptLatelyLockRecordLogsDay天的数据，keptLatelyLockRecordLogsDay天之前的数据将删除
            long slotTime = DateUtil.getCurrentTimestamp() - (long)keptLatelyLockRecordLogsDay * 86400000;
            lockRecordClientService.getLockRecordClient().deleteSlotRecentlyBySlotTime(slotTime);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("[CleanLogMonitor.cleanMonitor] end.");
    }

}
