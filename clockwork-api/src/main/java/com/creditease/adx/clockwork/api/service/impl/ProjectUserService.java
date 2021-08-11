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

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.api.service.IProjectUserService;
import com.creditease.adx.clockwork.api.service.base.impl.AbstractBaseRdmsService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectUser;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectUserExample;
import com.creditease.adx.clockwork.common.framework.entity.Pagination;
import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;
import com.creditease.adx.clockwork.common.pojo.TbClockworkProjectUserPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkProjectUserMapper;


@Service
public class ProjectUserService extends AbstractBaseRdmsService<TbClockworkProjectUser, TbClockworkProjectUserPojo,
        TbClockworkProjectUserExample, TbClockworkProjectUserMapper> implements IProjectUserService {

    @Resource
    private TbClockworkProjectUserMapper clockworkProjectUserMapper;

    @Override
    public TbClockworkProjectUserMapper getMapper() {
        return clockworkProjectUserMapper;
    }

    @Override
    public Pagination<TbClockworkProjectUserPojo> pageUser(QueryCriteria<TbClockworkProjectUserPojo> queryCriteria) {
        return queryPagination(queryCriteria);
    }
}
