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

package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.client.service.TaskSubmitClientService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:26 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
@Api("任务操作相关接口")
@RestController
@RequestMapping("/clockwork/web/task/operation")
public class TaskOperationController {

    private final Logger LOG = LoggerFactory.getLogger(TaskOperationController.class);

    @Autowired
    private TaskSubmitClientService taskSubmitClientService;

    @Autowired
    private TaskOperationClientService taskOperationClientService;

    /**
     * 添加任务（默认状态ENABLE、上线）
     *
     * @param taskPojo task
     * @return
     */
    @PostMapping(value = "/addTask")
    public Map<String, Object> addTask(@RequestBody TbClockworkTaskPojo taskPojo) {
        try {
            return taskOperationClientService.getTaskOperationClient().addTask(taskPojo);
        } catch (Exception e) {
            LOG.error("TaskOperationController-addTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新任务（不修改任务状态、上下线）
     *
     * @param taskPojo task
     * @return
     */
    @PostMapping(value = "/updateTask")
    public Map<String, Object> updateTask(@RequestBody TbClockworkTaskPojo taskPojo) {
        try {
            return taskOperationClientService.getTaskOperationClient().updateTask(taskPojo);
        } catch (Exception e) {
            LOG.error("TaskOperationController-updateTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新任务状态
     *
     * @param taskId id
     * @param status status
     * @return
     */
    @PostMapping(value = "/updateTaskStatus")
    public Map<String, Object> updateTaskStatus(@RequestParam(value = "taskId") Integer taskId,
                                                @RequestParam(value = "status") String status) {
        try {
            return taskOperationClientService.getTaskOperationClient().updateTaskStatus(taskId, status);
        } catch (Exception e) {
            LOG.error("TaskOperationController-updateTaskStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 上线任务
     *
     * @param taskId taskId
     * @return
     */
    @PostMapping(value = "/enableTask")
    public Map<String, Object> enableTask(@RequestParam(value = "taskId") Integer taskId) {
        try {
            return taskOperationClientService.getTaskOperationClient().enableTask(taskId);
        } catch (Exception e) {
            LOG.error("TaskOperationController-enableTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 下线任务
     *
     * @param taskId taskId
     * @return
     */
    @PostMapping(value = "/disableTask")
    public Map<String, Object> disableTask(@RequestParam(value = "taskId") Integer taskId) {
        try {
            return taskOperationClientService.getTaskOperationClient().disableTask(taskId);
        } catch (Exception e) {
            LOG.error("TaskOperationController-disableTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除任务
     *
     * @param taskId taskId
     * @return
     */
    @PostMapping(value = "/deleteTask")
    public Map<String, Object> deleteTask(@RequestParam(value = "taskId") Integer taskId) {
        try {
            return taskOperationClientService.getTaskOperationClient().deleteTask(taskId);
        } catch (Exception e) {
            LOG.error("TaskOperationController-deleteTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 停止单个任务
     *
     * @param taskId 任务ID
     * @return
     */
    @PostMapping(value = "/stopTask")
    public Map<String, Object> stopTask(@RequestParam(value = "taskId") Integer taskId) {
        return taskOperationClientService.getTaskOperationClient().stopTask(taskId);
    }

    /**
     * 停止任务以及自己的子任务,||停止自己子任务
     *
     * @param taskId 任务ID
     * @return
     */
    @PostMapping(value = "/stopTaskAndChildrens")
    public Map<String, Object> stopTaskAndChildrens(@RequestParam(value = "taskId") Integer taskId,
                                                    @RequestParam(value = "stopType") Integer stopType) {
//       根据taskId获得他子任务的id
        return taskOperationClientService.getTaskOperationClient().stopTaskAndChirldens(taskId, stopType);
    }


    /**
     * 重启任务（自己、所有子节点不包括自己、所有子节点包括自己）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:self,3:all_children_not_self,4:all_children_and_self]
     * @param parameter     参数
     * @param operatorName  操作人
     * @return task
     */
    @PostMapping(value = "/rerunTask")
    public Map<String, Object> submitReRunTask(@RequestParam(value = "taskId") Integer taskId,
                                               @RequestParam(value = "taskReRunType", required = false) Integer taskReRunType,
                                               @RequestParam(value = "parameter", required = false) String parameter,
                                               @RequestParam(value = "operatorName", required = false) String operatorName) {
        return taskSubmitClientService.getTaskSubmitClient().submitReRunTask(taskId, taskReRunType, parameter, operatorName);
    }


    /**
     * 重启任务（历史）
     */
    @ApiOperation(value = "重启历史运行任务（单个任务）", notes = "taskReRunType=0:his_rerun, 1:his_routine, 2:his_fill_data")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "需要重启的任务ID", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "executeType", value = "执行类型[0:his_rerun, 1:his_routine, 2:his_fill_data]",
                    required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "logId", value = "logId", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "parameter", value = "重启参数json", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "operatorName", value = "操作人", required = false, dataType = "String")
    })
    @PostMapping(value = "/rerunTaskHis")
    public Map<String, Object> submitReRunTaskHis(@RequestParam(value = "taskId") Integer taskId,
                                                  @RequestParam(value = "taskReRunType", required = false) Integer taskReRunType,
                                                  @RequestParam(value = "logId", required = false) Integer logId,
                                                  @RequestParam(value = "parameter", required = false) String parameter,
                                                  @RequestParam(value = "operatorName", required = false) String operatorName) {
        return taskSubmitClientService.getTaskSubmitClient().submitReRunTaskHis(taskId, taskReRunType, logId, parameter, operatorName);
    }


    /**
     * 重启任务根据 dag id
     *
     * @param dagId        dagId
     * @param parameter    参数
     * @param operatorName 操作人
     * @return
     */
    @PostMapping(value = "/rerunTaskByDagId")
    public Map<String, Object> submitReRunTaskByDagId(@RequestParam(value = "dagId") Integer dagId,
                                                      @RequestParam(value = "parameter", required = false) String parameter,
                                                      @RequestParam(value = "operatorName", required = false) String operatorName) {
        return taskSubmitClientService.getTaskSubmitClient().submitReRunTaskByDagId(dagId, parameter, operatorName);
    }


    /**
     * 重启任务根据 group id
     *
     * @param groupId      group id
     * @param parameter    参数
     * @param operatorName 操作人
     * @return
     */
    @PostMapping(value = "/rerunTaskByGroupId")
    public Map<String, Object> submitReRunTaskByGroupId(@RequestParam(value = "groupId") Integer groupId,
                                                        @RequestParam(value = "parameter", required = false) String parameter,
                                                        @RequestParam(value = "operatorName", required = false) String operatorName) {
        return taskSubmitClientService.getTaskSubmitClient().submitReRunTaskByGroupId(groupId, parameter, operatorName);
    }

}
