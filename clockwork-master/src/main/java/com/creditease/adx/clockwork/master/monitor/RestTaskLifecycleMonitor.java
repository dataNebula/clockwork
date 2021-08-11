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
import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.common.enums.TaskSource;
import com.creditease.adx.clockwork.common.enums.UniqueValueRecordType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 重置生命周期状态
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:58 2019-09-29
 * @ Description：
 * @ Modified By：
 */
@Service
public class RestTaskLifecycleMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(RestTaskLifecycleMonitor.class);

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskOperationClientService")
    private TaskOperationClientService taskOperationClientService;

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    /**
     * 重置生命周期状态
     */
    @Scheduled(cron = "${monitor.reset.life.cycle.cron.exp}")
    public void resetLifecycleStatusMonitor() {

        LOG.info("[RestTaskLifecycleMonitor-resetLifecycleStatusMonitor] start ...");

        try {
            // 获取执行权限
            boolean toResetLifeCycleStatus = lockRecordClientService.getLockAndRecord(
                    UniqueValueRecordType.MASTER_RESET_LIFECYCLE.getValue(), nodeIp, Integer.parseInt(nodePort));

            if (!toResetLifeCycleStatus) {
                LOG.debug("[RestTaskLifecycleMonitor-resetLifecycleStatusMonitor]The current thread authority {}.", false);
                return;
            }
        } catch (Exception e){
            LOG.error("[RestTaskLifecycleMonitor-LockRecordClientService.getLockAndRecord] ERROR {}", e.getMessage(), e);
        }

        try {

            // 根据DDS任务DAD_ID重置周期
            List<Integer> dagIds = taskClientService.getTaskDagIdsByCrossSource(TaskSource.DDS_2.getValue());
            if(CollectionUtils.isEmpty(dagIds)){
                return;
            }
            LOG.info("[RestTaskLifecycleMonitor-resetTaskLifecycleStatusByDagIdsNotExists] dagIds.size = {}, {}", dagIds.size(), dagIds);
            boolean result = taskOperationClientService.resetTaskLifecycleStatusByDagIdsNotExists(dagIds);
            if (result) {
                LOG.info("[RestTaskLifecycleMonitor-resetTaskLifecycleStatusByDagIdsNotExists] result = true");
            } else {
                LOG.error("[RestTaskLifecycleMonitor-resetTaskLifecycleStatusByDagIdsNotExists] result = false");
            }

        } catch (Exception e) {
            LOG.error("[RestTaskLifecycleMonitor-resetLifecycleStatusMonitor]", e);
        } finally {
            // 根据source重置周期
            boolean result = taskOperationClientService.resetTaskLifecycleStatusBySource(TaskSource.DDS_2.getValue());
            if (result) {
                LOG.info("[RestTaskLifecycleMonitor-resetTaskLifecycleStatusBySource] result = true");
            } else {
                LOG.error("[RestTaskLifecycleMonitor-resetTaskLifecycleStatusBySource] result = false");
            }
            LOG.info("[RestTaskLifecycleMonitor-resetLifecycleStatusMonitor] end.");
        }
    }

}
