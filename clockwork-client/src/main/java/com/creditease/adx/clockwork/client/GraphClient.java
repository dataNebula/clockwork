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

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "${api.service.name}")
public interface GraphClient {

    @GetMapping(value = "/clockwork/api/graph/getGraphAllRelationByTaskId")
    Map<String, Object> getGraphAllRelationByTaskId(@RequestParam(value = "taskId") Integer taskId);

    @GetMapping(value = "/clockwork/api/graph/getGraphAllTaskIdsByTaskId")
    Map<String, Object> getGraphAllTaskIdsByTaskId(@RequestParam(value = "taskId") Integer taskId);

    @GetMapping(value = "/clockwork/api/graph/getGraphAllTasksByTaskId")
    Map<String, Object> getGraphAllTasksByTaskId(@RequestParam(value = "taskId") Integer taskId);
}
