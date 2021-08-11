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

package com.creditease.adx.clockwork.worker.controller;

import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.client.service.TaskRerunClientService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.worker.service.TaskRunService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * TaskRunController - 接收所有任务的下发
 * <p>
 * 下发的任务tuple, 放入执行队列中，更改状态为已接收
 *
 * <p>
 * 1。接收例行下发的任务 - runBatchTaskRoutine
 * 2。接收重启下发的任务 - runBatchTaskReRun
 * 3。接收补数下发的任务 - runBatchTaskFillData
 * 4。接收信号下发的任务 - runBatchTaskSignal
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:58 2019-11-29
 * @ Description：任务下发处理 TaskRunController
 * @ Modified By：
 */
@Api("任务运行处理接口")
@RestController
@RequestMapping("/clockwork/worker/task/run")
public class TaskRunController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRunController.class);

    @Autowired
    private TaskRunService taskRunService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskRerunClientService")
    private TaskRerunClientService taskRerunClientService;

    /**
     * 运行routine任务
     *
     * @param taskDistributeTupleRoutine 分发对象
     * @return
     */
    @PostMapping(value = "/batch/routine")
    public Map<String, Object> runBatchTaskRoutine(@RequestBody TaskDistributeTupleRoutine taskDistributeTupleRoutine) {
        try {
            int executeType = taskDistributeTupleRoutine.getExecuteType();
            List<TbClockworkTaskPojo> tasks = taskDistributeTupleRoutine.getTaskPojoList();
            List<Integer> taskIds = taskDistributeTupleRoutine.getTaskIds();
            if (TaskExecuteType.ROUTINE.getCode() == executeType) {
                LOG.info("runBatchTaskRoutine info, executeType = {}, nodeId = {}, taskIds = {}",
                        executeType,
                        taskDistributeTupleRoutine.getNodeId(),
                        taskIds);

                // 获取task
                // 没有直接传递传递的task对象就通过ids获取
                if (CollectionUtils.isEmpty(tasks)) {
                    if (CollectionUtils.isEmpty(taskIds)) {
                        return Response.fail("task info is null");
                    }
                    tasks = taskClientService.getTaskByTaskIds(taskIds);
                    if (CollectionUtils.isEmpty(tasks)) {
                        return Response.fail("[getTaskByTaskIds]task is null");
                    }
                }

                // 加入队列
                taskDistributeTupleRoutine.setTaskPojoList(tasks);
                taskRunService.addTupleToNeedBeExecutedQueue(taskDistributeTupleRoutine);
            }
            return Response.success(tasks.size());
        } catch (Exception e) {
            LOG.error("runBatchTaskRoutine /batch/routine Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 运行重启任务
     *
     * @param taskDistributeTupleReRun 接收重启tuple
     */
    @PostMapping(value = "/batch/rerun")
    public Map<String, Object> runBatchTaskReRun(@RequestBody TaskDistributeTupleReRun taskDistributeTupleReRun) {
        try {
            Long rerunBatchNumber = taskDistributeTupleReRun.getRerunBatchNumber();
            if (rerunBatchNumber == null || rerunBatchNumber < 1) {
                return Response.fail("rerunBatchNumber is null");
            }

            List<Integer> taskIds = taskDistributeTupleReRun.getTaskIds();
            if (CollectionUtils.isEmpty(taskIds)) {
                LOG.info("[TaskRunController] runBatchTaskReRun.getTaskIds() is null, taskIds = {}", taskIds);
                return Response.success(0);
            }

            LOG.info("runBatchTaskReRun info, executeType = {}, nodeId = {}, rerunBatchNumber = {}, taskIds = {}",
                    taskDistributeTupleReRun.getExecuteType(),
                    taskDistributeTupleReRun.getNodeId(),
                    rerunBatchNumber,
                    taskIds);

            // 加入队列
            taskRunService.addTupleToNeedBeExecutedQueue(taskDistributeTupleReRun);
            return Response.success(taskIds.size());
        } catch (Exception e) {
            LOG.error("runBatchTaskReRun /batch/rerun Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 运行补数任务
     *
     * @param taskDistributeTupleFillData 补数tuple
     */
    @PostMapping(value = "/batch/filldata")
    public Map<String, Object> runBatchTaskFillData(@RequestBody TaskDistributeTupleFillData taskDistributeTupleFillData) {
        try {
            Long rerunBatchNumber = taskDistributeTupleFillData.getRerunBatchNumber();
            if (rerunBatchNumber == null || rerunBatchNumber < 1) {
                return Response.fail("rerunBatchNumber is null");
            }

            List<Integer> taskIds = taskDistributeTupleFillData.getTaskIds();
            if (CollectionUtils.isEmpty(taskIds)) {
                LOG.info("[TaskRunController] runBatchTaskFillData.getTaskIds() is null, taskIds = {}", taskIds);
                return Response.success(0);
            }

            LOG.info("runBatchTaskFillData info, executeType = {}, nodeId = {}, rerunBatchNumber = {}, fillDataTime = {}, taskIds = {}",
                    taskDistributeTupleFillData.getExecuteType(),
                    taskDistributeTupleFillData.getNodeId(),
                    rerunBatchNumber,
                    taskDistributeTupleFillData.getFillDataTime(),
                    taskIds);

            // 加入队列
            taskRunService.addTupleToNeedBeExecutedQueue(taskDistributeTupleFillData);
            return Response.success(taskIds.size());
        } catch (Exception e) {
            LOG.error("runBatchTaskFillData /batch/filldata Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 运行信号触发任务
     *
     * @param taskDistributeTupleSignal tuple
     */
    @PostMapping(value = "/batch/signal")
    public Map<String, Object> runBatchTaskSignal(@RequestBody TaskDistributeTupleSignal taskDistributeTupleSignal) {
        try {
            List<TbClockworkTaskPojo> tasks = taskDistributeTupleSignal.getTaskPojoList();
            List<Integer> taskIds = taskDistributeTupleSignal.getTaskIds();
            LOG.info("runBatchTaskSignal info, executeType = {}, nodeId = {}, taskIds = {}",
                    taskDistributeTupleSignal.getExecuteType(),
                    taskDistributeTupleSignal.getNodeId(),
                    taskIds);

            // 获取task
            // 没有直接传递传递的task对象就通过ids获取
            if (CollectionUtils.isEmpty(tasks)) {
                if (CollectionUtils.isEmpty(taskIds)) {
                    return Response.fail("task is null");
                }
                tasks = taskClientService.getTaskByTaskIds(taskIds);
                if (CollectionUtils.isEmpty(tasks)) {
                    return Response.fail("[getTaskByTaskIds]task is null");
                }
            }

            // 加入队列
            taskDistributeTupleSignal.setTaskPojoList(tasks);
            taskRunService.addTupleToNeedBeExecutedQueue(taskDistributeTupleSignal);
            return Response.success(tasks.size());
        } catch (Exception e) {
            LOG.error("runBatchTaskSignal /batch/signal Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
