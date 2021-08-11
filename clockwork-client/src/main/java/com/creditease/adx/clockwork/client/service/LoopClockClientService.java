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

package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.LoopClockClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:41 下午 2019/9/10
 * @ Description：
 * @ Modified By：
 */
@Service(value = "loopClockClientService")
public class LoopClockClientService {

    private static final Logger LOG = LoggerFactory.getLogger(LoopClockClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected LoopClockClient loopClockClient;

    /**
     * 构建作业环形时钟信息
     *
     * @return
     */
    public boolean buildTaskLoopClock() {
        Map<String, Object> interfaceResult = loopClockClient.buildTaskLoopClock();
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 添加任务下一次执行时间到执行的槽位
     *
     * @param taskId                 taskId
     * @param nextMatchingTimeNumber 默认为1
     * @return
     */
    public boolean addTaskToLoopClockSlot(Integer taskId, Integer nextMatchingTimeNumber) {
        try {
            if (nextMatchingTimeNumber == null || nextMatchingTimeNumber < 1) {
                nextMatchingTimeNumber = 1;
            }
            Map<String, Object> interfaceResult
                    = loopClockClient.addTaskToLoopClockSlot(taskId, nextMatchingTimeNumber);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return false;
            }
            // 业务返回值判断
            return (boolean) interfaceResult.get(Constant.DATA);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;

    }

    /**
     * 批量添加任务LoopClockSlot
     * Master服务下发失败后调用此接口，以及Worker加入到队列中后调用此接口
     * 更新作业下一次执行槽位和前端界面显示的下一次触发时间信息，此接口采用批量方式
     *
     * @param taskPojoList task list
     * @return
     */
    public boolean addTaskToLoopClockSlotByBatch(List<TbClockworkTaskPojo> taskPojoList) {
        Map<String, Object> interfaceResult = loopClockClient.addTaskToLoopClockSlotByBatch(taskPojoList);
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return false;
        }
        // 业务返回值判断
        return (boolean) interfaceResult.get(Constant.DATA);
    }

    /**
     * 获取指定时间槽位的作业
     *
     * @return
     */
    public List<TbClockworkTaskPojo> getHasCronTaskFromSlot(int slotPosition) {
        // 获取指定时间槽位的作业
        Map<String, Object> interfaceResult = loopClockClient.getHasCronTaskFromSlot(slotPosition);
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)
                || interfaceResult.get(Constant.DATA) == null) {
            return new ArrayList<>();
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });

    }

}
