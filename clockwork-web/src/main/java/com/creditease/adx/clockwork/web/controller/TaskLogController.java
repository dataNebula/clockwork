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

import com.creditease.adx.clockwork.client.service.TaskLogClientService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.TaskLogSearchPageEntity;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.web.service.IUserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Api("任务日志")
@RestController
@RequestMapping("/clockwork/web/task/log")
public class TaskLogController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogController.class);

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();

    @Resource(name = "userService")
    private IUserService userService;

    @Autowired
    private TaskLogClientService taskLogClientService;

    /**
     * 获取任务当前日志文件参数（最新任务日志文件对应的参数）
     *
     * @param taskId task primary key
     */
    @GetMapping(value = "/getLatestTaskLogFileParamByTaskId")
    public Map<String, Object> getLatestTaskLogFileParamByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        return taskLogClientService.getTaskLogClient().getLatestTaskLogFileParamByTaskId(taskId);
    }


    /**
     * 分页查询TaskLog Page
     *
     * @param pageParam pageParam
     */
    @PostMapping(value = "/searchPageTaskLogList")
    public Map<String, Object> searchPageTaskLogList(@RequestBody PageParam pageParam) {
        try {
            TaskLogSearchPageEntity searchPage = gson.fromJson(pageParam.getCondition(), TaskLogSearchPageEntity.class);

            if (StringUtils.isBlank(pageParam.getUserName())) {
                throw new RuntimeException("userName field is null that value must be user name info！");
            }

            // 赋予查询角色权限，无角色查询自己创建的，管理员查询所有，非管理员查询该用户角色下所有用户公开的任务
            TbClockworkUserPojo userPojo = userService.getUserAndRoleByUserName(pageParam.getUserName());
            if (userPojo == null) {
                searchPage.setCreateUser(pageParam.getUserName());
            } else if (!userPojo.getIsAdmin()) {
                searchPage.setRoleName(userPojo.getRoleName());
            }
            pageParam.setCondition(gson.toJson(searchPage));
            return taskLogClientService.getTaskLogClient().searchPageList(pageParam);
        } catch (Exception e) {
            LOG.error("[TaskLogController-searchPageTaskLogList], Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
