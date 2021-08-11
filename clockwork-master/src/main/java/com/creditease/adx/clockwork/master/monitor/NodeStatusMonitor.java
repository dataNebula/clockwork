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

import java.util.List;

import javax.annotation.Resource;

import com.creditease.adx.clockwork.client.service.LockRecordClientService;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.common.enums.UniqueValueRecordType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.client.service.NodeClientService;
import com.creditease.adx.clockwork.common.enums.NodeStatus;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;

/**
 * 节点状态监控，发现异常自动下线，节点恢复自动上线
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:58 2019-09-29
 * @ Description：
 * @ Modified By：
 */
@Service
public class NodeStatusMonitor {

    private final Logger LOG = LoggerFactory.getLogger(NodeStatusMonitor.class);

    @Resource(name = "nodeClientService")
    private NodeClientService nodeClientService;

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    /**
     * 监控节点状态
     */
    @Scheduled(cron = "${monitor.node.status.cron.exp}")
    public void nodeStatus() {
        try {
            // 获取执行权限
            boolean toLaunchTaskCurrentTime = lockRecordClientService.getLockAndRecord(
                    UniqueValueRecordType.MASTER_NODE_MONITOR.getValue(), nodeIp, Integer.parseInt(nodePort));

            // 检查是否当前master执行此次下发任务
            if (!toLaunchTaskCurrentTime) {
                return;
            }

            LOG.info("[NodeStatusMonitor.NodeStatusMonitor] start...");
            List<TbClockworkNodePojo> tbClockworkNodes = nodeClientService.getAllNode();
            if(CollectionUtils.isNotEmpty(tbClockworkNodes))for (TbClockworkNodePojo tbClockworkNode: tbClockworkNodes){
                String ip = tbClockworkNode.getIp();
                String port = tbClockworkNode.getPort();
                String role = tbClockworkNode.getRole();
                String status = tbClockworkNode.getStatus();
                try {
                    if (nodeClientService.checkServerNodeStatus(ip, port, role)) {
                        LOG.info("[NodeStatusMonitor]task node alive! ip = {}, port = {}, role = {}, status = {}",
                                ip, port, role, status);

                        // 节点存活但是状态不是ENABLE修改状态为ENABLE
                        if (!TaskStatus.ENABLE.getValue().equals(status)) {
                            tbClockworkNode.setStatus(TaskStatus.ENABLE.getValue());
                            nodeClientService.updateNode(tbClockworkNode);
                        }
                    }
                } catch (Exception e) {
                    // 如果node节点服务挂了，这里应该会出现连接超时异常
                    tbClockworkNode.setStatus(NodeStatus.DISABLE.getValue());
                    nodeClientService.updateNode(tbClockworkNode);
                    LOG.error("[NodeStatusMonitor]task node not alive! exception is {}, node ip = {}, port = {}, "
                    		+ "role = {}", e, ip, port, role);
                }
            }
            LOG.info("[NodeStatusMonitor.NodeStatusMonitor] end.");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
