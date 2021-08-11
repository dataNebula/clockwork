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

package com.creditease.adx.clockwork.client;

import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoFillData;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoRerun;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoRouTine;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoSignal;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:50 下午 2020/4/1
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${master.service.name}")
public interface TaskDistributeClient {

    /**
     * 分发例行任务
     *
     * @param taskSubmitInfo 任务
     * @return
     */
    @PostMapping(value = "/clockwork/master/task/distribute/routine")
    Map<String, Object> distributeRoutineTask(@RequestBody TaskSubmitInfoRouTine taskSubmitInfo);

    /**
     * 分发重跑任务
     *
     * @param taskSubmitInfo 任务
     * @return
     */
    @PostMapping(value = "/clockwork/master/task/distribute/rerun")
    Map<String, Object> distributeReRunTask(@RequestBody TaskSubmitInfoRerun taskSubmitInfo);

    /**
     * 分发补数任务
     *
     * @param taskSubmitInfo 提交信息
     * @return
     */
    @PostMapping(value = "/clockwork/master/task/distribute/filldata")
    Map<String, Object> distributeFillDataTask(@RequestBody TaskSubmitInfoFillData taskSubmitInfo);

    /**
     * 分发信号任务
     *
     * @param taskSubmitInfoSignal 提交信息
     * @return
     */
    @PostMapping(value = "/clockwork/master/task/distribute/signal")
    Map<String, Object> distributeSignalTask(@RequestBody TaskSubmitInfoSignal taskSubmitInfoSignal);


}
