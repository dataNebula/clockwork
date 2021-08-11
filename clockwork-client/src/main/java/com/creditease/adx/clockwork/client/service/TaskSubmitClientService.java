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

package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.TaskDistributeClient;
import com.creditease.adx.clockwork.client.TaskSubmitClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 任务提交服务
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:54 下午 2020/4/1
 * @ Description：Submit Task
 * @ Modified By：
 */
@Service(value = "taskSubmitClientService")
public class TaskSubmitClientService {

    protected static final Logger LOG = LoggerFactory.getLogger(TaskSubmitClientService.class);

    @Autowired
    protected TaskSubmitClient taskSubmitClient;

    @Autowired
    protected TaskDistributeClient taskDistributeClient;

    @Autowired
    protected TaskStateClientService taskStateClientService;

    public TaskSubmitClient getTaskSubmitClient() {
        return taskSubmitClient;
    }

    /**
     * 提交 Api提交任务
     *
     * @param taskSubmitInfo submitInfo
     * @return boolean
     */
    public boolean submitTask(TaskSubmitInfo taskSubmitInfo) {
        // 任务状态处理，任务修改为提交状态Submit, 设置LogId
        taskStateClientService.taskStateSubmitIncludeTask(taskSubmitInfo);

        // 提交任务到Master任务分发器
        return distributeTaskHandle(taskSubmitInfo);
    }

    /**
     * 提交：Worker提交子任务
     *
     * @param parentTaskId        parentTaskId 父任务
     * @param logName             logName 父任务日志文件名
     * @param childTaskSubmitInfo submitInfo 子任务信息
     * @return boolean
     */
    public boolean submitChildTask(Integer parentTaskId, String logName, TaskSubmitInfo childTaskSubmitInfo) {
        boolean result = false;
        try {
            // check
            MDC.put("logFileName", logName);
            if (childTaskSubmitInfo == null || childTaskSubmitInfo.getTaskIds() == null) {
                return false;
            }

            // 任务状态处理，任务修改为提交状态Submit, 设置LogId, 不包含修改任务状态
            taskStateClientService.taskStateSubmitNotIncludeTask(childTaskSubmitInfo);

            // 提交任务到Master任务分发器
            result = distributeTaskHandle(childTaskSubmitInfo);
            LOG.info("[Submit-success]Submit children task to worker success, parent taskId = {}, childTaskIds.size = {}, "
                            + "childTaskIds = {}, result = {}.", parentTaskId, childTaskSubmitInfo.getTaskIds().size(),
                    childTaskSubmitInfo.getTaskIds(), result);
        } catch (Exception e) {
            LOG.error("[submitChildTask]Error {}.", e.getMessage(), e);
        } finally {
            LOG.info("[END!]");
            MDC.remove("logFileName");
        }
        return result;

    }


    /**
     * 提交任务到Master任务分发器
     *
     * @param taskSubmitInfo taskSubmitInfo
     * @return boolean
     */
    private boolean distributeTaskHandle(TaskSubmitInfo taskSubmitInfo) {

        Map<String, Object> interfaceResult = null;
        /*
         * 任务分发submit信息到Master任务分发器(根据类型分发)
         */
        if (taskSubmitInfo instanceof TaskSubmitInfoRouTine) {
            // RouTine
            interfaceResult = taskDistributeClient.distributeRoutineTask((TaskSubmitInfoRouTine) taskSubmitInfo);
        } else if (taskSubmitInfo instanceof TaskSubmitInfoRerun) {
            // Rerun
            interfaceResult = taskDistributeClient.distributeReRunTask((TaskSubmitInfoRerun) taskSubmitInfo);
        } else if (taskSubmitInfo instanceof TaskSubmitInfoFillData) {
            // FillData
            interfaceResult = taskDistributeClient.distributeFillDataTask((TaskSubmitInfoFillData) taskSubmitInfo);
        } else if (taskSubmitInfo instanceof TaskSubmitInfoSignal) {
            // Signal Task
            interfaceResult = taskDistributeClient.distributeSignalTask((TaskSubmitInfoSignal) taskSubmitInfo);
        } else {
            throw new RuntimeException(
                    "[TaskDistributeClientService-submitTaskToTaskDistributor] taskSubmitInfo type is Error.");
        }

        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return false;
        }
        return (boolean) interfaceResult.get(Constant.DATA);

    }

}
