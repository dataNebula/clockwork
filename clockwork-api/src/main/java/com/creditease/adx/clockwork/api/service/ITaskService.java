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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDependencyScript;
import com.creditease.adx.clockwork.common.pojo.DDSAndDataWorkTaskInfoPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;

import java.util.List;
import java.util.Map;

public interface ITaskService {

    TbClockworkTaskMapper getMapper();

    /**
     * 获取任务需要替换的依赖脚本文件
     *
     * @param taskId
     * @return
     */
    List<TbClockworkTaskDependencyScript> getDependencyScriptFileByTaskId(Integer taskId);

    List<TbClockworkTask> getTbClockworkTasksByIds(List<Integer> ids);

    TbClockworkTaskPojo getTaskById(Integer id);

    TbClockworkTaskPojo getTaskByName(String name);

    Map<String, Map<Integer, String>> getTaskStatusByTableName(String businessInfo);

    List<TbClockworkTaskPojo> getTaskByStatus(String status);

    List<TbClockworkTaskPojo> getTaskOnlineByStatus(String status);

    List<TbClockworkTaskPojo> getTaskByRunFailedStatus(List<String> status, String beforeDateStr, String currentDateStr);

    List<TbClockworkTaskPojo> getTaskListByStatusList(List<String> statusList);

    List<TbClockworkTaskPojo> getTaskByDelayStatus(int delayStatus);

    List<TbClockworkTaskPojo> getTasksByDagId(int dagId);

    List<TbClockworkTaskPojo> getTasksNotIncludeDDSByDagId(int dagId);

    List<Integer> getTaskDagIdsBySource(Integer source);

    List<Integer> getTaskDagIdsByCrossSource(Integer source);

    List<TbClockworkTaskPojo> getTasksByGroupId(int groupId);

    List<TbClockworkTask> getTaskByTaskGroupId(int taskGroupId);

    List<TbClockworkTask> getTaskByTaskIds(List<Integer> ids);

    List<TbClockworkTask> getTaskByNames(List<String> names);

    List<TbClockworkTask4PagePojo> getAllTaskByCondition(TbClockworkTask4PagePojo tbClockworkTask);

    List<TbClockworkTask4PagePojo> getAllTaskUsedByAutoComplete(String idOrNameSegment);

    String getTaskStatusById(int taskId);

    List<TbClockworkTaskPojo> getTasksByDagIdWhereTaskIsOnline(int dagId);

    int getAllTaskByPageParamCount(TbClockworkTask4PagePojo task);

    List<TbClockworkTask4PagePojo> getAllTaskByPageParam(TbClockworkTask4PagePojo task, int pageNumber, int pageSize);

    List<DDSAndDataWorkTaskInfoPojo> getDDSAndDataWorkTasks(TbClockworkTask4PagePojo task);
}
