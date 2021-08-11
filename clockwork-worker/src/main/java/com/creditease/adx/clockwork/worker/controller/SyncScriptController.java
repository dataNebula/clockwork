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

import com.creditease.adx.clockwork.client.service.DfsClientService;
import com.creditease.adx.clockwork.common.entity.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 任务同步脚本
 */
@RestController
@RequestMapping("/clockwork/worker/sync")
public class SyncScriptController {

    private static final Logger LOG = LoggerFactory.getLogger(SyncScriptController.class);

    @Resource(name = "dfsClientService")
    protected DfsClientService dfsClientService;

    @Value("${node.synchronize.files}")
    protected Boolean synchronizeFiles;

    /**
     * 同步脚本文件
     *
     * @param fileAbsolutePath filePath
     * @return
     */
    @PostMapping(value = "/syncScriptFile")
    public Map<String, Object> syncScriptFile(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath) {
        long startTime = System.currentTimeMillis();
        try {
            // 查看配置，是否同步文件
            if (!synchronizeFiles) {
                LOG.info("[SyncScriptController-syncScriptFile]skip sync file  = {}", fileAbsolutePath);
                return Response.success(true);
            }

            // 同步文件
            boolean result = dfsClientService.downloadFile2Path(fileAbsolutePath);
            LOG.info("[SyncScriptController-syncScriptFile]sync file result = {}, file absolute path = {}, cost time = {}",
                    result, fileAbsolutePath, System.currentTimeMillis() - startTime);
            return result ? Response.success(true) : Response.fail(false);
        } catch (Exception e) {
            LOG.error("[SyncScriptController-syncScriptFile]file absolute path = {}, cost time = {}, sync Error {}.",
                    fileAbsolutePath, System.currentTimeMillis() - startTime, e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
