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

package com.creditease.adx.clockwork.master.controller;

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.master.service.ITaskDistributeService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:34 2020/9/9
 * @ Description：stop
 * @ Modified By：
 */
@Api("任务停止相关接口")
@RestController
@RequestMapping("/clockwork/master/task/stop")
public class TaskStopController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskStopController.class);

    @Autowired
    private ITaskDistributeService taskDistributeService;

    /**
     * 移除Master队列中的任务
     *
     * @param taskId 任务id
     * @param isAuto auto
     * @return
     */
    @PostMapping(value = "/removeTaskFromWaitForDistributeQueue")
    public Map<String, Object> removeTaskFromNeedBeExecutedQueue(@RequestParam(value = "taskId") Integer taskId,
                                                                 @RequestParam(value = "isAuto") Boolean isAuto) {
        try {
            if (taskId == null) {
                LOG.error("TaskStopController-removeTaskFromWaitForDistributeQueue taskId is null.");
                return Response.fail("taskId is null.");
            }

            // 需要从submitInfoWaitForDistributeQueue队列中移除
            int count = taskDistributeService.removeTaskFromWaitForDistributeQueue(taskId);
            LOG.info("[TaskStopController]removeTaskFromWaitForDistributeQueue taskId = {}, count = {}", taskId, count);

            // 最终不存在队列中
            if (count == 0) {
                LOG.info("[removeTaskFromWaitForDistributeQueue]Does not exist in the queue, " +
                        "taskId = {}, isAuto = {}", taskId, isAuto);
            }
            return Response.success(count);
        } catch (Exception e) {
            LOG.error("[removeTaskFromWaitForDistributeQueue] Error. msg: {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
