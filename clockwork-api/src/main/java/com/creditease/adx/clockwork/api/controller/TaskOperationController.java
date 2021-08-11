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

import com.creditease.adx.clockwork.api.service.ITaskOperationService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.enums.TaskSource;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:26 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
@Api("任务操作相关接口")
@RestController
@RequestMapping("/clockwork/api/task/operation")
public class TaskOperationController {

    private final Logger LOG = LoggerFactory.getLogger(TaskOperationController.class);

    @Autowired
    private ITaskOperationService taskOperationService;

    @Resource(name = "redisService")
    private IRedisService redisService;

    @ApiOperation("新增task")
    @PostMapping(value = "/addTask")
    public Map<String, Object> addTask(@RequestBody TbClockworkTaskPojo task) {
        try {
            taskOperationService.addTask(task);
            return Response.success(task.getId());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("新增taskList")
    @PostMapping(value = "/addTaskList")
    public Map<String, Object> addTaskList(@RequestBody TaskGroupAndTasks taskGroupAndTasks) {
        long startTime = System.currentTimeMillis();
        try {
            LOG.info("[TaskOperationController-addTaskList-begin]taskGroupAndTask = {}", taskGroupAndTasks);
            taskOperationService.addTaskList(taskGroupAndTasks);
            LOG.info("[TaskOperationController-addTaskList]operate success,cost time = {} ms,taskGroupAndTask = {}",
                    System.currentTimeMillis() - startTime, taskGroupAndTasks);
            return Response.success(taskGroupAndTasks);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            LOG.info("[TaskOperationController-addTaskList]operate exception,cost time = {} ms,taskGroupAndTask = {}",
                    System.currentTimeMillis() - startTime, taskGroupAndTasks);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 修改Task（不修改状态、上下线）
     *
     * @param taskPojo task
     * @return taskId
     */
    @ApiOperation("修改Task（不修改状态、上下线）")
    @PostMapping(value = "/updateTask")
    public Map<String, Object> updateTask(@RequestBody TbClockworkTaskPojo taskPojo) {
        try {
            taskOperationService.updateTask(taskPojo);
            return Response.success(taskPojo.getId());
        } catch (Exception e) {
            LOG.error("TaskOperationController-updateTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 批量修改Task（不修改状态、上下线）
     *
     * @param taskGroupAndTasks taskGroupAndTasks
     * @return taskGroupAndTasks
     */
    @ApiOperation("修改TaskList")
    @PostMapping(value = "/updateTaskList")
    public Map<String, Object> updateTaskList(@RequestBody TaskGroupAndTasks taskGroupAndTasks) {
        long startTime = System.currentTimeMillis();
        try {
            LOG.info("[TaskOperationController-updateTaskList-begin]taskGroupAndTask = {}", taskGroupAndTasks);
            taskOperationService.updateTaskList(taskGroupAndTasks);
            LOG.info("[TaskOperationController-updateTaskList]operate success,cost time = {} ms,taskGroupAndTask = {}",
                    System.currentTimeMillis() - startTime, taskGroupAndTasks);
            return Response.success(taskGroupAndTasks);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            LOG.info("[TaskOperationController-updateTaskList]operate exception,cost time = {} ms,taskGroupAndTask = {}",
                    System.currentTimeMillis() - startTime, taskGroupAndTasks);
            return Response.fail(e.getMessage());
        }
    }


    @ApiOperation("使task生效上线")
    @PostMapping(value = "/enableTask")
    public Map<String, Object> enableTask(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null) {
                return Response.fail(taskId, "task info is null!");
            }
            if (taskOperationService.enableTaskTx(taskId)) {
                return Response.success(taskId);
            } else {
                return Response.fail(taskId, "enable task failure!");
            }
        } catch (Exception e) {
            LOG.error("TaskOperationController-disableTask taskId = {}, Error {}.", taskId, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("下线task任务")
    @PostMapping(value = "/disableTask")
    public Map<String, Object> disableTask(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null) {
                return Response.fail(taskId, "task info is null!");
            }
            if (taskOperationService.disableTaskTx(taskId)) {
                return Response.success(taskId);
            } else {
                return Response.fail(taskId, "disable task failure!");
            }

        } catch (Exception e) {
            LOG.error("TaskOperationController-disableTask taskId = {}, Error {}.", taskId, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("删除Task")
    @PostMapping(value = "/deleteTask")
    public Map<String, Object> deleteTask(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null) {
                return Response.fail(taskId, "task info is null!");
            }
            taskOperationService.deleteTask(taskId);
            return Response.success(taskId);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("删除TaskList")
    @PostMapping(value = "/deleteTaskList")
    public Map<String, Object> deleteTaskList(@RequestParam(value = "taskIds") List<Integer> taskIds) {
        try {
            taskOperationService.deleteTaskList(taskIds);
            return Response.success(taskIds);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }

    }

    /**
     * 修改任务状态
     *
     * @param taskId id
     * @param status status
     * @return count
     */
    @PostMapping(value = "/updateTaskStatus")
    public Map<String, Object> updateTaskStatus(@RequestParam(value = "taskId") Integer taskId,
                                                @RequestParam(value = "status") String status) {
        try {
            int count = taskOperationService.updateTaskStatus(taskId, status);
            if (count == 1) {
                return Response.success(count);
            }
            return Response.fail(count);
        } catch (Exception e) {
            LOG.error("updateTaskStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 更新子任务状态为FatherNotSuccess
     *
     * @param taskId taskId
     * @return bool
     */
    @PostMapping(value = "/updateTaskAllChildrenStatusFatherNotSuccess")
    public Map<String, Object> updateTaskAllChildrenStatusFatherNotSuccess(@RequestParam(value = "taskId") Integer taskId,
                                                                           @RequestParam(value = "executeType") Integer executeType,
                                                                           @RequestParam(value = "rerunBatchNumber", required = false) Long rerunBatchNumber) {
        try {
            if (taskId == null || executeType == null) {
                return Response.fail("param taskId and executeType can't be empty.");
            }
            boolean result = taskOperationService.updateTaskAllChildrenStatusFatherNotSuccess(taskId, executeType, rerunBatchNumber);
            if (result) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("updateTaskAllChildrenStatusFatherNotSuccess Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改作业各种信息，仅限于内部使用（包括任务状态、上下线）
     *
     * @param task task
     */
    @PostMapping(value = "/updateTaskInfo")
    public Map<String, Object> updateTaskInfo(@RequestBody TbClockworkTaskPojo task) {
        try {
            int count = taskOperationService.updateTaskInfo(task);
            if (count == 1) {
                return Response.success(count);
            }
            return Response.fail(count);
        } catch (Exception e) {
            LOG.error("updateTaskInfo Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新任务的依赖关系字段通过Id
     *
     * @param taskId 任务id
     * @return bool
     */
    @PostMapping(value = "/updateTaskDependencyIdFieldById")
    public Map<String, Object> updateTaskDependencyIdFieldById(@RequestParam(value = "taskId") Integer taskId) {
        try {
            taskOperationService.updateTaskDependencyIdFieldById(taskId);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("updateTaskDependencyIdFieldById Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 更新状态为Submit
     *
     * @param taskId taskId
     * @return
     */
    @PostMapping(value = "/updateTaskStatusSubmit")
    public Map<String, Object> updateTaskStatusSubmit(@RequestParam(value = "taskId") Integer taskId) {
        try {
            LOG.info("updateTaskStatusSubmit info, taskId = {}", taskId);
            if (taskId == null) {
                return Response.fail("updateTaskStatusSubmit info, taskId is null.");
            }

            // 更新任务状态
            boolean updateTasksStatusResult = taskOperationService.updateTaskStatusSubmit(taskId);
            if (updateTasksStatusResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("updateTaskStatusSubmit Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 批量更新状态为Submit
     *
     * @param taskStatusSubmit submit
     * @return
     */
    @PostMapping(value = "/updateTaskStatusSubmitBatch")
    public Map<String, Object> updateTaskStatusSubmitBatch(@RequestBody BatchUpdateTaskStatusSubmit taskStatusSubmit) {
        try {
            LOG.info("updateTaskStatusSubmitBatch info, taskIds = {}, status = {}",
                    taskStatusSubmit.getTaskIds(),
                    taskStatusSubmit.getStatus());
            boolean updateTasksStatusResult = taskOperationService.updateTaskStatusSubmitBatch(taskStatusSubmit);
            if (updateTasksStatusResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("updateTaskStatusSubmitBatch Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 批量更新状态为结束状态
     *
     * @param taskStatusEnd batch data
     * @return
     */
    @PostMapping(value = "/updateTaskStatusEndBatch")
    public Map<String, Object> updateTaskStatusEndBatch(@RequestBody BatchUpdateTaskStatusEnd taskStatusEnd) {
        try {
            LOG.info("updateTaskStatusEndBatch info, taskIds = {}, status = {}",
                    taskStatusEnd.getTaskIds(),
                    taskStatusEnd.getStatus());
            boolean updateTasksStatusResult = taskOperationService.updateTaskStatusEndBatch(taskStatusEnd);
            if (updateTasksStatusResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("updateTaskStatusEndBatch Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 批量更新状态
     *
     * @param updateTaskStatusPojo BatchUpdateTaskStatusParam
     * @return
     */
    @PostMapping(value = "/updateTasksStatus")
    public Map<String, Object> updateTaskStatusBatch(@RequestBody BatchUpdateTaskStatusParam updateTaskStatusPojo) {
        try {
            LOG.info("updateTaskStatusBatch info, taskIds = {}, status = {}",
                    updateTaskStatusPojo.getTaskIds(),
                    updateTaskStatusPojo.getStatus());
            boolean updateTasksStatusResult = taskOperationService.updateTaskStatusBatch(updateTaskStatusPojo);
            if (updateTasksStatusResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("updateTasksStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 批量更新延迟策略状态
     *
     * @param param param
     * @return bool
     */
    @PostMapping(value = "/updateTasksDelayStatusByBatch")
    public Map<String, Object> updateTasksDelayStatusByBatch(@RequestBody BatchUpdateTasksDelayStatusParam param) {
        try {
            if (CollectionUtils.isEmpty(param.getTaskIds())) {
                return Response.fail("tasks is null.");
            }
            return Response.success(taskOperationService.updateTasksDelayStatusByBatch(param.getTaskIds(), param.getDelayStatus()));
        } catch (Exception e) {
            LOG.error("updateTasksDelayStatusByBatch Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 将作业的状态设置为生命周期重置状态
     *
     * @param taskIds taskIds
     * @return bool
     */
    @PostMapping(value = "/resetTaskDescendantsLifecycleStatusInBatch")
    public Map<String, Object> resetTaskDescendantsLifecycleStatusInBatch(@RequestBody List<Integer> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Response.fail("task ids information is null,please check it.");
        }
        try {
            boolean result = taskOperationService.resetTaskDescendantsLifecycleStatusInBatch(taskIds);
            if (result) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("resetTaskDescendantsLifecycleStatusInBatch Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据dagIds将作业的状态设置为生命周期重置状态
     *
     * @param param BatchResetTaskLifecycleParam
     * @return bool
     */
    @PostMapping(value = "/resetTaskLifecycleStatusByDagIdsInBatch")
    public Map<String, Object> resetTaskLifecycleStatusByDagIdsInBatch(@RequestBody BatchResetTaskLifecycleParam param) {
        try {
            if (param == null
                    || CollectionUtils.isEmpty(param.getTaskIds())
                    || CollectionUtils.isEmpty(param.getDagIds())) {
                return Response.fail("param information is null,please check it.");
            }

            boolean result = taskOperationService.resetTaskLifecycleStatusByDagIdsInBatch(param.getDagIds(), param.getTaskIds());
            if (result) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("resetTaskLifecycleStatusByDagIdsInBatch Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据source将作业的状态设置为生命周期重置状态
     *
     * @param source source
     * @return bool
     */
    @PostMapping(value = "/resetTaskLifecycleStatusBySource")
    public Map<String, Object> resetTaskLifecycleStatusBySource(@RequestParam Integer source) {
        try {
            if (source == null) {
                return Response.fail("param source is null,please check it.");
            }

            TaskSource taskSource = TaskSource.getTaskSourceByValue(source);
            if (taskSource == null) {
                return Response.fail("param source is not support it, source = " + source);
            }
            boolean result = taskOperationService.resetTaskLifecycleStatusBySource(taskSource.getValue());
            if (result) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("resetTaskLifecycleStatusBySource Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 根据dagIds将作业的状态设置为生命周期重置状态
     *
     * @param dagIds dagIds
     * @return bool
     */
    @PostMapping(value = "/resetTaskLifecycleStatusByDagIds")
    public Map<String, Object> resetTaskLifecycleStatusByDagIds(@RequestBody List<Integer> dagIds) {
        try {
            if (CollectionUtils.isEmpty(dagIds)) {
                return Response.fail("param dagId is null,please check it.");
            }

            boolean result = taskOperationService.resetTaskLifecycleStatusByDagIds(dagIds);
            if (result) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("resetTaskLifecycleStatusByDagIds Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/resetTaskLifecycleStatusByDagIdsNotExists")
    public Map<String, Object> resetTaskLifecycleStatusByDagIdsNotExists(@RequestBody List<Integer> dagIds) {
        try {
            if (CollectionUtils.isEmpty(dagIds)) {
                return Response.fail("param dagId is null,please check it.");
            }

            boolean result = taskOperationService.resetTaskLifecycleStatusByDagIdsNotExists(dagIds);
            if (result) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("resetTaskLifecycleStatusByDagIdsNotExists Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 检查父任务是否成功
     *
     * @param taskPojo task
     * @return
     */
    @PostMapping(value = "/checkParentsSuccess")
    public Map<String, Object> checkParentsSuccess(@RequestBody TbClockworkTaskPojo taskPojo) {
        try {
            if (taskPojo == null || taskPojo.getId() == null) {
                LOG.error("checkParentsSuccess, invalid taskPojo");
                return Response.fail("invalid taskPojo");
            }

            boolean result = taskOperationService.checkParentsSuccess(taskPojo);
            LOG.info("TaskOperationController-checkParentsSuccess, task id = {}, result = {}", taskPojo.getId(), result);
            return Response.success(result);
        } catch (Exception e) {
            LOG.error("TaskOperationController-checkParentsSuccess Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 如果是父任务不成功的任务，
     * 就把他们的延迟策略修改为父任务延迟恢复，同时刷新子任务中时间依赖的时钟
     *
     * @param  taskId
     * @return
     */
    @PostMapping(value = "/updateTaskChildrensDelayStatusAndRefreshSlots")
    public Map<String, Object> updateTaskChildrensDelayStatusAndRefreshSlots(@RequestParam(value = "taskId") Integer taskId,
                                                                             @RequestParam(value = "taskRerunType") Integer taskRerunType) {
        try {
            if (taskId == null) {
                return Response.fail("param taskId can't be empty.");
            }
            boolean result = taskOperationService.updateTaskChildrensDelayStatusAndRefreshSlots(taskId, taskRerunType);
            if (result) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("updateTaskChildrensDelayStatusAndRefreshSlots Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
