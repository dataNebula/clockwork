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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.api.service.IProjectService;
import com.creditease.adx.clockwork.api.service.base.impl.AbstractBaseRdmsService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProject;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectResource;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectResourceExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectUser;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectUserExample;
import com.creditease.adx.clockwork.common.enums.ProjectStatus;
import com.creditease.adx.clockwork.common.framework.entity.Pagination;
import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;
import com.creditease.adx.clockwork.common.pojo.TbClockworkProjectPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.TbClockworkProjectExtMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkProjectMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkProjectResourceMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkProjectUserMapper;


@Service
public class ProjectService extends AbstractBaseRdmsService<TbClockworkProject, TbClockworkProjectPojo,
        TbClockworkProjectExample, TbClockworkProjectMapper> implements IProjectService {

    @Resource
    private TbClockworkProjectMapper clockworkProjectMapper;
    @Resource
    private TbClockworkProjectUserMapper clockworkProjectUserMapper;
    @Resource
    private TbClockworkProjectResourceMapper clockworkProjectResourceMapper;
    @Resource
    private TbClockworkProjectExtMapper tbClockworkProjectExtMapper;

    @Override
    public TbClockworkProjectMapper getMapper() {
        return clockworkProjectMapper;
    }

    @Override
    public int saveProject(TbClockworkProjectPojo tbClockworkProjectPojo) {
        if (StringUtils.isEmpty(tbClockworkProjectPojo.getName())) {
            throw new RuntimeException("Project name can not be empty!");
        }
        if (tbClockworkProjectPojo.getVcores() == null || tbClockworkProjectPojo.getVcores() < 1) {
            throw new RuntimeException("Project vcores is illegal.");
        }
        if (tbClockworkProjectPojo.getMemory() == null || tbClockworkProjectPojo.getMemory() < 1) {
            throw new RuntimeException("Project memory is illegal.");
        }
        tbClockworkProjectPojo.setCreateTime(new Date());
        final TbClockworkProject project = PojoUtil.convert(tbClockworkProjectPojo, TbClockworkProject.class);
        final int result = clockworkProjectMapper.insertSelective(project);
        if (project.getId() > 0) {
            final TbClockworkProjectResource resource = new TbClockworkProjectResource();
            resource.setProjectId(project.getId());
            resource.setCreateBy(tbClockworkProjectPojo.getCreateBy());
            resource.setCreateTime(new Date());
            resource.setVcores(tbClockworkProjectPojo.getVcores());
            resource.setMemory(tbClockworkProjectPojo.getMemory());
            clockworkProjectResourceMapper.insertSelective(resource);
        }
        return result;
    }

    @Override
    public List<TbClockworkProjectPojo> getProjectsByUser(String username) {
        if (StringUtils.isEmpty(username)) {
            throw new RuntimeException("Username can not be empty!");
        }
        TbClockworkProjectUserExample projectUserExample = new TbClockworkProjectUserExample();
        final TbClockworkProjectUserExample.Criteria projectUserExampleCriteria = projectUserExample.createCriteria();
        projectUserExampleCriteria.andUserNameEqualTo(username);
        final List<TbClockworkProjectUser> projectUsers = clockworkProjectUserMapper.selectByExample(projectUserExample);
        if (CollectionUtils.isNotEmpty(projectUsers)) {
            final List<Long> projectIds = projectUsers.stream().map(TbClockworkProjectUser::getProjectId).collect(Collectors.toList());
            TbClockworkProjectExample projectExample = new TbClockworkProjectExample();
            final TbClockworkProjectExample.Criteria projectExampleCriteria = projectExample.createCriteria();
            projectExampleCriteria.andIdIn(projectIds);
            return PojoUtil.convertList(clockworkProjectMapper.selectByExample(projectExample), TbClockworkProjectPojo.class);
        }
        return Collections.emptyList();
    }

    @Override
    public int inviteUsers(TbClockworkProjectUser tbClockworkProjectUser) {
        if (null == tbClockworkProjectUser.getProjectId() || tbClockworkProjectUser.getProjectId() < 1) {
            throw new RuntimeException("ProjectId is illegal.");
        }
        if (StringUtils.isBlank(tbClockworkProjectUser.getUserName())) {
            throw new RuntimeException("Username is illegal.");
        }
        // 删除名字中的空格
        tbClockworkProjectUser.setUserName(tbClockworkProjectUser.getUserName().replaceAll("\\s", ""));
        tbClockworkProjectUser.setCreateTime(new Date());
        return tbClockworkProjectExtMapper.inviteUser(tbClockworkProjectUser);
    }

    @Override
    public Pagination<TbClockworkProjectPojo> page(QueryCriteria<TbClockworkProjectPojo> queryCriteria) {
        final Pagination<TbClockworkProjectPojo> pagination = queryPagination(queryCriteria);
        if (CollectionUtils.isNotEmpty(pagination.getData())) {
            final List<Long> projectIds = pagination.getData().stream().map(TbClockworkProjectPojo::getId).collect(Collectors.toList());
            TbClockworkProjectResourceExample example = new TbClockworkProjectResourceExample();
            final TbClockworkProjectResourceExample.Criteria resourceExampleCriteria = example.createCriteria();
            resourceExampleCriteria.andProjectIdIn(projectIds);
            final List<TbClockworkProjectResource> projectResources = clockworkProjectResourceMapper.selectByExample(example);
            final Map<Long, TbClockworkProjectResource> projectResourceMap = projectResources.parallelStream()
            		.collect(Collectors.toMap(TbClockworkProjectResource::getProjectId, x -> x));
            pagination.getData().parallelStream().forEach(projectPojo -> {
                if (projectResourceMap.containsKey(projectPojo.getId())) {
                    projectPojo.setVcores(projectResourceMap.get(projectPojo.getId()).getVcores());
                    projectPojo.setMemory(projectResourceMap.get(projectPojo.getId()).getMemory());
                } else {
                    projectPojo.setMemory(0);
                    projectPojo.setVcores(0);
                }
            });
        }
        return pagination;
    }

    @Override
    public int updateProjectStatus(Long projectId, int status) {
        if (status != ProjectStatus.ONLINE.getStatus() ||  status != ProjectStatus.OFFLINE.getStatus()) {
            throw new RuntimeException("Status is illegal.");
        }
        TbClockworkProject project = new TbClockworkProject();
        project.setId(projectId);
        project.setStatus(status);
        project.setUpdateTime(new Date());
        return clockworkProjectMapper.updateByPrimaryKeySelective(project);
    }

    @Override
    public int updateProjectResource(TbClockworkProjectResource tbClockworkProjectResource) {
        if (null == tbClockworkProjectResource.getProjectId() || tbClockworkProjectResource.getProjectId() < 1) {
            throw new RuntimeException("ProjectId is illegal.");
        }
        tbClockworkProjectResource.setUpdateTime(new Date());
        TbClockworkProjectResourceExample example = new TbClockworkProjectResourceExample();
        example.createCriteria().andProjectIdEqualTo(tbClockworkProjectResource.getProjectId());
        return clockworkProjectResourceMapper.updateByExampleSelective(tbClockworkProjectResource, example);
    }

    @Override
    public int deleteProjectUser(Long id) {
        return clockworkProjectUserMapper.deleteByPrimaryKey(id);
    }
}
