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

package com.creditease.adx.clockwork.api.service;

import java.util.Date;
import java.util.List;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogFlow;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogFlowPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:57 2019-10-14
 * @ Description：
 * @ Modified By：
 */
public interface ITaskLogFlowService {

    /**
     * 添加到kafka队列
     *
     * @param logFlowPojoList list
     * @return
     */
    boolean addToKafkaQueue(List<TbClockworkTaskLogFlowPojo> logFlowPojoList);

    /**
     * 添加作业状态变化记录
     *
     * @param logFlowPojo record
     * @return
     */
    boolean addTaskLogFlow(TbClockworkTaskLogFlowPojo logFlowPojo);

    boolean addBatchTaskLogFlow(List<TbClockworkTaskLogFlowPojo> logFlowPojoList);


    /**
     * 获取所有未结束的任务生命周期日志信息
     *
     * @return
     */
    List<TbClockworkTaskLogFlow> getAllNotEndTaskLogFlow();

    /**
     * 获取时间范围内的生命周期信息, 通过groupId
     *
     * @param groupId
     * @param startTime
     * @param endTime
     * @return
     */
    String getTaskLogFlowInfo(Integer groupId, Date startTime, Date endTime);

    String getTaskLogFlowList(Integer groupId, Date startTime, Date endTime);
}
