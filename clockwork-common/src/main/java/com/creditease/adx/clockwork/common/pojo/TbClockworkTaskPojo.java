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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDependencyScript;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

public class TbClockworkTaskPojo extends TbClockworkTask {

    @ApiModelProperty(value = "依赖文件全路径，List存储，支持多个，可选参数")
    private List<TbClockworkTaskDependencyScript> dependencyScript;

    @ApiModelProperty(value = "调度系统跨任务组的依赖父任务ID，多个的话则逗号分割")
    private String taskFatherIdsCrossTaskGroup;

    @ApiModelProperty(value = "外部系统任务的ID,通过此参数做唯一标记")
    private String externalSystemTaskId;

    @ApiModelProperty(value = "外部系统任务依赖父亲任务的ID，多个的话则逗号分割 ,通过此参数构建依赖关系")
    private String externalSystemTaskDependencyId;

    private Integer taskLogId;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TbClockworkTaskPojo other = (TbClockworkTaskPojo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "TbClockworkTaskPojo{" +
                "id=" + super.getId() +
                ", name=" + super.getName() +
                ", aliasName=" + super.getAliasName() +
                ", description=" + super.getDescription() +
                ", dagId=" + super.getDagId() +
                ", groupId=" + super.getGroupId() +
                ", nodeGId=" + super.getNodeGid() +
                ", location=" + super.getLocation() +
                ", scriptName=" + super.getScriptName() +
                ", scriptParameter=" + super.getScriptParameter() +
                ", command=" + super.getCommand() +
                ", parameter=" + super.getParameter() +
                ", triggerMode=" + super.getTriggerMode() +
                ", cronExp=" + super.getCronExp() +
                ", triggerTime=" + super.getTriggerTime() +
                ", nextTriggerTime=" + super.getNextTriggerTime() +
                ", timeType=" + super.getTimeType() +
                ", runFrequency=" + super.getRunFrequency() +
                ", dependencyId=" + super.getDependencyId() +
                ", expiredTime=" + super.getExpiredTime() +
                ", failedRetries=" + super.getFailedRetries() +
                ", status=" + super.getStatus() +
                ", delayStatus=" + super.getDelayStatus() +
                ", online=" + super.getOnline() +
                ", lastStartTime=" + super.getLastStartTime() +
                ", lastEndTime=" + super.getLastEndTime() +
                ", runTimeout=" + super.getRunTimeout() +
                ", runEngine=" + super.getRunEngine() +
                ", isPrivate=" + super.getIsPrivate() +
                ", isFirst=" + super.getIsFirst() +
                ", source=" + super.getSource() +
                ", proxyUser=" + super.getProxyUser() +
                ", businessInfo=" + super.getBusinessInfo() +
                ", createUser=" + super.getCreateUser() +
                ", operatorName=" + super.getOperatorName() +
                ", emailList=" + super.getEmailList() +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", externalSystemTaskId=" + externalSystemTaskId +
                ", externalSystemTaskDependencyId=" + externalSystemTaskDependencyId +
                '}';
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getTriggerTime() {
        return super.getTriggerTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getNextTriggerTime() {
        return super.getNextTriggerTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getLastStartTime() {
        return super.getLastStartTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getLastEndTime() {
        return super.getLastEndTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getExpiredTime() {
        return super.getExpiredTime();
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


    public List<TbClockworkTaskDependencyScript> getDependencyScript() {
        // 添加任务，修改任务是关联脚本
        // 执行任务时获取相关依赖脚本
        return dependencyScript;
    }

    public void setDependencyScript(List<TbClockworkTaskDependencyScript> dependencyScript) {
        this.dependencyScript = dependencyScript;
    }

    public Integer getTaskLogId() {
        return taskLogId;
    }

    public void setTaskLogId(Integer taskLogId) {
        this.taskLogId = taskLogId;
    }

    public String getTaskFatherIdsCrossTaskGroup() {
        return taskFatherIdsCrossTaskGroup;
    }

    public void setTaskFatherIdsCrossTaskGroup(String taskFatherIdsCrossTaskGroup) {
        this.taskFatherIdsCrossTaskGroup = taskFatherIdsCrossTaskGroup;
    }

    public String getExternalSystemTaskId() {
        return externalSystemTaskId;
    }

    public void setExternalSystemTaskId(String externalSystemTaskId) {
        this.externalSystemTaskId = externalSystemTaskId;
    }

    public String getExternalSystemTaskDependencyId() {
        return externalSystemTaskDependencyId;
    }

    public void setExternalSystemTaskDependencyId(String externalSystemTaskDependencyId) {
        this.externalSystemTaskDependencyId = externalSystemTaskDependencyId;
    }
}
