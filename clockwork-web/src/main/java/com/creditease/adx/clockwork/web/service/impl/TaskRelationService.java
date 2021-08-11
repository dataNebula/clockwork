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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelationExample;
import com.creditease.adx.clockwork.common.enums.TaskRelationTakeEffectStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRelationMapper;
import com.creditease.adx.clockwork.web.service.ITaskRelationService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "taskRelationService")
public class TaskRelationService implements ITaskRelationService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRelationService.class);

    @Autowired
    private TbClockworkTaskRelationMapper tbClockworkTaskRelationMapper;

    @Override
    public TbClockworkTaskRelationMapper getMapper() {
        return this.tbClockworkTaskRelationMapper;
    }


    @Override
    public List<TbClockworkTaskRelationPojo> findTaskDirectlyChildren(int taskId) {
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria()
                .andFatherTaskIdEqualTo(taskId).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> children = tbClockworkTaskRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(children)) {
            return PojoUtil.convertList(children, TbClockworkTaskRelationPojo.class);
        }
        return null;
    }

    @Override
    public List<TbClockworkTaskRelationPojo> findTaskDirectlyFather(int taskId) {
        LOG.info("[TaskRelationService-findTaskDirectlyFather] taskId = {}", taskId);
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria()
                .andTaskIdEqualTo(taskId).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> relations = tbClockworkTaskRelationMapper.selectByExample(example);

        // 没有父节点
        if (CollectionUtils.isEmpty(relations)) {
            LOG.info("[TaskRelationService-findTaskDirectlyFather] No fatherTaskIds，fatherTaskIds.size = 0, taskId = {}",
                    taskId);
            return null;
        }

        List<TbClockworkTaskRelationPojo> result = new ArrayList<>();
        for (TbClockworkTaskRelation relation : relations) {
            TbClockworkTaskRelationPojo pojo = new TbClockworkTaskRelationPojo();
            pojo.setTaskId(relation.getFatherTaskId());
            pojo.setTaskName(relation.getFatherTaskName());
            pojo.setFatherTaskId(-1);
            result.add(pojo);
        }

        LOG.warn("[TaskRelationService-findTaskDirectlyFather] task relations info, relations.size = {}, "
                + "father.size = {}, taskId = {}", relations.size(), relations.size(), taskId);
        return result;
    }


}
