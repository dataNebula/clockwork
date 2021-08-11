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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroupExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskGroupPojo;
import com.creditease.adx.clockwork.dao.mapper.TaskGroupMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskGroupMapper;
import com.creditease.adx.clockwork.web.service.ITaskGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-08-05
 */
@Service(value = "taskGroupService")
public class TaskGroupService extends
        AbstractBaseRdmsService<TbClockworkTaskGroup, TbClockworkTaskGroupPojo,
                TbClockworkTaskGroupExample, TbClockworkTaskGroupMapper> implements ITaskGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskGroupService.class);

    @Autowired
    private TaskGroupMapper taskGroupMapper;

    @Autowired
    private TbClockworkTaskGroupMapper tbClockworkTaskGroupMapper;


    @Override
    public TbClockworkTaskGroupMapper getMapper() {
        return this.tbClockworkTaskGroupMapper;
    }

    @Override
    public TbClockworkTaskGroup getTaskGroupById(int id) {
        return tbClockworkTaskGroupMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getAllTaskGroupIdAndName() {
        return taskGroupMapper.selectAllIdAndName();
    }

    @Override
    public List<Map<String, Object>> getTaskGroupIdAndNameByUserGroupName(String userName, String userGroupName) {
        LOG.info("getTaskGroupIdAndNameByUserGroupName, userName = {}, userGroupName = {}", userName, userGroupName);
        Map<String, String> param = new HashMap<>();
        param.put("userName",userName);
        param.put("userGroupName",userGroupName == null ? "-" : userGroupName);
        return taskGroupMapper.selectTaskGroupIdAndNameByUserGroupName(param);
    }
}
