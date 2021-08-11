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

package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.NodeClient;
import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:41 下午 2019/9/10
 * @ Description：
 * @ Modified By：
 */
@Service(value = "nodeClientService")
public class NodeClientService {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Autowired
    private NodeClient nodeClient;

    public NodeClient getNodeClient() {
        return nodeClient;
    }

    public boolean checkServerNodeStatus(final String ip, final String port, final String role) {
        String url = String.format("http://%s:%s/clockwork/%s/node/status/isAlive", ip, port, role);
        // post
        Map<String, Object> interfaceResult
                = restTemplateClient.getResult(url, new LinkedMultiValueMap<String, String>());
        return HttpUtil.checkInterfaceCodeSuccess(interfaceResult);
    }

    public Map<Integer, TbClockworkNode> getNodeMap(List<TbClockworkNode> tbClockworkNodes) {
        Map<Integer, TbClockworkNode> map = new HashMap<>();
        for (TbClockworkNode node : tbClockworkNodes) {
            map.put(node.getId(), node);
        }
        return map;
    }

    /**
     * 根据task状态获取taskList
     *
     * @return nodeList
     */
    public List<TbClockworkNodePojo> getAllNode() {
        Map<String, Object> nodeMap = nodeClient.getAllNode();
        return OBJECT_MAPPER.convertValue(
                nodeMap.get(Constant.DATA), new TypeReference<List<TbClockworkNodePojo>>() {
                });
    }

    public List<TbClockworkNode> getAllEnableNodeByRole(String role) {
        Map<String, Object> interfaceResult = nodeClient.getAllEnableNodeByRole(role);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult) || interfaceResult.get(Constant.DATA) == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkNode>>() {
                });
    }

    public List<TbClockworkNode> getAllEnableNodeByRoleAndGroupId(String role, int nodeGroupId) {
        Map<String, Object> interfaceResult = nodeClient.getAllEnableNodeByRoleAndGroupId(role, nodeGroupId);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult) || interfaceResult.get(Constant.DATA) == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkNode>>() {
                });
    }

    public void updateNode(TbClockworkNodePojo tbClockworkNodePojo) {
        //更新node状态
        nodeClient.updateNode(tbClockworkNodePojo);
    }


}
