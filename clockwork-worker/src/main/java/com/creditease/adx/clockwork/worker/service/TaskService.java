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

import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusParam;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:59 2019-11-28
 * @ Description：
 * @ Modified By：
 */
@Service
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    @Resource(name = "taskOperationClientService")
    private TaskOperationClientService taskOperationClientService;

    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
    private LinuxService linuxService;

    @Value("${dfs.service.name}")
    private String dfsServiceName;

    /**
     * 更改任务状态
     *
     * @param tasks
     * @param status
     * @return
     */
    public boolean updateTaskStatusBatch(List<TbClockworkTaskPojo> tasks, String status) {
        //更改状态到提交
        BatchUpdateTaskStatusParam batchUpdateTaskStatusParam = new BatchUpdateTaskStatusParam();
        batchUpdateTaskStatusParam.setTaskIds(findTaskIds(tasks));
        batchUpdateTaskStatusParam.setStatus(status);
        taskOperationClientService.updateTaskStatusBatch(batchUpdateTaskStatusParam);
        return true;
    }

    /**
     * 获取id
     *
     * @param taskPojoList
     * @return
     */
    public List<Integer> findTaskIds(List<TbClockworkTaskPojo> taskPojoList) {
        List<Integer> result = new ArrayList<>();
        for (TbClockworkTaskPojo sysTbClockworkTaskPojo : taskPojoList) {
            result.add(sysTbClockworkTaskPojo.getId());
        }
        return result;
    }

    public String getRuntimeDirClientUrl() {
        String ip = null;
        int port = 0;
        Applications applications = eurekaClient.getApplications();
        Application applicationEureka = applications.getRegisteredApplications(dfsServiceName);
        List<InstanceInfo> instanceInfos = applicationEureka.getInstances();
        for (InstanceInfo instanceInfo : instanceInfos) {
            ip = instanceInfo.getIPAddr();
            port = instanceInfo.getPort();
            LOG.info("getRuntimeDirClientUrl method :::::runtimeDirClientUrl ip:port==" + ip + ":" + port);
            if (ip != null && !"".equals(ip) && port != 0) {
                break;
            }
        }
        return ip + ":" + port;
    }

    /**
     * 发现进程不存在则返回true，反之true
     *
     * @param taskLog
     * @return
     */
    public boolean checkRunningTaskLogIsExist(TbClockworkTaskLogPojo taskLog) {
        Set<String> pids = linuxService.getPidsByCommand(taskLog.getRealCommand());
        // 没有发现pid，则任务进程已经不存在
        if (pids == null || pids.size() < 1) {
            return false;
        }
        for (String p : pids) {
            LOG.info("[checkRunningTaskLog] found pid = {}, run script = {}, task id = {}, target pid = {}",
                    p, taskLog.getRealCommand(), taskLog.getTaskId(), taskLog.getPid());
        }
        return true;
    }
}
