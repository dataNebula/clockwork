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

package com.creditease.adx.clockwork.worker.service;

import com.creditease.adx.clockwork.client.service.DagClientService;
import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.common.enums.TaskSource;
import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.enums.TimeCycle;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.CronExpression;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.common.util.TaskUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:06 2019-11-28
 * @ Description：TaskLifecycleService
 */
@Service
public class TaskLifecycleService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLifecycleService.class);

    @Resource(name = "dagClientService")
    private DagClientService dagClientService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskOperationClientService")
    private TaskOperationClientService taskOperationClientService;

    private int skipTimeDifference = 120 * 1000;

    /**
     * 检查是否有需要重置生命周期的任务，如果有则设置
     *
     * @param canBeSubmitToWorkerTasks
     */
    public void checkAndResetTaskLifecycle(List<TbClockworkTaskPojo> canBeSubmitToWorkerTasks) {
        if (CollectionUtils.isEmpty(canBeSubmitToWorkerTasks)) {
            LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle]canBeSubmitToWorkerTasks is null.");
            return;
        }

        List<Integer> needResetStatusTaskIds = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (TbClockworkTaskPojo canBeSubmitToSlaveTask : canBeSubmitToWorkerTasks) {
            // 如果当前任务没有父任务并且有时间维度配置，则认为此任务是最顶层的任务，需要进行任务生命周期重置
            if (!checkTaskIfNeedResetLifecycle(canBeSubmitToSlaveTask)) {
                LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle-0]do not need reset life cycle," +
                                "task id = {},task dependency id = {},task cron exp = {},task group id = {}",
                        canBeSubmitToSlaveTask.getId(), canBeSubmitToSlaveTask.getDependencyId(),
                        canBeSubmitToSlaveTask.getCronExp(), canBeSubmitToSlaveTask.getGroupId());
                continue;
            } else {
                LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle-1]need reset life cycle," +
                                "task id = {},task dependency id = {},task cron exp = {},task group id = {}",
                        canBeSubmitToSlaveTask.getId(), canBeSubmitToSlaveTask.getDependencyId(),
                        canBeSubmitToSlaveTask.getCronExp(), canBeSubmitToSlaveTask.getGroupId());
                needResetStatusTaskIds.add(canBeSubmitToSlaveTask.getId());
            }
            needResetStatusTaskIds.add(canBeSubmitToSlaveTask.getId());
        }

        LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle]find can be reset life cycle status task size = {}, "
                + "cost time = {} ms.", needResetStatusTaskIds.size(), System.currentTimeMillis() - startTime);

        long phaseStartTime = System.currentTimeMillis();

        // 无需要重置状态的任务则返回
        if (needResetStatusTaskIds.isEmpty()) {
            LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle]" +
                            "Reset task life cycle success,task id size = {},phase cost time = {} ms,cost time = {}",
                    0, System.currentTimeMillis() - phaseStartTime, System.currentTimeMillis() - startTime);
            return;
        }

        // 批量更新任务状态到生命周期重置
        boolean result = taskOperationClientService.resetTaskDescendantsLifecycleStatusInBatch(needResetStatusTaskIds);
        LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle]" +
                        "Reset task life cycle result = {}, task id size = {}, phase cost time = {} ms,cost time = {}",
                result, needResetStatusTaskIds.size(), System.currentTimeMillis() - phaseStartTime,
                System.currentTimeMillis() - startTime);
    }

    /**
     * 检查是否有需要重置生命周期的任务，如果有则设置(通过dagId重置整个图)
     *
     * @param canBeSubmitToWorkerTasks tasks
     */
    public void checkAndResetTaskLifecycle2(List<TbClockworkTaskPojo> canBeSubmitToWorkerTasks) {
        if (CollectionUtils.isEmpty(canBeSubmitToWorkerTasks)) {
            LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle2]canBeSubmitToWorkerTasks is null.");
            return;
        }

        List<Integer> needResetStatusDagIds = new ArrayList<>();
        List<Integer> notNeedResetStatusTaskIds = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (TbClockworkTaskPojo canBeSubmitToWorkerTask : canBeSubmitToWorkerTasks) {
            try {
                if (TaskSource.DDS_2.getValue().equals(canBeSubmitToWorkerTask.getSource())) {
                    LOG.debug("[checkAndResetTaskLifecycle2] skip task = {}", canBeSubmitToWorkerTask.getId());
                    continue;
                }
                if (TaskTriggerModel.TIME.getValue().intValue() != canBeSubmitToWorkerTask.getTriggerMode().intValue()
                        || StringUtils.isBlank(canBeSubmitToWorkerTask.getCronExp())) {
                    continue;
                }

                // 如果DagId 为空需要构建dagId
                Integer dagId = canBeSubmitToWorkerTask.getDagId();
                if (dagId == null || dagId == -1) {
                    // 构建dagId
                    dagId = dagClientService.buildDagIdForTaskId(canBeSubmitToWorkerTask.getId());
                    if (dagId == -1) {
                        LOG.error("RoutineTaskSelector-checkAndResetTaskLifecycle2 buildDagIdForTaskId Error. taskId = {}, "
                                + "dagId = -1", canBeSubmitToWorkerTask.getId());
                        continue;
                    } else {
                        LOG.info("RoutineTaskSelector-checkAndResetTaskLifecycle2 buildDagIdForTaskId success. taskId = {}, "
                                        + "dagId = {}, cost time = {}",
                                canBeSubmitToWorkerTask.getId(), dagId, System.currentTimeMillis() - startTime);

                    }
                }
                // 查询所有关联图节点
                // 找出图节点中优先级别最高的任务
                List<TbClockworkTaskPojo> tasksByDagId = taskClientService.getTasksByDagId(dagId);
                int taskId = TaskUtil.getHighPriorityTaskId(tasksByDagId);
                LOG.info("TaskUtil.getHighPriorityTaskId dagId = {}, HighPriorityTaskId = {}.", dagId, taskId);
                if (taskId == -1 || taskId != canBeSubmitToWorkerTask.getId()) {
                    LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle-0]do not need reset life cycle, " +
                                    "task id = {}, task dependency id = {}, task cron exp = {}, task group id = {}",
                            canBeSubmitToWorkerTask.getId(), canBeSubmitToWorkerTask.getDependencyId(),
                            canBeSubmitToWorkerTask.getCronExp(), canBeSubmitToWorkerTask.getGroupId());
                    continue;
                }

                // 需要重置生命周期（生命周期重置逻辑）
                needResetStatusDagIds.add(dagId);
                LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle-1]need reset life cycle, " +
                                "task id = {}, task dependency id = {}, task cron exp = {}, task group id = {}",
                        taskId, canBeSubmitToWorkerTask.getDependencyId(),
                        canBeSubmitToWorkerTask.getCronExp(), canBeSubmitToWorkerTask.getGroupId());
                // 检测任务图的调度时间周期
                TimeCycle curCycle = CronExpression.getCurCycle(canBeSubmitToWorkerTask.getCronExp());
                LOG.info("CronExpression.getCurCycle CronExp = {}, CycleType = {}", canBeSubmitToWorkerTask.getCronExp(), curCycle.getType());
                for (TbClockworkTaskPojo taskPojo : tasksByDagId) {  // 遍历该DAG的所有任务
                    if (taskPojo == null
                            || TaskTriggerModel.TIME.getValue().intValue() != canBeSubmitToWorkerTask.getTriggerMode().intValue()
                            || StringUtils.isBlank(taskPojo.getCronExp())) {
                        continue;
                    }
                    // 排除掉自己
                    if (taskPojo.getId() == taskId) {
                        notNeedResetStatusTaskIds.add(taskPojo.getId());
                        continue;
                    }

                    // 排除掉CronExp相差非常小的任务
                    long cronExpTimeDifference =
                            CronExpression.getCronExpTimeDifference(taskPojo.getCronExp(), canBeSubmitToWorkerTask.getCronExp());
                    if (cronExpTimeDifference == -1 || cronExpTimeDifference < skipTimeDifference) {
                        notNeedResetStatusTaskIds.add(taskPojo.getId());
                        continue;
                    }
                    // 排除掉运行状态的作业
                    if (TaskStatusUtil.isStartedTaskStatus(taskPojo.getStatus())) {
                        notNeedResetStatusTaskIds.add(taskPojo.getId());
                        continue;
                    }
                    // 排除掉大于该调度时间周期的任务，即按天运行的任务不能重置按周运行任务的状态
                    TimeCycle checkCycle = CronExpression.getCurCycle(taskPojo.getCronExp());
                    if (checkCycle.getTime() > curCycle.getTime()) {
                        notNeedResetStatusTaskIds.add(taskPojo.getId());
                    }
                }
            } catch (Exception e) {
                LOG.error("checkAndResetTaskLifecycle2 taskId = {}, dagId = {} 严重Error {}.",
                        canBeSubmitToWorkerTask.getId(), canBeSubmitToWorkerTask.getDagId(), e.getMessage(), e);
            }
        }

        LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle] find can be reset life cycle status dagId size = {},"
                + "cost time = {} ms.", needResetStatusDagIds.size(), System.currentTimeMillis() - startTime);

        long phaseStartTime = System.currentTimeMillis();
        // 无需要重置状态的任务则返回
        if (needResetStatusDagIds.isEmpty()) {
            LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle]" +
                            "Reset task life cycle success,task id size = {}, phase cost time = {} ms, cost time = {}",
                    0, System.currentTimeMillis() - phaseStartTime, System.currentTimeMillis() - startTime);
            return;
        }

        // 批量更新任务状态到生命周期重置
        LOG.info("[RoutineTaskSelector-resetTaskLifecycleStatusByDagIdsInBatch]needResetStatusDagIds = {}, "
                + "notNeedResetStatusTaskIds = {}", needResetStatusDagIds, notNeedResetStatusTaskIds);
        boolean result = taskOperationClientService.
                resetTaskLifecycleStatusByDagIdsInBatch(needResetStatusDagIds, notNeedResetStatusTaskIds);
        LOG.info("[RoutineTaskSelector-checkAndResetTaskLifecycle]Reset task life cycle result = {}, dag id size = {}, "
                        + "phase cost time = {} ms, cost time = {}", result, needResetStatusDagIds.size(),
                System.currentTimeMillis() - phaseStartTime, System.currentTimeMillis() - startTime);
    }

    /**
     * 如果当前任务没有父任务并且有时间维度配置，则认为此任务是最顶层的任务，需要进行任务生命周期重置
     *
     * @param canBeSubmitToWorkerTask canBeSubmitToWorkerTask
     * @return
     */
    public boolean checkTaskIfNeedResetLifecycle(TbClockworkTaskPojo canBeSubmitToWorkerTask) {
        // 没有时间维度的配置直接跳过
        if (StringUtils.isBlank(canBeSubmitToWorkerTask.getCronExp())) {
            return false;
        }

        // 有父亲直接跳过
        if (StringUtils.isNotBlank(canBeSubmitToWorkerTask.getDependencyId())) {
            // 等于-1也代表没有父任务
            return canBeSubmitToWorkerTask.getDependencyId().equals("-1");
        }
        return true;
    }

}
