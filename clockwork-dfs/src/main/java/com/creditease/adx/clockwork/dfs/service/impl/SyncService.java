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

package com.creditease.adx.clockwork.dfs.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.creditease.adx.clockwork.dfs.service.ISyncService;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:24 下午 2020/1/14
 * @ Description：
 * @ Modified By：
 */
@Service
public class SyncService implements ISyncService {

    private static final Logger LOG = LoggerFactory.getLogger(SyncService.class);

    private final static Executor executor = Executors.newCachedThreadPool();

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Autowired
    private EurekaClient eurekaClient;

    @Value("${worker.service.name}")
    private String workerServiceName = null;

    @Override
    public void syncScriptFile(String fileAbsolutePath) {
        List<String> workerIpAndPort = getSlaveServiceIpAndPort();
        if (CollectionUtils.isEmpty(workerIpAndPort)) {
            LOG.info("[RuntimeDirServiceImpl-syncScriptFile]worker ip and port info is null.");
            return;
        }
        long startTime = System.currentTimeMillis();

        for (String ipAndPort : workerIpAndPort) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String workerSyncScriptUrl = String.format("http://%s/clockwork/worker/sync/syncScriptFile", ipAndPort);
                    LOG.info("[RuntimeDirServiceImpl-syncScriptFile]workerSyncScriptUrl = {}", workerSyncScriptUrl);
                    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
                    params.add("fileAbsolutePath", fileAbsolutePath);

                    Map<String, Object> interfaceResult = restTemplateClient.getResult(workerSyncScriptUrl, params);

                    if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                        LOG.info("[RuntimeDirServiceImpl-syncScriptFile]" +
                                        "sync script file failure,fileAbsolutePath = {},workerSyncScriptUrl = {},cost time = {} ms.",
                                fileAbsolutePath, workerSyncScriptUrl, System.currentTimeMillis() - startTime);
                    } else {
                        LOG.info("[RuntimeDirServiceImpl-syncScriptFile]" +
                                        "sync script file success,fileAbsolutePath = {},workerSyncScriptUrl = {},cost time = {} ms.",
                                fileAbsolutePath, workerSyncScriptUrl, System.currentTimeMillis() - startTime);
                    }
                }
            });
        }
    }

    private List<String> getSlaveServiceIpAndPort() {
        List<String> result = new ArrayList<>();
        Applications applications = eurekaClient.getApplications();
        Application applicationEureka = applications.getRegisteredApplications(workerServiceName);
        List<InstanceInfo> instanceInfos = applicationEureka.getInstances();
        for (InstanceInfo instanceInfo : instanceInfos) {
            String ip = instanceInfo.getIPAddr();
            int port = instanceInfo.getPort();
            result.add(ip + ":" + port);
            LOG.info("[RuntimeDirServiceImpl-getSlaveServiceIpAndPort]ip = {},port = {}", ip, port);
        }
        return result;
    }
}
