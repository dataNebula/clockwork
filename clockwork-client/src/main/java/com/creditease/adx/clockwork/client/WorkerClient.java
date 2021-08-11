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

import com.creditease.adx.clockwork.common.entity.LogFileParam;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-09-05
 */
@FeignClient(value = "${worker.service.name}")
public interface WorkerClient {

    /**
     * 查看任务日志
     */
    @PostMapping(value = "/clockwork/worker/task/log/catLogFileContent")
    Map <String, Object> catLogFileContent(@RequestBody LogFileParam logFileParam);

    /**
     * work节点同步文件
     * @param fileAbsolutePath
     * @return
     */
    @PostMapping(value = "/clockwork/worker/sync/syncScriptFile")
    Map <String, Object> syncScriptFile(@RequestBody String fileAbsolutePath);
}
