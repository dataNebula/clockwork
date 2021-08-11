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

package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.INodeService;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestNodeController {

    @Resource(name = "nodeService")
    private INodeService nodeService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

   // @Value("${server.port}")
    protected String nodePort="9006";


    @Test
    public void addNode() {

        TbClockworkNodePojo tbClockworkNodePojo = new TbClockworkNodePojo();
        tbClockworkNodePojo.setIp(nodeIp);
        tbClockworkNodePojo.setPort(nodePort);
        tbClockworkNodePojo.setDomainName(nodeIp);
        tbClockworkNodePojo.setRole("worker");
        tbClockworkNodePojo.setStatus("enable");
        tbClockworkNodePojo.setCreateTime(new Date());
        int count =  nodeService.addNode(tbClockworkNodePojo);
        System.out.println(count);

    }


}

