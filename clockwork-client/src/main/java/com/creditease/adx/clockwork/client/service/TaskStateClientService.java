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

import com.creditease.adx.clockwork.client.TaskOperationClient;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.enums.TaskReRunType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运行作业处于的几大阶段
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:03 2020-09-11
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskStateClientService")
public class TaskStateClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskStateClientService.class);

    @Autowired
    private TaskLogClientService taskLogClientService;

    @Autowired
    private TaskLogFlowClientService taskLogFlowClientService;

    @Autowired
    private TaskOperationClientService taskOperationClientService;

    @Autowired
    protected TaskFillDataClientService taskFillDataClientService;

    @Autowired
    protected TaskOperationClient taskOperationClient;

    /**
     * 运行任务状态阶段一：SUBMIT
     * (包含修改任务状态)
     *
     * @param taskSubmitInfo submit
     * @return
     */
    public void taskStateSubmitIncludeTask(TaskSubmitInfo taskSubmitInfo) {
        List<Integer> taskIds = taskSubmitInfo.getTaskIds();
        boolean result = taskOperationClientService.updateTaskStatusSubmitBatch(new BatchUpdateTaskStatusSubmit(taskIds));
        if (!result) {
            LOG.error("[taskStateSubmitIncludeTask] update status Error. batch update tasks status to submit, "
                    + "task.size = {}, taskIds = {}", taskIds.size(), taskIds);
        }
        taskStateSubmitNotIncludeTask(taskSubmitInfo);
    }

    /**
     * 运行任务状态阶段一：SUBMIT
     * (不包含修改任务状态)
     *
     * @param taskSubmitInfo submit
     * @return
     */
    public void taskStateSubmitNotIncludeTask(TaskSubmitInfo taskSubmitInfo) {

        String status = TaskStatus.SUBMIT.getValue();
        int executeType = taskSubmitInfo.getExecuteType();
        long start = System.currentTimeMillis();

        try {
            /*
             * SUBMIT 阶段
             * 修改任务状态\init构建log\构建logFlow[没有日志文件]
             */
            if (taskSubmitInfo.getTaskPojoList() == null || taskSubmitInfo.getTaskPojoList().size() == 0) {
                throw new RuntimeException("[TaskStateClientService-taskStateSubmit]add info failure! taskList is null");
            }

            /*
             *  批量添加taskLog -【TASK_STATUS_SUBMIT】 提交任务，等待执行 submit.
             *  并且记录taskLogId
             */
            if ((taskSubmitInfo instanceof TaskSubmitInfoRouTine || taskSubmitInfo instanceof TaskSubmitInfoSignal)) {
                // 例行
                taskLogClientService.addBatchTaskLog(taskSubmitInfo.getTaskPojoList(), executeType, null, null);
            } else if (taskSubmitInfo instanceof TaskSubmitInfoRerun) {
                // 重启
                TaskSubmitInfoRerun reRun = (TaskSubmitInfoRerun) taskSubmitInfo;
                taskLogClientService.addBatchTaskLog(
                        taskSubmitInfo.getTaskPojoList(), executeType, null, reRun.getRerunBatchNumber());
            } else if (taskSubmitInfo instanceof TaskSubmitInfoFillData) {
                // 补数
                TaskSubmitInfoFillData fillData = (TaskSubmitInfoFillData) taskSubmitInfo;
                taskLogClientService.addBatchTaskLog(
                        taskSubmitInfo.getTaskPojoList(), executeType, fillData.getFillDataTime(), fillData.getRerunBatchNumber());
            } else {
                throw new RuntimeException("TaskStateClientService needBeExecutedTuple type is Error.");
            }

            LOG.info("[TaskStateClientService-taskStateSubmit]add taskLog, tasks size = {}, cost time = {} ms.",
                    taskSubmitInfo.getTaskIds().size(), System.currentTimeMillis() - start);

            /*
             *  批量添加taskLogFlow任务的生命周期
             *  此信息主要用于提供给监控系统，以及内部任务执行状态记录
             */
            boolean addResult = taskLogFlowClientService.addBatchTaskLogFlow(taskSubmitInfo.getTaskPojoList(), status, null);
            if (!addResult) {
                throw new RuntimeException("[TaskStateClientService-taskStateSubmit]add taskLogFlow failure!");
            }

            LOG.info("[TaskStateClientService-taskStateSubmit]update task status operate success, " +
                            "status = {}, executeType = {}, tasks size = {}, cost time = {} ms.",
                    status, executeType, taskSubmitInfo.getTaskIds().size(), System.currentTimeMillis() - start);

        } catch (Exception e) {
            LOG.error("[TaskStateClientService-taskStateSubmit]status to submit error, {},task.size = {}. task = {}",
                    e.getMessage(), taskSubmitInfo.getTaskIds().size(), taskSubmitInfo.getTaskIds());
            BatchUpdateTaskStatusParam batch = new BatchUpdateTaskStatusParam();
            batch.setTaskIds(taskSubmitInfo.getTaskIds());
            batch.setStatus(TaskStatus.FAILED.getValue());
            taskOperationClientService.updateTaskStatusBatch(batch);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 运行任务状态阶段二：MASTER_HAS_RECEIVE
     * <p>
     * 修改taskLog状态、并且记录此状态的生命周期
     *
     * @param taskSubmitInfo submitInfo
     */
    public void taskStateMasterHasReceive(TaskSubmitInfo taskSubmitInfo) {

        String status = TaskStatus.MASTER_HAS_RECEIVE.getValue();
        int executeType = taskSubmitInfo.getExecuteType();

        try {
            List<Integer> taskIds = taskSubmitInfo.getTaskIds();
            /*
             * 批量更新可以提交的任务的状态为已接收
             * 加入此状态的原因是控制到提交状态这个时间窗口内，不让用户更新任务信息操作
             */
            LOG.info("taskStateMasterHasReceive taskIds = {}, update status = {}", taskIds, status);
            boolean result = taskOperationClientService.updateTaskStatusBatch(new BatchUpdateTaskStatusParam(taskIds, status));
            if (!result) {
                LOG.error("[taskStateMasterHasReceive-updateTaskStatusBatch] update status Error." +
                        "batch update tasks status to submit, task.size = {}, taskIds = {}", taskIds.size(), taskIds);
                throw new RuntimeException("updateTaskStatusBatch update status Error.");
            }

            if (taskSubmitInfo.getTaskPojoList() == null || taskSubmitInfo.getTaskPojoList().size() == 0) {
                throw new RuntimeException("[TaskStateClientService-taskMasterHasReceive]add taskLog failure!");
            }

            // 批量更新taskLog状态为MASTER_HAS_RECEIVE
            List<Integer> logIds = taskSubmitInfo.getTaskPojoList().stream().map(TbClockworkTaskPojo::getTaskLogId).collect(Collectors.toList());
            taskLogClientService.updateBatchTaskLogMasterHasReceive(logIds, status);

            // 批量记录taskLogFlow任务的生命周期
            boolean addResult = taskLogFlowClientService.addBatchTaskLogFlow(
                    taskSubmitInfo.getTaskPojoList(), status, null);
            if (!addResult) {
                throw new RuntimeException("[TaskStateClientService-taskMasterHasReceive]add taskLogFlow failure!");
            }

            LOG.info("[TaskStateClientService-taskMasterHasReceive]update task status operate success, status = {}, "
                    + "executeType = {}, tasks size = {}", status, executeType, taskSubmitInfo.getTaskIds().size());
        } catch (Exception e) {
            LOG.error("[TaskStateClientService-taskMasterHasReceive]status to masterHasReceive error, {}, task.size = {}, task = {}",
                    e.getMessage(), taskSubmitInfo.getTaskIds().size(), taskSubmitInfo.getTaskIds());
        }
    }

    /**
     * 运行任务状态阶段三：WORKER_HAS_RECEIVE
     * 修改taskLog状态、并且记录此状态的生命周期
     *
     * @param tuple TaskDistributeTuple
     */
    public void taskStateWorkerHasReceive(TaskDistributeTuple tuple, Integer nodeId) {

        String status = TaskStatus.WORKER_HAS_RECEIVE.getValue();
        int executeType = tuple.getExecuteType();

        try {

            List<TbClockworkTaskPojo> taskPojoList = tuple.getTaskPojoList();
            if (CollectionUtils.isEmpty(taskPojoList)) {
                throw new RuntimeException("[taskStateWorkerHasReceive]taskPojoList is null!");
            }

            /*
             * 批量更新可以提交的任务的状态为已接收
             * 加入此状态的原因是控制到提交状态这个时间窗口内，不让用户更新任务信息操作
             */
            List<Integer> taskIds = tuple.getTaskIds();
            boolean result = taskOperationClientService.updateTaskStatusBatch(new BatchUpdateTaskStatusParam(taskIds, status));
            if (!result) {
                LOG.error("[TaskStateClientService-updateTaskStatusBatch] update status Error." +
                        "batch update tasks status to submit, task.size = {}, taskIds = {}", taskIds.size(), taskIds);
                throw new RuntimeException("updateTaskStatusBatch update status Error.");
            }
            LOG.info("TaskStateClientService taskIds = {}, update status = {}", taskIds, status);

            // 修改状态
            for (TbClockworkTaskPojo tbClockworkTaskPojo : taskPojoList) {
                tbClockworkTaskPojo.setStatus(status);
            }

            // 批量更新taskLog状态为WORKER_HAS_RECEIVE
            List<Integer> logIds = tuple.getTaskPojoList().stream().map(TbClockworkTaskPojo::getTaskLogId).collect(Collectors.toList());
            taskLogClientService.updateBatchTaskLogWorkerHasReceive(logIds, status, nodeId);

            // 记录taskLogFlow任务的生命周期
            boolean addResult = taskLogFlowClientService.addBatchTaskLogFlow(taskPojoList, status, tuple.getNodeId());
            if (!addResult) {
                throw new RuntimeException("[TaskStateClientService-taskWorkerHasReceive]update taskLogFlow failure!");
            }

            LOG.info("[TaskStateClientService-taskWorkerHasReceive]update taskLog status operate success, status = {}, "
                    + "executeType = {}, tasks size = {}", status, executeType, taskPojoList.size());

        } catch (Exception e) {
            LOG.error("[preBatchTasksHandle-taskStateWorkerHasReceive]status to worker_has_receive error, {}, task.size = {}, "
                    + "task = {}", e.getMessage(), tuple.getTaskPojoList().size(), tuple.getTaskIds());
        }
    }

    /**
     * 运行任务状态阶段四
     * <p>
     * 作业开始运行，更新作业状态 - RUNNING
     *
     * @param cell    基本运行实例
     * @param logId   运行日志ID
     * @param pid     运行进程号
     * @param logName 运行日志名
     * @return
     */
    public boolean taskStateRunning(TaskRunCell cell, Integer logId, int pid, String logName) {

        // 更新任务状态running
        cell.getTask().setStatus(TaskStatus.RUNNING.getValue());
        TbClockworkTaskPojo task = cell.getTask();
        boolean result = updateTaskRunning(task.getId());
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskRan]updateTaskStarted error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskRan]updateTaskStarted error!");
        }

        // 更新运行日志running
        result = taskLogClientService.updateTaskLogRunning(logId, pid, logName);
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskRan]updateTaskLogRunning error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskRan]updateTaskLogRunning error!");
        }

        // 添加生命周期
        result = taskLogFlowClientService.addTaskLogFlow(task, TaskStatus.RUNNING.getValue(), logId, cell.getNodeId());
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskRan]addTaskLogFlow error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskRan]addTaskLogFlow error!");
        }

        // 当运行实例为RunTaskFillDataCell补数时-更新补数状态running, 接口实现做限制
        if (cell instanceof TaskRunCellFillData) {
            TaskRunCellFillData runTaskFillDataCell = (TaskRunCellFillData) cell;
            Long rerunBatchNumber = runTaskFillDataCell.getRerunBatchNumber();
            result = taskFillDataClientService.updateTaskFillDataIsRan(String.valueOf(rerunBatchNumber));
            if (!result) {
                LOG.error("[updateTaskStatusWhenTaskRan]updateTaskFillDataRunning error!");
                throw new RuntimeException("[updateTaskStatusWhenTaskRan]updateTaskFillDataRunning error!");
            }
        }

        return true;
    }

    /**
     * 运行任务状态阶段五（该阶段只有被kill时才会出现）
     * <p>
     * 作业正在被KILL，更新作业状态 - KILLING | RUN_TIMEOUT_KILLING
     *
     * @param cell       运行单元cell
     * @param logId      日志log id（如果为空，自己尝试获取一次）
     * @param status     状态（）
     * @param returnCode 返回状态码
     * @return
     */
    public boolean taskStateKilling(TaskRunCell cell, Integer logId, String status, int returnCode) {

        // 更新任务状态（KILLING | RUN_TIMEOUT_KILLING）
        TbClockworkTaskPojo task = cell.getTask();
        boolean result = updateTaskKilling(task.getId(), status);
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskBeKilling]updateTaskKilling error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskBeKilling]updateTaskKilling error!");
        }

        // 更新运行日志Killing（KILLING | RUN_TIMEOUT_KILLING）
        result = taskLogClientService.updateTaskLogKilling(logId, status, returnCode);
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskBeKilling]updateTaskLogKilling error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskBeKilling]updateTaskLogKilling error!");
        }

        // 添加生命周期（KILLING | RUN_TIMEOUT_KILLING）
        result = taskLogFlowClientService.addTaskLogFlow(task, status, logId, cell.getNodeId());
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskBeKilling]addTaskLogFlow error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskBeKilling]addTaskLogFlow error!");
        }

        // 当运行实例为RunTaskFillDataCell补数时-更新补数状态KILLING, 接口实现做限制
        if (cell instanceof TaskRunCellFillData) {
            TaskRunCellFillData runTaskFillDataCell = (TaskRunCellFillData) cell;
            Long rerunBatchNumber = runTaskFillDataCell.getRerunBatchNumber();
            result = taskFillDataClientService.updateTaskFillDataIsKilling(String.valueOf(rerunBatchNumber), status);
            if (!result) {
                LOG.error("[updateTaskStatusWhenTaskBeKilling]updateTaskFillDataKilling error!");
                throw new RuntimeException("[updateTaskStatusWhenTaskBeKilling]updateTaskFillDataKilling error!");
            }
        }

        return true;
    }

    /**
     * 运行任务状态阶段六
     * <p>
     * 作业处于完结状态时，更新任务和对应日志的状态以及记录生命周期
     *
     * @param cell       运行单元cell
     * @param logId      日志log id（如果为空，自己尝试获取一次）
     * @param status     结束状态
     * @param isStarted  该任务是否启动
     * @param returnCode 返回状态码
     * @return
     */
    public boolean taskStateFinished(TaskRunCell cell, Integer logId, String status, boolean isStarted, int returnCode) {
        // 设置内存状态
        cell.getTask().setStatus(status);
        TbClockworkTaskPojo task = cell.getTask();
        Integer nodeId = cell.getNodeId();

        // 更新任务状态end
        boolean result = updateTaskEnd(task.getId(), status, isStarted);
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskFinished]updateTaskEnd error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskFinished]updateTaskEnd error!");
        }

        if (logId == null) {
            TbClockworkTaskLogPojo taskLog = taskLogClientService.getTaskLogByTaskId(task.getId());
            if (taskLog != null && TaskStatusUtil.isStartedTaskStatus(taskLog.getStatus())) {
                logId = taskLog.getId();
                nodeId = taskLog.getNodeId();
            } else {
                return false;
            }
        }
        // 更新运行日志end
        result = taskLogClientService.updateTaskLogEnd(logId, status, returnCode);
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskFinished]updateTaskLogEnd error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskFinished]updateTaskLogEnd error!");
        }

        // 添加生命周期
        result = taskLogFlowClientService.addTaskLogFlow(task, status, logId, nodeId);
        if (!result) {
            LOG.error("[updateTaskStatusWhenTaskFinished]addTaskLogFlow error!");
            throw new RuntimeException("[updateTaskStatusWhenTaskFinished]addTaskLogFlow error!");
        }

        // 如果为补数任务，并且任务运行异常 更新补数状态end
        Long rerunBatchNumber = null;
        if ((cell instanceof TaskRunCellFillData) && !TaskStatus.SUCCESS.getValue().equals(status)) {
            TaskRunCellFillData runTaskFillDataCell = (TaskRunCellFillData) cell;
            rerunBatchNumber = runTaskFillDataCell.getRerunBatchNumber();
            result = taskFillDataClientService.updateTaskFillDataIsEnd(String.valueOf(rerunBatchNumber), status, isStarted);
            if (!result) {
                LOG.error("[updateTaskStatusWhenTaskFinished]updateTaskFillDataEnd error! " +
                        "rerunBatchNumber = {}, status = {}", rerunBatchNumber, status);
                throw new RuntimeException("[updateTaskStatusWhenTaskFinished]updateTaskFillDataEnd error!");
            }
            LOG.info("[updateTaskStatusWhenTaskFinished]updateTaskFillDataEnd success! " +
                    "rerunBatchNumber = {}, status = {}", rerunBatchNumber, status);
        }

        // 如果是失败状态，则需要更新下面所有的子任务状态为FatherNotSuccess
        if (TaskStatusUtil.getFailedTaskStatus().get(status) != null) {
            if (cell instanceof TaskRunCellReRun) {
                TaskRunCellReRun runCellReRun = (TaskRunCellReRun) cell;
                rerunBatchNumber = runCellReRun.getRerunBatchNumber();
            }
            taskOperationClientService.updateTaskAllChildrenStatusFatherNotSuccess(task.getId(), cell.getExecuteType(), rerunBatchNumber);
            LOG.info("taskStateFinished-update taskId = {}, status = {}", task.getId(), status);
            // 如果子任务是时间依赖双触发的，刷新子任务中有时钟的任务，将所有任务的延迟策略清空，
            taskOperationClientService.updateTaskChildrensDelayStatusAndRefreshSlots(task.getId(), TaskReRunType.ALL_CHILDREN_NOT_SELF.getCode());
            LOG.info("taskStateFinished-update taskId = {}, get the task all childrens, clear these delay status, " +
              "if task triggerMode equal 2， refresh task slot. ", task.getId());
        }
        return true;
    }

    /**
     * 更新状态为Running
     *
     * @param taskId taskID
     * @return boolean
     */
    private boolean updateTaskRunning(Integer taskId) {
        try {
            Map<String, Object> interfaceResult =
                    taskOperationClient.updateTaskStatus(taskId, TaskStatus.RUNNING.getValue());
            if (HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return true;
            }
        } catch (Exception e) {
            LOG.error("updateTaskRunning Error {}.", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 更新状态为Killing
     *
     * @param taskId taskID
     * @return boolean
     */
    private boolean updateTaskKilling(Integer taskId, String status) {
        try {
            Map<String, Object> interfaceResult = taskOperationClient.updateTaskStatus(taskId, status);
            if (HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return true;
            }
        } catch (Exception e) {
            LOG.error("updateTaskKilling Error {}.", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 更新任务状态为已结束
     *
     * @param taskId    id ID
     * @param status    status 状态
     * @param isStarted 是否开始运行（没有开始，即没有submit就已经结束）
     * @return boolean
     */
    private boolean updateTaskEnd(Integer taskId, String status, boolean isStarted) {
        try {
            TbClockworkTaskPojo updateTask = new TbClockworkTaskPojo();
            updateTask.setId(taskId);
            updateTask.setStatus(status);
            updateTask.setLastEndTime(new Date());
            Map<String, Object> interfaceResult = taskOperationClient.updateTaskInfo(updateTask);
            if (HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return true;
            }
        } catch (Exception e) {
            LOG.error("updateTaskEnd Error {}.", e.getMessage(), e);
        }
        return false;
    }
}
