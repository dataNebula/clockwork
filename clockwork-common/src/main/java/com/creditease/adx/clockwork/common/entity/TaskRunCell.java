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

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:36 下午 2020/4/7
 * @ Description：任务运行基本单元Cell
 * @ Modified By：
 */
@Getter
@Setter
public class TaskRunCell {

    @ApiModelProperty(value = "当前运行节点信息")
    private Integer nodeId;

    @ApiModelProperty(value = "当前运行客户端URL")
    private String runtimeDirClientUrl;

    @ApiModelProperty(value = "当前任务执行类型")
    private Integer executeType;

    @ApiModelProperty(value = "当前任务执行命令")
    private String[] cmd;

    @ApiModelProperty(value = "当前任务运行实例")
    private TbClockworkTaskPojo task;

    public TaskRunCell(Integer nodeId, String runtimeDirClientUrl, Integer executeType, TbClockworkTaskPojo task) {
        this.nodeId = nodeId;
        this.runtimeDirClientUrl = runtimeDirClientUrl;
        this.executeType = executeType;
        this.task = task;
    }

    @Override
    public String toString() {
        Integer taskId = (task != null) ? task.getId() : null;
        return "RunTaskCell{" +
                "nodeId=" + nodeId +
                ", runtimeDirClientUrl='" + runtimeDirClientUrl + '\'' +
                ", executeType=" + executeType +
                ", cmd=" + cmd +
                ", task.id=" + taskId +
                '}';
    }
}
