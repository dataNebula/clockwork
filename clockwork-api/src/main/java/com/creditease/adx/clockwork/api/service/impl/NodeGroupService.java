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

import com.creditease.adx.clockwork.api.service.INodeGroupService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeGroup;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeGroupExample;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkNodeGroupMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NodeGroupService implements INodeGroupService {

    @SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(NodeGroupService.class);

    @Autowired
    private TbClockworkNodeGroupMapper tbClockworkNodeGroupMapper;

    @Override
    public List<TbClockworkNodeGroup> getAllNodeGroup() {
        return tbClockworkNodeGroupMapper.selectByExample(new TbClockworkNodeGroupExample());
    }

}
