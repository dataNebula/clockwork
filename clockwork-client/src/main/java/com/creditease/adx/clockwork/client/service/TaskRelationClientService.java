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

import com.creditease.adx.clockwork.client.TaskRelationClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:57 2019-12-04
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskRelationClientService")
public class TaskRelationClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRelationClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected TaskRelationClient taskRelationClient;

    public List<TbClockworkTaskPojo> getTaskDirectlyChildrenNotIncludeSelf(Integer taskId) {
        try {
            Map<String, Object> interfaceResult = taskRelationClient.getTaskDirectlyChildrenNotIncludeSelf(taskId);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)
                    || interfaceResult.get(Constant.DATA) == null) {
                return null;
            }
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public List<TbClockworkTaskPojo> getTaskDirectlyFatherNotIncludeSelf(Integer taskId) {
        try {
            Map<String, Object> interfaceResult = taskRelationClient.getTaskDirectlyFatherNotIncludeSelf(taskId);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return null;
            }
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {});
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }


}
