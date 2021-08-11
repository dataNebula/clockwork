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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeGroup;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeGroupExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodeGroupPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.NodeGroupMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkNodeGroupMapper;
import com.creditease.adx.clockwork.web.service.INodeGroupService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class NodeGroupService implements INodeGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(NodeGroupService.class);

    @Autowired
    private NodeGroupMapper nodeGroupMapper;

    @Autowired
    private TbClockworkNodeGroupMapper tbClockworkNodeGroupMapper;

    @Override
    public List<TbClockworkNodeGroup> getAllNodeGroup() {
        return tbClockworkNodeGroupMapper.selectByExample(new TbClockworkNodeGroupExample());
    }

    /**
     * 通过name查询节点组信息
     *
     * @param name name
     * @return
     */
    @Override
    public TbClockworkNodeGroupPojo getNodeGroupByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        TbClockworkNodeGroupExample example = new TbClockworkNodeGroupExample();
        example.createCriteria().andNameEqualTo(name);
        // 查询
        List<TbClockworkNodeGroup> tbClockworkNodeGroups = tbClockworkNodeGroupMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkNodeGroups)) {
            return PojoUtil.convert(tbClockworkNodeGroups.get(0), TbClockworkNodeGroupPojo.class);
        }
        return null;
    }

    /**
     * 添加
     *
     * @param tbClockworkNodePojo pojo
     * @return
     */
    @Override
    public int addNodeGroup(TbClockworkNodeGroupPojo tbClockworkNodePojo) {
        tbClockworkNodePojo.setCreateTime(new Date());
        return tbClockworkNodeGroupMapper.insertSelective(tbClockworkNodePojo);
    }

    /**
     * 修改
     *
     * @param tbClockworkNodePojo pojo
     * @return
     */
    @Override
    public int updateNodeGroup(TbClockworkNodeGroupPojo tbClockworkNodePojo) {
        return tbClockworkNodeGroupMapper.updateByPrimaryKeySelective(tbClockworkNodePojo);
    }

    /**
     * 删除
     *
     * @param id id
     * @return
     */
    @Override
    public int deleteNodeGroup(int id) {
        LOG.info("NodeGroupService-deleteNodeGroup, delete id = {}", id);
        return tbClockworkNodeGroupMapper.deleteByPrimaryKey(id);
    }


    /**
     * 分页查询
     *
     * @param node       node
     * @param pageNumber number
     * @param pageSize   size
     * @return
     */
    @Override
    public List<TbClockworkNodeGroupPojo> getAllNodeGroupByPageParam(TbClockworkNodeGroupPojo node, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("id", node.getId());
        return nodeGroupMapper.selectAllNodeGroupByPageParam(param);
    }

    @Override
    public int getAllNodeGroupByPageParamCount(TbClockworkNodeGroupPojo node) {
        return nodeGroupMapper.countAllNodeGroupByPageParam(node);
    }
}
