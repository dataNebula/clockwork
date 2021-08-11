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

import com.creditease.adx.clockwork.client.service.TaskLogFileClientService;
import com.creditease.adx.clockwork.common.entity.LogFileParam;
import com.creditease.adx.clockwork.common.entity.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api("任务日志文件")
@RestController
@RequestMapping("/clockwork/web/task/log/file")
public class TaskLogFileController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogFileController.class);

    @Autowired
    private TaskLogFileClientService taskLogFileClientService;

    /**
     * 获取日志文件内容
     *
     * @param logFileParam logFileParam json
     */
    @ApiOperation(value = "获取日志文件内容")
    @PostMapping(value = "/catLogFileContent")
    public Map<String, Object> catLogFileContent(@RequestBody LogFileParam logFileParam) {
        try {
            return taskLogFileClientService.getTaskLogFileClient().catLogFileContent(logFileParam);
        } catch (Exception e) {
            LOG.error("[TaskLogController-catLogFileContent], Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
