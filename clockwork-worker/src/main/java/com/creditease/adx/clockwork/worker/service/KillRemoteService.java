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

package com.creditease.adx.clockwork.worker.service;

import com.creditease.adx.clockwork.common.enums.TaskRunEngine;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service(value = "killRemoteService")
public class KillRemoteService {

    private static final Logger LOG = LoggerFactory.getLogger(KillRemoteService.class);

    @Value("${task.run.log.dir}")
    private String taskRunLogDir;

    @PostConstruct
    public void init() {
        taskRunLogDir = taskRunLogDir.endsWith(File.separator) ? taskRunLogDir : taskRunLogDir + File.separator;
    }

    /**
     * kill hive && yarn
     *
     * @param taskLog
     */
    public void killProcess(TbClockworkTaskLogPojo taskLog) {

        if (StringUtils.isBlank(taskLog.getRunEngine())) {
            return;
        }

        String logDir = taskRunLogDir + DateUtil.formatDate(taskLog.getCreateTime()) + File.separator + taskLog.getLogName() + ".log";

        // 构建 kill Commands
        List<String> commands = new ArrayList<>();

        // 杀掉hive的任务
        if (TaskRunEngine.HIVE.getCode().equals(taskLog.getRunEngine())) {
            /*
             * e.g Kill Command = /usr/local/hadoop/bin/hadoop task -kill task_1545832287495_7170
             * hive 任务类型查杀，先杀hive yarn 上的资源，还要将本地的进程资源杀掉
             */
            List<String> linesList = new ArrayList<>();
            try (Stream<String> lines = Files.lines(Paths.get(logDir))) {
                linesList = lines.filter(line -> line.contains("Kill Command")).collect(Collectors.toList());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            for (String hiveKillCommandStr : linesList) {
                commands.add(hiveKillCommandStr.split("=")[1]);
            }

        }
        // 杀掉MoonBox的任务
        else if (TaskRunEngine.MOON_BOX.getCode().equals(taskLog.getRunEngine())) {
            List<String> linesList = new ArrayList<>();
            try (Stream<String> lines = Files.lines(Paths.get(logDir))) {
                linesList = lines.filter(line -> line.contains("YarnApplicationId: application")).collect(Collectors.toList());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            for (String hiveKillCommandStr : linesList) {
                commands.add("yarn application -kill " + hiveKillCommandStr.split(": ")[1]);
            }
        }

        if (commands.size() > 0) for (String command : commands) {
            String[] cmd = {"/bin/sh", "-c", command};
            ProcessBuilder builder = new ProcessBuilder(cmd);
            try {
                LOG.info("[killProcess] Kill {} task Command = {}, execute begin....", taskLog.getRunEngine(), command);
                builder.start();
                LOG.info("[killProcess] Kill {} task Command = {}, execute success!", taskLog.getRunEngine(), command);
            } catch (Exception e) {
                LOG.error("[killProcess]Kill {} task Command = {}, execute failure! Error {}.",
                        taskLog.getRunEngine(), command, e.getMessage(), e);
            }
            return;
        }
        LOG.info("[KillRemoteServiceImpl]do not need to kill remote task resources,because task run engine = {}," +
                "task id = {}, log id = {}", taskLog.getRunEngine(), taskLog.getTaskId(), taskLog.getId());
    }
}
