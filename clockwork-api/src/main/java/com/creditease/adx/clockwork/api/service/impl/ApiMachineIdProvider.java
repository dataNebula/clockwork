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

package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.api.service.INodeService;
import com.robert.vesta.service.impl.provider.DbMachineIdProvider;
import com.robert.vesta.service.impl.provider.MachineIdProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-11-14
 */
@Service(value = "apiMachineIdProvider")
public class ApiMachineIdProvider implements MachineIdProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DbMachineIdProvider.class);

    @Autowired
    private INodeService nodeService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    public long getMachineId() {
        TbClockworkNode tbClockworkNode = nodeService.getNodeByIpAndPort(nodeIp,nodePort);
        if(tbClockworkNode == null){
            throw new RuntimeException("[apiMachineIdProvider]No found node info ,ip = " + nodeIp + ",port = " + nodePort);
        }
        long machineId = tbClockworkNode.getId();
        LOG.info("[apiMachineIdProvider]get machine id = {}, ip = {}, port = {}, role = {}",
        		machineId, nodeIp, nodePort, tbClockworkNode.getRole());
        return machineId;
    }

}
