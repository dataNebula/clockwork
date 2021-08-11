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

package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeExample;
import com.creditease.adx.clockwork.common.enums.NodeStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;
import com.creditease.adx.clockwork.dao.mapper.NodeMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkNodeMapper;
import com.creditease.adx.clockwork.web.service.INodeService;
import com.creditease.adx.clockwork.web.service.ITaskUploadFileService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class NodeService implements INodeService {

    private static final Logger LOG = LoggerFactory.getLogger(NodeService.class);

    @Autowired
    private NodeMapper nodeMapper;

    @Autowired
    private TbClockworkNodeMapper tbClockworkNodeMapper;

    @Resource(name = "taskUploadFileService")
    private ITaskUploadFileService taskUploadFileService;

    @Override
    public TbClockworkNode getNodeById(int id) {
        return tbClockworkNodeMapper.selectByPrimaryKey(id);
    }

    /**
     * 获取节点通过ip和端口
     *
     * @param ip   ip
     * @param port port
     * @return
     */
    @Override
    public TbClockworkNode getNodeByIpAndPort(final String ip, final String port) {
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria().andIpEqualTo(ip).andPortEqualTo(port);

        List<TbClockworkNode> tbClockworkNodes = tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
        if (CollectionUtils.isNotEmpty(tbClockworkNodes)) {
            return tbClockworkNodes.get(0);
        }
        return null;
    }

    @Override
    public List<TbClockworkNode> getAllEnableNodeByRole(String role) {
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria().andRoleEqualTo(role).andStatusEqualTo(NodeStatus.ENABLE.getValue());
        return tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
    }

    @Override
    public List<TbClockworkNode> getAllNode() {
        return tbClockworkNodeMapper.selectByExample(new TbClockworkNodeExample());
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
    public int deleteNode(int id) {
        LOG.info("deleteNode, id = {}", id);
        int count = tbClockworkNodeMapper.deleteByPrimaryKey(id);

        //如果删除成功 则删除关联文件关系
        if (count == 1) {
            taskUploadFileService.deleteNodeRelsByNodeId(id);
        }
        return count;
    }

    public int getAllNodeByPageParamCount(TbClockworkNodePojo node) {
        return nodeMapper.countAllNodeByPageParam(node);
    }

    /**
     * 分页查询
     *
     * @param node       node
     * @param pageNumber number
     * @param pageSize   size
     * @return
     */
    public List<TbClockworkNodePojo> getAllNodeByPageParam(TbClockworkNodePojo node, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("id", node.getId());
        param.put("ip", node.getIp());
        param.put("role", node.getRole());
        param.put("status", node.getStatus());
        param.put("nodeGroupId", node.getNodeGroupId());
        param.put("domainName", node.getDomainName());
        param.put("groupName", node.getGroupName());
        return nodeMapper.selectAllNodeByPageParam(param);
    }

}
