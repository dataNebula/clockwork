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

package com.creditease.adx.clockwork.common.pojo;

import java.util.Date;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.enums.TaskGroupStatusUpdateType;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-11-26
 */
public class TaskGroupStatusUpdatePojo {

    private TbClockworkTask tbClockworkTask;

    private Date startTime;

    private Date endTime;

    private String taskStatus;

    private Integer taskLogId;

    private TaskGroupStatusUpdateType taskGroupStatusUpdateType;

    public TbClockworkTask getTbClockworkTask() {
        return tbClockworkTask;
    }

    public void setTbClockworkTask(TbClockworkTask tbClockworkTask) {
        this.tbClockworkTask = tbClockworkTask;
    }

    public TaskGroupStatusUpdateType getTaskGroupStatusUpdateType() {
        return taskGroupStatusUpdateType;
    }

    public void setTaskGroupStatusUpdateType(TaskGroupStatusUpdateType taskGroupStatusUpdateType) {
        this.taskGroupStatusUpdateType = taskGroupStatusUpdateType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getTaskLogId() {
        return taskLogId;
    }

    public void setTaskLogId(Integer taskLogId) {
        this.taskLogId = taskLogId;
    }

}
