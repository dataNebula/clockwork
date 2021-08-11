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

import com.creditease.adx.clockwork.client.TaskClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDependencyScript;
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
 * @ Date       ：Created in 11:44 2019-12-04
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskClientService")
public class TaskClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected TaskClient taskClient;

    public TaskClient getTaskClient() {
        return taskClient;
    }

    /**
     * 获得任务详细信息，根据ID
     *
     * @param taskId
     * @return
     */
    public TbClockworkTaskPojo getTaskById(Integer taskId) {
        TbClockworkTaskPojo result = null;
        try {
            Map<String, Object> interfaceResult = taskClient.getTaskById(taskId);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            result = OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<TbClockworkTaskPojo>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 获得任务列表详细信息，根据ids
     *
     * @param ids task list id
     * @return
     */
    public List<TbClockworkTaskPojo> getTaskByTaskIds(List<Integer> ids) {
        List<TbClockworkTaskPojo> result = null;
        try {
            Map<String, Object> interfaceResult = taskClient.getTaskByTaskIds(ids);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
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

    /**
     * 获取依赖的脚本文件
     *
     * @param taskId task id
     * @return
     */
    public List<TbClockworkTaskDependencyScript> getDependencyScriptFileByTaskId(Integer taskId) {
        List<TbClockworkTaskDependencyScript> result = null;
        try {
            Map<String, Object> interfaceResult = taskClient.getDependencyScriptFileByTaskId(taskId);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            result = OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA),
                    new TypeReference<List<TbClockworkTaskDependencyScript>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 根据task状态获取taskList
     *
     * @return taskList
     */
    public List<TbClockworkTaskPojo> getTaskByStatus(String status) {
        Map<String, Object> interfaceResult = taskClient.getTaskByStatus(status);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });
    }

    /**
     * 获取online的任务
     *
     * @param status status
     * @return
     */
    public List<TbClockworkTaskPojo> getTaskOnlineByStatus(String status) {
        Map<String, Object> interfaceResult = taskClient.getTaskOnlineByStatus(status);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });
    }


    /**
     * 获取失败状态一段时间内的online的任务
     *
     * @param status status list
     * @return
     */
    public List<TbClockworkTaskPojo> getTaskByRunFailedStatus(List<String> status, String beforeDateStr, String currentDateStr) {
        Map<String, Object> interfaceResult = taskClient.getTaskByRunFailedStatus(status, beforeDateStr, currentDateStr);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });
    }

    /**
     * 根据taskList状态列表，获取taskList
     *
     * @return taskList
     */
    public List<TbClockworkTaskPojo> getTaskListByStatusList(List<String> statusList) {
        Map<String, Object> interfaceResult = taskClient.getTaskListByStatusList(statusList);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });
    }

    /**
     * 获取任务， 根据delay status
     *
     * @param delayStatus delay status
     * @return
     */
    public List<TbClockworkTaskPojo> getTaskByDelayStatus(int delayStatus) {
        Map<String, Object> interfaceResult = taskClient.getTaskByDelayStatus(delayStatus);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });
    }

    /**
     * 获取任务， 根据dag id
     *
     * @param dagId dag id
     * @return
     */
    public List<TbClockworkTaskPojo> getTasksByDagId(int dagId) {
        Map<String, Object> interfaceResult = taskClient.getTasksByDagId(dagId);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });
    }

    /**
     * 获取任务， 根据dag id（不包括dds）
     *
     * @param dagId dag id
     * @return
     */
    public List<TbClockworkTaskPojo> getTasksNotIncludeDDSByDagId(int dagId) {
        Map<String, Object> interfaceResult = taskClient.getTasksNotIncludeDDSByDagId(dagId);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
                });
    }

    /**
     * 根据source 获取到所有的dagId
     *
     * @param source source
     * @return
     */
    public List<Integer> getTaskDagIdsByCrossSource(Integer source) {
        Map<String, Object> interfaceResult = taskClient.getTaskDagIdsByCrossSource(source);
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<List<Integer>>() {
                });
    }



    /**
     * 获取任务状态
     *
     * @param taskId task id
     * @return
     */
    public String getTaskStatusById(Integer taskId) {
        try {
            Map<String, Object> interfaceResult = taskClient.getTaskStatusById(taskId);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            return (String) interfaceResult.get(Constant.DATA);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 上传文件前缀
     *
     * @return
     */
    public String[] getTaskUploadPathPrefix() {
        Map<String, Object> interfaceResult = taskClient.getTaskUploadPathPrefix();
        if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) return null;
        return OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<String[]>() {
                });
    }

    /**
     * 获得任务列表详细信息(分页)
     *
     * @param
     * @return
     */
    public Map<String, Object> searchPageListTask(PageParam pageParam) {
      return taskClient.searchPageListTask(pageParam);
    }

}
