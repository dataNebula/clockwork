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

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "${api.service.name}")
public interface LoopClockClient {

    /**
     * Master服务调用此接口，更新作业下一次执行槽位和前端界面显示的触发时间信息，此接口采用批量方式
     *
     * @return
     */
    @PostMapping(value = "/clockwork/api/loop/clock/addTaskToLoopClockSlotByBatch")
    Map<String, Object> addTaskToLoopClockSlotByBatch(@RequestBody List<TbClockworkTaskPojo> sysTbClockworkTaskPojos);

    /**
     * 构建作业环形时钟信息
     *
     * @return
     */
    @PostMapping(value = "/clockwork/api/loop/clock/buildTaskLoopClock")
    Map<String, Object> buildTaskLoopClock();

    /**
     * 获取指定时间槽位的作业
     *
     * @return
     */
    @GetMapping(value = "/clockwork/api/loop/clock/getHasCronTaskFromSlot")
    Map<String, Object> getHasCronTaskFromSlot(@RequestParam(value = "slotPosition") Integer slotPosition);

    /**
     * 添加任务下一次执行时间到执行的槽位
     *
     * @param taskId
     * @param nextMatchingTimeNumber
     * @return
     */
    @PostMapping(value = "/clockwork/api/loop/clock/addTaskToLoopClockSlot")
    Map<String, Object> addTaskToLoopClockSlot(
            @RequestParam(value = "taskId") Integer taskId,
            @RequestParam(value = "nextMatchingTimeNumber") Integer nextMatchingTimeNumber);


}
