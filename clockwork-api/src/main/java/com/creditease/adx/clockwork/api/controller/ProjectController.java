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

package com.creditease.adx.clockwork.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.api.service.IProjectService;
import com.creditease.adx.clockwork.api.service.IProjectUserService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectResource;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectUser;
import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;
import com.creditease.adx.clockwork.common.pojo.TbClockworkProjectPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkProjectUserPojo;

@Api(description = "项目接口")
@RestController
@RequestMapping("/project")
public class ProjectController {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
    private final IProjectService projectService;
    private final IProjectUserService projectUserService;

    public ProjectController(IProjectService projectService, IProjectUserService projectUserService) {
        this.projectService = projectService;
        this.projectUserService = projectUserService;
    }

    @ApiOperation(value = "新增项目")
    @PostMapping("/addProject")
    public Map<String, Object> addProject(@RequestBody TbClockworkProjectPojo tbClockworkProjectPojo) {
        try {
            LOG.info("Add project {} success.", tbClockworkProjectPojo.getName());
            return Response.success(projectService.saveProject(tbClockworkProjectPojo));
        } catch (Exception e) {
            LOG.error("Add project error.", e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "获取我的项目")
    @GetMapping("/my")
    public Map<String, Object> getMyProject(@RequestParam String username) {
        try {
            return Response.success(projectService.getProjectsByUser(username));
        } catch (Exception e) {
            LOG.error("Get projects for user [{}] error.", username, e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("邀请项目成员")
    @PostMapping("/{projectId}/invite")
    public Map<String, Object> inviteProjectUser(@PathVariable Long projectId, @RequestBody TbClockworkProjectUser tbClockworkProjectUser) {
        tbClockworkProjectUser.setProjectId(projectId);
        try {
            return Response.success(projectService.inviteUsers(tbClockworkProjectUser));
        } catch (Exception e) {
            LOG.error("Add project user error.", e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("分页查询项目")
    @PostMapping("/page")
    public Map<String, Object> page(@RequestBody QueryCriteria<TbClockworkProjectPojo> queryCriteria) {
        try {
            return Response.success(projectService.page(queryCriteria));
        } catch (Exception e) {
            LOG.error("Get project page error.", e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("获取项目下成员")
    @GetMapping("/{projectId}/user")
    public Map<String, Object> pageUser(@PathVariable Long projectId, @RequestBody QueryCriteria<TbClockworkProjectUserPojo> queryCriteria) {
        try {
            if (null == queryCriteria.getEntityDto()) {
                queryCriteria.setEntityDto(new TbClockworkProjectUserPojo());
            }
            queryCriteria.getEntityDto().setProjectId(projectId);
            return Response.success(projectUserService.pageUser(queryCriteria));
        } catch (Exception e) {
            LOG.error("Get project user page error.", e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "上下线项目", notes = "0:下线,1:上线")
    @PutMapping("/{projectId}/status")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "projectId", value = "项目id", required = true, dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "status", value = "状态", required = true, allowableValues = "0, 1", dataType = "Integer")
    })
    public Map<String, Object> updateProjectStatus(@PathVariable Long projectId, @RequestParam int status) {
        try {
            return Response.success(projectService.updateProjectStatus(projectId, status));
        } catch (Exception e) {
            LOG.error("Update project status error.", e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("更新项目资源")
    @PutMapping("/{projectId}/resource")
    public Map<String, Object> updateProjectResource(@PathVariable Long projectId, @RequestBody TbClockworkProjectResource tbClockworkProjectResource) {
        try {
            tbClockworkProjectResource.setProjectId(projectId);
            return Response.success(projectService.updateProjectResource(tbClockworkProjectResource));
        } catch (Exception e) {
            LOG.error("Update project resource error.", e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("移除项目下用户")
    @DeleteMapping("/user/{id}")
    public Map<String, Object> deleteProjectUser(@PathVariable Long id) {
        try {
            return Response.success(projectService.deleteProjectUser(id));
        } catch (Exception e) {
            LOG.error("Update project resource error.", e);
            return Response.fail(e.getMessage());
        }
    }
}
