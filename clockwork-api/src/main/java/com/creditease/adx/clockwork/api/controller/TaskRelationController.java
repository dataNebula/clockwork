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
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.api.service.ITaskRelationService;
import com.creditease.adx.clockwork.common.entity.Response;

/**
 * 任务依赖关系
 */
@Api("任务相关依赖接口")
@RestController
@RequestMapping("/clockwork/api/task/relation")
public class TaskRelationController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRelationController.class);

    @Resource(name = "taskRelationService")
    private ITaskRelationService taskRelationService;

    /**
     * 获得当前任务的所有后代，包含自己本身
     *
     * curl http://localhost:9005/clockwork/api/task/relation/getTaskAllChildrenIncludeSelf?taskId=46380
     *
     * @param taskId
     * @return
     */
    @ApiOperation("获得当前作业的所有后代，包含自己本身")
    @GetMapping(value = "/getTaskAllChildrenIncludeSelf")
    public Map<String, Object> getTaskAllChildrenIncludeSelf(@RequestParam(value = "taskId") Integer taskId) {
        try {
            return Response.success(taskRelationService.getTaskAllChildrenIncludeSelf(taskId));
        } catch (Exception e) {
            LOG.error("TaskRelationController-getTaskAllChildrenIncludeSelf Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询当前task的直接子任务, 不包含自己
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/getTaskDirectlyChildrenNotIncludeSelf")
    public Map<String, Object> getTaskDirectlyChildrenNotIncludeSelf(@RequestParam(value = "taskId") Integer taskId) {
        if (taskId == null) {
            return Response.fail("task id is null,please check it.");
        }
        try {
            return Response.success(taskRelationService.getTaskDirectlyChildrenNotIncludeSelf(taskId));
        } catch (Exception e) {
            LOG.error("TaskRelationController-getChildTasks Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询当前task的直接父任务, 不包含自己
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/getTaskDirectlyFatherNotIncludeSelf")
    public Map<String, Object> getTaskDirectlyFatherNotIncludeSelf(@RequestParam(value = "taskId") Integer taskId) {
        if (taskId == null) {
            return Response.fail("task id is null,please check it.");
        }
        try {
            return Response.success(taskRelationService.getTaskDirectlyFatherNotIncludeSelf(taskId));
        } catch (Exception e) {
            LOG.error("TaskRelationController-getTaskDirectlyFatherNotIncludeSelf Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
