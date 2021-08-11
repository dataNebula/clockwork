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

import com.creditease.adx.clockwork.client.TaskOperationClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:44 2019-12-04
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskOperationClientService")
public class TaskOperationClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskOperationClientService.class);

    @Autowired
    protected TaskClientService taskClientService;

    @Autowired
    protected TaskOperationClient taskOperationClient;

    @Autowired
    protected TaskFillDataClientService taskFillDataClientService;

    @Autowired
    protected TaskLogClientService taskLogClientService;

    @Autowired
    protected TaskLogFlowClientService taskLogFlowClientService;

    public TaskOperationClient getTaskOperationClient() {
        return taskOperationClient;
    }

    /**
     * 只更新作业的状态
     *
     * @param taskId id
     * @param status status
     * @return
     */
    public boolean updateTaskStatus(Integer taskId, String status) {
        try {

            // killed || killing 跳过成功的状态
            if (status.equals(TaskStatus.KILLED.getValue()) ||
                    status.equals(TaskStatus.KILLING.getValue())) {
                String statusDb = taskClientService.getTaskStatusById(taskId);
                if (statusDb != null && statusDb.equals(TaskStatus.SUCCESS.getValue())) {
                    return true;
                }
            }

            Map<String, Object> interfaceResult = taskOperationClient.updateTaskStatus(taskId, status);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("updateTaskStatus Error {}", e.getMessage(), e);
        }
        return true;
    }


    /**
     * 更新子任务状态为FatherNotSuccess
     *
     * @param taskId taskId
     * @return bool
     */
    public boolean updateTaskAllChildrenStatusFatherNotSuccess(Integer taskId, Integer executeType, Long rerunBatchNumber) {
        try {
            Map<String, Object> interfaceResult =
                    taskOperationClient.updateTaskAllChildrenStatusFatherNotSuccess(taskId, executeType, rerunBatchNumber);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("updateTaskAllChildrenStatusFatherNotSuccess Error {}", e.getMessage(), e);
        }
        return true;
    }


    /**
     * 更新作业的延迟状态
     *
     * @param taskIds     作业ids
     * @param delayStatus 延迟状态
     * @return
     */
    public boolean updateTasksDelayStatusBatch(List<Integer> taskIds, int delayStatus) {
        try {
            BatchUpdateTasksDelayStatusParam param = new BatchUpdateTasksDelayStatusParam();
            param.setTaskIds(taskIds);
            param.setDelayStatus(delayStatus);
            Map<String, Object> interfaceResult = taskOperationClient.updateTasksDelayStatusByBatch(param);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("updateTasksDelayStatusByBatch Error {}", e.getMessage(), e);
        }
        return true;
    }

    /**
     * 批量修改任务状态
     *
     * @param batchUpdateTaskStatusParam param
     * @return
     */
    public boolean updateTaskStatusBatch(BatchUpdateTaskStatusParam batchUpdateTaskStatusParam) {
        Map<String, Object> interfaceResult = taskOperationClient.updateTaskStatusBatch(batchUpdateTaskStatusParam);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 修改任务状态（Submit）
     *
     * @param taskId taskId
     * @return
     */
    public boolean updateTaskStatusSubmit(Integer taskId) {
        Map<String, Object> interfaceResult = taskOperationClient.updateTaskStatusSubmit(taskId);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 批量修改任务状态（Submit）
     *
     * @param batchUpdateTaskStatusSubmit param
     * @return
     */
    public boolean updateTaskStatusSubmitBatch(BatchUpdateTaskStatusSubmit batchUpdateTaskStatusSubmit) {
        Map<String, Object> interfaceResult = taskOperationClient.updateTaskStatusSubmitBatch(batchUpdateTaskStatusSubmit);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 批量修改任务状态结束
     *
     * @param batchUpdateTaskStatusEnd param
     * @return
     */
    public boolean updateTaskStatusEndBatch(BatchUpdateTaskStatusEnd batchUpdateTaskStatusEnd) {
        Map<String, Object> interfaceResult = taskOperationClient.updateTaskStatusEndBatch(batchUpdateTaskStatusEnd);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }


    /**
     * 杀死正在运行的任务
     *
     * @param stopRunningTaskParam param
     * @return
     */
    public boolean stopRunningTask(StopRunningTaskParam stopRunningTaskParam) {
        Map<String, Object> interfaceResult = taskOperationClient.stopRunningTask(stopRunningTaskParam);
        // 接口CODE 代码判断
        return HttpUtil.checkInterfaceCodeSuccess(interfaceResult);
    }

    /**
     * 停止任务(多个任务)
     *
     * @param taskIdList task list
     * @return
     */
    public boolean stopTaskList(List<Integer> taskIdList) {
        Map<String, Object> interfaceResult = taskOperationClient.stopTaskList(taskIdList);
        // 接口CODE 代码判断
        return HttpUtil.checkInterfaceCodeSuccess(interfaceResult);
    }

    public boolean resetTaskDescendantsLifecycleStatusInBatch(List<Integer> taskIds) {
        Map<String, Object> interfaceResult = taskOperationClient.resetTaskDescendantsLifecycleStatusInBatch(taskIds);
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    public boolean resetTaskLifecycleStatusByDagIdsInBatch(List<Integer> dagIds, List<Integer> taskIds) {
        BatchResetTaskLifecycleParam param = new BatchResetTaskLifecycleParam();
        param.setDagIds(dagIds);
        param.setTaskIds(taskIds);
        Map<String, Object> interfaceResult = taskOperationClient.resetTaskLifecycleStatusByDagIdsInBatch(param);
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    public boolean resetTaskLifecycleStatusBySource(Integer source) {
        Map<String, Object> interfaceResult = taskOperationClient.resetTaskLifecycleStatusBySource(source);
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    public boolean resetTaskLifecycleStatusByDagIdsNotExists(List<Integer> dagIds) {
        Map<String, Object> interfaceResult = taskOperationClient.resetTaskLifecycleStatusByDagIdsNotExists(dagIds);
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }



    public boolean checkParentsSuccess(TbClockworkTaskPojo taskPojo) {
        Map<String, Object> interfaceResult = taskOperationClient.checkParentsSuccess(taskPojo);
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }


    public boolean updateTaskChildrensDelayStatusAndRefreshSlots(Integer taskId, Integer taskRerunType) {
        try {
            Map<String, Object> interfaceResult =
                    taskOperationClient.updateTaskChildrensDelayStatusAndRefreshSlots(taskId, taskRerunType);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("updateTaskChildrensDelayStatusAndRefreshSlots Error {}", e.getMessage(), e);
        }
        return true;
    }


    /**
     * 停止任务以及他的子任务
     *
     * @param taskId taskId
     * @return
     */
    public boolean stopTaskAndChirldens(Integer taskId, Integer stopType) {
        Map<String, Object> interfaceResult = taskOperationClient.stopTaskAndChirldens(taskId, stopType);
        // 接口CODE 代码判断
        return HttpUtil.checkInterfaceCodeSuccess(interfaceResult);
    }

}
