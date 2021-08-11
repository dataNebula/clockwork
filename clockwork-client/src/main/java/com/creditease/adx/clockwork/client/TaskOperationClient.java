package com.creditease.adx.clockwork.client;
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

import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:52 下午 2020/8/6
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface TaskOperationClient {

    @PostMapping(value = "/clockwork/api/task/operation/enableTask")
    Map<String, Object> enableTask(@RequestParam(value = "taskId") Integer taskId);

    @PostMapping(value = "/clockwork/api/task/operation/disableTask")
    Map<String, Object> disableTask(@RequestParam(value = "taskId") Integer taskId);

    @PostMapping(value = "/clockwork/api/task/operation/deleteTask")
    Map<String, Object> deleteTask(@RequestParam(value = "taskId") Integer taskId);

    @PostMapping(value = "/clockwork/api/task/operation/deleteTaskList")
    Map<String, Object> deleteTaskList(@RequestParam(value = "taskIds") List<Integer> taskIds);

    @PostMapping(value = "/clockwork/api/task/operation/addTask")
    Map<String, Object> addTask(@RequestBody TbClockworkTaskPojo taskPojo);

    /**
     * 修改task信息（不可以修改任务状态、上下线）
     *
     * @param taskPojo task
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTask")
    Map<String, Object> updateTask(@RequestBody TbClockworkTaskPojo taskPojo);

    /**
     * 修改task信息（可以修改任务状态、上下线）
     *
     * @param task task
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTaskInfo")
    Map<String, Object> updateTaskInfo(@RequestBody TbClockworkTaskPojo task);


    /**
     * 修改任务状态
     *
     * @param taskId id
     * @param status status
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTaskStatus")
    Map<String, Object> updateTaskStatus(@RequestParam(value = "taskId") Integer taskId,
                                         @RequestParam(value = "status") String status);

    /**
     * 更新子任务状态为FatherNotSuccess
     *
     * @param taskId taskId
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTaskAllChildrenStatusFatherNotSuccess")
    Map<String, Object> updateTaskAllChildrenStatusFatherNotSuccess(@RequestParam(value = "taskId") Integer taskId,
                                                                    @RequestParam(value = "executeType") Integer executeType,
                                                                    @RequestParam(value = "rerunBatchNumber", required = false) Long rerunBatchNumber);


    /**
     * 修改task状态 (开始时间)
     *
     * @param taskId taskId
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTaskStatusSubmit")
    Map<String, Object> updateTaskStatusSubmit(@RequestParam(value = "taskId") Integer taskId);

    /**
     * 批量修改task状态 (开始时间)
     *
     * @param taskStatusSubmit submit
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTaskStatusSubmitBatch")
    Map<String, Object> updateTaskStatusSubmitBatch(@RequestBody BatchUpdateTaskStatusSubmit taskStatusSubmit);

    /**
     * 修改task状态结束
     *
     * @param taskStatusEnd batch data
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTaskStatusEndBatch")
    Map<String, Object> updateTaskStatusEndBatch(@RequestBody BatchUpdateTaskStatusEnd taskStatusEnd);

    /**
     * 批量更新任务状态
     *
     * @param batchUpdateTaskStatusParam batchInfo
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/operation/updateTasksStatus")
    Map<String, Object> updateTaskStatusBatch(@RequestBody BatchUpdateTaskStatusParam batchUpdateTaskStatusParam);

    /**
     * 批量更新延迟策略状态
     *
     * @param param param
     * @return
     */
    @PostMapping("/clockwork/api/task/operation/updateTasksDelayStatusByBatch")
    Map<String, Object> updateTasksDelayStatusByBatch(@RequestBody BatchUpdateTasksDelayStatusParam param);

    /**
     * 找出所有任务的所有子孙任务， 将作业的状态设置为生命周期重置状态
     *
     * @param taskIds ids
     * @return
     */
    @PostMapping("/clockwork/api/task/operation/resetTaskDescendantsLifecycleStatusInBatch")
    Map<String, Object> resetTaskDescendantsLifecycleStatusInBatch(@RequestBody List<Integer> taskIds);

    /**
     * 根据dagID将作业的状态设置为生命周期重置状态
     *
     * @param param param
     * @return
     */
    @PostMapping("/clockwork/api/task/operation/resetTaskLifecycleStatusByDagIdsInBatch")
    Map<String, Object> resetTaskLifecycleStatusByDagIdsInBatch(@RequestBody BatchResetTaskLifecycleParam param);

    /**
     * 根据source将作业的状态设置为生命周期重置状态
     *
     * @param source param
     * @return
     */
    @PostMapping("/clockwork/api/task/operation/resetTaskLifecycleStatusBySource")
    Map<String, Object> resetTaskLifecycleStatusBySource(@RequestParam(value = "source") Integer source);

    /**
     * 根据DagIds将作业的状态设置为生命周期重置状态(非DDS)
     *
     * @param dagIds param
     * @return
     */
    @PostMapping("/clockwork/api/task/operation/resetTaskLifecycleStatusByDagIdsNotExists")
    Map<String, Object> resetTaskLifecycleStatusByDagIdsNotExists(@RequestBody List<Integer> dagIds);

    /**
     * 检测父任务是否成功
     *
     * @param taskPojo task
     * @return
     */
    @PostMapping("/clockwork/api/task/operation/checkParentsSuccess")
    Map<String, Object> checkParentsSuccess(@RequestBody TbClockworkTaskPojo taskPojo);

    /**
     * 杀死正在运行的任务
     *
     * @param stopRunningTaskParam param
     * @return
     */
    @PostMapping("/clockwork/api/task/stop/stopRunningTask")
    Map<String, Object> stopRunningTask(@RequestBody StopRunningTaskParam stopRunningTaskParam);

    /**
     * 杀死任务(并且从队列中移除)
     *
     * @param taskId 单个任务
     * @return
     */
    @PostMapping("/clockwork/api/task/stop/stopTask")
    Map<String, Object> stopTask(@RequestParam(value = "taskId") Integer taskId);

    /**
     * 杀死任务(并且从队列中移除)
     *
     * @param taskIdList 多个任务
     * @return
     */
    @PostMapping("/clockwork/api/task/stop/stopTaskList")
    Map<String, Object> stopTaskList(@RequestBody List<Integer> taskIdList);
/**/
    /**
     * @Description 如果是父任务不成功的任务，而且，子任务是时间依赖双触发的，
     * 就把他们的延迟策略修改为父任务延迟恢复，同时刷新他们的时钟
     *
     * @Param [taskId]
     * @return java.util.Map<java.lang.String,java.lang.Object>*/
    @PostMapping("/clockwork/api/task/operation/updateTaskChildrensDelayStatusAndRefreshSlots")
    Map<String, Object> updateTaskChildrensDelayStatusAndRefreshSlots(@RequestParam(value = "taskId") Integer taskId,
                                                                      @RequestParam(value = "taskRerunType") Integer taskRerunType);

    /**
     * 杀死任务以及他的子任务,此处获得的任务id同rerun task中任务自身及子任务 (并且从队列中移除)
     *
     * @param taskId 任务
     * @return
     */
    @PostMapping("/clockwork/api/task/stop/stopTaskAndChirldens")
    Map<String, Object> stopTaskAndChirldens(@RequestParam(value = "taskId") Integer taskId,
                                             @RequestParam(value = "stopType") Integer stopType);

}
