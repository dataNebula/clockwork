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

package com.creditease.adx.clockwork.master.controller;

import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.master.service.ITaskDistributeService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:54 2019-12-25
 * @ Description：
 * @ Modified By：
 */
@Api("任务分发器")
@RestController
@RequestMapping("/clockwork/master/task/distribute")
public class TaskDistributeController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskDistributeController.class);

    @Autowired
    private ITaskDistributeService taskDistributeService;

    /**
     * 例行任务下发
     *
     * @param taskSubmitInfoRouTine 提交信息
     * @return
     */
    @PostMapping(value = "/routine")
    public Map<String, Object> distributeRoutineTask(@RequestBody TaskSubmitInfoRouTine taskSubmitInfoRouTine) {
        try {
            List<Integer> taskIds = taskSubmitInfoRouTine.getTaskIds();
            if (taskIds == null) {
                return Response.fail("tasks is null");
            }

            if (taskDistributeService.addSubmitInfoToWaitForDistributeQueue(taskSubmitInfoRouTine)) {
                LOG.info("routineTaskDistribute submit task list success, tasks size = {}", taskIds.size());
                return Response.success(true);
            } else {
                LOG.info("routineTaskDistribute submit task list failure, " +
                        "tasks size = {}, taskIds = {}", taskIds.size(), taskIds);
                return Response.fail("Same transaction for submit task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 重启任务下发
     *
     * @param taskSubmitInfoRerun 提交信息
     * @return
     */
    @PostMapping(value = "/rerun")
    public Map<String, Object> distributeReRunTask(@RequestBody TaskSubmitInfoRerun taskSubmitInfoRerun) {
        try {
            List<Integer> taskIds = taskSubmitInfoRerun.getTaskIds();
            if (taskIds == null) {
                return Response.fail("tasks is null");
            }
            if (taskSubmitInfoRerun.getRerunBatchNumber() == null) {
                return Response.fail("rerunBatchNumber is null");
            }
            // 分发任务
            if (taskDistributeService.addSubmitInfoToWaitForDistributeQueue(taskSubmitInfoRerun)) {
                LOG.info("reRunTaskDistribute submit task list success, tasks size = {}", taskIds.size());
                return Response.success(true);
            } else {
                LOG.info("reRunTaskDistribute submit task list failure, " +
                        "tasks size = {}, taskIds = {}", taskIds.size(), taskIds);
                return Response.fail("Same transaction for submit task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 补数任务下发
     *
     * @param taskSubmitInfoFillData 提交信息
     * @return
     */
    @PostMapping(value = "/filldata")
    public Map<String, Object> distributeFillDataTask(@RequestBody TaskSubmitInfoFillData taskSubmitInfoFillData) {
        try {
            List<Integer> taskIds = taskSubmitInfoFillData.getTaskIds();
            LOG.info("fillDataTaskDistribute info, taskIds = {}, rerunBatchNumber = {}, fillDataTime = {}",
                    taskIds, taskSubmitInfoFillData.getRerunBatchNumber(), taskSubmitInfoFillData.getFillDataTime());

            if (taskIds == null) {
                return Response.fail("tasks is null");
            }
            if (taskSubmitInfoFillData.getRerunBatchNumber() == null) {
                return Response.fail("rerunBatchNumber is null");
            }
            if (taskSubmitInfoFillData.getFillDataTime() == null) {
                return Response.fail("fillDataTime is null");
            }

            // 分发任务
            if (taskDistributeService.addSubmitInfoToWaitForDistributeQueue(taskSubmitInfoFillData)) {
                LOG.info("fillDataTaskDistribute submit task list success tasks size = {},", taskIds.size());
                return Response.success(true);
            } else {
                LOG.info("fillDataTaskDistribute submit task list failure,  " +
                        "tasks size = {}, taskIds = {}", taskIds.size(), taskIds);
                return Response.fail("Same transaction for submit task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 信号任务任务下发
     *
     * @param taskSubmitInfoSignal 提交信息
     * @return
     */
    @PostMapping(value = "/signal")
    public Map<String, Object> distributeRoutineTask(@RequestBody TaskSubmitInfoSignal taskSubmitInfoSignal) {
        try {
            List<Integer> taskIds = taskSubmitInfoSignal.getTaskIds();
            if (taskIds == null) {
                return Response.fail("tasks is null");
            }

            // 分发任务
            if (taskDistributeService.addSubmitInfoToWaitForDistributeQueue(taskSubmitInfoSignal)) {

                LOG.info("reRunTaskDistribute submit task list success, tasks size = {}", taskIds.size());
                return Response.success(true);
            } else {
                LOG.info("reRunTaskDistribute submit task list failure, tasks size = {}", taskIds.size());
                return Response.fail("Same transaction for submit task,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
