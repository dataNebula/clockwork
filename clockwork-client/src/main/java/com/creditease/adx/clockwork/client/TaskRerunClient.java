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
public interface TaskRerunClient {

    @GetMapping(value = "/clockwork/api/task/rerun/getTaskRerunChild")
    Map<String, Object> getTaskRerunChild(
            @RequestParam(value = "taskId") Integer taskId, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber);

    @GetMapping(value = "/clockwork/api/task/rerun/getTaskRerunFather")
    Map<String, Object> getTaskRerunFather(
            @RequestParam(value = "taskId") Integer taskId, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber);

    @GetMapping(value = "/clockwork/api/task/rerun/getTaskRerunFatherIds")
    Map<String, Object> getTaskRerunFatherIds(
            @RequestParam(value = "taskId") Integer taskId, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber);

    @GetMapping(value = "/clockwork/api/task/rerun/getTaskRerunByTaskIds")
    Map<String, Object> getTaskRerunByTaskIds(
            @RequestParam(value = "ids") List<Integer> ids, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber);

    @GetMapping(value = "/clockwork/api/task/rerun/getTaskRerunByTaskId")
    Map<String, Object> getTaskRerunByTaskId(
            @RequestParam(value = "id") Integer id, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber);

    @GetMapping(value = "/clockwork/api/task/rerun/getTaskRerunRootTaskIds")
    Map<String, Object> getTaskRerunRootTaskIds(@RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber);

    /**
     * 分页查询接口
     *
     * @param pageParam pageParam
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/rerun/searchTaskRerunPageList")
    Map<String, Object> searchTaskRerunPageList(@RequestBody PageParam pageParam);

}
