package com.creditease.adx.clockwork.client;
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

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:52 下午 2020/8/6
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface TaskSubmitClient {


    /**
     * 重启任务（自己、所有子节点不包括自己、所有子节点包括自己）
     *
     * @param taskId        任务id
     * @param taskReRunType 重启类型
     * @param parameter     参数
     * @param operatorName  操作人
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/submit/rerunTask")
    Map<String, Object> submitReRunTask(@RequestParam(value = "taskId") Integer taskId,
                                        @RequestParam(value = "taskReRunType", required = false) Integer taskReRunType,
                                        @RequestParam(value = "parameter", required = false) String parameter,
                                        @RequestParam(value = "operatorName", required = false) String operatorName);


    /**
     * 重启历史运行任务（单个任务）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:normal,1:routine,0:rerun,2:fill_data]
     * @param logId         历史日志ID
     * @param parameter     参数
     * @param operatorName  操作人
     * @return task
     */
    @PostMapping(value = "/clockwork/api/task/submit/rerunTaskHis")
    Map<String, Object> submitReRunTaskHis(@RequestParam(value = "taskId") Integer taskId,
                                           @RequestParam(value = "taskReRunType", required = false) Integer taskReRunType,
                                           @RequestParam(value = "logId", required = false) Integer logId,
                                           @RequestParam(value = "parameter", required = false) String parameter,
                                           @RequestParam(value = "operatorName", required = false) String operatorName);

    /**
     * 重启任务根据 dag id
     *
     * @param dagId        dagId
     * @param parameter    参数
     * @param operatorName 操作人
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/submit/rerunTaskByDagId")
    Map<String, Object> submitReRunTaskByDagId(@RequestParam(value = "dagId") Integer dagId,
                                               @RequestParam(value = "parameter", required = false) String parameter,
                                               @RequestParam(value = "operatorName", required = false) String operatorName);


    /**
     * 重启任务根据 group id
     *
     * @param groupId      group id
     * @param parameter    参数
     * @param operatorName 操作人
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/submit/rerunTaskByGroupId")
    Map<String, Object> submitReRunTaskByGroupId(@RequestParam(value = "groupId") Integer groupId,
                                                 @RequestParam(value = "parameter", required = false) String parameter,
                                                 @RequestParam(value = "operatorName", required = false) String operatorName);

}
