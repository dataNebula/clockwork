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

import com.creditease.adx.clockwork.common.entity.StopRunningTaskParam;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:47 下午 2020/4/8
 * @ Description：停止任务服务
 * @ Modified By：
 */
public interface ITaskStopService {

    void stopRunningTask(StopRunningTaskParam stopRunningTaskParam);

    void stopTaskListAndRemoveFromQueue(List<Integer> taskList);

    void stopFillDataTask(String rerunBatchNumber);

}
