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

package com.creditease.adx.clockwork.client;

import com.creditease.adx.clockwork.common.entity.PageParam;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "${api.service.name}")
public interface TaskClient {

    @GetMapping(value = "/clockwork/api/task/getTaskStatusById")
    Map<String, Object> getTaskStatusById(@RequestParam(value = "taskId") Integer taskId);

    @GetMapping(value = "/clockwork/api/task/getDependencyScriptFileByTaskId")
    Map<String, Object> getDependencyScriptFileByTaskId(@RequestParam(value = "taskId") Integer taskId);

    @GetMapping(value = "/clockwork/api/task/getTaskById")
    Map<String, Object> getTaskById(@RequestParam(value = "taskId") Integer taskId);

    @GetMapping(value = "/clockwork/api/task/getTaskByTaskIds")
    Map<String, Object> getTaskByTaskIds(@RequestParam(value = "ids") List<Integer> ids);

    /**
     * 根据状态查询task
     *
     * @param status task status
     */
    @GetMapping(value = "/clockwork/api/task/getTaskByStatus")
    Map<String, Object> getTaskByStatus(@RequestParam(value = "status") String status);

    @GetMapping(value = "/clockwork/api/task/getTaskOnlineByStatus")
    Map<String, Object> getTaskOnlineByStatus(@RequestParam(value = "status") String status);

    @GetMapping(value = "/clockwork/api/task/getTaskByRunFailedStatus")
    Map<String, Object> getTaskByRunFailedStatus(@RequestParam(value = "status") List<String> status,
                                                 @RequestParam(value = "beforeDateStr", required = false) String beforeDateStr,
                                                 @RequestParam(value = "currentDateStr", required = false) String currentDateStr);

    @GetMapping(value = "/clockwork/api/task/getTaskListByStatusList")
    Map<String, Object> getTaskListByStatusList(@RequestParam(value = "statusList") List<String> statusList);

    /**
     * 根据dagId查询taskList
     *
     * @param dagId dagId
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/getTasksByDagId")
    Map<String, Object> getTasksByDagId(@RequestParam(value = "dagId") int dagId);

    /**
     * 根据dagId查询taskList（不包括dds）
     *
     * @param dagId dagId
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/getTasksNotIncludeDDSByDagId")
    Map<String, Object> getTasksNotIncludeDDSByDagId(@RequestParam(value = "dagId") int dagId);

    /**
     * 根据source 获取到所有的dagId
     *
     * @param source source
     * @return dagId list
     */
    @GetMapping(value = "/clockwork/api/task/getTaskDagIdsBySource")
    Map<String, Object> getTaskDagIdsBySource(@RequestParam(value = "source") Integer source);

    /**
     * 根据source 获取到所有的dagId，并且存在dag任务跨source
     *
     * @param source source
     * @return dagId list
     */
    @GetMapping(value = "/clockwork/api/task/getTaskDagIdsByCrossSource")
    Map<String, Object> getTaskDagIdsByCrossSource(@RequestParam(value = "source") Integer source);

    /**
     * 查询延迟任务
     *
     * @param delayStatus
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/getTaskByDelayStatus")
    Map<String, Object> getTaskByDelayStatus(@RequestParam(value = "delayStatus") int delayStatus);

    /**
     * 上传文件前缀
     */
    @GetMapping(value = "/clockwork/api/task/uploadPathPrefix")
    Map<String, Object> getTaskUploadPathPrefix();

    /**
     * 获得任务列表详细信息(分页)
     *
     * @param
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/searchPageListTask")
    Map<String, Object> searchPageListTask(@RequestBody PageParam pageParam);
}
