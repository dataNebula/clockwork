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

import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.api.service.IUserService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.enums.TaskSource;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.enums.TaskTakeEffectStatus;
import com.creditease.adx.clockwork.common.pojo.DDSAndDataWorkTaskInfoPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("任务获取相关接口")
@RestController
@RequestMapping("/clockwork/api/task")
public class TaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();


    @Resource(name = "taskService")
    private ITaskService taskService;

    @Resource(name = "userService")
    private IUserService userService;

    @Value("${task.upload.path.prefix}")
    private String[] uploadPathPrefix = null;

    /**
     * 获取指定任务组下task
     *
     * @param taskGroupId taskGroup id
     */
    @GetMapping(value = "/getTaskByTaskGroupId")
    public Map<String, Object> getTaskByTaskGroupId(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        try {
            if (taskGroupId == null || taskGroupId < 1) {
                LOG.error("getTaskByTaskGroupId, invalid taskGroupId");
                return Response.fail("invalid taskGroupId");
            }
            LOG.info("getTaskByTaskGroupId, taskGroupId = {}", taskGroupId);
            return Response.success(taskService.getTaskByTaskGroupId(taskGroupId));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据主键查询Task
     *
     * @param taskId id
     */
    @GetMapping(value = "/getTaskById")
    public Map<String, Object> getTaskById(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                LOG.error("Get task by id failure,task id is null");
                return Response.fail("task id is null");
            }
            return Response.success(taskService.getTaskById(taskId));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据task ids查询Task
     *
     * @param ids task ids
     */
    @GetMapping(value = "/getTaskByTaskIds")
    public Map<String, Object> getTaskByTaskIds(@RequestParam(value = "ids") List<Integer> ids) {
        try {
            if (CollectionUtils.isEmpty(ids)) {
                LOG.error("Parameter exception,task ids is null");
                return Response.fail("task ids is null");
            }

            return Response.success(taskService.getTaskByTaskIds(ids));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 根据name查询Task
     *
     * @param name name
     */
    @GetMapping(value = "/getTaskByName")
    public Map<String, Object> getTaskByName(@RequestParam(value = "name") String name) {
        try {
            if (StringUtils.isBlank(name)) {
                LOG.error("Get task by name failure,task name is null");
                return Response.fail("task name is null");
            }
            return Response.success(taskService.getTaskByName(name));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据taskNames查询task
     *
     * @param names task name
     */
    @GetMapping(value = "/getTaskByNames")
    public Map<String, Object> getTaskByNames(@RequestParam(value = "names") List<String> names) {
        try {
            if (CollectionUtils.isEmpty(names)) {
                LOG.error("Parameter exception, task names is null");
                return Response.fail("task names is null");
            }
            return Response.success(taskService.getTaskByNames(names));
        } catch (Exception e) {
            LOG.error("getTaskByNames Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据状态查询task
     *
     * @param status task status
     */
    @GetMapping(value = "/getTaskByStatus")
    public Map<String, Object> getTaskByStatus(@RequestParam(value = "status") String status) {
        try {
            LOG.info("getTaskByStatus, status = {}", status);
            return Response.success(taskService.getTaskByStatus(status));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据状态查询online task
     *
     * @param status task status
     */
    @GetMapping(value = "/getTaskOnlineByStatus")
    public Map<String, Object> getTaskOnlineByStatus(@RequestParam(value = "status") String status) {
        try {
            LOG.info("getTaskOnlineByStatus, status = {}", status);
            return Response.success(taskService.getTaskOnlineByStatus(status));
        } catch (Exception e) {
            LOG.error("getTaskOnlineByStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据失败状态查询一段时间内失败的task
     *
     * @param status task status
     */
    @GetMapping(value = "/getTaskByRunFailedStatus")
    public Map<String, Object> getTaskByRunFailedStatus(@RequestParam(value = "status") List<String> status,
                                                        @RequestParam(value = "beforeDateStr", required = false) String beforeDateStr,
                                                        @RequestParam(value = "currentDateStr", required = false) String currentDateStr) {
        try {
            if (CollectionUtils.isEmpty(status)) {
                LOG.error("getTaskByRunFailedStatus status is null");
                return Response.fail("getTaskByRunFailedStatus status is null");
            }
            LOG.info("getTaskOnlineByStatus, status = {}, beforeDateStr = {}, currentDateStr ={}.",
                    status, beforeDateStr, currentDateStr);
            return Response.success(taskService.getTaskByRunFailedStatus(status, beforeDateStr, currentDateStr));
        } catch (Exception e) {
            LOG.error("getTaskOnlineByStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据delayStatus查询taskList
     *
     * @param delayStatus delay status
     */
    @GetMapping(value = "/getTaskByDelayStatus")
    public Map<String, Object> getTaskByDelayStatus(@RequestParam(value = "delayStatus") Integer delayStatus) {
        try {
            if (delayStatus == null) {
                LOG.error("Parameter exception, delayStatus is null");
                return Response.fail("delayStatus is null");
            }
            return Response.success(taskService.getTaskByDelayStatus(delayStatus));
        } catch (Exception e) {
            LOG.error("getTaskByDelayStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据dagId 查询all task
     *
     * @param dagId dagId
     */
    @GetMapping(value = "/getTasksByDagId")
    public Map<String, Object> getTasksByDagId(@RequestParam(value = "dagId") int dagId) {
        try {
            return Response.success(taskService.getTasksByDagId(dagId));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据dagId 查询all task（不包括dds）
     *
     * @param dagId dagId
     */
    @GetMapping(value = "/getTasksNotIncludeDDSByDagId")
    public Map<String, Object> getTasksNotIncludeDDSByDagId(@RequestParam(value = "dagId") int dagId) {
        try {
            return Response.success(taskService.getTasksNotIncludeDDSByDagId(dagId));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据source 获取到所有的dagId
     *
     * @param source source
     * @return dagId list
     */
    @GetMapping(value = "/getTaskDagIdsBySource")
    public Map<String, Object> getTaskDagIdsBySource(@RequestParam(value = "source") Integer source) {
        try {
            if( source == null ){
                return Response.fail("The source is null.");
            }
            return Response.success(taskService.getTaskDagIdsBySource(source));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据source 获取到所有的dagId，并且存在dag任务跨source
     *
     * @param source source
     * @return dagId list
     */
    @GetMapping(value = "/getTaskDagIdsByCrossSource")
    public Map<String, Object> getTaskDagIdsByCrossSource(@RequestParam(value = "source") Integer source) {
        try {
            if( source == null || TaskSource.DDS_2.getValue() != source.intValue()){
                return Response.fail("The source is not recognized.");
            }
            return Response.success(taskService.getTaskDagIdsByCrossSource(source));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询当前task的状态
     *
     * @param taskId taskId
     */
    @GetMapping(value = "/getTaskStatusById")
    public Map<String, Object> getTaskStatusById(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                LOG.error("getTaskStatusById, invalid taskId");
                return Response.fail("invalid taskId");
            }
            LOG.info("getTaskStatusById, taskId = {}", taskId);
            return Response.success(taskService.getTaskStatusById(taskId));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("查询Task状态按中台表名")
    @GetMapping(value = "/getTaskStatusByTableName")
    public Map<String, Object> getTaskStatusByTableName(
            @RequestParam(value = "businessInfo", required = false) String businessInfo) {
        try {
            return Response.success(taskService.getTaskStatusByTableName(businessInfo));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("通过作业状态按表名")
    @GetMapping(value = "/getTaskIdsByTableNames")
    public Map<String, Object> getTaskIdsByTableNames(
            @RequestParam(value = "businessInfo", required = false) String businessInfo) {
        Map<String, Map<Integer, String>> result = null;
        try {
            if (StringUtils.isBlank(businessInfo)) {
                return Response.fail("businessInfo is null");
            }
            result = taskService.getTaskStatusByTableName(businessInfo);
            if (result == null || result.size() < 1) {
                return Response.fail(
                        "[getTaskIdsByTableNames]Not found tasks info that tables name is " + businessInfo);
            }
            return Response.success(result);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("查询Task状态按中台表名,返回数据域为字符串,专门为shell检查依赖的表的状态定制")
    @GetMapping(value = "/getTaskStatusForTableCheck")
    public Map<String, Object> getTaskStatusForTableCheck(
            @RequestParam(value = "businessInfo", required = false) String businessInfo) {
        try {
            if (StringUtils.isBlank(businessInfo)) {
                return Response.fail("businessInfo is null");
            }

            if (businessInfo.contains(",")) {
                return Response.fail("only need a table name");
            }

            Map<String, Map<Integer, String>> result = taskService.getTaskStatusByTableName(businessInfo);

            if (result == null || result.size() < 1) {
                LOG.info("[TaskController-getTaskStatusForTableCheck]" +
                        "[1]No found task info that table name is {}", businessInfo);
                return Response.fail("[TaskController-getTaskStatusForTableCheck]" +
                        "[1]Not found tasks info that table name is " + businessInfo);
            }

            if (result.get(businessInfo) == null || result.get(businessInfo).size() < 1) {
                LOG.info("[TaskController-getTaskStatusForTableCheck]" +
                        "[2]No found task info that table name is {}", businessInfo);
                return Response.fail("[TaskController-getTaskStatusForTableCheck]" +
                        "[2]Not found tasks info that table name is " + businessInfo);
            }
            Map<Integer, String> taskStatusInfo = result.get(businessInfo);

            List<String> taskStatusLog = new ArrayList<>();
            for (Map.Entry<Integer, String> taskStatus : taskStatusInfo.entrySet()) {
                LOG.info("[TaskController-getTaskStatusForTableCheck] task id = {},task status = {},businessInfo = {}",
                        taskStatus.getKey(), taskStatus.getValue(), businessInfo);
                taskStatusLog.add(taskStatus.getKey() + ":" + taskStatus.getValue());
            }

            String businessData = StringUtils.join(taskStatusInfo.values(), ",");
            String taskStatusLogPrint = StringUtils.join(taskStatusLog, ",");

            LOG.info("[TaskController-getTaskStatusForTableCheck] tasks status = {},tasks id and status = {},"
                    + "businessInfo = {}", businessData, taskStatusLogPrint, businessInfo);
            return Response.success(businessData);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取依赖脚本脚本
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/getDependencyScriptFileByTaskId")
    public Map<String, Object> getDependencyScriptFileByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                return Response.fail(" task is invalid ");
            }
            return Response.success(taskService.getDependencyScriptFileByTaskId(taskId));
        } catch (Exception e) {
            LOG.error("getDependencyScriptFileByTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取任务，根据状态列表
     *
     * @param statusList task list
     * @return
     */
    @GetMapping(value = "/getTaskListByStatusList")
    public Map<String, Object> getTaskListByStatusList(@RequestParam(value = "statusList") List<String> statusList) {
        try {
            if (CollectionUtils.isEmpty(statusList)) {
                return Response.fail("statusList is null");
            }
            return Response.success(taskService.getTaskListByStatusList(statusList));
        } catch (Exception e) {
            LOG.error("getDependencyScriptFileByTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询接口
     *
     * @param task 查询参数
     */
    @PostMapping(value = "/getAllTaskByCondition")
    public Map<String, Object> getAllTaskByCondition(@RequestBody(required = false) TbClockworkTask4PagePojo task) {
        try {
            LOG.info("[TaskController-getAllTaskByCondition] pageParam = {}", task != null ? task.toString() : null);

            // 查询数据
            List<TbClockworkTask4PagePojo> tasks = taskService.getAllTaskByCondition(task);
            LOG.info("[TaskController-getAllTaskByCondition] tasks.size = {}", tasks != null ? tasks.size() : 0);
            return Response.success(tasks);
        } catch (Exception e) {
            LOG.error("getAllTaskByCondition Error. msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 自动补全查询接口
     *
     * @param idOrNameSegment
     * @return
     */
    @GetMapping(value = "/getAllTaskUsedByAutoComplete")
    public Map<String, Object> getAllTaskUsedByAutoComplete(@RequestParam(required = true) String idOrNameSegment) {
        try {
            LOG.info("[TaskController-getAllTaskUsedByAutoComplete] idOrNameSegment = {}", idOrNameSegment);
            // 自动补全查询接口查询结果
            List<TbClockworkTask4PagePojo> tasks = taskService.getAllTaskUsedByAutoComplete(idOrNameSegment);
            LOG.info("[TaskController-getAllTaskUsedByAutoComplete] tasks.size = {}", tasks != null ? tasks.size() : 0);
            return Response.success(tasks);
        } catch (Exception e) {
            LOG.error("getAllTaskUsedByAutoComplete Error. msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取所有状态和描述信息
     *
     * @return
     */
    @GetMapping(value = "/getTaskStatus")
    public Map<String, Object> getTaskStatus() {
        try {
            Map<String, String> result = new HashMap<String, String>();
            TaskStatus[] values = TaskStatus.values();
            for (TaskStatus value : values) {
                result.put(value.getValue(), value.getDesc());
            }
            return Response.success(result);
        } catch (Exception e) {
            LOG.error("getTaskStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    @ApiOperation("任务上传文件前缀")
    @GetMapping(value = "/uploadPathPrefix")
    public Map<String, Object> getTaskUploadPathPrefix() {
        return Response.success(uploadPathPrefix);
    }

    /**
     * 获得任务列表详细信息(分页)
     *
     * @param
     * @return
     */
    @ApiOperation("分页查询")
    @PostMapping(value = "/searchPageListTask")
    public Map<String, Object> searchPageListTask(@RequestBody PageParam pageParam){
            try {
            // 参数处理
            TbClockworkTask4PagePojo task = gson.fromJson(pageParam.getCondition(), TbClockworkTask4PagePojo.class);
            int pageNumber = pageParam.getPageNum();
            if (pageNumber < 1) {
                pageNumber = 1;
            }
            int pageSize = pageParam.getPageSize();
            if (pageSize < 10) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            if (task == null) {
                task = new TbClockworkTask4PagePojo();
            }
            if (task.getOnline() != null && task.getOnline() == TaskTakeEffectStatus.ONLINE_AND_OFFLINE.getValue()) {
                task.setOnline(null);
            }

            if (StringUtils.isBlank(pageParam.getUserName())) {
                throw new RuntimeException("userName field is null that value must be user email info.");
            }

            // 赋予查询角色权限，无角色查询自己创建的，管理员查询所有，非管理员查询该用户角色下所有用户公开的任务
            TbClockworkUserPojo userPojo = userService.getUserAndRoleByUserName(pageParam.getUserName());
            if (userPojo == null) {
                task.setCreateUser(pageParam.getUserName());
            } else if (!userPojo.getIsAdmin()) {
                task.setRoleName(userPojo.getRoleName());
            }

            // 查询数据
            int total = taskService.getAllTaskByPageParamCount(task);
            List<TbClockworkTask4PagePojo> tasks = taskService.getAllTaskByPageParam(task, pageNumber, pageSize);

            if (CollectionUtils.isNotEmpty(tasks)) {
                PageInfo<TbClockworkTaskPojo> pageInfo = new PageInfo(tasks);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[TaskController-system-searchPageListTask]" +
                                "dateSize = {}, roleName = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        tasks.size(), userPojo != null ? userPojo.getRoleName() : null, total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkTaskPojo> pageInfo = new PageInfo(new ArrayList<TbClockworkTask4PagePojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[TaskController-system-searchPageListTask]get taskSize = {}, roleName = {}", 0, userPojo != null ? userPojo.getRoleName() : null);
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询接口(dds ,datawork)
     *
     * @param task 查询参数
     */
    @PostMapping(value = "/getDDSAndDataWorkTasks")
    public Map<String, Object> getDDSAndDataWorkTasks(@RequestBody(required = false) TbClockworkTask4PagePojo task) {
      try {
        LOG.info("[TaskController-getDDSAndDataWorkTasks] pageParam = {}", task != null ? task.toString() : null);

        // 查询数据
        List<DDSAndDataWorkTaskInfoPojo> tasks = taskService.getDDSAndDataWorkTasks(task);
        LOG.info("[TaskController-getDDSAndDataWorkTasks] tasks.size = {}", tasks != null ? tasks.size() : 0);
        return Response.success(tasks);
      } catch (Exception e) {
        LOG.error("getDDSAndDataWorkTasks Error. msg:{}", e.getMessage(), e);
        return Response.fail(e.getMessage());
      }
    }


}
