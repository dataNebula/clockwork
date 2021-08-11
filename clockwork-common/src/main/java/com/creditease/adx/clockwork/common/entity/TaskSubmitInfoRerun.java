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
import lombok.ToString;

import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:01 2019-11-06
 * @ Description：重启任务提交基本信息
 * @ Modified By：
 */
@Getter
@Setter
@ToString
public class TaskSubmitInfoRerun extends TaskSubmitInfo {

    @ApiModelProperty(value = "批次号", required = true)
    private Long rerunBatchNumber;

    @SuppressWarnings("unused")
	private TaskSubmitInfoRerun(){}


    /**
     * 任务提交信息的构造函数
     *
     * @param tasksPojoList    任务pojo
     * @param rerunBatchNumber 批次号
     */
    public TaskSubmitInfoRerun(List<TbClockworkTaskPojo> tasksPojoList, Long rerunBatchNumber) {
        super.setExecuteType(TaskExecuteType.RERUN.getCode());
        super.setTaskIds(tasksPojoList.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList()));
        super.setTaskPojoList(tasksPojoList);
        this.rerunBatchNumber = rerunBatchNumber;
    }

}
