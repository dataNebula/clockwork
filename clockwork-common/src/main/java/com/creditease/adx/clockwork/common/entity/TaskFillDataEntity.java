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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:11 2019-12-25
 * @ Description：
 * @ Modified By：
 */
@ApiModel(value = "补数参数", description = "补数参数对象")
public class TaskFillDataEntity {

    @ApiModelProperty(value = "补数TaskId列表", required = true)
    private List<Integer> taskIds;

    @ApiModelProperty(value = "冗余字段", required = true)
    private String taskGroupAliasName;

    @ApiModelProperty(value = "补数时间类型（hour，day，week，month）", required = true, notes = "hour，day，week，month")
    private String fillDataType;

    @ApiModelProperty(value = "补数基准时间列表,fillDataType=hour时格式:yyyy-MM-dd HH, 其他格式为：yyyy-MM-dd", required = true)
    private List<String> fillDataTimes;

    @ApiModelProperty(value = "操作人", required = true, notes = "操作人邮箱")
    private String operatorName;

    @ApiModelProperty(value = "描述", required = false)
    private String description;

    public List<Integer> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Integer> taskIds) {
        this.taskIds = taskIds;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFillDataType() {
        return fillDataType;
    }

    public void setFillDataType(String fillDataType) {
        this.fillDataType = fillDataType;
    }

    public List<String> getFillDataTimes() {
        return fillDataTimes;
    }

    public void setFillDataTimes(List<String> fillDataTimes) {
        this.fillDataTimes = fillDataTimes;
    }

    public String getTaskGroupAliasName() {
        return taskGroupAliasName;
    }

    public void setTaskGroupAliasName(String taskGroupAliasName) {
        this.taskGroupAliasName = taskGroupAliasName;
    }

    @Override
    public String toString() {
        return "TaskFillDataEntity{" +
                "taskIds=" + taskIds +
                "taskGroupAliasName=" + taskGroupAliasName +
                ", fillDataType='" + fillDataType + '\'' +
                ", fillDataTimes=" + fillDataTimes +
                ", operatorName='" + operatorName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
