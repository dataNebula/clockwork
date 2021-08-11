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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;

import java.util.Date;
import java.util.List;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2020-01-02
 */
public class ConsanguinityAnalysisPojo {

    private long taskGroupBatchId;

    private int taskGroupId;

    private Date taskGroupStartTime;

    private Date taskGroupEndTime;

    private String taskGroupStatus;

    private List<TbClockworkTaskLog> tasksLog = null;


    public long getTaskGroupBatchId() {
        return taskGroupBatchId;
    }

    public void setTaskGroupBatchId(long taskGroupBatchId) {
        this.taskGroupBatchId = taskGroupBatchId;
    }

    public int getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(int taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public Date getTaskGroupStartTime() {
        return taskGroupStartTime;
    }

    public void setTaskGroupStartTime(Date taskGroupStartTime) {
        this.taskGroupStartTime = taskGroupStartTime;
    }

    public Date getTaskGroupEndTime() {
        return taskGroupEndTime;
    }

    public void setTaskGroupEndTime(Date taskGroupEndTime) {
        this.taskGroupEndTime = taskGroupEndTime;
    }

    public String getTaskGroupStatus() {
        return taskGroupStatus;
    }

    public void setTaskGroupStatus(String taskGroupStatus) {
        this.taskGroupStatus = taskGroupStatus;
    }

    public List <TbClockworkTaskLog> getTasksLog() {
        return tasksLog;
    }

    public void setTasksLog(List <TbClockworkTaskLog> tasksLog) {
        this.tasksLog = tasksLog;
    }

}
