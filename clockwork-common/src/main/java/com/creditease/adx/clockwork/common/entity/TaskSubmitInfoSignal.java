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

import java.util.ArrayList;
import java.util.stream.Collectors;

import lombok.ToString;

import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:01 2020-05-11
 * @ Description：信号触发任务提交基本信息
 * @ Modified By：
 */
@ToString
public class TaskSubmitInfoSignal extends TaskSubmitInfo {

    @SuppressWarnings("unused")
	private TaskSubmitInfoSignal(){}

    /**
     * 任务提交信息的构造函数
     *
     * @param tasksPojo 任务pojo
     */
    public TaskSubmitInfoSignal(ArrayList<TbClockworkTaskPojo> tasksPojo) {
        super.setExecuteType(TaskExecuteType.ROUTINE.getCode());
        super.setTaskIds(tasksPojo.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList()));
        super.setTaskPojoList(tasksPojo);
    }

}
