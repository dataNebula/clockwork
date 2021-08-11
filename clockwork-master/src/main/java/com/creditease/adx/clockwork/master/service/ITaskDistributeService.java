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

package com.creditease.adx.clockwork.master.service;

import com.creditease.adx.clockwork.master.service.distribute.DistributePolicyInterface;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:01 下午 2020/4/1
 * @ Description：
 * @ Modified By：
 */
public interface ITaskDistributeService {

    boolean addSubmitInfoToWaitForDistributeQueue(TaskSubmitInfo taskSubmitInfo);

    boolean taskDistributeToWorker(TaskSubmitInfo taskSubmitInfo, DistributePolicyInterface policy) throws Exception;

    int removeTaskFromWaitForDistributeQueue(int taskId);

}
