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

import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:23 下午 2020/5/11
 * @ Description：
 * @ Modified By；
 */
public interface ITaskSubmitService {

    /**
     * 重启任务（自己、所有子节点不包括自己、所有子节点包括自己）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:self,3:all_children_not_self,4:all_children_and_self]
     * @param parameter     参数
     * @param operatorName  操作人
     */
    TaskSubmitInfoRerun submitReRunTaskTx(Integer taskId, Integer taskReRunType, String parameter, String operatorName, String scriptParameter);

    /**
     * 重启历史运行任务（单个任务）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:normal,1:routine,0:rerun,2:fill_data]
     * @param logId         历史日志ID
     * @param parameter     参数
     * @param operatorName  操作人
     */
    TaskSubmitInfoRerun submitReRunTaskHisTx(Integer taskId, Integer taskReRunType, Integer logId, String parameter, String operatorName, String scriptParameter);

    /**
     * 重启任务（更具dagID）
     *
     * @param dagId        dagId
     * @param parameter    参数
     * @param operatorName 操作人
     */
    TaskSubmitInfoRerun submitReRunTaskByDagIdTx(Integer dagId, String parameter, String operatorName);

    /**
     * 重启任务（更具groupId）
     *
     * @param groupId      groupId
     * @param parameter    参数
     * @param operatorName 操作人
     */
    TaskSubmitInfoRerun submitReRunTaskByGroupIdTx(Integer groupId, String parameter, String operatorName);

    /**
     * 重启任务（批量任务）
     *
     * @param taskList 多个任务
     */
    TaskSubmitInfoRerun submitReRunTaskListTx(List<TbClockworkTaskPojo> taskList);


    /**
     * 重启补数任务
     *
     * @param entity entity
     * @return 补数记录
     */
    TaskSubmitInfoFillData submitFillDataTaskListTx(TaskFillDataEntity entity, long rerunBatchNumber);


    /**
     * 提交信号触发任务
     *
     * @param signal 信号任务
     */
    TaskSubmitInfoSignal submitSignalTaskListTx(TaskSignalEntity signal);

    /**
     * 获取所有的子节点，包括自己，以及该任务的直接子节点的直接父节点不成功的任务
     *
     * @param taskId 任务Id
     * @return
     */
    List<TbClockworkTaskPojo> getAllChildrenAndSelfIncludeFailedFather(Integer taskId);
}
