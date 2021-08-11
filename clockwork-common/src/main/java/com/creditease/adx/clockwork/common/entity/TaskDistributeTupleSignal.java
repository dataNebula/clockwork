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

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:06 下午 2020/5/11
 * @ Description：任务分发信号任务Tuple信息
 * @ Modified By：
 */
@Getter
@Setter
public class TaskDistributeTupleSignal extends TaskDistributeTuple {

    String URL = "http://%s:%s/clockwork/worker/task/run/batch/signal";

    @SuppressWarnings("unused")
	private TaskDistributeTupleSignal(){}

    public TaskDistributeTupleSignal(List<TbClockworkTaskPojo> tasksPojo, Integer nodeId) {
        super.setExecuteType(TaskExecuteType.ROUTINE.getCode());
        super.setTaskIds(tasksPojo.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList()));
        super.setTaskPojoList(tasksPojo);
        super.setNodeId(nodeId);
        super.setDistributeURL(URL);
    }


}
