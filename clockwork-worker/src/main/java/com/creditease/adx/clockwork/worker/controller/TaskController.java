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

package com.creditease.adx.clockwork.worker.controller;

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.worker.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Task任务处理Controller
 */
@RestController
@RequestMapping("/clockwork/worker/task")
public class TaskController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;


    /**
     * 检查任务进程
     *
     * @param taskLogPojo
     * @return
     */
    @PostMapping(value = "/checkRunningTask")
    public Map<String, Object> checkRunningTaskLog(@RequestBody TbClockworkTaskLogPojo taskLogPojo) {
        try {
            // 参数
            if (taskLogPojo == null || taskLogPojo.getId() == null) {
                LOG.error("TaskController-checkRunningTask, invalid taskLog");
                return Response.fail("invalid taskLog");
            }
            boolean result = taskService.checkRunningTaskLogIsExist(taskLogPojo);
            LOG.info("TaskController-checkRunningTask, taskLog = {}, result = {}", taskLogPojo, result);
            return Response.success(result);
        } catch (Exception e) {
            LOG.error("TaskController-checkRunningTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
