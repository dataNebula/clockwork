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

package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroupExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskGroupPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskGroupMapper;

import java.util.List;
import java.util.Map;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-08-05
 */
public interface ITaskGroupService extends
        IBaseRdmsService<TbClockworkTaskGroup, TbClockworkTaskGroupPojo,
                TbClockworkTaskGroupExample, TbClockworkTaskGroupMapper> {
    /**
     * 通过ID获取任务信息
     *
     * @param id
     * @return
     */
    TbClockworkTaskGroup getTaskGroupById(int id);

    List<Map<String, Object>> getAllTaskGroupIdAndName();

    List<Map<String, Object>> getTaskGroupIdAndNameByUserGroupName(String userName, String userGroupName);


}
