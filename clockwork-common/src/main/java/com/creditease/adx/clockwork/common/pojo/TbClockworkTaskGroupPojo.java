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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import com.creditease.adx.clockwork.common.framework.annotation.QueryLike;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class TbClockworkTaskGroupPojo extends TbClockworkTaskGroup {

    private String taskGroupStatus;

	@QueryLike
    private String name;
	
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskGroupStatus() {
		return taskGroupStatus;
	}

	public void setTaskGroupStatus(String taskGroupStatus) {
		this.taskGroupStatus = taskGroupStatus;
	}

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Override
    public Date getLastStartTime(){
        return super.getLastStartTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Override
    public Date getLastEndTime(){
        return super.getLastEndTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getCreateTime() {
        return super.getCreateTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getUpdateTime() {
        return super.getUpdateTime();
    }

    @Override
    public String toString() {
        return "TbClockworkTaskGroupPojo{" +
                "id=" + getId() +
                ", userGroupName=" + super.getUserGroupName() +
                ", name=" + super.getName() +
                ", userName=" + super.getUserName() +
                ", description=" + super.getDescription() +
                ", status=" + super.getStatus() +
                ", batchNumber=" + super.getBatchNumber() +
                ", aliasName=" + super.getAliasName() +
                ", aliasNameType=" + super.getAliasNameType() +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", taskGroupStatus=" + getTaskGroupStatus() +
                ", externalId=" + getExternalId() +
                '}';
    }
}
