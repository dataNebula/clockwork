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

import com.creditease.adx.clockwork.client.TaskRerunClient;
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
@Service(value = "taskRerunClientService")
public class TaskRerunClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRerunClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public TaskRerunClient getTaskRerunClient() {
        return taskRerunClient;
    }

    @Autowired
    protected TaskRerunClient taskRerunClient;

    public List<TbClockworkTaskPojo> getTaskRerunChild(Integer taskId, Long batchNum) {
        List<TbClockworkTaskPojo> result = null;
        try {
            Map<String, Object> interfaceResult = taskRerunClient.getTaskRerunChild(taskId, batchNum);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                LOG.info("[TaskRerunClientService-getTaskRerunChild] DATA is null");
                return null;
            }
            result = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    public List<TbClockworkTaskPojo> getTaskRerunFather(Integer taskId, Long batchNum) {
        List<TbClockworkTaskPojo> result = null;
        try {
            Map<String, Object> interfaceResult = taskRerunClient.getTaskRerunFather(taskId, batchNum);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                LOG.error("[TaskRerunClientService-getTaskRerunFather] DATA is null");
                return null;
            }
            result = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    public List<Integer> getTaskRerunFatherIds(Integer taskId, Long batchNum) {
        List<Integer> result = null;
        try {
            Map<String, Object> interfaceResult = taskRerunClient.getTaskRerunFatherIds(taskId, batchNum);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                LOG.error("[TaskRerunClientService-getTaskRerunFatherIds] DATA is null");
                return null;
            }
            result = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<Integer>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    public TbClockworkTaskPojo getTaskRerunByTaskId(Integer id, Long batchNum) {
        TbClockworkTaskPojo result = null;
        try {
            Map<String, Object> interfaceResult = taskRerunClient.getTaskRerunByTaskId(id, batchNum);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                LOG.error("[TaskRerunClientService]getTaskRerunByTaskId DATA == null");
                return null;
            }
            result = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<TbClockworkTaskPojo>() {
                    });
        } catch (Exception e) {
            LOG.error("[TaskRerunClientService]getTaskRerunByTaskIds msg: {}", e.getMessage(), e);
        }
        return result;
    }

    public List<TbClockworkTaskPojo> getTaskRerunByTaskIds(List<Integer> ids, Long batchNum) {
        List<TbClockworkTaskPojo> result = null;
        try {
            Map<String, Object> interfaceResult = taskRerunClient.getTaskRerunByTaskIds(ids, batchNum);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                LOG.error("[TaskRerunClientService-getTaskRerunByTaskIds] DATA is null");
                return null;
            }
            result = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                    });
        } catch (Exception e) {
            LOG.error("[TaskRerunClientService]getTaskRerunByTaskIds msg: {}", e.getMessage(), e);
        }
        return result;
    }

    public List<Integer> getTaskRerunRootTaskIds(Long rerunBatchNumber) {
        List<Integer> result = null;
        try {
            Map<String, Object> interfaceResult = taskRerunClient.getTaskRerunRootTaskIds(rerunBatchNumber);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                LOG.error("[TaskRerunClientService-getTaskRerunRootTaskIds] DATA is null");
                return null;
            }
            result = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<Integer>>() {
                    });
        } catch (Exception e) {
            LOG.error("[TaskRerunClientService-getTaskRerunRootTaskIds] Error. msg: {}", e.getMessage(), e);
        }
        return result;
    }


}
