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

package com.creditease.adx.clockwork.web.service;

import java.util.List;
import java.util.Map;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import io.swagger.models.auth.In;

public interface ITaskService {

    TbClockworkTaskMapper getMapper();

    TbClockworkTaskPojo getTaskById(Integer id);

    List<TbClockworkTask> getTaskByTaskIds(List<Integer> ids);

    List<Map<String, Object>> getAllTaskIdAndNameNotInThisId(Integer id);

    List<Map<String, Object>> getTaskIdAndNameByUserGroupName(String userName, String userGroupName, Integer id);

    List<TbClockworkTaskPojo> getTasksByDagId(int dagId);

    List<TbClockworkTask> getTaskByTaskGroupId(int taskGroupId);

    long getCountByTaskName(String taskName);

//    List<TbClockworkTask4PagePojo> getAllTaskByPageParam(TbClockworkTask4PagePojo pojo, int pageNumber, int pageSize);
//
//    int getAllTaskByPageParamCount(TbClockworkTask4PagePojo task);

}
