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

import lombok.Data;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:11 2020-05-21
 * @ Description：
 * @ Modified By：
 */
@Data
@ApiModel(value = "信号触发参数", description = "信号触发参数对象")
public class TaskSignalEntity {

    @ApiModelProperty(value = "信号触发TaskId列表", required = true)
    private List<Integer> taskIds;

    @ApiModelProperty(value = "操作人", required = true, notes = "操作人邮箱")
    private String operatorName;

    @Override
    public String toString() {
        return "TaskSignalEntity{" +
                "taskIds=" + taskIds +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}
