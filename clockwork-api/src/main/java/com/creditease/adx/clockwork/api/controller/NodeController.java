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

import io.swagger.annotations.Api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.api.service.INodeService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:43 下午 2019/07/10
 * @ Description：节点服务
 * @ Modified By：
 */
@Api("节点服务相关接口")
@RestController
@RequestMapping("/clockwork/api/node")
public class NodeController {

    private static final Logger LOG = LoggerFactory.getLogger(NodeController.class);

    @Autowired
    private INodeService nodeService;

    /**
     * 节点启动初始化
     *
     * @param ip   ip
     * @param port port
     * @param role 角色
     * @return
     */
    @PostMapping(value = "/nodeStartInit")
    public Map<String, Object> nodeStartInit(@RequestParam(value = "ip") String ip,
                                             @RequestParam(value = "port") String port,
                                             @RequestParam(value = "role") String role) {
        try {
            LOG.info("NodeController-nodeStartInit, ip = {}, port = {}, role = {}", ip, port, role);
            return Response.success(nodeService.nodeStartInit(ip, port, role));
        } catch (Exception e) {
            LOG.error("NodeController-nodeStartInit error! ip = {}, port = {}, role = {}", ip, port, role, e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 当前节点状态
     *
     * @return
     */
    @PostMapping(value = "/status/isAlive")
    public Map<String, Object> isAlive() {
        LOG.info("I am living");
        return Response.success(true);
    }

    @GetMapping(value = "/getAllNode")
    public Map<String, Object> getAllNode() {
        try {
            return Response.success(PojoUtil.convertList(nodeService.getAllNode(), TbClockworkNodePojo.class));
        } catch (Exception e) {
            LOG.error("NodeController-getAllNode Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/getAllEnableNodeByRole")
    public Map<String, Object> getAllEnableNodeByRole(@RequestParam(value = "role") String role) {
        try {
            return Response.success(nodeService.getAllEnableNodeByRole(role));
        } catch (Exception e) {
            LOG.error("NodeController-getAllEnableNodeByRole Error {}", e.getMessage(), e);
            return Response.fail(null, e.getMessage());
        }
    }

    @GetMapping(value = "/getAllEnableNodeByRoleAndGroupId")
    public Map<String, Object> getAllEnableNodeByRoleAndGroupId(@RequestParam(value = "role") String role,
                                                                @RequestParam(value = "nodeGroupId") int nodeGroupId) {
        try {
            return Response.success(nodeService.getAllEnableNodeByRoleAndGroupId(role,nodeGroupId));
        } catch (Exception e) {
            LOG.error("NodeController-getAllEnableNodeByRole Error {}", e.getMessage(), e);
            return Response.fail(null, e.getMessage());
        }
    }

    /**
     * 使node失效
     *
     * @param nodeId node id
     */
    @PostMapping(value = "/disableNode")
    public Map<String, Object> disableNode(@RequestParam(value = "nodeId") Integer nodeId) {
        if (nodeId == null || nodeId < 1) {
            LOG.error("NodeController-disableNode, invalid nodeId");
            return Response.fail("invalid nodeId");
        }
        try {
            return Response.success(nodeService.disableNode(nodeId));
        } catch (Exception e) {
            LOG.error("NodeController-disableNode Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改node
     *
     * @param tbClockworkNodePojo json string
     */
    @PostMapping(value = "/updateNode")
    public Map<String, Object> updateNode(@RequestBody TbClockworkNodePojo tbClockworkNodePojo) {
        try {
            return Response.success(nodeService.updateNode(tbClockworkNodePojo));
        } catch (Exception e) {
            LOG.error("NodeController-updateNode Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
