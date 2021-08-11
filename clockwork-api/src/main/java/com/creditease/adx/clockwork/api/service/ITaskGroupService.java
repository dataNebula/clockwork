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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:29 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
public interface ITaskGroupService {

    /**
     * 添加任务组
     *
     * @param taskGroup taskGroup
     * @return count
     */
    int addTaskGroup(TbClockworkTaskGroup taskGroup);

    /**
     * 修改任务组
     *
     * @param taskGroup taskGroup
     * @return count
     */
    int updateTaskGroup(TbClockworkTaskGroup taskGroup);

    /**
     * 下线任务组
     *
     * @param id 任务组ID
     * @return count
     */
    int disableTaskGroupTx(int id);

    /**
     * 上线任务组
     *
     * @param id 任务组ID
     * @return count
     */
    int enableTaskGroup(int id);

    boolean taskGroupIsExists(String taskGroupName);


    TbClockworkTaskGroup getTaskGroupById(int id);


}
