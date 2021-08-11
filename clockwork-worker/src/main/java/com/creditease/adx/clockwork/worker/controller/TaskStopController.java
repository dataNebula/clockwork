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

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.StopRunningTaskParam;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.worker.service.TaskKillService;
import com.creditease.adx.clockwork.worker.service.TaskRunService;
import com.creditease.adx.clockwork.worker.service.task.execute.FillDataTaskExecuteService;
import com.creditease.adx.clockwork.worker.service.task.execute.ReRunTaskExecuteService;
import com.creditease.adx.clockwork.worker.service.task.execute.RoutineTaskExecuteService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:34 2020/4/9
 * @ Description：stop
 * @ Modified By：
 */
@Api("任务停止相关接口")
@RestController
@RequestMapping("/clockwork/worker/task/stop")
public class TaskStopController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskStopController.class);

    @Autowired
    private TaskRunService taskRunService;

    @Autowired
    private RoutineTaskExecuteService routineTaskExecuteService;

    @Autowired
    private ReRunTaskExecuteService reRunTaskExecuteService;

    @Autowired
    private FillDataTaskExecuteService fillDataTaskExecuteService;

    @Autowired
    private TaskKillService taskKillService;

    /**
     * 移除taskRun队列中的任务(不包括补数任务)
     *
     * @param taskId 任务id
     * @param isAuto auto
     * @return
     */
    @PostMapping(value = "/removeTaskFromExecutedQueue")
    public Map<String, Object> removeTaskFromNeedBeExecutedQueue(@RequestParam(value = "taskId") Integer taskId,
                                                                 @RequestParam(value = "isAuto") Boolean isAuto) {
        try {
            if (taskId == null) {
                LOG.error("TaskStopController-removeTaskFromNeedBeExecutedQueue taskId is null.");
                return Response.fail("taskId is null.");
            }

            // 需要从TaskNeedBeExecutedQueue队列中移除
            int count = taskRunService.removeTaskFromNeedBeExecutedQueue(taskId);
            LOG.info("[TaskStopController]removeTaskFromNeedBeExecutedQueue taskId = {}, count = {}", taskId, count);

            // 需要从TaskExecuteService队列中移除
            if (count == 0) {
                count = routineTaskExecuteService.removeTaskExecuteServiceQueue(
                        routineTaskExecuteService.getRoutineTaskExecuteServiceQueue(), taskId);
                LOG.info("[TaskStopController]routineTaskExecuteService-removeTaskExecuteServiceQueue " +
                        "taskId = {}, count = {}", taskId, count);
            }

            if (count == 0) {
                count = reRunTaskExecuteService.removeTaskExecuteServiceQueue(
                        reRunTaskExecuteService.getReRunTaskExecuteServiceQueue(), taskId);
                LOG.info("[TaskStopController]reRunTaskExecuteService-removeTaskExecuteServiceQueue " +
                        "taskId = {}, count = {}", taskId, count);
            }
            // 最终不存在队列中
            if (count == 0) {
                LOG.info("[removeTaskFromExecutedQueue] " +
                        "Does not exist in the queue, " +
                        "taskId = {}, isAuto = {}", taskId, isAuto);
            }
            return Response.success(count);
        } catch (Exception e) {
            LOG.error("[removeTaskFromNeedBeExecutedQueue] Error. msg: {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 移除taskRun队列中的任务(补数任务)
     *
     * @param taskId 任务id
     * @param isAuto auto
     * @return
     */
    @PostMapping(value = "/removeFillDataTaskFromExecutedQueue")
    public Map<String, Object> removeFillDataTaskFromExecutedQueue(@RequestParam(value = "taskId") Integer taskId,
                                                                   @RequestParam(value = "isAuto") Boolean isAuto) {
        try {
            if (taskId == null) {
                LOG.error("removeFillDataTaskFromExecutedQueue taskId is null.");
                return Response.fail("taskId is null.");
            }

            // 需要从TaskNeedBeExecutedQueue队列中移除(补数任务)
            int count = taskRunService.removeTaskFillDataFromNeedBeExecutedQueue(taskId);
            LOG.info("[TaskStopController-removeTaskFillDataFromNeedBeExecutedQueue] taskId = {}, count = {}", taskId, count);

            // 需要从FillDataTaskExecuteService补数任务队列中移除
            // 补数队列中的状态已经是submit所以需要修改状态为killed
            if (count == 0) {
                count = fillDataTaskExecuteService.removeTaskExecuteServiceQueue(
                        fillDataTaskExecuteService.getFillDataTaskExecuteServiceQueue(), taskId);
                LOG.info("[TaskStopController-removeTaskExecuteServiceQueue]fillDataTaskExecuteService " +
                        "taskId = {}, count = {}", taskId, count);
            }

            // 最终不存在队列中
            if (count == 0) {
                LOG.info("[removeTaskFromNeedBeExecutedQueue] " +
                        "Does not exist in the queue, " +
                        "taskId = {}, isAuto = {}", taskId, isAuto);
            }

            return Response.success(count);
        } catch (Exception e) {
            LOG.error("[removeTaskFromNeedBeExecutedQueue] Error. msg: {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 停止杀死运行的任务
     *
     * @param stopRunningTaskParam stopRunningTaskParam
     * @return
     */
    @PostMapping(value = "/stopRunningTask")
    public Map<String, Object> stopRunningTaskLog(@RequestBody StopRunningTaskParam stopRunningTaskParam) {

        if (stopRunningTaskParam == null) {
            LOG.error("[TaskStopController-stopRunningTaskLog]stopRunningTaskParam is null.");
            return Response.fail("stopRunningTaskParam is null");
        }

        // 参数判断
        TbClockworkTaskLogPojo taskLogPojo = stopRunningTaskParam.getTaskLogPojo();
        Integer killSceneFlag = stopRunningTaskParam.getKillSceneFlag();

        if (taskLogPojo == null) {
            LOG.error("[TaskStopController-stopRunningTaskLog]taskLog information is null.");
            return Response.fail("taskLog information is null");
        }

        if (taskLogPojo.getId() == null || taskLogPojo.getId() < 1) {
            LOG.error("[TaskStopController-stopRunningTaskLog]the id of taskLog information is null.");
            return Response.fail("the id of taskLog information is null.");
        }

        if (taskLogPojo.getPid() == null || taskLogPojo.getPid() < 1) {
            LOG.error("[TaskStopController-stopRunningTaskLog]the pid of taskLog information is null.");
            return Response.fail("the pid of taskLog information is null.");
        }

        if (killSceneFlag == null || killSceneFlag < 1) {
            LOG.error("[TaskController-killRunningTaskLog]the flag of kill scene information is null.");
            return Response.fail("the flag of kill scene information is null.");
        }

        try {
            LOG.info("[TaskStopController-stopRunningTaskLog]receive msg to stop task," +
                            "TaskId = {}, Pid = {}, executeType = {}, operatorName = {}, rerunBatchNumber = {}",
                    taskLogPojo.getTaskId(), taskLogPojo.getPid(), taskLogPojo.getExecuteType(),
                    stopRunningTaskParam.getOperatorName(), taskLogPojo.getRerunBatchNumber());

            return Response.success(taskKillService.addTaskWaitingToBeKilled(taskLogPojo, killSceneFlag));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
