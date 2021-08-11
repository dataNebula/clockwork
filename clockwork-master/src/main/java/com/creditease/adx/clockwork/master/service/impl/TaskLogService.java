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

package com.creditease.adx.clockwork.master.service.impl;

import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.creditease.adx.clockwork.master.service.ITaskLogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(value = "taskLogService")
public class TaskLogService implements ITaskLogService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogService.class);

    @Autowired
    private RestTemplateClient restTemplateClient;

    /**
     * 判断进程是否存在
     *
     * @param taskLog         task log
     * @param tbClockworkNode node
     * @return
     */
    @Override
    public boolean isExistRunningProcess(TbClockworkTaskLogPojo taskLog, TbClockworkNode tbClockworkNode) {
        if (tbClockworkNode == null) {
            return false;
        }

        String url = String.format("http://%s:%s/clockwork/worker/task/checkRunningTask",
                tbClockworkNode.getIp(), tbClockworkNode.getPort());

        Map<String, Object> interfaceResult = restTemplateClient.getResult(url, taskLog);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return false;
        }
        boolean result = (boolean) interfaceResult.get(Constant.DATA);
        LOG.info("check task process exist result = {}, task id = {}, task pid = {}",
                result, taskLog.getTaskId(), taskLog.getPid());

        return result;
    }

}
