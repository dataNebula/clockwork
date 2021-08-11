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

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:25 2019-12-30
 * @ Description：补数任务运行基本单元Cell
 * @ Modified By：
 */
@Getter
@Setter
public class TaskRunCellFillData extends TaskRunCell {

    @ApiModelProperty("批次号")
    private Long rerunBatchNumber;

    @ApiModelProperty("补数的日期")
    private String fillDataTime;

    public TaskRunCellFillData(
            int nodeId, String runtimeDirClientUrl, Integer executeType, TbClockworkTaskPojo task,
            Long rerunBatchNumber, String fillDataTime) {
        super(nodeId, runtimeDirClientUrl, executeType, task);
        this.rerunBatchNumber = rerunBatchNumber;
        this.fillDataTime = fillDataTime;
    }

    @Override
    public String toString() {
        Integer taskId = (super.getTask() != null) ? super.getTask().getId() : null;
        return "RunTaskCell{" +
                "nodeId=" + super.getNodeId() +
                ", runtimeDirClientUrl='" + super.getRuntimeDirClientUrl() + '\'' +
                ", executeType=" + super.getExecuteType() +
                ", rerunBatchNumber=" + rerunBatchNumber +
                ", fillDataTime=" + fillDataTime +
                ", task.id=" + taskId +
                '}';
    }

}
