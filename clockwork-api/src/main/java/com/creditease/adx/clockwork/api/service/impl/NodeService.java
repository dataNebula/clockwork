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

import com.creditease.adx.clockwork.api.service.INodeService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeExample;
import com.creditease.adx.clockwork.common.enums.NodeStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkNodeMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service(value = "nodeService")
public class NodeService implements INodeService {

    private static final Logger LOG = LoggerFactory.getLogger(NodeService.class);

    @Autowired
    private TbClockworkNodeMapper tbClockworkNodeMapper;

    /**
     * start init node
     *
     * @param ip   ip
     * @param port port
     * @param role role
     * @return
     */
    @Override
    public boolean nodeStartInit(final String ip, final String port, final String role) {
        TbClockworkNode node = getNodeByIpAndPort(ip, port);
        // Node does not exist. add TbClockworkNodePojo.
        if (node == null || !role.equals(node.getRole())) {
            TbClockworkNodePojo tbClockworkNodePojo = new TbClockworkNodePojo();
            tbClockworkNodePojo.setIp(ip);
            tbClockworkNodePojo.setPort(port);
            tbClockworkNodePojo.setDomainName(ip);
            tbClockworkNodePojo.setRole(role);
            tbClockworkNodePojo.setStatus(NodeStatus.ENABLE.getValue());
            tbClockworkNodePojo.setCreateTime(new Date());
            addNode(tbClockworkNodePojo);
            LOG.info("Node does not exist. add {}", tbClockworkNodePojo);
            return true;
        }

        // Nodes are disabled. update status to enable.
        if (NodeStatus.DISABLE.getValue().equals(node.getStatus())) {
            node.setStatus(NodeStatus.ENABLE.getValue());
            updateNode(PojoUtil.convert(node, TbClockworkNodePojo.class));
        }
        return true;
    }

    @Override
    public TbClockworkNode getNodeById(int id) {
        return tbClockworkNodeMapper.selectByPrimaryKey(id);
    }

    /**
     * 获取节点通过ip和端口
     *
     * @param ip
     * @param port
     * @return
     */
    @Override
    public TbClockworkNode getNodeByIpAndPort(final String ip, final String port) {
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria().andIpEqualTo(ip).andPortEqualTo(port);

        List<TbClockworkNode> tbClockworkNodes = tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
        TbClockworkNode tbClockworkNode = null;
        if (CollectionUtils.isNotEmpty(tbClockworkNodes)) {
            tbClockworkNode = tbClockworkNodes.get(0);
        }
        return tbClockworkNode;
    }

    @Override
    public List<TbClockworkNode> getAllEnableNodeByRole(String role) {
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria().andRoleEqualTo(role).andStatusEqualTo(NodeStatus.ENABLE.getValue());
        return tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
    }

    @Override
    public List<TbClockworkNode> getAllEnableNodeByRoleAndGroupId(String role, int nodeGroupId) {
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria()
                .andRoleEqualTo(role)
                .andNodeGroupIdEqualTo(nodeGroupId)
                .andStatusEqualTo(NodeStatus.ENABLE.getValue());
        return tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
    }

    @Override
    public List<TbClockworkNode> getAllNode() {
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        return tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
    }

    @Override
    public int addNode(TbClockworkNodePojo tbClockworkNodePojo) {
        tbClockworkNodePojo.setCreateTime(new Date());
        tbClockworkNodePojo.setStatus(NodeStatus.ENABLE.getValue());
        return tbClockworkNodeMapper.insertSelective(tbClockworkNodePojo);
    }

    @Override
    public int updateNode(TbClockworkNodePojo tbClockworkNodePojo) {
        return tbClockworkNodeMapper.updateByPrimaryKeySelective(tbClockworkNodePojo);
    }

    @Override
    public int disableNode(int id) {
        TbClockworkNodePojo tbClockworkNodePojo = new TbClockworkNodePojo();
        tbClockworkNodePojo.setStatus(NodeStatus.DISABLE.getValue());

        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria().andIdEqualTo(id);
        return tbClockworkNodeMapper.updateByExampleSelective(tbClockworkNodePojo, tbClockworkNodeExample);
    }

}
