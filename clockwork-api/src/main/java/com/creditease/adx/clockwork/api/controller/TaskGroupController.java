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

import com.creditease.adx.clockwork.api.service.ITaskGroupService;
import com.creditease.adx.clockwork.api.service.ITaskOperationService;
import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskGroupMapper;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TaskGroup服务类
 */
@Api("任务组接口")
@RestController
@RequestMapping("/clockwork/api/task/group")
public class TaskGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskGroupController.class);

    @Autowired
    private ITaskService taskService;

    @Autowired
    private ITaskGroupService taskGroupService;

    @Autowired
    private ITaskOperationService taskOperationService;

    @Autowired
    private TbClockworkTaskGroupMapper tbClockworkTaskGroupMapper;

    /**
     * 添加taskGroup，不用加任务修改锁，因为不涉及操作任务的状态
     *
     * @param taskGroup json string
     */
    @PostMapping(value = "/addTaskGroup")
    public Map<String, Object> addTaskGroup(@RequestBody TbClockworkTaskGroup taskGroup) {
        try {
            taskGroupService.addTaskGroup(taskGroup);
            return Response.success(taskGroup.getId());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改taskGroup
     *
     * @param taskGroup json string
     */
    @PostMapping(value = "/updateTaskGroup")
    public Map<String, Object> updateTaskGroup(@RequestBody TbClockworkTaskGroup taskGroup) {
        try {
            taskGroupService.updateTaskGroup(taskGroup);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("updateTaskGroup Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 使taskGroup本身失效，并将其内部的所有作业任务也一并失效，涉及操作作业状态必须加锁
     * 作业组以及作业下线是，队列中的任务需要移除
     *
     * @param taskGroupId taskGroupId
     */
    @PostMapping(value = "/disableTaskGroup")
    public Map<String, Object> disableTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        try {
            taskGroupService.disableTaskGroupTx(taskGroupId);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("disableTaskGroup taskGroupId = {}, Error {}", taskGroupId, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 使taskGroup生效，并将其内部的所有作业任务也一并生效，涉及操作作业状态必须加锁
     *
     * @param taskGroupId taskGroupId
     */
    @PostMapping(value = "/enableTaskGroup")
    public Map<String, Object> enableTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        try {
            // 分布式锁环境下安全进行
            taskGroupService.enableTaskGroup(taskGroupId);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("enableTaskGroup taskGroupId = {}, Error {}", taskGroupId, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param taskGroupId 任务组Id
     * @return
     */
    @PostMapping(value = "/delete")
    public Map<String, Object> deleteTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        try {
            LOG.info("[TaskGroupController-deleteTaskGroup] taskGroupId = {}", taskGroupId);
            if (taskGroupId == null || taskGroupId < 1) {
                return Response.fail("Parameter invalid！");
            }

            // 查询任务组下面所有任务,是否可以删除
            boolean canBeDelete = true;
            List<TbClockworkTask> tbClockworkTasks = taskService.getTaskByTaskGroupId(taskGroupId);
            for (TbClockworkTask tbClockworkTask : tbClockworkTasks) {
                if (!TaskStatusUtil.canBeDeleteCurrentTaskStatus(tbClockworkTask.getStatus())) {
                    LOG.info("[TaskGroupController-deleteTaskGroup] Task cannot be deleted temporarily, please stop it first. " +
                                    "taskGroupId = {}, taskId = {}, taskStatus = {}",
                            taskGroupId,
                            tbClockworkTask.getId(),
                            tbClockworkTask.getStatus());
                    canBeDelete = false;
                    break;
                }
            }

            // 不能删除，则返回信息
            if (!canBeDelete) {
                LOG.info("[TaskGroupController-deleteTaskGroup] Task cannot be deleted temporarily, please stop it first." +
                        "taskGroupId = {}, canBeDelete = {}", taskGroupId, canBeDelete);
                return Response.fail("TaskGroup cannot be deleted temporarily, please stop it first.");
            }
            // 删除任务组 && 删除该任务组下所有的任务
            else {
                // 删除任务组
                int count = tbClockworkTaskGroupMapper.deleteByPrimaryKey(taskGroupId);

                // 删除任务
                List<Integer> ids = new ArrayList<>(tbClockworkTasks.size());
                for (TbClockworkTask tbClockworkTask : tbClockworkTasks) {
                    ids.add(tbClockworkTask.getId());
                }
                taskOperationService.deleteTaskList(ids);

                LOG.info("[TaskGroupController-deleteTaskGroup] " +
                                "delete count={}, taskGroupId = {}, canBeDelete = {}, ids.size = {}",
                        count, taskGroupId, canBeDelete, ids.size());

                return Response.success(count);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 查询taskGroupName是否重复
     *
     * @param taskGroupName taskGroup name
     */
    @GetMapping(value = "/taskGroupIsExists")
    public Map<String, Object> taskGroupIsExists(@RequestParam(value = "taskGroupName") String taskGroupName) {

        try {
            if (StringUtils.isBlank(taskGroupName)) {
                LOG.error("TaskGroupController-taskGroupIsExists, invalid taskGroupName");
                return Response.fail("invalid taskGroupName");
            }
            return Response.success(taskGroupService.taskGroupIsExists(taskGroupName));
        } catch (Exception e) {
            LOG.error("taskGroupIsExists taskGroupName = {}, Error {}", taskGroupName, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取taskGroup信息，通过taskGroupId
     *
     * @param taskGroupId taskGroupId
     * @return
     */
    @GetMapping(value = "/getTaskGroupById")
    public Map<String, Object> getTaskGroupById(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        if (taskGroupId == null || taskGroupId < 1) {
            LOG.error("TaskGroupController-getTaskGroupById, invalid taskGroupId");
            return Response.fail("invalid taskGroupId");
        }

        try {
            LOG.info("getTaskGroupById, taskGroupId = {}", taskGroupId);
            return Response.success(taskGroupService.getTaskGroupById(taskGroupId));
        } catch (Exception e) {
            LOG.error("TaskGroupController-getTaskGroupById taskGroupId = {}, Error {}", taskGroupId, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
