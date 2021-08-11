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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUploadFileAndNodeRelationExample;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkUploadFileAndNodeRelationMapper;
import com.creditease.adx.clockwork.web.service.ITaskUploadFileService;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-06-27
 */
@Service(value="taskUploadFileService")
public class TaskUploadFileService implements ITaskUploadFileService {

    @Autowired
    private TbClockworkUploadFileAndNodeRelationMapper TbClockworkUploadFileAndNodeRelationMapper;

    @Override
    public void deleteNodeRelsByNodeId(Integer nodeId) {
        TbClockworkUploadFileAndNodeRelationExample example = new TbClockworkUploadFileAndNodeRelationExample();
        example.createCriteria().andNodeIdEqualTo(nodeId);
        TbClockworkUploadFileAndNodeRelationMapper.deleteByExample(example);
    }

}
