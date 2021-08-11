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
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 12:44 下午 2020/3/3
 * @ Description：
 * @ Modified By：
 */
public class TbClockworkTaskLogPojo extends TbClockworkTaskLog {

    private int operationType;

    private String taskGroupName;

    @ApiModelProperty("任务来源：0调度系统,2dataHub,3dataWorks,4dds")
    private Integer source ;

    @ApiModelProperty("任务别名")
    private String taskAliasName;

    @ApiModelProperty("任务组别名")
    private String taskGroupAliasName;

    @ApiModelProperty("外部系统Id")
    private Integer externalId;

    private String createUser;

    @ApiModelProperty("日志节点ip")
    private String nodeIp;

    @ApiModelProperty("日志节点port")
    private String nodePort;

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public String getTaskGroupName() {
        return taskGroupName;
    }

    public void setTaskGroupName(String taskGroupName) {
        this.taskGroupName = taskGroupName;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public String getTaskAliasName() {
        return taskAliasName;
    }

    public void setTaskAliasName(String taskAliasName) {
        this.taskAliasName = taskAliasName;
    }

    public String getTaskGroupAliasName() {
        return taskGroupAliasName;
    }

    public void setTaskGroupAliasName(String taskGroupAliasName) {
        this.taskGroupAliasName = taskGroupAliasName;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getStartTime() {
        return super.getStartTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getExecuteTime() {
        return super.getExecuteTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getEndTime() {
        return super.getEndTime();
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getTriggerTime() {
        return super.getTriggerTime();
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public Long getRerunBatchNumber() {
        return super.getRerunBatchNumber();
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public Long getBatchNumber() {
        return super.getBatchNumber();
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Override
    public String toString() {
        return "TbClockworkTaskLogPojo{" +
                "id=" + getId() +
                ", taskId=" + getTaskId() +
                ", nodeId=" + getNodeId() +
                ", taskName=" + getTaskName() +
                ", groupId=" + getGroupId() +
                ", status=" + getStatus() +
                ", pid=" + getPid() +
                ", realCommand=" + getRealCommand() +
                ", returnCode=" + getReturnCode() +
                ", logName=" + getLogName() +
                ", startTime=" + getStartTime() +
                ", executeTime=" + getExecuteTime() +
                ", runningTime=" + getRunningTime() +
                ", endTime=" + getEndTime() +
                ", executeType=" + getExecuteType() +
                ", returnMsg=" + getReturnMsg() +
                ", runEngine=" + getRunEngine() +
                ", triggerTime=" + getTriggerTime() +
                ", groupId=" + getGroupId() +
                ", batchNum=" + getBatchNumber() +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", operationType=" + operationType +
                ", taskGroupName=" + taskGroupName +
                ", source=" + source +
                ", taskAliasName=" + taskAliasName +
                ", externalId=" + externalId +
                ", taskGroupAliasName=" + taskGroupAliasName +
                ", isEnd=" + getIsEnd() +
                '}';
    }


}
