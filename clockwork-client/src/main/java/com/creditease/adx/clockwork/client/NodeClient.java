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

package com.creditease.adx.clockwork.client;

import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:46 2019-12-04
 * @ Description：节点相关
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface NodeClient {

    /**
     * 节点启动的时候初始化
     *
     * @param ip   ip
     * @param port port
     * @param role role
     * @return
     */
    @PostMapping(value = "/clockwork/api/node/nodeStartInit")
    Map<String, Object> nodeStartInit(@RequestParam(value = "ip") String ip,
                                      @RequestParam(value = "port") String port,
                                      @RequestParam(value = "role") String role);

    @PostMapping(value = "/clockwork/api/node/updateNode", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> updateNode(@RequestBody TbClockworkNodePojo tbClockworkNodePojo);

    @GetMapping(value = "/clockwork/api/node/getAllNode")
    Map<String, Object> getAllNode();

    @GetMapping(value = "/clockwork/api/node/getAllEnableNodeByRole")
    Map<String, Object> getAllEnableNodeByRole(@RequestParam(value = "role") String role);

    @GetMapping(value = "/clockwork/api/node/getAllEnableNodeByRoleAndGroupId")
    Map<String, Object> getAllEnableNodeByRoleAndGroupId(
            @RequestParam(value = "role") String role,
            @RequestParam(value = "nodeGroupId") int nodeGroupId);


}
