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

package com.creditease.adx.clockwork.master.config;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.creditease.adx.clockwork.client.IdClient;
import com.creditease.adx.clockwork.client.service.LoopClockClientService;
import com.creditease.adx.clockwork.client.service.NodeClientService;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.master.routine.RoutineTaskSelector;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:27 2019-09-10
 * @ Description：
 * @ Modified By：
 */
@Component
public class InitConfig implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InitConfig.class);

    @Autowired
    protected IdClient idClient;

    @Autowired
    private RoutineTaskSelector routineTaskSelector;

    @Resource(name = "nodeClientService")
    private NodeClientService nodeClientService;

    @Resource(name = "loopClockClientService")
    private LoopClockClientService loopClockClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    @Value("${spring.run.init}")
    protected boolean runInit;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        // 针对有时间触发配置的任务，对其在指定时间将其调度起来，我们称之为例行作业拣选器。
        long startTime = System.currentTimeMillis();
        try {
            LOG.info("[RoutineTaskSelector]setup runInit = {}", runInit);
            if (runInit) {
                boolean buildTaskLoopClockResult = loopClockClientService.buildTaskLoopClock();
                if (!buildTaskLoopClockResult) {
                    LOG.info("[RoutineTaskSelector]build task loop clock information failure,cost time = {} ms",
                            System.currentTimeMillis() - startTime);
                    System.exit(-1);
                    return;
                }else{
                    LOG.info("[RoutineTaskSelector]" +
                                    "build task loop clock information success,init flag = {}, cost time = {} ms",
                            runInit, System.currentTimeMillis() - startTime);
                }
            }else{
                LOG.info("[RoutineTaskSelector]" +
                                "build task loop clock information be skipped,init flag = {}, cost time = {} ms",
                        runInit, System.currentTimeMillis() - startTime);
            }
            routineTaskSelector.setName("adx-routine-task-selector-thread");
            routineTaskSelector.start();
        } catch (Exception e) {
            LOG.error("[RoutineTaskSelector]build task loop clock information exception,cost time = {} ms",
                    System.currentTimeMillis() - startTime);
            LOG.error(e.getMessage(), e);
        }

        Map<String, Object> result = nodeClientService.getNodeClient().nodeStartInit(nodeIp, nodePort,
                NodeType.MASTER.getValue());
        if(result.get(Constant.CODE).equals(Constant.SUCCESS_CODE)){
            LOG.info("Master Node init. {}", result);
        }
    }
}
