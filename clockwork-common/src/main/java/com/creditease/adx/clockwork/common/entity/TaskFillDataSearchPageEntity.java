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

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:10 2020-03-26
 * @ Description：
 * @ Modified By：
 */
@ApiModel(value = "补数列表参数", description = "补数列表参数对象")
public class TaskFillDataSearchPageEntity {

    @ApiModelProperty(value = "补数TaskIds,多个用都好隔开")
    private String taskIds;

    @ApiModelProperty(value = "任务组id")
    private Integer taskGroupId;

    @ApiModelProperty("taskGroupAliasName，例如外部系统的taskGroupName")
    private String taskGroupAliasName;

    @ApiModelProperty(value = "补数时间类型（hour，day，week，month）", notes = "hour，day，week，month")
    private String fillDataType;

    @ApiModelProperty(value = "补数基准时间列表,fillDataType=hour时格式:yyyy-MM-dd HH, 其他格式为：yyyy-MM-dd")
    private String fillDataTime;

    @ApiModelProperty(value = "操作人", notes = "操作人邮箱")
    private String operatorName;

    @ApiModelProperty(value = "批次号")
    private String rerunBatchNumber;

    @ApiModelProperty("任务来源：0调度系统,2dataHub,3dataWorks,4dds")
    private Integer source ;

    @ApiModelProperty("开始时间")
    private Date createTimeStart;

    @ApiModelProperty("结束时间")
    private Date createTimeEnd;

    private String description;

    @ApiModelProperty("外部系统Id，例如projectId")
    private Integer externalId;

    /**
     * 用户角色
     */
    private String roleName;

    /**
     * 创建者
     */
    private String createUser;


    public String getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getFillDataType() {
        return fillDataType;
    }

    public void setFillDataType(String fillDataType) {
        this.fillDataType = fillDataType;
    }

    public String getFillDataTime() {
        return fillDataTime;
    }

    public void setFillDataTime(String fillDataTime) {
        this.fillDataTime = fillDataTime;
    }

    public Integer getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(Integer taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getRerunBatchNumber() {
        return rerunBatchNumber;
    }

    public void setRerunBatchNumber(String rerunBatchNumber) {
        this.rerunBatchNumber = rerunBatchNumber;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getCreateTimeStart() {
        return createTimeStart;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public void setCreateTimeStart(Date createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    public String getTaskGroupAliasName() {
        return taskGroupAliasName;
    }

    public void setTaskGroupAliasName(String taskGroupAliasName) {
        this.taskGroupAliasName = taskGroupAliasName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "TaskFillDataSearchPageEntity{" +
                "taskIds=" + taskIds +
                ", taskGroupId=" + taskGroupId +
                ", taskGroupAliasName='" + taskGroupAliasName + '\'' +
                ", fillDataType='" + fillDataType + '\'' +
                ", fillDataTime='" + fillDataTime + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", rerunBatchNumber=" + rerunBatchNumber +
                ", description='" + description + '\'' +
                ", externalId=" + externalId  +
                ", source=" + source  +
                ", createTimeStart=" + createTimeStart +
                ", createTimeEnd=" + createTimeEnd +
                '}';
    }
}
