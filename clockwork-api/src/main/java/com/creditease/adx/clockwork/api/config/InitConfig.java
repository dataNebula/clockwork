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

package com.creditease.adx.clockwork.api.config;

import com.creditease.adx.clockwork.api.service.impl.NodeService;
import com.creditease.adx.clockwork.common.enums.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:27 2019-09-10
 * @ Description：
 * @ Modified By：
 */
@Component
public class InitConfig implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(InitConfig.class);

    @Autowired
    protected NodeService nodeService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 节点启动初始化
        boolean result = nodeService.nodeStartInit(nodeIp, nodePort, NodeType.API.getValue());
        LOG.info("[InitConfig-afterPropertiesSet] - Node init. {}", result);
    }

}
