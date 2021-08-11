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

import com.creditease.adx.clockwork.client.service.TaskFillDataClientService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.TaskFillDataSearchPageEntity;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.web.service.IUserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:10 2020/9/20
 * @ Description：
 * @ Modified By：
 */
@Api("任务相关依赖接口")
@RestController
@RequestMapping("/clockwork/web/task/fillData")
public class TaskFillDataController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskFillDataController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Resource(name = "userService")
    private IUserService userService;

    @Autowired
    private TaskFillDataClientService taskFillDataClientService;

    @PostMapping(value = "/searchFillDataPageList")
    public Map<String, Object> searchPageList(@RequestBody PageParam pageParam) {
        try{
            if (StringUtils.isBlank(pageParam.getUserName())) {
                throw new RuntimeException("userName field is null that value must be user name info.");
            }

            TaskFillDataSearchPageEntity entity = gson.fromJson(pageParam.getCondition(), TaskFillDataSearchPageEntity.class);

            // 赋予查询角色权限，无角色查询自己创建的，管理员查询所有，非管理员查询该用户角色下所有用户公开的任务
            TbClockworkUserPojo userPojo = userService.getUserAndRoleByUserName(pageParam.getUserName());
            if (userPojo == null) {
                entity.setCreateUser(pageParam.getUserName());
            } else if (!userPojo.getIsAdmin()) {
                entity.setRoleName(userPojo.getRoleName());
            }
            pageParam.setCondition(gson.toJson(entity));
            return taskFillDataClientService.getTaskFillDataClient().searchPageList(pageParam);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }
}
