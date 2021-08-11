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
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:25 2019-12-30
 * @ Description：任务分发补数Tuple信息
 * @ Modified By：
 */
@Getter
@Setter
public class TaskDistributeTupleFillData extends TaskDistributeTuple {

    String URL = "http://%s:%s/clockwork/worker/task/run/batch/filldata";

    @ApiModelProperty("重跑批次号")
    private Long rerunBatchNumber;

    @ApiModelProperty("补数时间")
    private String fillDataTime;

    @SuppressWarnings("unused")
	private TaskDistributeTupleFillData(){}

    public TaskDistributeTupleFillData(List<TbClockworkTaskPojo> tasksPojo, Long rerunBatchNumber, String fillDataTime, Integer nodeId) {
        super.setExecuteType(TaskExecuteType.FILL_DATA.getCode());
        super.setTaskIds(tasksPojo.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList()));
        super.setTaskPojoList(tasksPojo);
        super.setNodeId(nodeId);
        super.setDistributeURL(URL);
        this.rerunBatchNumber = rerunBatchNumber;
        this.fillDataTime = fillDataTime;
    }


}
