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

package com.creditease.adx.clockwork.api.service;

import java.util.List;

import com.creditease.adx.clockwork.api.service.base.IBaseRdmsService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProject;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectResource;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectUser;
import com.creditease.adx.clockwork.common.framework.entity.Pagination;
import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;
import com.creditease.adx.clockwork.common.pojo.TbClockworkProjectPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkProjectMapper;

public interface IProjectService extends IBaseRdmsService<TbClockworkProject, TbClockworkProjectPojo,
        TbClockworkProjectExample, TbClockworkProjectMapper> {

    /**
     * 保存项目
     */
    int saveProject(TbClockworkProjectPojo tbClockworkProjectPojo);

    /**
     * 获取用户的项目
     */
    List<TbClockworkProjectPojo> getProjectsByUser(String username);

    /**
     * 给项目添加用户
     */
    int inviteUsers(TbClockworkProjectUser tbClockworkProjectUser);

    /**
     * 分页查询项目列表
     */
    Pagination<TbClockworkProjectPojo> page(QueryCriteria<TbClockworkProjectPojo> queryCriteria);

    /**
     * 更新项目上下线状态
     */
    int updateProjectStatus(Long projectId, int status);

    /**
     * 更新项目的资源
     */
    int updateProjectResource(TbClockworkProjectResource tbClockworkProjectResource);

    /**
     * 删除项目下用户
     */
    int deleteProjectUser(Long id);
}
