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

package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusEnd;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusParam;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusSubmit;
import com.creditease.adx.clockwork.common.entity.TaskGroupAndTasks;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:29 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
public interface ITaskOperationService {

    boolean enableTaskTx(Integer taskId);

    boolean disableTaskTx(Integer taskId);

    boolean deleteTask(int taskId);

    boolean deleteTaskList(List<Integer> taskIds);

    boolean addTask(TbClockworkTaskPojo task);

    TaskGroupAndTasks addTaskList(TaskGroupAndTasks taskGroupAndTasks);

    int updateTask(TbClockworkTaskPojo taskPojo);

    TaskGroupAndTasks updateTaskList(TaskGroupAndTasks taskGroupAndTasks);

    int updateTaskDagId(Integer taskId, Integer dagId);

    int updateTaskDagIdByBatch(List<Integer> taskIds, Integer tagId);

    void updateChildrenTaskDependencyIfFatherTaskIdsChange(List<Integer> fatherTaskId);

    void updateChildrenTaskDependencyIfFatherTaskIdChange(Integer fatherTaskId);

    void updateTaskDependencyIdFieldById(Integer taskId);

    boolean updateTaskNextTriggerTime(Integer taskId, Date triggerTime);

    int updateTaskStatus(Integer taskId, String status);

    int updateTaskInfo(TbClockworkTaskPojo task);

    boolean updateTaskStatusSubmit(int taskId);

    boolean updateTaskStatusSubmitBatch(BatchUpdateTaskStatusSubmit batchUpdateTaskStatusSubmit);

    boolean updateTaskStatusEndBatch(BatchUpdateTaskStatusEnd batchUpdateTaskStatusEnd);

    boolean updateTaskAllChildrenStatusFatherNotSuccess(Integer taskId, Integer executeType, Long rerunBatchNumber);

    /**
     * 批量修改任务的状态
     *
     * @param batchUpdateTaskStatusParam
     * @return
     */
    boolean updateTaskStatusBatch(BatchUpdateTaskStatusParam batchUpdateTaskStatusParam);

    boolean updateTasksDelayStatusByBatch(List<Integer> taskIds, int delayStatus);

    /**
     * 将作业的状态设置为生命周期重置状态
     *
     * @param taskIds
     * @return
     */
    boolean resetTaskDescendantsLifecycleStatusInBatch(List<Integer> taskIds);

    /**
     * 根据dagId 将作业的状态设置为生命周期重置状态
     *
     * @param dagIds
     * @return
     */
    boolean resetTaskLifecycleStatusByDagIdsInBatch(List<Integer> dagIds, List<Integer> taskIds);

    /**
     * 根据source 将作业的状态设置为生命周期重置状态
     *
     * @param source
     * @return
     */
    boolean resetTaskLifecycleStatusBySource(Integer source);

    boolean resetTaskLifecycleStatusByDagIds(List<Integer> dagIds);

    boolean resetTaskLifecycleStatusByDagIdsNotExists(List<Integer> dagIds);

    boolean checkParentsSuccess(TbClockworkTaskPojo task);

    boolean updateTaskChildrensDelayStatusAndRefreshSlots(Integer taskId, Integer taskRerunType);
}
