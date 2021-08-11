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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.creditease.adx.clockwork.common.entity.LogFileParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.common.entity.Response;

/**
 * TaskLog 处理器
 */
@RestController
@RequestMapping("/clockwork/worker/task/log")
public class TaskLogController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogController.class);

    @Value("${task.run.log.dir}")
    private String taskRunLogDir;

    @PostConstruct
    public void init() {
        taskRunLogDir = taskRunLogDir.endsWith(File.separator) ? taskRunLogDir : taskRunLogDir + File.separator;
    }

    /**
     * 按行信息返回task日志
     *
     * @param logFileParam logFileParam json
     */
    @PostMapping(value = "/catLogFileContent")
    public Map<String, Object> catLogFileContent(@RequestBody LogFileParam logFileParam) {
        List<String> data = new ArrayList<>();
        String logFilePath = taskRunLogDir + logFileParam.getCreateTime() + File.separator + logFileParam.getLogName() + ".log";

        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            data.add("log file [" + logFilePath + "] not exist!");
            return Response.success(data);
        }

        try (Stream<String> lines = Files.lines(Paths.get(logFilePath))) {
            data = lines.skip(logFileParam.getOffset()).limit(logFileParam.getSize()).collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("[TaskLogController][catLogFileContent]logFilePath = {},offset = {},rows = {},resultLogSize = {}",
                logFilePath,
                logFileParam.getOffset(),
                logFileParam.getSize(),
                data.size());
        return Response.success(data);
    }


}
