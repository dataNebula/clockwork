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

package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskSubscriptionPojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:40 下午 2020/12/8
 * @ Description：
 * @ Modified By：
 */
public interface ITaskSubscriptionService {

    /**
     * 添加订阅信息
     *
     * @param pojo pojo
     * @return
     */
    boolean addTaskSubscription(TbClockworkTaskSubscriptionPojo pojo);


    /**
     * 获取订阅信息，通过用户名
     *
     * @param userName userName
     * @return list
     */
    List<TbClockworkTaskSubscriptionPojo> getTaskSubscriptionByUserName(String userName);


    /**
     * 获取订阅信息，通过taskId
     *
     * @param taskId taskId
     * @return list
     */
    List<TbClockworkTaskSubscriptionPojo> getTaskSubscriptionByTaskId(Integer taskId);

}
