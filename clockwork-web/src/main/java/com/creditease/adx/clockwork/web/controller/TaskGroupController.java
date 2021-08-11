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

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.client.service.TaskGroupClientService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import com.creditease.adx.clockwork.common.enums.SystemRole;
import com.creditease.adx.clockwork.common.framework.entity.Pagination;
import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskGroupPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.web.service.ILdapService;
import com.creditease.adx.clockwork.web.service.ITaskGroupService;
import com.creditease.adx.clockwork.web.service.IUserService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:43 下午 2020/8/10
 * @ Description：TaskGroup服务类
 * @ Modified By：
 */
@Api("任务组接口")
@RestController
@RequestMapping("/clockwork/web/task/group")
public class TaskGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskGroupController.class);

    @Resource(name = "taskGroupService")
    private ITaskGroupService taskGroupService;

    @Resource(name = "userService")
    private IUserService userService;

    @Resource(name = "ldapService")
    private ILdapService ldapService;

    @Autowired
    private TaskGroupClientService taskGroupClientService;

    /**
     * 添加taskGroup
     *
     * @param taskGroup json string
     */
    @PostMapping(value = "/addTaskGroup")
    public Map<String, Object> addTaskGroup(@RequestBody TbClockworkTaskGroup taskGroup) {
        try {
            return taskGroupClientService.getTaskOperationClient().addTaskGroup(taskGroup);
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
            return taskGroupClientService.getTaskOperationClient().updateTaskGroup(taskGroup);
        } catch (Exception e) {
            LOG.error("updateTaskGroup Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 使taskGroup本身失效，并将其内部的所有作业任务也一并失效，涉及操作作业状态必须加锁
     * 作业组以及作业下线是，队列中的任务需要移除
     *
     * @param taskGroupId id
     */
    @PostMapping(value = "/disableTaskGroup")
    public Map<String, Object> disableTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        try {
            return taskGroupClientService.getTaskOperationClient().disableTaskGroup(taskGroupId);
        } catch (Exception e) {
            LOG.error("disableTaskGroup Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 使taskGroup生效，并将其内部的所有作业任务也一并生效，涉及操作作业状态必须加锁
     *
     * @param taskGroupId id
     */
    @PostMapping(value = "/enableTaskGroup")
    public Map<String, Object> enableTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        try {
            return taskGroupClientService.getTaskOperationClient().enableTaskGroup(taskGroupId);
        } catch (Exception e) {
            LOG.error("enableTaskGroup Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询taskGroupName是否重复
     *
     * @param taskGroupName taskGroup name
     */
    @PostMapping(value = "/checkTaskGroupName")
    public Map<String, Object> checkTaskGroupName(@RequestParam(value = "taskGroupName") String taskGroupName) {
        try {
            return taskGroupClientService.getTaskOperationClient().checkTaskGroupName(taskGroupName);
        } catch (Exception e) {
            LOG.error("enableTaskGroup Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param taskGroupId id
     * @return
     */
    @PostMapping(value = "/delete")
    public Map<String, Object> deleteTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        try {
            return taskGroupClientService.getTaskOperationClient().deleteTaskGroup(taskGroupId);
        } catch (Exception e) {
            LOG.error("deleteTaskGroup Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据主键查询taskGroup
     *
     * @param taskGroupId taskGroupId
     */
    @GetMapping(value = "/getTaskGroupById")
    public Map<String, Object> getTaskGroupById(@RequestParam(value = "taskGroupId") Integer taskGroupId) {
        if (taskGroupId == null || taskGroupId < 1) {
            LOG.error("getTaskGroupById, invalid taskGroupId");
            return Response.fail("invalid taskGroupId");
        }

        try {
            LOG.info("getTaskGroupById, taskGroupId = {}", taskGroupId);
            return Response.success(taskGroupService.getTaskGroupById(taskGroupId));
        } catch (Exception e) {
            LOG.error("getTaskGroupById taskGroupId = {}, Error {}", taskGroupId, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据作者查询taskGroup
     * 返回该作者所在部门的taskGroup
     */
    @GetMapping(value = "/getTaskGroupIdAndNameByUserName")
    public Map<String, Object> getTaskGroupIdAndNameByUserName(@RequestParam(value = "userName") String userName) {
        if (StringUtils.isBlank(userName)) {
            LOG.error("getTaskGroupIdAndNameByUserName, invalid userName");
            return Response.fail("invalid userName");
        }
        try {
            List<Map<String, Object>> taskGroups = null;
            //获取当前登陆用户的角色，如果是管理员可以看到所有的taskGroup
            String role = userService.getRoleByUserName(userName);
            if (SystemRole.ADMIN.getValue().equals(role)) {
                taskGroups = taskGroupService.getAllTaskGroupIdAndName();
            }
            // 普通用户
            else if (SystemRole.NORMAL.getValue().equals(role)) {
                String userGroupName = ldapService.getOrgInfoByEmail(userName);
                LOG.info("getTaskGroupIdAndNameByUserName, userName={}, userGroupName={}", userName, userGroupName);
                taskGroups = taskGroupService.getTaskGroupIdAndNameByUserGroupName(userName, userGroupName);
            } else {
                taskGroups = new ArrayList<>();
                LOG.warn("Don't support role name = {}, userName = {} ", role, userName);
            }

            LOG.info("getTaskGroupIdAndNameByUserName, taskGroups.size = {}", taskGroups.size());
            return Response.success(taskGroups);
        } catch (Exception e) {
            LOG.error("getTaskGroupIdAndNameByUserName userName = {}, Error {}", userName, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取任务组信息通过查询条件
     * 分页查询
     *
     * @param pageParam 查询条件
     * @return 任务组信息
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @PostMapping(value = "/getAllTaskGroupByUserName")
    public Map<String, Object> getTaskGroupByPageParam(@RequestBody PageParam pageParam) {
        try {
            LOG.info("[TaskGroupController-getTaskGroupByPageParam]pageParam = {}", pageParam != null ? pageParam.toString() : null);
            if (pageParam == null) {
                throw new RuntimeException("Page param is null,please check it!");
            }
            if (StringUtils.isEmpty(pageParam.getUserName())) {
                throw new RuntimeException("userName field is null!");
            }
            // 获取当前登陆用户的角色，如果是管理员可以看到所有的taskGroup
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

            // 设置查询条件
            QueryCriteria<TbClockworkTaskGroupPojo> queryCriteria = new QueryCriteria<>();
            queryCriteria.setPageIndex(pageNumber);
            queryCriteria.setPageSize(pageSize);
            TbClockworkTaskGroupPojo tbClockworkTaskGroupPojo = new TbClockworkTaskGroupPojo();

            if (!StringUtils.isEmpty(pageParam.getCondition())) {
                tbClockworkTaskGroupPojo = JSONObject.parseObject(pageParam.getCondition(), TbClockworkTaskGroupPojo.class);
//                tbClockworkTaskGroupPojo = gson.fromJson(pageParam.getCondition(), TbClockworkTaskGroupPojo.class);
            }

            // 管理员
            String role = userService.getRoleByUserName(pageParam.getUserName());
            if (SystemRole.ADMIN.getValue().equals(role)) {
                LOG.info("[TaskGroupController-system-admin-role-getTaskGroupByPageParam]" +
                        "userRole = {}, userName = {}", role, pageParam.getUserName());
            }
            // 普通用户
            else if (SystemRole.NORMAL.getValue().equals(role)) {
                String userGroupName = ldapService.getOrgInfoByEmail(pageParam.getUserName());
                tbClockworkTaskGroupPojo.setUserGroupName(userGroupName);
                tbClockworkTaskGroupPojo.setUserName(pageParam.getUserName());

                LOG.info("[TaskGroupController-system-normal-role-getTaskGroupByPageParam]" +
                        "userRole  = {}, userName = {}", role, pageParam.getUserName());
            } else {
                LOG.warn("[User Role = {}]userRole do not support,userName = {}", role, pageParam.getUserName());
                return Response.success(new PageInfo(new ArrayList<>()));
            }

            // 执行查询
            queryCriteria.setEntityDto(tbClockworkTaskGroupPojo);
            Pagination<TbClockworkTaskGroupPojo> pagination = taskGroupService.queryPagination(queryCriteria);
            List<TbClockworkTaskGroupPojo> data = pagination.getData();
            if (CollectionUtils.isEmpty(data)) {
                return Response.success(null);
            }

            PageInfo<TbClockworkTaskPojo> pageInfo = new PageInfo(data);
            pageInfo.setPageNum(pageNumber);
            pageInfo.setPageSize(pageSize);
            pageInfo.setPages(Integer.valueOf(String.valueOf(pagination.getTotalPageCount())));
            pageInfo.setTotal(pagination.getTotalCount());

            LOG.info("[TaskGroupController-system-getTaskGroupByPageParam]" +
                            "get dataSize = {}, total = {}, pageSize = {}, pageNumber = {}, pages = {}",
                    pagination.getData().size(),
                    pagination.getTotalCount(),
                    pageSize,
                    pageNumber,
                    pageInfo.getPages());

            return Response.success(pageInfo);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
