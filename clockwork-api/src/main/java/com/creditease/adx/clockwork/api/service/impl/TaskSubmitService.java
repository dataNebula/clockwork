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

package com.creditease.adx.clockwork.api.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.api.service.*;
import com.creditease.adx.clockwork.client.service.TaskSubmitClientService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerun;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerunRelation;
import com.creditease.adx.clockwork.common.enums.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRerunPojo;
import com.creditease.adx.clockwork.common.util.DataUtil;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRerunMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRerunRelationMapper;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import com.robert.vesta.service.intf.IdService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 预处理任务类型运行参数，构建Submit Info处理逻辑
 * <p>
 * 当前任务状态为：可提交作业状态
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:23 下午 2020/5/11
 * @ Description：TaskSubmitService
 * @ Modified By：
 */
@Service(value = "taskSubmitService")
public class TaskSubmitService implements ITaskSubmitService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSubmitService.class);

    @Autowired
    private ITaskService taskService;

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Autowired
    private ITaskRelationService taskRelationService;

    @Autowired
    private ITaskOperationService taskOperationService;

    @Autowired
    private ITaskRerunRelationService taskRerunRelationService;

    @Autowired
    private TaskFillDataService taskFillDataService;

    @Autowired
    private TaskSubmitClientService taskSubmitClientService;

    @Autowired
    private TbClockworkTaskRerunMapper tbClockworkTaskRerunMapper;

    @Autowired
    private TbClockworkTaskRerunRelationMapper tbClockworkTaskRerunRelationMapper;

    @Autowired
    private IdService idService;

    /**
     * 重启任务（自己、所有子节点不包括自己、所有子节点包括自己）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:self,3:all_children_not_self,4:all_children_and_self]
     * @param parameter     参数
     * @param operatorName  操作人
     */
    @Override
    public TaskSubmitInfoRerun submitReRunTaskTx(Integer taskId, Integer taskReRunType, String parameter, String operatorName, String scriptParameter) {

        // 预处理 - 重启任务信息
        List<TbClockworkTaskPojo> taskPojoList = preProcessReRunTask(taskId, taskReRunType, null, parameter, operatorName, scriptParameter);
        if (CollectionUtils.isEmpty(taskPojoList)) {
            throw new RuntimeException("submitReRunTaskTx-preProcessReRunTask failed taskPojoList is null");
        }

        // 构建 - 重启SubmitInfo 信息 && 提交信息到Master
        return buildReRunTaskSubmitInfo(taskPojoList, taskReRunType);
    }


    /**
     * 重启历史运行任务（单个任务）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:normal,1:routine,0:rerun,2:fill_data]
     * @param logId         历史日志ID
     * @param parameter     参数
     * @param operatorName  操作人
     */
    @Override
    public TaskSubmitInfoRerun submitReRunTaskHisTx(@RequestParam(value = "taskId") Integer taskId,
                                                    @RequestParam(value = "taskReRunType", required = false) Integer taskReRunType,
                                                    @RequestParam(value = "logId", required = false) Integer logId,
                                                    @RequestParam(value = "parameter", required = false) String parameter,
                                                    @RequestParam(value = "operatorName", required = false) String operatorName,
                                                    @RequestParam(value = "scriptParameter", required = false) String scriptParameter) {

        // 预处理 - 重启任务信息
        List<TbClockworkTaskPojo> taskPojoList = preProcessReRunTask(taskId, taskReRunType, logId, parameter, operatorName, scriptParameter);
        if (CollectionUtils.isEmpty(taskPojoList)) {
            throw new RuntimeException("submitReRunTaskTx-submitReRunTaskHisTx failed taskPojoList is null");
        }

        // 构建-SubmitInfo信息
        return buildReRunTaskSubmitInfo(taskPojoList, taskReRunType);
    }


    /**
     * 重启任务，通过dagId
     *
     * @param dagId        dagId
     * @param parameter    参数
     * @param operatorName 操作人
     */
    @Override
    public TaskSubmitInfoRerun submitReRunTaskByDagIdTx(Integer dagId, String parameter, String operatorName) {

        // 通过dagId获取信息
        List<TbClockworkTaskPojo> taskList = taskService.getTasksByDagId(dagId);
        if (CollectionUtils.isEmpty(taskList)) {
            throw new RuntimeException("task info is null, dagId = " + dagId);
        }

        // 设置参数等
        for (TbClockworkTaskPojo taskPojo : taskList) {
            if (StringUtils.isNotBlank(parameter)) {
                taskPojo.setParameter(parameter);
            }
            if (StringUtils.isNotBlank(operatorName)) {
                taskPojo.setOperatorName(operatorName);
            }
        }

        // 预处理 - 重启任务信息
        List<TbClockworkTaskPojo> taskPojoList = preProcessReRunTaskList(taskList);
        if (CollectionUtils.isEmpty(taskPojoList)) {
            throw new RuntimeException("submitReRunTaskByDagIdTx-preProcessReRunTaskList failed taskPojoList is null");
        }

        // 构建 - 重启SubmitInfo 信息 && 提交信息到Master
        return buildReRunTaskSubmitInfo(taskPojoList, TaskReRunType.LIST.getCode());
    }

    /**
     * 重启任务，通过groupId
     *
     * @param groupId      groupId
     * @param parameter    参数
     * @param operatorName 操作人
     */
    @Override
    public TaskSubmitInfoRerun submitReRunTaskByGroupIdTx(Integer groupId, String parameter, String operatorName) {

        // 通过dagId获取信息
        List<TbClockworkTaskPojo> taskList = taskService.getTasksByGroupId(groupId);
        if (CollectionUtils.isEmpty(taskList)) {
            throw new RuntimeException("task info is null, groupId = " + groupId);
        }

        // 设置参数等
        for (TbClockworkTaskPojo taskPojo : taskList) {
            if (StringUtils.isNotBlank(parameter)) {
                taskPojo.setParameter(parameter);
            }
            if (StringUtils.isNotBlank(operatorName)) {
                taskPojo.setOperatorName(operatorName);
            }
        }

        // 预处理 - 重启任务信息
        List<TbClockworkTaskPojo> taskPojoList = preProcessReRunTaskList(taskList);
        if (CollectionUtils.isEmpty(taskPojoList)) {
            throw new RuntimeException("submitReRunTaskByGroupIdTx-preProcessReRunTaskList failed taskPojoList is null");
        }

        // 构建 - 重启SubmitInfo 信息 && 提交信息到Master
        return buildReRunTaskSubmitInfo(taskPojoList, TaskReRunType.LIST.getCode());
    }


    /**
     * 提交重启任务（批量任务）
     *
     * @param taskList 多个任务
     */
    @Override
    public TaskSubmitInfoRerun submitReRunTaskListTx(List<TbClockworkTaskPojo> taskList) {

        // 预处理 - 重启任务信息
        List<TbClockworkTaskPojo> taskPojoList = preProcessReRunTaskList(taskList);
        if (CollectionUtils.isEmpty(taskPojoList)) {
            LOG.info("submitReRunTaskListTx-preProcessReRunTaskList failed taskPojoList is null");
            return null;
        }
        // 构建 - 重启SubmitInfo 信息 && 提交信息到Master
        return buildReRunTaskSubmitInfo(taskPojoList, TaskReRunType.LIST.getCode());
    }


    /**
     * 提交补数记录
     *
     * @param entity entity
     * @return 补数记录
     */
    @Override
    public TaskSubmitInfoFillData submitFillDataTaskListTx(TaskFillDataEntity entity, long rerunBatchNumber) {

        // 预处理 - 补数任务
        List<TbClockworkTaskPojo> taskPojoList = preProcessFillDataTask(entity);
        if (CollectionUtils.isEmpty(taskPojoList)) {
            LOG.info("submitReRunTaskTx-preProcessFillDataTask failed taskPojoList is null");
            throw new RuntimeException("submitReRunTaskTx-preProcessFillDataTask taskPojoList is null.");
        }

        // 构建 - 补数submit Info & 提交任务
        return buildFillDataTaskSubmitInfo(entity, taskPojoList, rerunBatchNumber);


    }


    /**
     * 提交信号触发任务
     *
     * @param signal 信号任务
     */
    @Override
    public TaskSubmitInfoSignal submitSignalTaskListTx(TaskSignalEntity signal) {

        // 预处理 - 信号触发任务
        List<TbClockworkTaskPojo> taskPojoList = preProcessSignalTask(signal.getTaskIds());
        if (CollectionUtils.isEmpty(taskPojoList)) {
            LOG.info("submitReRunTaskTx-preProcessSignalTask failed, taskPojoList is null");
            throw new RuntimeException("submitReRunTaskTx-preProcessSignalTask taskPojoList is null.");
        }

        // 构建 - TaskSubmitInfo分发任务, 提交给master分发器
        ArrayList<TbClockworkTaskPojo> tbClockworkTaskPojoArrayList = new ArrayList<>(taskPojoList);
        return new TaskSubmitInfoSignal(tbClockworkTaskPojoArrayList);
    }


    /**
     * 预处理 - 重启任务
     *
     * @param taskId       任务Id
     * @param executeType  执行类型
     * @param logId        历史LogId
     * @param parameter    参数
     * @param operatorName 操作人
     * @return list
     */
    private List<TbClockworkTaskPojo> preProcessReRunTask(
            Integer taskId, int executeType, Integer logId, String parameter, String operatorName, String scriptParameter) {
        LOG.info("reRunTask submit task, taskId = {}, taskReRunType = {}, logId = {}, parameter = {}, operatorName = {}",
                taskId, executeType, logId, parameter, operatorName);

        // reRunType == HIS_XXX 时，reRunLogId不能为空
        if ((executeType == TaskReRunType.HIS_RERUN.getCode()
                || executeType == TaskReRunType.HIS_ROUTINE.getCode()
                || executeType == TaskReRunType.HIS_FILL_DATA.getCode()) && logId == null) {
            throw new RuntimeException("reRunTask executeType = " + TaskReRunType.getEnumByCode(executeType).getName() +
                    " but logId is null, taskId = " + taskId);
        }

        TaskReRunType enumByCode = TaskReRunType.getEnumByCode(executeType);
        if (enumByCode == null) {
            throw new RuntimeException("reRunTask error executeType = " + executeType + " , taskId = " + taskId);
        }

        List<TbClockworkTaskPojo> taskPojoList = null;
        switch (enumByCode) {
            case ALL_CHILDREN_AND_SELF:
                taskPojoList = getAllChildrenAndSelfIncludeFailedFather(taskId);
                break;
            case ALL_CHILDREN_NOT_SELF:
                taskPojoList = getAllChildrenAndNotIncludeSelfIncludeFailedFather(taskId);
                if (CollectionUtils.isEmpty(taskPojoList)) {
                    throw new RuntimeException("The task has no subTasks need to run, taskId = " + taskId);
                }
                break;
            case SELF:
            case HIS_RERUN:
            case HIS_FILL_DATA:
            case HIS_ROUTINE:
            case RERUN_WITH_SP:
                TbClockworkTaskPojo tbClockworkTaskPojo = taskService.getTaskById(taskId);
                taskPojoList = Collections.singletonList(tbClockworkTaskPojo);
                break;
            default:
                break;
        }

        if (CollectionUtils.isEmpty(taskPojoList)) {
            throw new RuntimeException("There are no tasks need to run, taskId = " + taskId);
        }

        // 分布式锁环境下安全进行，检查状态是否可以提交
        boolean getLock = false;
        try {
            getLock = redisService.tryLockForSubmitTask(5, TimeUnit.SECONDS);
            if (getLock) {
                // taskIds
                List<Integer> taskIds = new ArrayList<>();
                for (TbClockworkTaskPojo tbClockworkTaskPojo : taskPojoList) {
                    taskIds.add(tbClockworkTaskPojo.getId());
                    // 检查状态是否可以提交
                    if (!TaskStatusUtil.canBeSubmitCurrentTaskStatus(tbClockworkTaskPojo.getStatus())) {
                        throw new RuntimeException("task can not be run, because status not a submit state, taskId = " + tbClockworkTaskPojo.getId() +
                                ", status = " + tbClockworkTaskPojo.getStatus());
                    }
                    // 检测是否online
                    if (tbClockworkTaskPojo.getOnline() != TaskTakeEffectStatus.ONLINE.getValue()) {
                        throw new RuntimeException("task can not be rerun, because task is off line or deleted, taskId is " + taskId);
                    }
                    // 设置参数
                    tbClockworkTaskPojo.setOperatorName(operatorName);
                    if (StringUtils.isNotBlank(scriptParameter)) {
                        tbClockworkTaskPojo.setScriptParameter(scriptParameter);
                    }
                    if (StringUtils.isNotBlank(parameter)) {
                        tbClockworkTaskPojo.setParameter(parameter);
                    }
                }

                // 状态设置为RERUN_SCHEDULE_PREP
                BatchUpdateTaskStatusParam batchParam = new BatchUpdateTaskStatusParam();
                batchParam.setTaskIds(taskIds);
                batchParam.setStatus(TaskStatus.RERUN_SCHEDULE_PREP.getValue());
                taskOperationService.updateTaskStatusBatch(batchParam);
                LOG.info("preProcessReRunTask-update taskIds = {}, status = rerun_schedule_prep", taskIds);
            } else {
                LOG.error("Same transaction for submit task, please try again later, taskId = {}", taskId);
                throw new RuntimeException("Same transaction for submit task, please try again later.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        return taskPojoList;
    }

    /**
     * 预处理 - 重启任务
     *
     * @param taskList list task
     * @return list
     */
    private List<TbClockworkTaskPojo> preProcessReRunTaskList(List<TbClockworkTaskPojo> taskList) {
        // 获取任务信息, 并检测任务状态是否可提交
        if (CollectionUtils.isEmpty(taskList)) {
            return null;
        }

        List<TbClockworkTaskPojo> result = new ArrayList<>();
        List<Integer> taskIds = new ArrayList<>();
        // 分布式锁环境下安全进行
        boolean getLock = false;
        try {
            getLock = redisService.tryLockForSubmitTask(5, TimeUnit.SECONDS);
            if (getLock) {
                TbClockworkTaskPojo dbTask;
                for (TbClockworkTaskPojo taskPojo : taskList) {
                    dbTask = taskService.getTaskById(taskPojo.getId());
                    if (dbTask == null) continue;

                    dbTask.setParameter(taskPojo.getParameter());
                    dbTask.setOperatorName(taskPojo.getOperatorName());

                    LOG.info("[TaskSubmitService-preProcessReRunTask] task parameter = {}, task id = {}",
                            dbTask.getParameter(), taskPojo.getId());

                    // 检查状态是否可以提交
                    if (!TaskStatusUtil.canBeSubmitCurrentTaskStatus(dbTask.getStatus())) {
                        throw new RuntimeException("preProcessReRunTask. task can not be run, " +
                                "because status not a submit state, taskId = " + taskPojo.getId() +
                                ", status = " + dbTask.getStatus());
                    }
                    // 检查状态是否Online
                    if (dbTask.getOnline() != TaskTakeEffectStatus.ONLINE.getValue()) {
                        throw new RuntimeException("preProcessReRunTask. task can not be rerun," +
                                "because task is off line or deleted,task id is " + dbTask.getId());
                    }

                    LOG.info("[TaskSubmitService-preProcessReRunTask]Rerun task info," +
                                    "id = {},name = {}," +
                                    "groupId = {}," +
                                    "batchNumber = {}," +
                                    "trigger time = {}," +
                                    "time type = {},script name = {},script parameter = {}",
                            dbTask.getId(), dbTask.getName(),
                            dbTask.getGroupId(),
                            dbTask.getBatchNumber(),
                            dbTask.getTriggerTime(),
                            dbTask.getTimeType(), taskPojo.getScriptName(), taskPojo.getScriptParameter());

                    result.add(dbTask);
                    taskIds.add(taskPojo.getId());
                }


                // 状态设置为RERUN_SCHEDULE_PREP
                BatchUpdateTaskStatusParam batchParam = new BatchUpdateTaskStatusParam();
                batchParam.setTaskIds(taskIds);
                batchParam.setStatus(TaskStatus.RERUN_SCHEDULE_PREP.getValue());
                taskOperationService.updateTaskStatusBatch(batchParam);
                LOG.info("preProcessReRunTaskList-updateTaskStatusBatch taskIds = {}, status = RERUN_SCHEDULE_PREP", taskIds);
            } else {
                LOG.error("Same transaction for submit task, please try again later, taskIds = {}", taskIds);
                throw new RuntimeException("Same transaction for submit task, please try again later.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return result;
    }


    /**
     * 预处理 - 补数任务
     *
     * @param entity entity
     * @return list
     */
    private List<TbClockworkTaskPojo> preProcessFillDataTask(TaskFillDataEntity entity) {
        // 参数校验 是否为空
        if (entity == null) {
            throw new RuntimeException("task info is null, entity = null.");
        }
        List<Integer> taskIds = entity.getTaskIds();
        if (CollectionUtils.isEmpty(taskIds)) {
            throw new RuntimeException("task info is null");
        }
        String operatorName = entity.getOperatorName();

        // 获取任务信息, 并检测任务状态是否可提交
        List<TbClockworkTaskPojo> taskList = new ArrayList<>();
        List<TbClockworkTask> taskByTaskIds = taskService.getTaskByTaskIds(taskIds);
        List<TbClockworkTaskPojo> taskPojoList = PojoUtil.convertList(taskByTaskIds, TbClockworkTaskPojo.class);

        // 分布式锁环境下安全进行
        boolean getLock = false;
        try {
            getLock = redisService.tryLockForSubmitTask(5, TimeUnit.SECONDS);
            if (getLock) {
                for (TbClockworkTaskPojo dbTask : taskPojoList) {
                    dbTask.setOperatorName(operatorName);
                    LOG.info("[TaskSubmitService-reRunTaskFillData] task parameter = {}, taskId = {}",
                            dbTask.getParameter(), dbTask.getId());

                    // 检查状态是否可以提交
                    if (!TaskStatusUtil.canBeSubmitCurrentTaskStatus(dbTask.getStatus())) {
                        throw new RuntimeException("task can not be run, because status not a submit state, taskId = "
                                + dbTask.getId() + ", status = " + dbTask.getStatus());
                    }
                    // 检查状态是否Online
                    if (dbTask.getOnline() != TaskTakeEffectStatus.ONLINE.getValue()) {
                        throw new RuntimeException("task can not be rerun," +
                                "because task is off line or deleted, task id is " + dbTask.getId());
                    }

                    LOG.info("[TaskSubmitService-reRunTaskFillData]FillData task info," +
                                    "id = {},name = {}," +
                                    "groupId = {}," +
                                    "batchNumber = {}," +
                                    "trigger time = {}," +
                                    "time type = {}",
                            dbTask.getId(), dbTask.getName(),
                            dbTask.getGroupId(),
                            dbTask.getBatchNumber(),
                            dbTask.getTriggerTime(),
                            dbTask.getTimeType());

                    taskList.add(dbTask);
                }

                // 状态设置为RERUN_SCHEDULE_PREP
                BatchUpdateTaskStatusParam batchParam = new BatchUpdateTaskStatusParam();
                batchParam.setTaskIds(taskIds);
                batchParam.setStatus(TaskStatus.RERUN_SCHEDULE_PREP.getValue());
                taskOperationService.updateTaskStatusBatch(batchParam);
                LOG.info("preProcessFillDataTask-updateTaskStatusBatch taskIds = {}, status = RERUN_SCHEDULE_PREP", taskIds);
            } else {
                LOG.error("Same transaction for submit task, please try again later, taskIds = {}", taskIds);
                throw new RuntimeException("Same transaction for submit task, please try again later.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return taskList;
    }


    /**
     * 预处理 - 信号触发
     *
     * @param taskIds list
     */
    private List<TbClockworkTaskPojo> preProcessSignalTask(List<Integer> taskIds) {
        // 参数校验 是否为空
        if (CollectionUtils.isEmpty(taskIds)) {
            throw new RuntimeException("task ids is null");
        }

        // 获取任务信息, 并检测任务状态是否可提交
        ArrayList<TbClockworkTaskPojo> taskList = new ArrayList<>();
        List<TbClockworkTask> taskByTaskIds = taskService.getTaskByTaskIds(taskIds);
        List<TbClockworkTaskPojo> taskPojoList = PojoUtil.convertList(taskByTaskIds, TbClockworkTaskPojo.class);

        // 分布式锁环境下安全进行
        boolean getLock = false;
        try {
            getLock = redisService.tryLockForSubmitTask(5, TimeUnit.SECONDS);
            if (getLock) {
                for (TbClockworkTaskPojo dbTask : taskPojoList) {
                    // 检查状态是否可以提交
                    if (!TaskStatusUtil.canBeSubmitCurrentTaskStatus(dbTask.getStatus())) {
                        throw new RuntimeException("task can not be run, because status not a submit state, taskId = "
                                + dbTask.getId() + ", status = " + dbTask.getStatus());
                    }
                    // 检查状态是否Online
                    if (dbTask.getOnline() != TaskTakeEffectStatus.ONLINE.getValue()) {
                        throw new RuntimeException("task can not be run, " +
                                "because task is off line or deleted, task id is " + dbTask.getId());
                    }

                    if (dbTask.getTriggerMode().intValue() != TaskTriggerModel.SIGNAL.getValue().intValue()) {
                        throw new RuntimeException("task can not be run, because task trigger mode is " + dbTask.getTriggerMode()
                                + ", but not signal trigger mode, task id is " + dbTask.getId());
                    }

                    LOG.info("[TaskSubmitService-reRunTaskFillData]FillData task info," +
                                    "id = {}, name = {}, " +
                                    "groupId = {}, " +
                                    "trigger mode = {}",
                            dbTask.getId(), dbTask.getName(),
                            dbTask.getGroupId(),
                            dbTask.getTriggerMode());

                    taskList.add(dbTask);
                }
            } else {
                LOG.error("Same transaction for submit task, please try again later, taskIds = {}", taskIds);
                throw new RuntimeException("Same transaction for submit task, please try again later.");
            }
        } catch (Exception e) {
            LOG.error("[TaskRerunController-reRunTask] Error. Msg:{}", e.getMessage(), e);
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return taskList;
    }

    /**
     * 构建 - 重启SubmitInfo信息（批量任务）
     *
     * @param taskList list
     */
    private TaskSubmitInfoRerun buildReRunTaskSubmitInfo(List<TbClockworkTaskPojo> taskList, Integer rerunType) {
        // ReRun
        long rerunBatchNumber = idService.genId();
        String operatorName = taskList.get(0).getOperatorName();

        // 构建该批次的运行Dag依赖关系图（buildTaskRelation）
        BuildTaskRelationDAG buildReRunDAG = new BuildTaskRelationDAG(taskList, rerunBatchNumber, operatorName, rerunType).invoke();
        List<TbClockworkTaskRerunRelation> rerunDependencies = buildReRunDAG.getRerunDependencies();
        HashMap<Integer, TbClockworkTaskRerunRelation> rootMap = buildReRunDAG.getRootMap();
        List<TbClockworkTaskRerun> taskReruns = buildReRunDAG.getTaskReruns();

        // 入库 持久化
        LOG.info("[TaskRerunService-reRun]addTaskRerunBatch && addTaskRerunDependencyBatch. taskReruns.size = {}, "
                + "rerunDependencies.size = {}", taskReruns.size(), rerunDependencies.size());
        tbClockworkTaskRerunMapper.batchInsert(taskReruns);
        tbClockworkTaskRerunRelationMapper.batchInsert(rerunDependencies);

        // 获取根ROOT任务, 构建TaskSubmitInfo 信息
        List<TbClockworkTaskPojo> rootTaskList =
                taskList.stream().filter(task -> rootMap.containsKey(task.getId())).collect(Collectors.toList());
        return new TaskSubmitInfoRerun(rootTaskList, rerunBatchNumber);

    }

    /**
     * 构建 - 补数任务SubmitInfo
     *
     * @param entity           entity
     * @param taskList         list
     * @param rerunBatchNumber 批次号
     * @return TaskSubmitInfoFillData
     */
    private TaskSubmitInfoFillData buildFillDataTaskSubmitInfo(
            TaskFillDataEntity entity, List<TbClockworkTaskPojo> taskList, long rerunBatchNumber) {
        List<Integer> taskIds = entity.getTaskIds();
        List<String> fillDataTimes = entity.getFillDataTimes();
        String operatorName = entity.getOperatorName();
        int reRunType = TaskReRunType.FILL_DATA.getCode();
        if (CollectionUtils.isEmpty(taskIds) || CollectionUtils.isEmpty(fillDataTimes)) {
            throw new RuntimeException("task info is null, taskIds || dates = null.");
        }

        // 构建该批次的运行Dag依赖关系图
        BuildTaskRelationDAG buildReRunDAG = new BuildTaskRelationDAG(taskList, rerunBatchNumber, operatorName, reRunType).invoke();
        List<TbClockworkTaskRerunRelation> rerunDependencies = buildReRunDAG.getRerunDependencies();
        HashMap<Integer, TbClockworkTaskRerunRelation> rootMap = buildReRunDAG.getRootMap();
        List<TbClockworkTaskRerun> taskReruns = buildReRunDAG.getTaskReruns();

        // 入库 持久化
        LOG.info("[TaskSubmitService-buildFillDataTaskSubmitInfo]addTaskRerunBatch && addTaskRerunDependencyBatch. " +
                "taskReruns.size = {}, rerunDependencies.size = {}", taskReruns.size(), rerunDependencies.size());
        tbClockworkTaskRerunMapper.batchInsert(taskReruns);
        tbClockworkTaskRerunRelationMapper.batchInsert(rerunDependencies);

        // 获取根ROOT任务
        List<TbClockworkTaskPojo> collect
                = taskList.stream().filter(task -> rootMap.containsKey(task.getId())).collect(Collectors.toList());
        // 构建TaskSubmitInf分发任务, 提交给master分发器
        return new TaskSubmitInfoFillData(collect, rerunBatchNumber, fillDataTimes.get(0));
    }

    private class BuildTaskRelationDAG {
        private List<TbClockworkTaskPojo> taskList;
        private long rerunBatchNumber;
        private String operatorName;
        private Integer rerunType;
        private List<TbClockworkTaskRerunRelation> rerunDependencies;
        private HashMap<Integer, TbClockworkTaskRerunRelation> rootMap;
        private List<TbClockworkTaskRerun> taskReruns;

        public BuildTaskRelationDAG(List<TbClockworkTaskPojo> taskList, long rerunBatchNumber, String operatorName, Integer rerunType) {
            this.taskList = taskList;
            this.rerunBatchNumber = rerunBatchNumber;
            this.operatorName = operatorName;
            this.rerunType = rerunType;
        }

        public List<TbClockworkTaskRerunRelation> getRerunDependencies() {
            return rerunDependencies;
        }

        public HashMap<Integer, TbClockworkTaskRerunRelation> getRootMap() {
            return rootMap;
        }

        public List<TbClockworkTaskRerun> getTaskReruns() {
            return taskReruns;
        }

        public BuildTaskRelationDAG invoke() {
            //构建重跑task依赖关系（buildTaskRelation）
            LOG.info("[BuildTaskRelationDAG-invoke]buildTaskRelation start.rerunBatchNumber = {}", rerunBatchNumber);
            rerunDependencies = taskRerunRelationService.buildTaskRelation(taskList, rerunBatchNumber);

            // 获取该图的所有跟节点
            rootMap = new HashMap<>();
            for (TbClockworkTaskRerunRelation rerunDependency : rerunDependencies) {
                if (rerunDependency.getFatherTaskId() == null || rerunDependency.getFatherTaskId() == -1)
                    rootMap.put(rerunDependency.getTaskId(), rerunDependency);
            }
            LOG.info("[BuildTaskRelationDAG-invoke]buildTaskRelation end.");

            // 构建需要重跑的task任务
            taskReruns = new ArrayList<>();
            TbClockworkTaskRerun taskRerun;
            for (TbClockworkTaskPojo taskPojo : taskList) {
                taskRerun = new TbClockworkTaskRerunPojo();
                taskRerun.setTaskId(taskPojo.getId());
                taskRerun.setGroupId(taskPojo.getGroupId());

                // Task
                String parameter = taskPojo.getParameter();
                if (StringUtils.isNotBlank(parameter)) {
                    taskPojo.setParameter("");
                    taskRerun.setParameter(parameter);
                }
                if (StringUtils.isNotBlank(taskPojo.getScriptName())) {
                    taskPojo.setScriptName(DataUtil.specialCharHandle(taskPojo.getScriptName()));
                }
                if (StringUtils.isNotBlank(taskPojo.getScriptParameter())) {
                    taskPojo.setScriptParameter(DataUtil.specialCharHandle(taskPojo.getScriptParameter()));
                }
                if (StringUtils.isNotBlank(taskPojo.getCommand())) {
                    taskPojo.setCommand(DataUtil.specialCharHandle(taskPojo.getCommand()));
                }
                taskRerun.setTaskJson(JSONObject.toJSONString(taskPojo));
                taskRerun.setTaskName(taskPojo.getName());
                taskRerun.setRerunBatchNumber(rerunBatchNumber);
                taskRerun.setOperatorName(operatorName);
                taskRerun.setIsFirst(rootMap.get(taskPojo.getId()) != null);
                taskRerun.setType(rerunType);
                taskRerun.setUpdateTime(new Date());
                taskRerun.setCreateTime(new Date());

                taskReruns.add(taskRerun);
            }
            return this;
        }
    }


    /**
     * 获取所有的子节点，包括自己，以及该任务的直接子节点的直接父节点不成功的任务
     *
     * @param taskId 任务Id
     * @return
     */
    public List<TbClockworkTaskPojo> getAllChildrenAndSelfIncludeFailedFather(Integer taskId) {
        List<TbClockworkTaskPojo> result = new ArrayList<>();
        HashSet<Integer> skip = new LinkedHashSet<>();

        // 获取该节点的直接子任务的所有直接父任务，如果有失败的任务则需要加入运行队列（把失败的带动起来）
        List<TbClockworkTaskPojo> childrenIncludeSelf = taskRelationService.getTaskDirectlyChildrenNotIncludeSelf(taskId);
        if (CollectionUtils.isNotEmpty(childrenIncludeSelf)) {
            for (TbClockworkTaskPojo children : childrenIncludeSelf) {
                List<TbClockworkTaskPojo> fathers = taskRelationService.getTaskDirectlyFatherNotIncludeSelf(children.getId());
                if (CollectionUtils.isEmpty(fathers)) {
                    LOG.info("skip fathers is null, children = {}", children.getId());
                    continue;
                }
                for (TbClockworkTaskPojo father : fathers) {
                    String fatherStatus = father.getStatus();
                    if (TaskStatus.FAILED.getValue().equals(fatherStatus)
                            || TaskStatus.EXCEPTION.getValue().equals(fatherStatus)
                            || TaskStatus.RUN_TIMEOUT_KILLED.getValue().equals(fatherStatus)
                            || TaskStatus.KILLED.getValue().equals(fatherStatus)) {
                        if (skip.contains(father.getId())) {
                            LOG.info("skip already exists father, taskId = {}, status = {}", father.getId(), fatherStatus);
                            continue;
                        }
                        LOG.info("add father, taskId = {}, status = {}", father.getId(), fatherStatus);
                        skip.add(father.getId());
                        result.add(father);
                    } else {
                        LOG.info("skip father, taskId = {}, status = {}", father.getId(), fatherStatus);
                    }
                }

            }
        } else {
            LOG.error("getTaskDirectlyChildrenIncludeSelf is null, taskId = {}", taskId);
        }

        List<TbClockworkTaskPojo> childTasks = taskRelationService.getTaskAllChildrenIncludeSelf(taskId);
        if (CollectionUtils.isEmpty(childTasks)) {
            LOG.info("getTaskAllChildrenIncludeSelf is null, childTasks is 0, result = {}", result.size());
            return result;
        }

        // 合并数据，并且skip重复的数据
        for (TbClockworkTaskPojo childTask : childTasks) {
            if (!skip.contains(childTask.getId())) {
                skip.add(childTask.getId());
                result.add(childTask);
            }
        }
        return result;
    }

    /**
     * 获取所有的子节点，不包括自己，以及该任务的直接子节点的直接父节点不成功的任务
     *
     * @param taskId 任务Id
     * @return
     */
    private List<TbClockworkTaskPojo> getAllChildrenAndNotIncludeSelfIncludeFailedFather(Integer taskId) {

        List<TbClockworkTaskPojo> result = new ArrayList<>();
        HashSet<Integer> skip = new LinkedHashSet<>();

        // 获取该节点的直接子任务的所有直接父任务，如果有失败的任务则需要加入运行队列（把失败的带动起来）
        List<TbClockworkTaskPojo> childrenIncludeSelf = taskRelationService.getTaskDirectlyChildrenNotIncludeSelf(taskId);
        if (CollectionUtils.isNotEmpty(childrenIncludeSelf)) {
            for (TbClockworkTaskPojo children : childrenIncludeSelf) {
                List<TbClockworkTaskPojo> fathers = taskRelationService.getTaskDirectlyFatherNotIncludeSelf(children.getId());
                if (CollectionUtils.isEmpty(fathers)) {
                    LOG.info("skip fathers is null, children = {}", children.getId());
                    continue;
                }
                for (TbClockworkTaskPojo father : fathers) {
                    String fatherStatus = father.getStatus();
                    if (TaskStatus.FAILED.getValue().equals(fatherStatus)
                            || TaskStatus.EXCEPTION.getValue().equals(fatherStatus)
                            || TaskStatus.RUN_TIMEOUT_KILLED.getValue().equals(fatherStatus)
                            || TaskStatus.KILLED.getValue().equals(fatherStatus)) {
                        if (skip.contains(father.getId())) {
                            LOG.info("skip already exists father, taskId = {}, status = {}", father.getId(), fatherStatus);
                            continue;
                        }
                        LOG.info("insert father, taskId = {}, status = {}", father.getId(), fatherStatus);
                        skip.add(father.getId());
                        result.add(father);
                    } else {
                        LOG.info("skip father, taskId = {}, status = {}", father.getId(), fatherStatus);
                    }
                }

            }
        } else {
            LOG.error("getTaskDirectlyChildrenIncludeSelf is null, taskId = {}", taskId);
        }

        List<TbClockworkTaskPojo> childTasks = taskRelationService.getTaskAllChildrenNotIncludeSelf(taskId);
        if (CollectionUtils.isEmpty(childTasks)) {
            LOG.info("getTaskAllChildrenNotIncludeSelf is null, childTasks is 0, result = {}", result.size());
            return result;
        }

        // 合并数据，并且skip重复的数据
        for (TbClockworkTaskPojo childTask : childTasks) {
            if (!skip.contains(childTask.getId())) {
                skip.add(childTask.getId());
                result.add(childTask);
            }
        }
        return result;
    }
}
