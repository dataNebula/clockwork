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

import java.io.File;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.client.service.TaskLogClientService;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:32 2019-11-29
 * @ Description：任务日志服务类
 * @ Modified By：
 */
@Service
public class TaskLogService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogService.class);

    @Resource(name = "taskLogClientService")
    private TaskLogClientService taskLogClientService;

    @Value("${task.run.log.dir}")
    protected String taskRunLogDir;

    @PostConstruct
    public void init() {
        taskRunLogDir = taskRunLogDir.endsWith(File.separator) ? taskRunLogDir : taskRunLogDir + File.separator;
    }

    /**
     * 创建任务执行日志文件
     *
     * @param task
     * @param logId
     * @return
     */
    public String touchTaskExecuteLogFile(TbClockworkTaskPojo task, int logId) {
        String logName = null;
        File taskLogFile = null;
        boolean result = false;
        try {
            logName = task.getName() + "_" + DateUtil.formatDateToString(new Date(), DateUtil.DATE_KEY_STR_FULL);
            taskLogFile = new File(taskRunLogDir +
                    DateUtil.formatDate(new Date(), DateUtil.DATE_STD_STR) + File.separator + logName + ".log");
            FileUtils.touch(taskLogFile);
            taskLogClientService.updateTaskLogLogName(logId, logName);
            result = true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("[BaseTaskExecuteService]touch task execute log file result = {}, log file path = {}, task id = {}",
                result ? "success" : "failure", taskLogFile == null ? "" : taskLogFile.getAbsolutePath(), task.getId());
        return logName;
    }

}
