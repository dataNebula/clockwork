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

package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.ITaskLogFileService;
import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.common.entity.LogFileParam;
import com.creditease.adx.clockwork.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(value = "taskLogFileService")
public class TaskLogFileService implements ITaskLogFileService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogFileService.class);

    @Autowired
    private RestTemplateClient restTemplateClient;

    /**
     * 获取日志文件内容
     *
     * @param logFileParam param
     * @return
     */
    public Map<String, Object> catLogFileContent(LogFileParam logFileParam) {

        // 获取日志路径
        String URL = String.format("http://%s:%s/clockwork/worker/task/log/catLogFileContent",
                logFileParam.getNodeIp(), logFileParam.getNodePort());
        try {
            // 2020-11-24
            if (StringUtils.isNotBlank(logFileParam.getCreateTime())
                    && logFileParam.getCreateTime().length() > 10) {
                // TimeFormat
                logFileParam.setCreateTime(
                        DateUtil.getTimeByFormat(logFileParam.getCreateTime(), DateUtil.DATE_FULL_STR, DateUtil.DATE_STD_STR));
            }
        } catch (Exception e) {
            LOG.info("[TaskLogFileService-catLogFileContent] formatted createTime = {} Error. {}.",
                    logFileParam.getCreateTime(), e.getMessage(), e);
        }
        Map<String, Object> result = restTemplateClient.getResult(URL, logFileParam);
        LOG.info("[TaskLogFileService-catLogFileContent]worker node URL = {}, logFileParam = {}", URL, logFileParam);
        return result;
    }

}
