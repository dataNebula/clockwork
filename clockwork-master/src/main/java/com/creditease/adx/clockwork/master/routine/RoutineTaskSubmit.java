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

package com.creditease.adx.clockwork.master.routine;

import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.client.service.TaskStateClientService;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusSubmit;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoRouTine;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.master.service.impl.TaskDistributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:18 上午 2020/9/11
 * @ Description：submit master task
 * @ Modified By：
 */
@Service
public class RoutineTaskSubmit {

    private static final Logger LOG = LoggerFactory.getLogger(RoutineTaskSubmit.class);

    @Autowired
    private TaskStateClientService taskStateClientService;

    @Autowired
    private TaskDistributeService taskDistributeService;

    @Autowired
    private TaskOperationClientService taskOperationClientService;

    /**
     * Master Routine Task Submit
     *
     * @param taskPojoList list
     * @return
     */
    public boolean submitTask(ArrayList<TbClockworkTaskPojo> taskPojoList) {
        try {
            List<Integer> taskIds = taskPojoList.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList());

            // 批量更新可以提交的任务的状态为SUBMIT
            LOG.info("submitTask taskIds = {}, update status = submit", taskIds);
            boolean result = taskOperationClientService.updateTaskStatusSubmitBatch(new BatchUpdateTaskStatusSubmit(taskIds));
            if (!result) {
                LOG.error("[submitTask-updateTaskStatusBatch] update status Error. batch update tasks status to submit, "
                        + "task.size = {}, taskIds = {}", taskIds.size(), taskIds);
                throw new RuntimeException("updateTaskStatusBatch update status Error.");
            }

            // 处理SUBMIT状态的任务相关逻辑
            new SubmitTaskHandleThread(taskStateClientService, taskDistributeService, taskPojoList).start();
        } catch (Exception e) {
            LOG.error("submitTask Error {}.", e.getMessage(), e);
            return false;
        }

        return true;
    }

    /**
     * Master例行任务提交线程，提交任务到分发器
     */
    private static class SubmitTaskHandleThread extends Thread {
        private TaskStateClientService taskStateClientService;
        private TaskDistributeService taskDistributeService;
        private ArrayList<TbClockworkTaskPojo> taskPojoList;

        SubmitTaskHandleThread(TaskStateClientService taskStateClientService, TaskDistributeService taskDistributeService,
                               ArrayList<TbClockworkTaskPojo> taskPojoList) {
            this.taskStateClientService = taskStateClientService;
            this.taskDistributeService = taskDistributeService;
            this.taskPojoList = taskPojoList;
        }

        @Override
        public void run() {

            try {

                // 构建 TaskSubmitInfoRouTine
                TaskSubmitInfoRouTine taskSubmitInfo = new TaskSubmitInfoRouTine(taskPojoList);

                /*
                 * 任务状态处理，任务修改为提交状态Submit, 设置LogId, 不再该方法里面修改任务状态
                 */
                taskStateClientService.taskStateSubmitNotIncludeTask(taskSubmitInfo);

                /*
                 * 任务分发submit信息到任务分发器
                 * taskDistributeService
                 */
                taskDistributeService.addSubmitInfoToWaitForDistributeQueue(taskSubmitInfo);
            } catch (Exception e) {
                LOG.error("SubmitTaskHandle Error {}.", e.getMessage(), e);
            }
        }
    }
}
