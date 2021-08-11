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
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRelationMapper;

import java.util.List;

public interface ITaskRelationService {

    TbClockworkTaskRelationMapper getMapper();

    int addTaskRelation(TbClockworkTaskRelationPojo taskRelationPojo);

    int deleteDependencies(int taskId);

    int deleteDependenciesByTaskIds(List<Integer> taskIds);

    int updateTaskRelationIsEffective(int taskId, boolean isEffective);

    int updateTasksRelationIsEffective(List<Integer> taskIds, boolean isEffective);

    TbClockworkTaskRelationPojo findTaskOneRelation(Integer taskId);

    List<TbClockworkTaskRelationPojo> findTaskDirectlyChildren(int taskId);

    List<TbClockworkTaskRelationPojo> findTaskAllChildrenNotIncludeSelf(Integer taskId);

    List<TbClockworkTaskRelationPojo> findTaskDirectlyFather(int taskId);

    List<Integer> findDirectlyRelationTaskIdsIncludeSelf(int taskId);

    List<Integer> findDirectlyRelationTaskIdsNotIncludeSelf(int taskId);

    /**
     * 获得当前任务的直接孩子，不包含自己
     *
     * @param taskId int
     * @return
     */
    List<TbClockworkTaskPojo> getTaskDirectlyChildrenNotIncludeSelf(int taskId);

    /**
     * 获得当前任务的直接孩子，包含自己
     *
     * @param taskId int
     * @return
     */
    List<TbClockworkTaskPojo> getTaskDirectlyChildrenIncludeSelf(int taskId);

    /**
     * 获得当前任务的直接父亲，不包含自己
     *
     * @param taskId
     * @return
     */
    List<TbClockworkTaskPojo> getTaskDirectlyFatherNotIncludeSelf(int taskId);

    /**
     * 获得当前任务的直接关联任务（孩子、父亲），不包含自己
     *
     * @param taskId
     * @return
     */
    List<TbClockworkTask> getTaskDirectlyRelationTaskNotIncludeSelf(int taskId);

    /**
     * 获得当前任务的所有孩子，包含自己
     *
     * @param taskId
     * @return
     */
    List<TbClockworkTaskPojo> getTaskAllChildrenIncludeSelf(Integer taskId);

    /**
     * 获得当前任务的所有孩子，不包含自己
     *
     * @param taskId
     * @return
     */
    List<TbClockworkTaskPojo> getTaskAllChildrenNotIncludeSelf(Integer taskId);

    /**
     * 获得当前任务的所有孩子，包含自己的 id 列表
     *
     * @param taskId
     * @return
     */
    List<Integer> getAllChildrenAndSelfIds(Integer taskId, Integer stopType);
}
