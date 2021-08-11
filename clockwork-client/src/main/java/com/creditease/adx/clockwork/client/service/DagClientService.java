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

import com.creditease.adx.clockwork.client.DagClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:45 2019-12-04
 * @ Description：DAG Service Client
 * @ Modified By：
 */
@Service(value = "dagClientService")
public class DagClientService {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private DagClient dagClient;

    /**
     * 更新dag信息，以及获取dag信息（更新TaskCount、LeaderTaskId、LeaderTaskName、Description、UpdateTime）
     *
     * @param dagId dag id
     * @return
     */
    public TbClockworkDag refreshDagInfoById(Integer dagId) {
        Map<String, Object> interfaceResult = dagClient.refreshDagInfoById(dagId);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)
                || interfaceResult.get(Constant.DATA) == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA), new TypeReference<TbClockworkDag>() {
        });
    }


    /**
     * 清除空的DAG信息（没有task引用）
     *
     * @return boolean
     */
    public boolean cleanEmptyDagInfo() {
        Map<String, Object> interfaceResult = dagClient.cleanEmptyDagInfo();
        // 接口CODE 代码判断
        return HttpUtil.checkInterfaceCodeSuccess(interfaceResult)
                && interfaceResult.get(Constant.DATA) != null;
    }


    /**
     * 构建任务dagId
     *
     * @return dagId
     */
    public int buildDagIdForTaskId(Integer taskId) {
        try {
            Map<String, Object> interfaceResult = dagClient.buildDagIdForTaskId(taskId);
            // 接口CODE 代码判断
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)
                    || interfaceResult.get(Constant.DATA) == null) {
                return -1;
            }
            return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA), new TypeReference<Integer>() {
            });

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


}
