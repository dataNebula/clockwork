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

package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.ITaskRelationService;
import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.api.service.ITaskStopService;
import com.creditease.adx.clockwork.api.service.ITaskSubmitService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.StopRunningTaskParam;
import com.creditease.adx.clockwork.common.enums.RedisLockKey;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:43 下午 2020/4/8
 * @ Description：停止任务
 * @ Modified By：
 */
@Api("停止任务接口")
@RestController
@RequestMapping("/clockwork/api/task/stop")
public class TaskStopController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskStopController.class);

    @Resource(name = "taskStopService")
    private ITaskStopService taskStopService;

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Autowired
    private ITaskRelationService taskRelationService;

    /**
     * kill任务 (停止正在运行的任务)
     *
     * @param stopRunningTaskParam param
     * @return
     */
    @PostMapping(value = "/stopRunningTask")
    public Map<String, Object> stopRunningTask(@RequestBody StopRunningTaskParam stopRunningTaskParam) {


        if (stopRunningTaskParam == null) {
            LOG.error("stopRunningTaskParam is null.please check it.");
            return Response.fail("task id that need to be stopped is null.");
        }

        TbClockworkTaskLogPojo needTobeKilledTaskLog = stopRunningTaskParam.getTaskLogPojo();
        Integer killSceneFlag = stopRunningTaskParam.getKillSceneFlag();
        String operatorName = stopRunningTaskParam.getOperatorName();
        if (needTobeKilledTaskLog == null) {
            LOG.error("needTobeKilledTaskLog is null.please check it.");
            return Response.fail("needTobeKilledTaskLog is null.please check it.");
        }

        if (killSceneFlag == null) {
            LOG.error("killSceneFlag is null.please check it.");
            return Response.fail("killSceneFlag is null.please check it.");
        }

        Integer taskId = needTobeKilledTaskLog.getTaskId();
        Integer taskLogId = needTobeKilledTaskLog.getId();
        Integer nodeId = needTobeKilledTaskLog.getNodeId();

        boolean getLock = false;
        try {
            // 分布式锁环境下安全进行
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                taskStopService.stopRunningTask(stopRunningTaskParam);
                LOG.info("kill task info launched success, task id = {}, task log id = {}, kill scene flag = {}, " +
                                "node id = {}, operatorName = {}",
                        taskId, taskLogId, killSceneFlag, nodeId, operatorName);
                return Response.success(true);
            } else {
                LOG.info("kill task info launched failure, task id = {}, task log id = {}, kill scene flag = {}, " +
                                "node id = {}, operatorName = {}",
                        taskId, taskLogId, killSceneFlag, nodeId, operatorName);
                return Response.fail("Same transaction for update task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error("TaskStopController-stopRunningTask task id = {}, task log id = {}, kill scene flag = {}, " +
                            "node id = {}, Error {}.",
                    taskId, taskLogId, killSceneFlag, nodeId, e.getMessage(), e);
            return Response.fail(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 停止单个任务
     *
     * @param taskId 任务ID
     * @return
     */
    @ApiOperation(value = "停止单个任务", notes = "停止主要针对例行，重启的任务")
    @PostMapping("/stopTask")
    public Map<String, Object> stopTask(@RequestParam(value = "taskId") Integer taskId) {
        if (taskId == null || taskId < 1) {
            LOG.error("task id that need to be stopped is invalid.please check it.");
            return Response.fail("task id that need to be stopped is null.");
        }

        boolean getLock = false;
        try {
            // 分布式锁环境下安全进行
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                taskStopService.stopTaskListAndRemoveFromQueue(Collections.singletonList(taskId));
                LOG.info("stop task success, task id = {}", taskId);
                return Response.success(true);
            } else {
                LOG.info("stop task failure, task id = {}", taskId);
                return Response.fail("Same transaction for update task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 停止多个任务
     *
     * @param taskIdList 任务ID集合
     * @return
     */
    @ApiOperation(value = "停止多个任务", notes = "停止主要针对例行，重启的任务")
    @PostMapping(value = "/stopTaskList")
    public Map<String, Object> stopTaskListAndRemoveFromQueue(@RequestBody List<Integer> taskIdList) {
        if (CollectionUtils.isEmpty(taskIdList)) {
            LOG.error("task list that need to be stopped is null.please check it.");
            return Response.fail("task list that need to be stopped is null.");
        }
        boolean getLock = false;
        try {
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                taskStopService.stopTaskListAndRemoveFromQueue(taskIdList);
                LOG.info("stop task list success,task size = {}", taskIdList.size());
                return Response.success(taskIdList.size());
            } else {
                LOG.info("stop task list failure,task size = {}", taskIdList.size());
                return Response.fail("Same transaction for update task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 停止补数任务
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @ApiOperation(value = "停止补数任务", notes = "停止补数任务，根据批次号停止该批次在运行的所有任务")
    @PostMapping(value = "/stopFillDataTask")
    public Map<String, Object> stopFillDataTask(@RequestParam(value = "rerunBatchNumber") String rerunBatchNumber) {
        if (rerunBatchNumber == null) {
            LOG.error("stopFillDataTask task list that need to be stopped is null.please check it.");
            return Response.fail("stopFillDataTask task list that need to be stopped is null.");
        }

        boolean getLock = false;
        try {
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                taskStopService.stopFillDataTask(rerunBatchNumber);
                LOG.info("stopFillDataTask stop task list success, rerunBatchNumber = {}", rerunBatchNumber);
                return Response.success(true);
            } else {
                LOG.info("stopFillDataTask stop task list failure, rerunBatchNumber = {}", rerunBatchNumber);
                return Response.fail("Same transaction for update task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error("stopFillDataTask Error. Msg : {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 停止任务和他的子任务
     *
     * @param taskId 任务和他的子任务ID集合
     * @return
     */
    @ApiOperation(value = "停止任务以及他的子任务|| 停止所有子任务", notes = "停止主要针对例行，重启的任务")
    @PostMapping(value = "/stopTaskAndChirldens")
    public Map<String, Object> stopTaskAndChirldens(@RequestParam(value = "taskId") Integer taskId,
                                                    @RequestParam(value = "stopType") Integer stopType) {
//        1.根据他的任务id，获得他的所有子任务id
        List<Integer> taskIdList = taskRelationService.getAllChildrenAndSelfIds(taskId,stopType);
        if (CollectionUtils.isEmpty(taskIdList)) {
            LOG.error("task list that need to be stopped is null.please check it.");
            return Response.fail("task list that need to be stopped is null.");
        }
        boolean getLock = false;
        try {
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                taskStopService.stopTaskListAndRemoveFromQueue(taskIdList);
                LOG.info("stop task list success,task size = {}", taskIdList.size());
                return Response.success(taskIdList.size());
            } else {
                LOG.info("stop task list failure,task size = {}", taskIdList.size());
                return Response.fail("Same transaction for update task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

}
