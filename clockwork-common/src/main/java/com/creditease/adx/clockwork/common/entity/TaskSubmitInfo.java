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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:05 下午 2020/4/1
 * @ Description：任务提交基本信息
 * @ Modified By：
 */
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskSubmitInfo {

    @ApiModelProperty(value = "任务运行类型（模式）AdxCommonConstants.TaskExecuteType", required = true)
    private int executeType;

    @ApiModelProperty(value = "任务ids，不为空", required = true)
    private List<Integer> taskIds;

    @ApiModelProperty(value = "任务List，不为空", required = true)
    private List<TbClockworkTaskPojo> taskPojoList;

}
