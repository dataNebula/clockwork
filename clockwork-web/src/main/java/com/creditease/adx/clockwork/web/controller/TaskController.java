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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import com.creditease.adx.clockwork.common.enums.SystemRole;
import com.creditease.adx.clockwork.common.enums.TaskTakeEffectStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.common.util.CronExpression;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.web.service.ILdapService;
import com.creditease.adx.clockwork.web.service.ITaskGroupService;
import com.creditease.adx.clockwork.web.service.ITaskService;
import com.creditease.adx.clockwork.web.service.IUserService;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:43 下午 2020/8/10
 * @ Description：任务相关服务
 * @ Modified By：
 */
@Api("WEB任务获取相关接口")
@RestController
@RequestMapping("/clockwork/web/task")
public class TaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();

    @Resource(name = "taskService")
    private ITaskService taskService;

    @Resource(name = "userService")
    private IUserService userService;

    @Resource(name = "ldapService")
    private ILdapService ldapService;

    @Resource(name = "taskGroupService")
    private ITaskGroupService taskGroupService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    /**
     * 查询taskName是否重复
     *
     * @param taskName task name
     */
    @PostMapping(value = "/checkTaskName")
    public Map<String, Object> checkTaskName(@RequestParam(value = "taskName") String taskName) {
        if (StringUtils.isBlank(taskName)) {
            LOG.error("TaskController-checkTaskName, invalid taskName");
            return Response.fail("invalid taskName");
        }
        try {
            return Response.success(taskService.getCountByTaskName(taskName));
        } catch (Exception e) {
            LOG.error("TaskController-checkTaskName Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 通过触发时间，频率/频率时间类型 生成cron表达式
     * 从M开始，每N小时/分钟...执行F次
     *
     * @param triggerTime  触发时间
     * @param timeType     可选值有：minute、hour、day、week、month、year、century
     * @param runFrequency 频率
     * @return cronExp
     */
    @GetMapping(value = "/createCronExpByTriggerTime")
    public Map<String, Object> createCronExpByTriggerTime(@RequestParam(value = "triggerTime") String triggerTime,
                                                          @RequestParam(value = "timeType") String timeType,
                                                          @RequestParam(value = "runFrequency") Integer runFrequency) {
        if (StringUtils.isBlank(triggerTime) || StringUtils.isBlank(timeType) || runFrequency == null || runFrequency <= 0) {
            LOG.error("TaskController-createCronExpByTriggerTime, invalid params, triggerTime = {}, timeType = {}, runFrequency = {}",
                    triggerTime, timeType, runFrequency);
            return Response.fail("invalid params");
        }
        try {
            Date triggerDateTime = DateUtil.parse(triggerTime);
            return Response.success(CronExpression.createCronExpByTriggerTime(triggerDateTime, timeType, runFrequency));
        } catch (Exception e) {
            LOG.error("TaskController-createCronExpByTriggerTime Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 获取多个任务通过idList
     *
     * @param ids list
     * @return
     */
    @GetMapping(value = "/getTaskByTaskIds")
    public Map<String, Object> getTaskByTaskIds(@RequestParam(value = "ids") List<Integer> ids) {
        try {
            if (CollectionUtils.isEmpty(ids)) {
                LOG.error("TaskController-getTaskByTaskIds, Parameter exception, task ids is null");
                return Response.fail("task ids is null");
            }
            return Response.success(taskService.getTaskByTaskIds(ids));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据主键查询
     *
     * @param taskId id
     */
    @GetMapping(value = "/getTaskByTaskId")
    public Map<String, Object> getTaskByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                LOG.error("TaskController-getTaskByTaskId, Parameter exception, task id is null");
                return Response.fail("task id is null");
            }
            return Response.success(taskService.getTaskById(taskId));
        } catch (Exception e) {
            LOG.error("TaskController-getTaskByTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据主键查询（包含任务组等其他信息）
     *
     * @param taskId id
     */
    @GetMapping(value = "/getTaskJSONObjectById")
    public Map<String, Object> getTaskJSONObjectById(@RequestParam(value = "taskId") Integer taskId) {
        if (taskId == null || taskId < 1) {
            LOG.error("TaskController-getTaskJSONObjectById, invalid taskId");
            return Response.fail("invalid taskId");
        }

        try {
            TbClockworkTaskPojo tbClockworkTaskPojo = taskService.getTaskById(taskId);
            JSONObject taskResultJson = (JSONObject) JSONObject.toJSON(tbClockworkTaskPojo);
            TbClockworkTaskGroup taskGroup = taskGroupService.getTaskGroupById(tbClockworkTaskPojo.getGroupId());
            String taskGroupName = taskGroup != null ? taskGroup.getName() : null;
            taskResultJson.put("taskGroupName", taskGroupName);

            // 格式化时间
            taskResultJson = JSONObject
                    .parseObject(JSONObject.toJSONStringWithDateFormat(taskResultJson, JSON.DEFFAULT_DATE_FORMAT));

            LOG.info("TaskController-getTaskJSONObjectById, taskId = {}, taskGroupId = {}, taskGroupName = {} ",
                    taskId, tbClockworkTaskPojo.getGroupId(), taskGroupName);
            return Response.success(taskResultJson);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据作者查询task 非管理员：返回该作者创建的task和公有task 管理员：返回所有task
     */
    @GetMapping(value = "/getTaskIdAndNameByUserName")
    public Map<String, Object> getTaskIdAndNameByUserName(@RequestParam(value = "userName") String userName) {
        // 参数校验
        if (StringUtils.isBlank(userName)) {
            LOG.error("TaskController-getTaskIdAndNameByUserName, invalid userName");
            return Response.fail("invalid userName");
        }

        List<Map<String, Object>> tasks = null;
        try {
            // 获取当前登陆用户的角色，如果是管理员可以看到所有的task
            String role = userService.getRoleByUserName(userName);
            if (SystemRole.ADMIN.getValue().equals(role)) {
                tasks = taskService.getAllTaskIdAndNameNotInThisId(null);
            }
            // 普通用户
            else if (SystemRole.NORMAL.getValue().equals(role)) {
                String userGroupName = ldapService.getOrgInfoByEmail(userName);
                LOG.info("TaskController-getTaskIdAndNameByUserName, userName = {}, userGroupName = {}",
                        userName, userGroupName);
                tasks = taskService.getTaskIdAndNameByUserGroupName(userName, userGroupName, null);
            } else {
                tasks = new ArrayList<>();
                LOG.warn("Don't support role name = {}, userName = {} ", role, userName);
            }
            LOG.info("TaskController-getTaskIdAndNameByUserName, tasks.size = {}", tasks.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
        return Response.success(tasks);
    }

    /**
     * 获取任务Id和名称，不包括自己
     *
     * @param userName username
     * @param id       不包含的id
     * @return
     */
    @GetMapping(value = "/getTaskIdAndNameByUserNameNotInThisId")
    public Map<String, Object> getTaskIdAndNameByUserNameNotInThisId(@RequestParam(value = "userName") String userName,
                                                                     @RequestParam(value = "id") Integer id) {
        // 参数校验
        if (StringUtils.isBlank(userName) || id == null) {
            LOG.error("TaskController-getTaskIdAndNameByUserNameNotInThisId, invalid userName or id");
            return Response.fail("invalid userName or id");
        }

        List<Map<String, Object>> tasks = null;
        try {
            // 获取当前登陆用户的角色，如果是管理员可以看到所有的task
            String role = userService.getRoleByUserName(userName);
            if (SystemRole.ADMIN.getValue().equals(role)) {
                tasks = taskService.getAllTaskIdAndNameNotInThisId(id);
            }
            // 普通用户
            else if (SystemRole.NORMAL.getValue().equals(role)) {
                String userGroupName = ldapService.getOrgInfoByEmail(userName);
                LOG.info("TaskController-getTaskIdAndNameByUserNameNotInThisId, userName = {}, userGroupName = {}",
                        userName, userGroupName);
                tasks = taskService.getTaskIdAndNameByUserGroupName(userName, userGroupName, id);
            } else {
                tasks = new ArrayList<>();
                LOG.warn("Don't support role name = {}, userName = {} ", role, userName);
            }
            LOG.info("TaskController-getTaskIdAndNameByUserNameNotInThisId, tasks.size = {}", tasks.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
        return Response.success(tasks);
    }

    @ApiOperation("任务上传文件前缀")
    @GetMapping(value = "/uploadPathPrefix")
    public Map<String, Object> getTaskUploadPathPrefix() {
        return taskClientService.getTaskClient().getTaskUploadPathPrefix();
    }


    /**
     * 查询接口
     *
     * @param pageParam 查询参数
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PostMapping(value = "/searchPageListTask")
    public Map<String, Object> searchPageListTask(@RequestBody PageParam pageParam) {
        LOG.info("[TaskController-searchPageListTask]pageParam = {}", pageParam.toString());
        return taskClientService.searchPageListTask(pageParam);
    }

}
