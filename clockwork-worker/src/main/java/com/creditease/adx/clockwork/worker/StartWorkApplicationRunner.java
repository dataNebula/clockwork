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

package com.creditease.adx.clockwork.worker;

import com.creditease.adx.clockwork.client.service.NodeClientService;
import com.creditease.adx.clockwork.client.service.UploadFileClientService;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.worker.service.TaskScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author shs
 * @date 2019-09-12
 * <p>
 * 服务worker启动后，初始化数据检测
 */
@Component
public class StartWorkApplicationRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerApplication.class);

    @Resource(name = "nodeClientService")
    protected NodeClientService nodeClientService;

    @Resource(name = "uploadFileClientService")
    protected UploadFileClientService uploadFileClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    @Value("${node.synchronize.files}")
    protected Boolean synchronizeFiles;

    @Autowired
    private TaskScriptService taskScriptService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        // 服务启动检查是否再节点列表，不在插入一条
        Map<String, Object> initResultMap = nodeClientService.
                getNodeClient().nodeStartInit(nodeIp, nodePort, NodeType.WORKER.getValue());
        if (!initResultMap.get(Constant.CODE).equals(Constant.SUCCESS_CODE)) {
            LOG.info("[StartWorkApplicationRunner] nodeStartInit failure {}:{}", nodeIp, nodePort);
            return;
        }
        LOG.info("[StartWorkApplicationRunner] nodeStartInit success {}:{}", nodeIp, nodePort);

        // 查看配置，是否同步文件
        if (synchronizeFiles) {
            LOG.info("Nodes need to synchronize files!");
            // 检查添加和脚本文件和节点对应关系
            Map<String, Object> resultMap = uploadFileClientService.getUploadFileClient().addUploadFiles2NodeRels(nodeIp, nodePort);
            if (!resultMap.get(Constant.CODE).equals(Constant.SUCCESS_CODE)) {
                LOG.info("[StartWorkApplicationRunner] [addUploadFiles2NodeRels] failure {}:{}", nodeIp, nodePort);
                return;
            }

            LOG.info("[StartWorkApplicationRunner] addUploadFiles2NodeRels success {}:{}", nodeIp, nodePort);

            // 同步文件到该节点服务器
            if (taskScriptService.downloadAndSyncScriptFile(nodeIp, nodePort)) {
                LOG.info("[StartWorkApplicationRunner] downloadAndSyncScriptFile success");
            } else {
                LOG.info("[StartWorkApplicationRunner] downloadAndSyncScriptFile  failure");
            }
        } else {
            LOG.info("Nodes do not need to synchronize files!");
        }
    }
}
