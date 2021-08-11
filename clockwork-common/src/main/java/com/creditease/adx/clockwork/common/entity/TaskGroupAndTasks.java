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

package com.creditease.adx.clockwork.common.entity;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskGroupPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-08-13
 */
public class TaskGroupAndTasks {

    // 任务组
    private TbClockworkTaskGroupPojo taskGroup;

    // 分组下的所有任务
    private List<TbClockworkTaskPojo> tasks;

    // 操作人
    private String operator;

    // 删除的task id信息，多个逗号分隔
    private String deleteTaskIds;

    public TbClockworkTaskGroupPojo getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(TbClockworkTaskGroupPojo taskGroup) {
        this.taskGroup = taskGroup;
    }

    public List<TbClockworkTaskPojo> getTasks() {
        return tasks;
    }

    public void setTasks(List<TbClockworkTaskPojo> tasks) {
        this.tasks = tasks;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDeleteTaskIds() {
        return deleteTaskIds;
    }

    public void setDeleteTaskIds(String deleteTaskIds) {
        this.deleteTaskIds = deleteTaskIds;
    }


    @Override
    public String toString() {
        return "TaskGroupAndTask{" +
                "operator=" + operator +
                ", deleteTaskIds=" + deleteTaskIds +
                ", taskGroup=" + taskGroup +
                ", tasks.size=" + tasks.size() +
                '}';
    }

}
