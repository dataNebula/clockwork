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

import com.creditease.adx.clockwork.common.entity.BatchGetTaskLogByTaskIdsParam;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskLogStatusParam;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "${api.service.name}")
public interface TaskLogClient {

    @PostMapping(value = "/clockwork/api/task/log/addToKafkaQueue")
    Map<String, Object> addToKafkaQueue(@RequestBody List<TbClockworkTaskLogPojo> logPojoList);

    /**
     * 批量添加
     *
     * @param taskLogList
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/addBatchTaskLog", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> addBatchTaskLog(@RequestBody List<TbClockworkTaskLog> taskLogList);

    /**
     * 修改日志信息
     *
     * @param TbClockworkTaskLogPojo
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/updateTaskLog", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> updateTaskLog(@RequestBody TbClockworkTaskLogPojo TbClockworkTaskLogPojo);

    /**
     * 批量更新任务日志
     *
     * @param batchUpdateTaskLogStatusParam param
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/updateBatchTaskLogStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> updateBatchTaskLogStatus(@RequestBody BatchUpdateTaskLogStatusParam batchUpdateTaskLogStatusParam);

    /**
     * 更新LogName
     *
     * @param id      id
     * @param logName logName
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/updateTaskLogLogName")
    Map<String, Object> updateTaskLogLogName(@RequestParam(value = "id") Integer id,
                                             @RequestParam(value = "logName") String logName);

    /**
     * 更新Pid
     *
     * @param id  id
     * @param pid pid
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/updateTaskLogPid")
    Map<String, Object> updateTaskLogPid(@RequestParam(value = "id") Integer id,
                                         @RequestParam(value = "pid") Integer pid);

    /**
     * 获得日志信息根据ID
     *
     * @param taskLogId
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/log/getTaskLogById")
    Map<String, Object> getTaskLogById(@RequestParam(value = "taskLogId") Integer taskLogId);

    @GetMapping(value = "/clockwork/api/task/log/getFillDataTaskLogByTaskIds")
    Map<String, Object> getFillDataTaskLogByTaskIds(@RequestParam(value = "taskIds") List<Integer> taskIds,
                                                    @RequestParam(value = "rerunBatchNumber") long rerunBatchNumber,
                                                    @RequestParam(value = "fillDataTime") String fillDataTime);

    @PostMapping(value = "/clockwork/api/task/log/getTaskLogByTaskIdsAndStatusList")
    Map<String, Object> getTaskLogByTaskIdsAndStatusList(@RequestBody BatchGetTaskLogByTaskIdsParam param);

    /**
     * 根据taskId获得日志信息
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/log/getTaskLogByTaskId")
    Map<String, Object> getTaskLogByTaskId(@RequestParam(value = "taskId") Integer taskId);

    /**
     * 根据任务的状态获取任务的执行日志信息
     *
     * @param status
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/log/getTaskLogByTaskStatus")
    Map<String, Object> getTaskLogByTaskStatus(@RequestParam(value = "status") String status);


    /**
     * 分页列表
     *
     * @param pageParam
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/log/searchPageList")
    Map<String, Object> searchPageList(@RequestBody PageParam pageParam);


    /**
     * 获取未结束的日志记录
     */
    @GetMapping(value = "/clockwork/api/task/log/getAllNotEndTaskLog")
    Map<String, Object> getAllNotEndTaskLog();


    /**
     * 获取最后一个日志接口
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/log/getLatestTaskLogFileParamByTaskId")
    Map<String, Object> getLatestTaskLogFileParamByTaskId(@RequestParam(value = "taskId") Integer taskId);
}

