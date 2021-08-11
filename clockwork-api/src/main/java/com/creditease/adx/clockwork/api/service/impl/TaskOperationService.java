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

import com.creditease.adx.clockwork.api.service.*;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusEnd;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusParam;
import com.creditease.adx.clockwork.common.entity.BatchUpdateTaskStatusSubmit;
import com.creditease.adx.clockwork.common.entity.TaskGroupAndTasks;
import com.creditease.adx.clockwork.common.entity.gen.*;
import com.creditease.adx.clockwork.common.enums.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskGroupPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import com.creditease.adx.clockwork.common.util.CronExpression;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskBatchMapper;
import com.creditease.adx.clockwork.dao.mapper.TaskMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskDependencyScriptMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskGroupMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRelationMapper;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ Author     ：Muyuan Sun
 * @ Date       ：Created in 18:13 2019-09-11
 * @ Description：
 * @ Modified By：XuanDongTang
 */
@Service(value = "taskOperationService")
public class TaskOperationService implements ITaskOperationService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskOperationService.class);

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Autowired
    private TbClockworkTaskGroupMapper tbClockworkTaskGroupMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskBatchMapper taskBatchMapper;

    @Autowired
    private IDagService dagService;

    @Autowired
    private TbClockworkTaskRelationMapper tbClockworkTaskRelationMapper;

    @Autowired
    private TbClockworkTaskDependencyScriptMapper tbClockworkTaskDependencyScriptMapper;

    @Autowired
    private ITaskRelationService taskRelationService;

    @Autowired
    private ITaskRerunRelationService taskRerunRelationService;

    @Autowired
    private ILoopClockService loopClockService;

    @Autowired
    private ITaskService taskService;

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Value("${task.failed.retries.max.number}")
    private Integer taskFailedRetriesMaxNumber;

    @Value("${task.upload.path.prefix}")
    private String[] uploadPathPrefix = null;

    /**
     * 上线任务（时间触发任务需要添加环形时钟以及下次触发时间、依赖关系设置为有效=ONLINE，重新构建DagId）
     *
     * @param taskId taskId
     * @return boolean
     */
    @Override
    public boolean enableTaskTx(Integer taskId) {
        try {
            LOG.info("TaskOperationService-enableTaskTx taskId = {}.", taskId);
            if (taskId == null) return false;
            TbClockworkTask dbTbClockworkTask = tbClockworkTaskMapper.selectByPrimaryKey(taskId);
            if (dbTbClockworkTask == null) {
                LOG.error("disableTaskTx-selectByPrimaryKey taskId = {}, task info is null.", taskId);
                return false;
            }

            // 上线任务，更新任务Online状态为ONLINE（true）、dagId = -1
            TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
            updateTbClockworkTask.setId(taskId);
            updateTbClockworkTask.setDagId(-1);
            updateTbClockworkTask.setOnline(TaskTakeEffectStatus.ONLINE.getValue());
            int count = tbClockworkTaskMapper.updateByPrimaryKeySelective(updateTbClockworkTask);
            LOG.info("disableTaskTx-updateByPrimaryKeySelective taskId = {}, dagId = -1, count = {}", taskId, count);

            // 如果有配置时间，则将对应的时间配置设置一下
            if (StringUtils.isNotBlank(dbTbClockworkTask.getCronExp())) {
                loopClockService.addTaskToLoopClockSlot(dbTbClockworkTask, 1);
                Date triggerTime = CronExpression.nextMatchingDate(dbTbClockworkTask.getCronExp());
                updateTaskNextTriggerTime(taskId, triggerTime);
                LOG.info("enableTask-updateTaskNextTriggerTime taskId = {}, triggerTime = {}", taskId, triggerTime);
            }

            // 设置相应的依赖关系为有效
            int relation = taskRelationService.updateTaskRelationIsEffective(taskId, TaskRelationTakeEffectStatus.ONLINE.getValue());
            LOG.info("enableTask-updateTaskRelationIsEffective taskId = {}, isEffective ={}, relation = {}", taskId,
                    TaskRelationTakeEffectStatus.ONLINE.getValue(), relation);

            // 重新构建DagId
            dbTbClockworkTask.setOnline(TaskTakeEffectStatus.ONLINE.getValue()); // 设置内存状态
            int dagId = dagService.buildDagIdForTask(dbTbClockworkTask);
            LOG.info("enableTask-buildDagIdForTask taskId = {}, dagId = {}, relation = {}.", taskId, dagId, relation);

            // 更新所有子任务的DependenceId字段
            updateChildrenTaskDependencyIfFatherTaskIdChange(taskId);
            return true;
        } catch (Exception e) {
            LOG.error("TaskOperationService-enableTask taskId = {}, Error {}.", taskId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 下线任务任务（修改online状态=OFFLINE，依赖关系置为无效=OFFLINE，重新构建该任务相关的DAG）
     *
     * @param taskId taskId
     * @return boolean
     */
    @Override
    public boolean disableTaskTx(Integer taskId) {
        try {
            LOG.info("TaskOperationService-disableTaskTx taskId = {}.", taskId);
            if (taskId == null) return false;

            boolean getLock = false;
            int count = 0;
            Integer oldDagId = null; // 获取之前的dagId
            try {
                // 分布式锁环境下安全进行
                getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
                if (getLock) {
                    TbClockworkTask dbTbClockworkTask = tbClockworkTaskMapper.selectByPrimaryKey(taskId);
                    if (dbTbClockworkTask == null) {
                        LOG.error("disableTaskTx-selectByPrimaryKey taskId = {}, task info is null.", taskId);
                        return false;
                    } else {
                        oldDagId = dbTbClockworkTask.getDagId(); // oldDagId
                        if (TaskStatusUtil.isStartedTaskStatus(dbTbClockworkTask.getStatus())) {
                            throw new RuntimeException("task status is " + dbTbClockworkTask.getStatus() + " not finished,please try again later.");
                        }
                    }
                    // 下线任务，更新任务Online状态为OFFLINE（false）
                    TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
                    updateTbClockworkTask.setId(taskId);
                    updateTbClockworkTask.setDagId(-1);
                    updateTbClockworkTask.setOnline(TaskTakeEffectStatus.OFFLINE.getValue());
                    count = tbClockworkTaskMapper.updateByPrimaryKeySelective(updateTbClockworkTask);
                    LOG.info("disableTaskTx-updateByPrimaryKeySelective taskId = {}, dagId = -1, count = {}", taskId, count);
                } else {
                    throw new RuntimeException("Same transaction for update task is running,please try again later.");
                }
            } finally {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            }

            // 设置相应的依赖关系为无效
            count = taskRelationService.updateTaskRelationIsEffective(taskId, TaskRelationTakeEffectStatus.OFFLINE.getValue());
            LOG.info("disableTaskTx-updateTaskRelationIsEffective taskId = {}, isEffective = {}, count = {}",
                    taskId, TaskRelationTakeEffectStatus.OFFLINE.getValue(), count);

            // 重新构建DAG，重新构建该oldDagId所有关联任务
            if (oldDagId != null && oldDagId != -1) {
                boolean result = dagService.buildDagIdForDagId(oldDagId);
                LOG.info("disableTaskTx-buildDagIdForDagId taskId = {}, oldDagId = {}, result = {}", taskId, oldDagId, result);
            }

            // 更新所有子任务的DependenceId字段
            updateChildrenTaskDependencyIfFatherTaskIdChange(taskId);
            return true;
        } catch (Exception e) {
            LOG.error("TaskOperationService-disableTaskTx Error {}.", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除任务（非完结状态不能不能删除，需要删除记录，依赖关系，文件关系，重新构建旧的依赖关联关系）
     *
     * @param taskId taskId
     * @return boolean
     */
    @Override
    public boolean deleteTask(int taskId) {
        // check
        if (taskId < 1) {
            LOG.error("deleteTask task {} is null, please check it.", taskId);
            throw new RuntimeException("task id is null, please check it.");
        }

        boolean getLock = false;
        int count = 0;
        Integer oldDagId = null; // 获取之前的dagId
        try {
            // 分布式锁环境下安全进行
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                // 检查状态是不是可以删除
                TbClockworkTask dbTask = tbClockworkTaskMapper.selectByPrimaryKey(taskId);
                if (StringUtils.isBlank(dbTask.getStatus())) {
                    LOG.error("deleteTask task {} status is null, please check it.", taskId);
                    throw new RuntimeException("task status is null, please check it.");
                }
                // 非完结状态不能不能删除
                if (!TaskStatusUtil.canBeDeleteCurrentTaskStatus(dbTask.getStatus())) {
                    LOG.error("[TaskService-deleteTask] task {} can not be deleted,because status = {}", taskId, dbTask.getStatus());
                    throw new RuntimeException("task " + taskId + " status is [" + dbTask.getStatus() + "], "
                            + "can't be deleted,please stop it first.");
                }
                oldDagId = dbTask.getDagId();

                // 将本条记录删除
                count = tbClockworkTaskMapper.deleteByPrimaryKey(taskId);
                LOG.info("deleteTask-deleteByPrimaryKey taskId = {}, count = {}.", taskId, count);

                // 删除上下级的依赖关系
                count = taskRelationService.deleteDependencies(taskId);
                LOG.info("deleteTask-deleteDependencies taskId = {}, count = {}.", taskId, count);

            } else {
                throw new RuntimeException("Same transaction for update task is running,please try again later.");
            }
        } catch (Exception e) {
            LOG.error("TaskOperationService-deleteTask Error {}.", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
        }

        // 删除替换文件依赖关系
        TbClockworkTaskDependencyScriptExample example = new TbClockworkTaskDependencyScriptExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        count = tbClockworkTaskDependencyScriptMapper.deleteByExample(example);
        LOG.info("deleteTask-deleteTaskDependencyScriptByExample taskId = {}, count = {}.", taskId, count);

        // 重新构建旧的依赖关联关系
        boolean result = dagService.buildDagIdForDagId(oldDagId);
        LOG.info("deleteTask-buildDagIdForDagId taskId = {}, dagId = {}, result = {}.", taskId, oldDagId, result);
        return true;
    }

    /**
     * 批量删除任务（由于每个任务的关系逻辑都不同不能做批量操作，这里循环调用上面的删除任务方法）
     *
     * @param taskIds taskIds
     * @return boolean
     */
    @Override
    public boolean deleteTaskList(List<Integer> taskIds) {

        if (CollectionUtils.isEmpty(taskIds)) {         // 是否为空
            LOG.error("deleteTaskList taskIds = {} is null, please check it.", taskIds);
            return false;
        }

        List<TbClockworkTask> clockworkTasks = taskService.getTaskByTaskIds(taskIds);
        if (CollectionUtils.isEmpty(clockworkTasks)) {  // 是否为空
            LOG.error("deleteTaskList task {} info is null, please check it.", taskIds);
            return false;
        }
        boolean getLock = false;
        int count = 0;
        try {
            // 分布式锁环境下安全进行
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                // 检查状态是不是可以删除, 非完结状态不能删除
                for (TbClockworkTask clockworkTask : clockworkTasks) {
                    if (StringUtils.isBlank(clockworkTask.getStatus()) || !TaskStatusUtil.canBeDeleteCurrentTaskStatus(clockworkTask.getStatus())) {
                        LOG.error("[TaskService-deleteTaskList] task {} can not be deleted,because status = {}",
                                clockworkTask.getId(), clockworkTask.getStatus());
                        throw new RuntimeException("task " + clockworkTask.getId() + " status is [" + clockworkTask.getStatus()
                                + "], " + "can't be deleted,please stop it first.");
                    }
                }

                // 将任务删除
                TbClockworkTaskExample example = new TbClockworkTaskExample();
                example.createCriteria().andIdIn(taskIds);
                count = tbClockworkTaskMapper.deleteByExample(example);
                LOG.info("deleteTask-deleteByExample taskIds = {}, count = {}.", taskIds, count);

                // 删除上下级的依赖关系
                count = taskRelationService.deleteDependenciesByTaskIds(taskIds);
                LOG.info("deleteTask-deleteDependenciesByTaskIds taskIds = {}, count = {}.", taskIds, count);
            } else {
                throw new RuntimeException("Same transaction for update task is running,please try again later.");
            }
        } catch (Exception e) {
            LOG.error("TaskOperationService-deleteTaskList Error {}.", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
        }

        // 删除替换文件依赖关系
        TbClockworkTaskDependencyScriptExample dependencyScriptExample = new TbClockworkTaskDependencyScriptExample();
        dependencyScriptExample.createCriteria().andTaskIdIn(taskIds);
        count = tbClockworkTaskDependencyScriptMapper.deleteByExample(dependencyScriptExample);
        LOG.info("deleteTask-deleteTaskDependencyScriptByExample taskIds = {}, count = {}.", taskIds, count);

        // 重新构建旧的依赖关联关系
        // 获取到所有DagIds、获取到该组所有的taskIds
        HashSet<Integer> dagIdsSet = new HashSet<>();
        for (TbClockworkTask clockworkTask : clockworkTasks) {
            dagIdsSet.add(clockworkTask.getDagId());
        }
        List<Integer> dagIds = new ArrayList<>(dagIdsSet);
        boolean result = dagService.buildDagIdForDagIds(dagIds);
        LOG.info("deleteTask-buildDagIdForDagIds taskIds = {}, dagIds = {}, result = {}.", taskIds, dagIds, result);
        return true;
    }

    /**
     * 修改任务（不修改状态、上下线）
     *
     * @param taskPojo task
     * @return count
     */
    @Override
    public int updateTask(TbClockworkTaskPojo taskPojo) {
        int count = -1;
        LOG.info("[TaskOperationService-updateTask] task: {}", taskPojo);
        // 检查需要的字段信息是否都提供完整
        checkRequiredFieldForUpdateTask(taskPojo);

        Integer taskId = taskPojo.getId();
        // 设置任务上线下线以及状态
        taskPojo.setOnline(null);
        taskPojo.setStatus(null);

        // 设置失败尝试次数字段
        setTaskFailedRetries(taskPojo);

        // 查找到之前的关联节点（不包括自己）
        List<Integer> oldDirectlyRelatedTask = taskRelationService.findDirectlyRelationTaskIdsNotIncludeSelf(taskId);

        // 设置触发模式和cron表达式相关字段
        setTriggerModeAndCronExp(taskPojo);

        // 获得当前数据库中的老的任务信息
        TbClockworkTaskPojo oldTask = PojoUtil.convert(tbClockworkTaskMapper.selectByPrimaryKey(taskId), TbClockworkTaskPojo.class);

        boolean getLock = false;
        TbClockworkTask clockworkTask = null;
        try {
            // 分布式锁环境下安全进行
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                if (StringUtils.isNotBlank(taskPojo.getDependencyId())) {
                    // 删除依赖关系
                    TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
                    example.createCriteria().andTaskIdEqualTo(taskId);
                    tbClockworkTaskRelationMapper.deleteByExample(example);

                    // 添加新依赖
                    TbClockworkTaskPojo dbTask = taskService.getTaskById(taskPojo.getId());
                    setTaskRelation(taskPojo, dbTask.getOnline());
                    LOG.info("[TaskOperationService-updateTask]update task relation, " +
                                    "new dependencyId = {}, old dependencyId = {}, task name = {}, task id = {}",
                            taskPojo.getDependencyId(), oldTask.getDependencyId(), taskPojo.getName(), taskId);
                } else {
                    taskPojo.setDependencyId(null);
                    // 删除依赖关系
                    TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
                    example.createCriteria().andTaskIdEqualTo(taskId);
                    tbClockworkTaskRelationMapper.deleteByExample(example);

                    LOG.info("[TaskOperationService-updateTask]task relation is null, " +
                                    "new dependencyId = {}, old dependencyId = {}, task name = {}, task id = {}",
                            taskPojo.getDependencyId(), oldTask.getDependencyId(), taskPojo.getName(), taskId);
                }

                // 设置任务依赖脚本
                setTaskDependencyScript(taskPojo);

                // 设置新生成的command
                setCreateCommand(taskPojo);

                LOG.info("[TaskOperationService-updateTask] task update begin[1],task id = {},task cron exp = {}",
                        taskId, taskPojo.getCronExp());

                clockworkTask = PojoUtil.convert(taskPojo, TbClockworkTask.class);
                LOG.info("[TaskOperationService-updateTask] task update begin[2],task id = {},task cron exp = {}",
                        taskId, taskPojo.getCronExp());
                // 更新任务基本信息
                count = taskMapper.updateByPrimaryKeySelective(clockworkTask);
            } else {
                throw new RuntimeException("Same transaction for update task is running,please try again later.");
            }
        } catch (Exception e) {
            LOG.error("TaskOperationService-updateTask Error {}.", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
        }

        // 更新dagId(查找到关联节点，然后批量更新)
        boolean result = dagService.buildDagIdForTaskUpdate(taskId, oldDirectlyRelatedTask);
        LOG.info("[TaskOperationService-updateTask-buildDagIdForTaskId]update task {}, task id = {}", result, taskId);

        // 有cron表达式需要更新环形时钟数据结构，更新下一次正常触发时间
        if (StringUtils.isNotBlank(taskPojo.getCronExp())) {
            // 修改时钟信息需要这个字段
            if (taskPojo.getGroupId() == null || taskPojo.getGroupId() < 1) {
                clockworkTask.setGroupId(oldTask.getGroupId());
            }
            loopClockService.addTaskToLoopClockSlot(clockworkTask, 1);
            Date triggerTime = CronExpression.nextMatchingDate(taskPojo.getCronExp());
            updateTaskNextTriggerTime(clockworkTask.getId(), triggerTime);
        }
        LOG.info("[TaskOperationService][updateTask] task update success,task id = {},task cron exp = {}",
                taskId, taskPojo.getCronExp());
        return count;
    }

    /**
     * 批量添加任务组和任务
     *
     * @param taskGroupAndTasks 任务组以及任务List
     * @return
     */
    @Override
    public TaskGroupAndTasks addTaskList(TaskGroupAndTasks taskGroupAndTasks) {
        if (taskGroupAndTasks == null) {
            throw new RuntimeException("[addTaskList]taskGroupAndTask object is null.");
        }

        if (taskGroupAndTasks.getTaskGroup() == null) {
            throw new RuntimeException("[addTaskList]taskGroupAndTask#getTaskGroup is null.");
        }

        if (StringUtils.isBlank(taskGroupAndTasks.getTaskGroup().getName())) {
            throw new RuntimeException("[addTaskList]taskGroupAndTask#getTaskGroup is null.");
        }

        if (CollectionUtils.isEmpty(taskGroupAndTasks.getTasks())) {
            throw new RuntimeException("[addTaskList]taskGroupAndTask#getTasks is null.");
        }

        if (StringUtils.isBlank(taskGroupAndTasks.getOperator())) {
            throw new RuntimeException("[addTaskList]taskGroupAndTask#getOperator is null.");
        }

        TbClockworkTaskGroupPojo taskGroup = taskGroupAndTasks.getTaskGroup();

        // 检查任务组名称是否重复
        TbClockworkTaskGroupExample TbClockworkTaskGroupExample = new TbClockworkTaskGroupExample();
        TbClockworkTaskGroupExample.createCriteria().andNameEqualTo(taskGroup.getName());
        long counter = tbClockworkTaskGroupMapper.countByExample(TbClockworkTaskGroupExample);
        if (counter > 0) {
            throw new RuntimeException("[addTaskList]task group name already exist, name = "
                    + taskGroup.getName() + ", can't be added,please check it.");
        }

        // 添加任务组
        TbClockworkTaskGroup TbClockworkTaskGroup = PojoUtil.convert(taskGroup, TbClockworkTaskGroup.class);
        TbClockworkTaskGroup.setUserName(taskGroupAndTasks.getOperator());
        TbClockworkTaskGroup.setUpdateTime(new Date());
        TbClockworkTaskGroup.setCreateTime(new Date());
        TbClockworkTaskGroup.setTakeEffectStatus(TaskGroupTakeEffectStatus.ENABLE.getValue());
        tbClockworkTaskGroupMapper.insertSelective(TbClockworkTaskGroup);
        // 设置组ID用于返回给调用方
        taskGroupAndTasks.getTaskGroup().setId(TbClockworkTaskGroup.getId());
        // 添加tasks
        addTasksWhenAddTaskGroupAndTask(taskGroupAndTasks);
        return taskGroupAndTasks;
    }

    /**
     * 批量修改任务组和任务
     *
     * @param taskGroupAndTasks taskGroupAndTasks
     * @return
     */
    @Override
    public TaskGroupAndTasks updateTaskList(TaskGroupAndTasks taskGroupAndTasks) {
        if (taskGroupAndTasks == null) {
            throw new RuntimeException("[updateTaskList]taskGroupAndTask object is null.");
        }

        if (taskGroupAndTasks.getTaskGroup() == null) {
            throw new RuntimeException("[updateTaskList]taskGroupAndTask#getTaskGroup object is null.");
        }

        if (taskGroupAndTasks.getTaskGroup().getId() == null) {
            throw new RuntimeException("[updateTaskList]taskGroupAndTask#getTaskGroup#getId object is null.");
        }

        if (StringUtils.isBlank(taskGroupAndTasks.getTaskGroup().getName())) {
            throw new RuntimeException("[updateTaskList]taskGroupAndTask#getTaskGroup#getName object is null.");
        }

        if (StringUtils.isBlank(taskGroupAndTasks.getOperator())) {
            throw new RuntimeException("[updateTaskList]taskGroupAndTask#getOperator object is null.");
        }

        if (CollectionUtils.isEmpty(taskGroupAndTasks.getTasks())) {
            throw new RuntimeException("[updateTaskList]taskGroupAndTask.getTasks object is null.");
        }

        TbClockworkTaskGroupPojo taskGroup = taskGroupAndTasks.getTaskGroup();

        // 检查任务组名称是否重复
        TbClockworkTaskGroupExample TbClockworkTaskGroupExample = new TbClockworkTaskGroupExample();
        TbClockworkTaskGroupExample.createCriteria()
                .andNameEqualTo(taskGroup.getName())
                .andIdNotEqualTo(taskGroup.getId());
        long counter = tbClockworkTaskGroupMapper.countByExample(TbClockworkTaskGroupExample);
        if (counter > 0) {
            throw new RuntimeException("[updateTaskList]task group name already exist, name = "
                    + taskGroup.getName() + ",current update task group id = "
                    + taskGroup.getId() + ", can't be updated,please check it.");
        }

        TbClockworkTaskGroup TbClockworkTaskGroup = PojoUtil.convert(taskGroup, TbClockworkTaskGroup.class);
        TbClockworkTaskGroup.setUserName(taskGroupAndTasks.getOperator());
        TbClockworkTaskGroup.setUpdateTime(new Date());
        // 不能修改任务组的状态，专门的接口修改任务组的状态
        TbClockworkTaskGroup.setStatus(null);
        tbClockworkTaskGroupMapper.updateByPrimaryKeySelective(TbClockworkTaskGroup);

        List<TbClockworkTaskPojo> tasks = taskGroupAndTasks.getTasks();

        // 发现哪些是新增的、哪些是修改的、哪些是删除的
        List<TbClockworkTaskPojo> addTasks = new ArrayList<>();
        List<TbClockworkTaskPojo> updateTasks = new ArrayList<>();
        List<Integer> deleteTasksId = new ArrayList<>();

        // 找出需要新增和修改的任务作业，并将task组装成map结构用于后续找出需要删除的任务作业逻辑使用
        Map<Integer, TbClockworkTaskPojo> taskMap = new HashMap<>();
        for (TbClockworkTaskPojo task : tasks) {
            if (task.getId() != null) {
                updateTasks.add(task);
                taskMap.put(task.getId(), task);
                LOG.info("[updateTaskList]find need update task info is  {}", task.toString());
            } else {
                addTasks.add(task);
                LOG.info("[updateTaskList]find need add task info is  {}", task.toString());
            }
        }

        // 找出任务组下需要删除的任务作业
        TbClockworkTaskExample tbClockworkTaskExample = new TbClockworkTaskExample();
        tbClockworkTaskExample.createCriteria().andGroupIdEqualTo(taskGroup.getId());

        List<TbClockworkTask> TbClockworkTasks = tbClockworkTaskMapper.selectByExample(tbClockworkTaskExample);
        if (CollectionUtils.isNotEmpty(TbClockworkTasks)) {
            for (TbClockworkTask TbClockworkTask : TbClockworkTasks) {
                // 等于空则代表此任务是需要被删除的
                if (taskMap.get(TbClockworkTask.getId()) == null) {
                    deleteTasksId.add(TbClockworkTask.getId());
                    LOG.info("[updateTaskList]find need delete task id = {} that status is {}",
                            TbClockworkTask.getId(), TbClockworkTask.getStatus());
                } else {
                    LOG.info("[updateTaskList]Can not need delete task id = {} that status is {}",
                            TbClockworkTask.getId(), TbClockworkTask.getStatus());
                }
            }
        } else {
            LOG.info("[updateTaskList]Not found not tasks info that task group id is {}", taskGroup.getId());
        }

        if (CollectionUtils.isNotEmpty(addTasks)) {
            LOG.info("[updateTaskList]add tasks size = {},task stream id = {}",
                    addTasks.size(), taskGroupAndTasks.getTaskGroup().getId());
            addTasksWhenUpdateTaskGroupAndTask(taskGroupAndTasks, addTasks, updateTasks);
        } else {
            LOG.info("[updateTaskList]add tasks is null,task stream and task = {}", taskGroupAndTasks.toString());
        }

        if (CollectionUtils.isNotEmpty(updateTasks)) {
            LOG.info("[updateTaskList]update tasks size = {},task stream id = {}"
                    , updateTasks.size(), taskGroupAndTasks.getTaskGroup().getId());
            updateTasksWhenUpdateTaskGroupAndTask(taskGroupAndTasks, addTasks, updateTasks);
        } else {
            LOG.info("[updateTaskList]update tasks is null,task stream and task = {}", taskGroupAndTasks.toString());
        }

        if (CollectionUtils.isNotEmpty(deleteTasksId)) {
            LOG.info("[updateTaskList]delete tasks size = {},task stream id = {}",
                    deleteTasksId.size(), taskGroupAndTasks.getTaskGroup().getId());
            deleteTaskList(deleteTasksId);
        } else {
            LOG.info("[updateTaskList]delete tasks is null,task stream id = {}", taskGroupAndTasks.getTaskGroup().getId());
        }

        return taskGroupAndTasks;
    }

    /**
     * 检查当前任务依赖的父任务是否都是成功的状态
     *
     * @param task task
     * @return
     */
    @Override
    public boolean checkParentsSuccess(TbClockworkTaskPojo task) {
        // 检查当前任务依赖的父任务是否都是成功的状态
        List<TbClockworkTaskPojo> fartherTasks = taskRelationService.getTaskDirectlyFatherNotIncludeSelf(task.getId());

        // 没有父亲直接返回成功
        if (CollectionUtils.isEmpty(fartherTasks)) {
            LOG.info("Not found parents,so return success directly,task id = {}", task.getId());
            return true;
        } else {
            LOG.info("Found parents size is = {},task id = {}", fartherTasks.size(), task.getId());
        }

        // 获得状态不是成功的父任务信息
        List<String> fathersNoSuccess = new ArrayList<>();
        for (TbClockworkTaskPojo fartherTask : fartherTasks) {
            if (fartherTask.getOnline() == null || !fartherTask.getOnline()) {
                LOG.info("Found father is offline, skip. father current status = {}, father id = {}, task id = {}, "
                                + "task current status = {}", fartherTask.getStatus(), fartherTask.getId(),
                        task.getId(), task.getStatus());
                continue;
            }
            if (!fartherTask.getStatus().equals(TaskStatus.SUCCESS.getValue())) {
                LOG.info("Found father is not success status, father current status = {}, father id = {}, task id = {}, "
                                + "task current status = {}", fartherTask.getStatus(), fartherTask.getId(),
                        task.getId(), task.getStatus());
                fathersNoSuccess.add(fartherTask.getId() + ":" + fartherTask.getStatus());
            }
        }

        // 如果有父亲任务没有成功，则返回false
        if (!fathersNoSuccess.isEmpty()) {
            LOG.info("Found father is not success status, fathers status = {}, task id = {}, task current status = {}",
                    org.apache.commons.lang.StringUtils.join(fathersNoSuccess, ","), task.getId(), task.getStatus());
            return false;
        }
        return true;
    }

    /**
     * 新增任务
     *
     * @param taskPojo
     * @return
     */
    @Override
    public boolean addTask(TbClockworkTaskPojo taskPojo) {
        try {
            LOG.info("[TaskOperationService-addTask] task: {}", taskPojo);

            // 检查需要的字段信息是否都提供完整
            checkRequiredFieldForAddTask(taskPojo);

            //判断是否需要管理员审核，如果是则将新增任务的状态变为 DISABLE。
            taskPojo.setUpdateTime(new Date());
            taskPojo.setCreateTime(new Date());

            taskPojo.setStatus(TaskStatus.ENABLE.getValue());
            taskPojo.setOnline(TaskTakeEffectStatus.ONLINE.getValue());

            // 设置失败尝试次数字段
            setTaskFailedRetries(taskPojo);

            // 设置触发模式和cron相关表达式
            setTriggerModeAndCronExp(taskPojo);

            // 设置是不是流的第一个任务
            List<TbClockworkTask> tasks = taskService.getTaskByTaskGroupId(taskPojo.getGroupId());
            List<TbClockworkTaskPojo> taskPojos = PojoUtil.convertList(tasks, TbClockworkTaskPojo.class);
            taskPojo.setIsFirst(CollectionUtils.isEmpty(taskPojos));

            // 设置Command字段
            setCreateCommand(taskPojo);

            // 新增task信息
            TbClockworkTask tbClockworkTask = PojoUtil.convert(taskPojo, TbClockworkTask.class);
            tbClockworkTaskMapper.insertSelective(tbClockworkTask);

            taskPojo.setId(tbClockworkTask.getId());
            LOG.info("[TaskOperationService-addTask]add task success, task id = {}", tbClockworkTask.getId());

            // 有cron表达式需要更新环形时钟数据结构，更新下一次正常触发时间
            if (StringUtils.isNotBlank(taskPojo.getCronExp())) {
                loopClockService.addTaskToLoopClockSlot(tbClockworkTask, 1);
                Date triggerTime = CronExpression.nextMatchingDate(taskPojo.getCronExp());
                updateTaskNextTriggerTime(tbClockworkTask.getId(), triggerTime);
            }

            // 设置任务依赖关系
            if (StringUtils.isNotBlank(taskPojo.getDependencyId())) {
                setTaskRelation(taskPojo, true);
            }

            // 设置依赖文件
            setTaskDependencyScript(taskPojo);

            // 设置dagId(在更改依赖之后设置)
            dagService.buildDagIdForTaskId(taskPojo.getId());

            LOG.info("[TaskOperationService][addTask]add task success, task id = {}, trigger time = {}, "
                            + "run frequency = {}, time type = {}", tbClockworkTask.getId(),
                    taskPojo.getTriggerTime(), taskPojo.getRunFrequency(), taskPojo.getTimeType());
        } catch (Exception e) {
            LOG.error("Error {}.", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    /**
     * 更新任务的下一次触发时间
     *
     * @param taskId      任务Id
     * @param triggerTime 触发时间
     * @return bool
     */
    @Override
    public boolean updateTaskNextTriggerTime(Integer taskId, Date triggerTime) {
        TbClockworkTask record = new TbClockworkTask();
        record.setId(taskId);
        record.setNextTriggerTime(triggerTime);
        tbClockworkTaskMapper.updateByPrimaryKeySelective(record);
        return true;
    }

    /**
     * 更新任务状态
     *
     * @param taskId id
     * @param status status
     * @return count
     */
    @Override
    public int updateTaskStatus(Integer taskId, String status) {
        LOG.info("updateTaskStatus, taskPojo.id = {}, taskPojo.status = {}", taskId, status);
        TbClockworkTask record = new TbClockworkTask();
        record.setId(taskId);
        record.setStatus(status);
        return tbClockworkTaskMapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 更新作业任务信息
     *
     * @param taskPojo task
     * @return count
     */
    @Override
    public int updateTaskInfo(TbClockworkTaskPojo taskPojo) {
        LOG.info("updateTaskInfo, taskPojo.id = {}, taskPojo.status = {}", taskPojo.getId(), taskPojo.getStatus());
        return tbClockworkTaskMapper.updateByPrimaryKeySelective(PojoUtil.convert(taskPojo, TbClockworkTask.class));
    }

    /**
     * 更新任务dagId
     *
     * @param taskId 任务ID
     * @param dagId  dagId
     * @return count
     */
    @Override
    public int updateTaskDagId(Integer taskId, Integer dagId) {
        LOG.info("updateTaskDagId, taskId = {}, dagId = {}", taskId, dagId);
        TbClockworkTask record = new TbClockworkTask();
        record.setId(taskId);
        record.setDagId(dagId);
        return tbClockworkTaskMapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 批量更新任务dagId
     *
     * @param taskIds 任务IDs
     * @param dagId   dagId
     * @return count
     */
    @Override
    public int updateTaskDagIdByBatch(List<Integer> taskIds, Integer dagId) {
        LOG.info("updateTaskDagIdByBatch, taskIds = {}, dagId = {}", taskIds, dagId);
        if (CollectionUtils.isEmpty(taskIds)) return 0;
        return taskBatchMapper.batchUpdateTaskTagId(taskIds, dagId);
    }


    /**
     * 批量更新子任务的TaskDependencyId字段当父任务Ids变更时（disable/enable后），注意删除的关系不能调用此方法因为已经不存在
     *
     * @param fatherTaskId fatherTaskId
     */
    @Override
    public void updateChildrenTaskDependencyIfFatherTaskIdsChange(List<Integer> fatherTaskId) {
        try {
            if (CollectionUtils.isEmpty(fatherTaskId)) return;
            TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
            example.createCriteria().andFatherTaskIdIn(fatherTaskId);
            List<TbClockworkTaskRelation> relations = tbClockworkTaskRelationMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(relations)) {
                LinkedHashSet<Integer> childrenIdsSet = new LinkedHashSet<>();
                for (TbClockworkTaskRelation relation : relations) {
                    childrenIdsSet.add(relation.getTaskId());
                }
                List<Integer> childrenIds = new ArrayList<>(childrenIdsSet);
                // 更新子任务的DependenceId字段
                if (CollectionUtils.isEmpty(childrenIds)) return;
                for (Integer taskId : childrenIds) {
                    updateTaskDependencyIdFieldById(taskId);
                }
                LOG.info("updateChildrenTaskDependencyIfFatherTaskIdsChange, fatherTaskId = {}, childrenIds = {}.", fatherTaskId, childrenIds);

            } else {
                LOG.info("updateChildrenTaskDependencyIfFatherTaskIdsChange, not need update  fatherTaskId = {}, because children.count = 0.", fatherTaskId);
            }
        } catch (Exception e) {
            LOG.error("updateChildrenTaskDependencyIfFatherTaskIdsChange fatherTaskId = {}, Error {}.", fatherTaskId, e.getMessage(), e);
        }
    }

    /**
     * 更新子任务的TaskDependencyId字段当父任务Id变更时（disable/enable后），注意删除的关系不能调用此方法因为已经不存在
     *
     * @param fatherTaskId fatherTaskId
     */
    @Override
    public void updateChildrenTaskDependencyIfFatherTaskIdChange(Integer fatherTaskId) {
        try {
            if (fatherTaskId == null) return;
            TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
            example.createCriteria().andFatherTaskIdEqualTo(fatherTaskId);
            List<TbClockworkTaskRelation> relations = tbClockworkTaskRelationMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(relations)) {
                List<Integer> childrenIds = relations.stream().map(TbClockworkTaskRelation::getTaskId).collect(Collectors.toList());
                // 更新子任务的DependenceId字段
                if (CollectionUtils.isEmpty(childrenIds)) return;
                for (Integer taskId : childrenIds) {
                    updateTaskDependencyIdFieldById(taskId);
                }
                LOG.info("updateChildrenTaskDependencyIfFatherTaskIdChange, fatherTaskId = {}, childrenIds = {}.", fatherTaskId, childrenIds);
            } else {
                LOG.info("updateChildrenTaskDependencyIfFatherTaskIdChange, not need update fatherTaskId = {}, because children.count = 0.", fatherTaskId);
            }
        } catch (Exception e) {
            LOG.error("updateChildrenTaskDependencyIfFatherTaskIdChange fatherTaskId = {}, Error {}.", fatherTaskId, e.getMessage(), e);
        }
    }

    /**
     * 更新依赖关系字段
     *
     * @param taskId 需要更新的任务Id
     */
    @Override
    public void updateTaskDependencyIdFieldById(Integer taskId) {
        try {
            TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
            example.createCriteria()
                    .andTaskIdEqualTo(taskId).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
            List<TbClockworkTaskRelation> relations = tbClockworkTaskRelationMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(relations)) {
                List<Integer> fatherIds = relations.stream().map(TbClockworkTaskRelation::getFatherTaskId).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(fatherIds)) return;
                StringBuilder builder = new StringBuilder();
                for (Integer fatherId : fatherIds) {
                    builder.append(fatherId).append(",");
                }
                String dependencyId = builder.length() > 0 ? builder.substring(0, builder.length() - 1) : "";
                TbClockworkTask record = new TbClockworkTask();
                record.setId(taskId);
                record.setDependencyId(dependencyId);
                int count = tbClockworkTaskMapper.updateByPrimaryKeySelective(record);
                LOG.info("updateTaskDependencyIdFieldById, taskId = {}, dependencyId = {}, count = {}", taskId, dependencyId, count);
            } else {
                TbClockworkTask record = new TbClockworkTask();
                record.setId(taskId);
                record.setDependencyId("");
                int count = tbClockworkTaskMapper.updateByPrimaryKeySelective(record);
                LOG.info("updateTaskDependencyIdFieldById, taskId = {}, relations.size = 0, count = {}", taskId, count);
            }
        } catch (Exception e) {
            LOG.error("updateTaskDependencyIdFieldById taskId = {}, Error {}.", taskId, e.getMessage(), e);
        }
    }


    /**
     * 子任务更新为父任务不成功状态
     *
     * @param taskId           父任务Id
     * @param executeType      执行类型
     * @param rerunBatchNumber 批次号
     * @return bool
     */
    @Override
    public boolean updateTaskAllChildrenStatusFatherNotSuccess(Integer taskId, Integer executeType, Long rerunBatchNumber) {

        List<Integer> taskIds = null;           // 需要更改状态的所有子任务taskIds
        TbClockworkTaskExample example = null;  // example
        LOG.info("updateTaskAllChildrenStatusFatherNotSuccess taskId = {}, executeType = {}, rerunBatchNumber = {}.",
                taskId, executeType, rerunBatchNumber);
        // 例行任务更新为父任务不成功状态
        if (TaskExecuteType.ROUTINE.getCode().equals(executeType)) {
            List<TbClockworkTaskRelationPojo> children = taskRelationService.findTaskAllChildrenNotIncludeSelf(taskId);
            if (CollectionUtils.isEmpty(children)) {
                LOG.info("updateTaskAllChildrenStatusFatherNotSuccess not find children task, taskId = {}.", taskId);
                return true;
            }
            taskIds = children.stream().map(TbClockworkTaskRelationPojo::getTaskId).collect(Collectors.toList());
            example = new TbClockworkTaskExample();
            example.createCriteria().andIdIn(taskIds).andStatusEqualTo(TaskStatus.LIFE_CYCLE_RESET.getValue());
        }

        // 其他重启等任务更新为父任务不成功状态
        else {
            List<TbClockworkTaskRerunRelation> children
                    = taskRerunRelationService.findTaskAllChildrenNotIncludeSelf(taskId, rerunBatchNumber);
            if (CollectionUtils.isEmpty(children)) {
                LOG.info("updateTaskAllChildrenStatusFatherNotSuccess not find children task, taskId = {}.", taskId);
                return true;
            }
            taskIds = children.stream().map(TbClockworkTaskRerunRelation::getTaskId).collect(Collectors.toList());
            example = new TbClockworkTaskExample();
            example.createCriteria().andIdIn(taskIds).andStatusEqualTo(TaskStatus.RERUN_SCHEDULE_PREP.getValue());
        }

        // 更新状态update status = FATHER_NOT_SUCCESS
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(TaskStatus.FATHER_NOT_SUCCESS.getValue());
        int count = tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
        LOG.info("updateTaskAllChildrenStatusFatherNotSuccess taskId = {}, update taskIds = {}, status = {},  count = {}.",
                taskId, taskIds, TaskStatus.FATHER_NOT_SUCCESS.getValue(), count);
        return true;
    }

    @Override
    public boolean updateTaskStatusSubmit(int taskId) {
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setId(taskId);
        updateTbClockworkTask.setStatus(TaskStatus.SUBMIT.getValue());
        updateTbClockworkTask.setLastStartTime(new Date());
        updateTbClockworkTask.setLastEndTime(null);
        tbClockworkTaskMapper.updateByPrimaryKeySelective(updateTbClockworkTask);
        return true;
    }

    @Override
    public boolean updateTaskStatusSubmitBatch(BatchUpdateTaskStatusSubmit batchUpdateTaskStatusSubmit) {
        TbClockworkTaskExample TbClockworkTaskExample = new TbClockworkTaskExample();
        TbClockworkTaskExample.createCriteria().andIdIn(batchUpdateTaskStatusSubmit.getTaskIds());
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(batchUpdateTaskStatusSubmit.getStatus());
        updateTbClockworkTask.setLastStartTime(new Date());
        updateTbClockworkTask.setLastEndTime(null);
        tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, TbClockworkTaskExample);
        return true;
    }

    @Override
    public boolean updateTaskStatusEndBatch(BatchUpdateTaskStatusEnd batchUpdateTaskStatusEnd) {
        TbClockworkTaskExample TbClockworkTaskExample = new TbClockworkTaskExample();
        TbClockworkTaskExample.createCriteria().andIdIn(batchUpdateTaskStatusEnd.getTaskIds());
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(batchUpdateTaskStatusEnd.getStatus());
        if (!batchUpdateTaskStatusEnd.isStarted()) updateTbClockworkTask.setLastStartTime(new Date());
        updateTbClockworkTask.setLastEndTime(new Date());
        tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, TbClockworkTaskExample);
        return true;
    }

    @Override
    public boolean updateTaskStatusBatch(BatchUpdateTaskStatusParam batchUpdateTaskStatusParam) {
        TbClockworkTaskExample TbClockworkTaskExample = new TbClockworkTaskExample();
        TbClockworkTaskExample.createCriteria().andIdIn(batchUpdateTaskStatusParam.getTaskIds());
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(batchUpdateTaskStatusParam.getStatus());
        tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, TbClockworkTaskExample);
        return true;
    }

    @Override
    public boolean updateTasksDelayStatusByBatch(List<Integer> taskIds, int delayStatus) {
        TbClockworkTaskExample TbClockworkTaskExample = new TbClockworkTaskExample();
        TbClockworkTaskExample.createCriteria().andIdIn(taskIds);
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setDelayStatus(delayStatus);
        tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, TbClockworkTaskExample);
        return true;
    }

    /**
     * 找出任务的所有子孙任务，不包括自己，将作业的状态设置为生命周期重置状态
     *
     * @param taskIds
     * @return
     */
    @Override
    public boolean resetTaskDescendantsLifecycleStatusInBatch(List<Integer> taskIds) {
        List<TbClockworkTaskPojo> allDescendants = new ArrayList<>();
        for (Integer taskId : taskIds) {
            List<TbClockworkTaskPojo> descendantsOfTask = taskRelationService.getTaskAllChildrenNotIncludeSelf(taskId);
            if (descendantsOfTask.isEmpty()) {
                continue;
            }
            allDescendants.addAll(descendantsOfTask);
        }
        List<Integer> allTaskIds = new ArrayList<>();
        for (TbClockworkTaskPojo descendant : allDescendants) {
            allTaskIds.add(descendant.getId());
        }
        if (allTaskIds.isEmpty()) {
            return true;
        }
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andIdIn(allTaskIds);
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(TaskStatus.LIFE_CYCLE_RESET.getValue());
        tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
        return true;
    }

    /**
     * 根据dagIds将作业的状态设置为生命周期重置状态
     *
     * @param dagIds
     * @return
     */
    @Override
    public boolean resetTaskLifecycleStatusByDagIdsInBatch(List<Integer> dagIds, List<Integer> taskIds) {
        // 条件
        if (CollectionUtils.isEmpty(taskIds)) {
            taskIds = Collections.singletonList(-1);
        }
        LOG.info("resetTaskLifecycleStatusByDagIdsInBatch. dagIds = {}, not in taskIds = {}", dagIds, taskIds);
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andOnlineEqualTo(true).andDagIdIn(dagIds).andIdNotIn(taskIds);

        // 更新内容
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(TaskStatus.LIFE_CYCLE_RESET.getValue());
        tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
        return true;
    }


    /**
     * 根据source将作业的状态设置为生命周期重置状态
     *
     * @param source source
     * @return
     */
    @Override
    public boolean resetTaskLifecycleStatusBySource(Integer source) {
        LOG.info("resetTaskLifecycleStatusBySource, source = {}", source);
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andOnlineEqualTo(true)
                .andSourceEqualTo(source).andStatusNotEqualTo(TaskStatus.LIFE_CYCLE_RESET.getValue());

        // 更新内容
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(TaskStatus.LIFE_CYCLE_RESET.getValue());
        int count = tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
        LOG.info("resetTaskLifecycleStatusBySource, update size = {}", count);
        return true;
    }

    /**
     * 根据dagIds将作业的状态设置为生命周期重置状态
     *
     * @param dagIds dagIds
     * @return
     */
    @Override
    public boolean resetTaskLifecycleStatusByDagIds(List<Integer> dagIds) {
        LOG.info("resetTaskLifecycleStatusByDagIds, dagIds = {}", dagIds);
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andOnlineEqualTo(true).andDagIdIn(dagIds);

        // 更新内容
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(TaskStatus.LIFE_CYCLE_RESET.getValue());
        int count = tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
        LOG.info("resetTaskLifecycleStatusByDagIds, update size = {}", count);
        return true;
    }

    /**
     * 根据dagIds将作业的状态设置为生命周期重置状态(排除指定的source)
     *
     * @param dagIds dagIds
     * @return
     */
    @Override
    public boolean resetTaskLifecycleStatusByDagIdsNotExists(List<Integer> dagIds) {
        LOG.info("resetTaskLifecycleStatusByDagIdsNotExists, dagIds = {}", dagIds);
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andOnlineEqualTo(true).andSourceNotEqualTo(TaskSource.DDS_2.getValue()).andDagIdIn(dagIds);

        // 更新内容
        TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
        updateTbClockworkTask.setStatus(TaskStatus.LIFE_CYCLE_RESET.getValue());
        int count = tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
        LOG.info("resetTaskLifecycleStatusByDagIdsNotExists, update size = {}", count);
        return true;
    }

    /**
     * 检查新增和修改task的必要字段信息是否全部提供
     *
     * @param taskPojo param
     */
    private void checkRequiredFieldForAddTask(TbClockworkTaskPojo taskPojo) {
        if (taskPojo == null
                || taskPojo.getGroupId() == null
                || taskPojo.getGroupId() < 1
                || StringUtils.isBlank(taskPojo.getName())
                || StringUtils.isBlank(taskPojo.getLocation())
                || StringUtils.isBlank(taskPojo.getScriptName())
                || taskPojo.getNodeGid() == null || taskPojo.getNodeGid() < 1
                || StringUtils.isBlank(taskPojo.getCreateUser())
                || StringUtils.isBlank(taskPojo.getOperatorName())) {
            if (taskPojo == null) throw new RuntimeException("param is null");
            throw new RuntimeException("task required field[" +
                    (taskPojo.getGroupId() == null || taskPojo.getGroupId() < 1 ? "groupId," : "") +
                    (taskPojo.getNodeGid() == null || taskPojo.getNodeGid() < 1 ? "nodeGid," : "") +
                    (StringUtils.isBlank(taskPojo.getName()) ? "name," : "") +
                    (StringUtils.isBlank(taskPojo.getLocation()) ? "location," : "") +
                    (StringUtils.isBlank(taskPojo.getScriptName()) ? "scriptName," : "") +
                    (StringUtils.isBlank(taskPojo.getCreateUser()) ? "createUser," : "") +
                    (StringUtils.isBlank(taskPojo.getOperatorName()) ? "operatorName" : "") +
                    "] cannot be empty, please check it.");
        }

        // 作业脚本所在的文件夹全路径，必须以指定的配置路径为前缀
        String locationValidate = StringUtils.endsWith(taskPojo.getLocation(), File.separator) ?
                taskPojo.getLocation() : taskPojo.getLocation() + File.separator;

        if (!StringUtil.locationStartsWithListPrefix(locationValidate, uploadPathPrefix)) {
            throw new RuntimeException("task location must be in [" + uploadPathPrefix + "] "
                    + "as prefix, please check it, task location is [" + locationValidate + "]");
        }


        // 作业脚本的脚本名称，不包含文件路径
        if (StringUtils.startsWith(taskPojo.getScriptName(), "sh ")) {
            throw new RuntimeException("task script name don't need add 'sh ' prefix!");
        }

        // 检查新增的task作业名称是否已经存在，如果存在不能添加
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andNameEqualTo(taskPojo.getName());
        long counter = tbClockworkTaskMapper.countByExample(example);
        if (counter > 0) {
            throw new RuntimeException("[checkRequiredFieldForAddTask]task name already exist, name = ["
                    + taskPojo.getName() + "], can't be added, please check it.");
        } else {
            LOG.info("[checkRequiredFieldForAddTask]Check same name of task passed, task name = {}", taskPojo.getName());
        }
    }

    /**
     * 检查并控制任务失败重试次数的合理性，不设置则为0不重试，失败最多尝试三次
     *
     * @param taskPojo
     */
    private void setTaskFailedRetries(TbClockworkTaskPojo taskPojo) {
        if (taskPojo.getFailedRetries() == null || taskPojo.getFailedRetries() < 0) {
            taskPojo.setFailedRetries(0);
            return;
        }

        if (taskPojo.getFailedRetries() > taskFailedRetriesMaxNumber) {
            taskPojo.setFailedRetries(taskFailedRetriesMaxNumber);
            return;
        }

        if (taskPojo.getFailedRetries() > 0 && taskPojo.getFailedRetries() <= taskFailedRetriesMaxNumber) {
            taskPojo.setFailedRetries(taskPojo.getFailedRetries());
        }
    }

    private void setTaskRelation(TbClockworkTaskPojo taskPojo, Boolean isEffective) {
        String[] ids = taskPojo.getDependencyId().split(",");
        List<Integer> idIntegers = new ArrayList<>();
        for (String id : ids) {
            idIntegers.add(Integer.parseInt(id));
        }
        List<TbClockworkTask> tasks = taskService.getTaskByTaskIds(idIntegers);
        for (int i = 0; i < tasks.size(); i++) {
            TbClockworkTaskRelationPojo taskRelation = new TbClockworkTaskRelationPojo();
            taskRelation.setTaskId(taskPojo.getId());
            taskRelation.setTaskName(taskPojo.getName());
            taskRelation.setCreateTime(new Date());
            taskRelation.setFatherTaskId(Integer.parseInt(ids[i]));
            taskRelation.setFatherTaskName(tasks.get(i).getName());
            taskRelation.setIsEffective(isEffective);
            taskRelationService.addTaskRelation(taskRelation);
        }
    }

    private void setTriggerModeAndCronExp(TbClockworkTaskPojo taskPojo) {
        //设置trigger mode
        if (!TaskTriggerModel.SIGNAL.getValue().equals(taskPojo.getTriggerMode())) {
            // 有cron4j表达式或者有时间相关的参数，则触发模式为TIME_AND_DEPENDENCY || TIME
            if (StringUtils.isNotBlank(taskPojo.getCronExp()) ||
                    (taskPojo.getTriggerTime() != null
                            && taskPojo.getRunFrequency() != null
                            && taskPojo.getTimeType() != null)) {
                taskPojo.setTriggerMode(StringUtils.isNotBlank(taskPojo.getDependencyId()) ?
                        TaskTriggerModel.TIME_AND_DEPENDENCY.getValue() : TaskTriggerModel.TIME.getValue());
            } else {
                // 没有时间，那必须是单存依赖
                taskPojo.setTriggerMode(TaskTriggerModel.DEPENDENCY.getValue());
            }
        }

        //cron exp 生成逻辑
        Integer triggerMode = taskPojo.getTriggerMode();
        TaskTriggerModel triggerModelEnum = TaskTriggerModel.getEnumByCode(triggerMode);
        switch (triggerModelEnum) {
            case TIME:
            case TIME_AND_DEPENDENCY:
                // 当触发模式为时间触发或者时间触发和依赖触发时，需要检测或生成cron4j表达式
                if (StringUtils.isNotBlank(taskPojo.getCronExp())) {
                    break;
                }
                if (taskPojo.getTriggerTime() != null
                        && taskPojo.getRunFrequency() != null
                        && taskPojo.getTimeType() != null) {
                    // 生成并设置任务的cron时间表达式
                    String cronExp = CronExpression.createCronExpByTriggerTime(
                            taskPojo.getTriggerTime(), taskPojo.getTimeType(), taskPojo.getRunFrequency());
                    taskPojo.setCronExp(cronExp);
                    LOG.info("[TaskOperationService]Current task have cron exp set," +
                                    "trigger time = {}, run frequency = {}, time type = {}, cron exp = {}",
                            taskPojo.getTriggerTime(), taskPojo.getRunFrequency(), taskPojo.getTimeType(), cronExp);
                } else {
                    // 时间相关触发模式，既没有cron表达式，也没有相关生成逻辑，则永远也运行不起来
                    throw new RuntimeException(
                            "Task has neither cron expression nor related generation logic,that will be stop forever!" +
                                    " task id = " + taskPojo.getId() +
                                    " ,trigger mode = " + triggerMode +
                                    ", trigger time = " + taskPojo.getTriggerTime() +
                                    ", run frequency = " + taskPojo.getRunFrequency() +
                                    ", time type = " + taskPojo.getTimeType());
                }
                break;
            case DEPENDENCY:
                if (StringUtils.isBlank(taskPojo.getDependencyId())) {
                    throw new RuntimeException(
                            "Dependent task dependency id cannot be empty! " +
                                    " task id = " + taskPojo.getId() +
                                    " ,trigger mode = " + triggerMode +
                                    " ,dependency id = " + taskPojo.getDependencyId());
                }
                break;
            case SIGNAL:
                taskPojo.setCronExp(null);
                taskPojo.setTriggerTime(null);
                taskPojo.setRunFrequency(null);
                taskPojo.setTimeType(null);
                break;
        }

        LOG.info("[TaskOperationService]Current task cron exp info, task id = {}, trigger mode = {}, cron exp = {}, " +
                        "trigger time = {}, run frequency = {}, time type = {}",
                taskPojo.getId(), triggerMode, taskPojo.getCronExp(),
                taskPojo.getTriggerTime(), taskPojo.getRunFrequency(), taskPojo.getTimeType());
    }

    /**
     * 设置command字段
     *
     * @param taskPojo
     * @return
     */
    private void setCreateCommand(TbClockworkTaskPojo taskPojo) {
        String command = null;
        taskPojo.setLocation(taskPojo.getLocation().trim());
        String location = taskPojo.getLocation().endsWith(File.separator) ?
                taskPojo.getLocation() : taskPojo.getLocation() + File.separator;
        if (StringUtils.isBlank(taskPojo.getScriptParameter())) {
            command = "sh " + location + taskPojo.getScriptName();
        } else {
            command = "sh " + location + taskPojo.getScriptName() + " " + taskPojo.getScriptParameter();
        }
        if (StringUtils.isBlank(command)) {
            throw new RuntimeException("Create command failure, command is null!");
        }
        taskPojo.setCommand(command);
    }

    //设置任务依赖的脚本
    private void setTaskDependencyScript(TbClockworkTaskPojo taskPojo) {
        if (CollectionUtils.isNotEmpty(taskPojo.getDependencyScript())) {
            Date now = new Date();
            // 先删除关系
            TbClockworkTaskDependencyScriptExample example = new TbClockworkTaskDependencyScriptExample();
            example.createCriteria().andTaskIdEqualTo(taskPojo.getId());
            tbClockworkTaskDependencyScriptMapper.deleteByExample(example);
            // 添加依赖脚本关系
            for (TbClockworkTaskDependencyScript dependencyScript : taskPojo.getDependencyScript()) {
                if (StringUtils.isNotBlank(dependencyScript.getScriptFileAbsolutePath())) {
                    dependencyScript.setTaskId(taskPojo.getId());
                    dependencyScript.setCreateTime(now);
                    dependencyScript.setUpdateTime(now);
                    tbClockworkTaskDependencyScriptMapper.insert(dependencyScript);
                }
            }
        }
    }

    /**
     * 检查新增和修改task的必要字段信息是否全部提供
     *
     * @param taskPojo param
     */
    private void checkRequiredFieldForUpdateTask(TbClockworkTaskPojo taskPojo) {
        if (taskPojo == null
                || taskPojo.getId() == null
                || taskPojo.getGroupId() == null || taskPojo.getGroupId() < 1
                || taskPojo.getNodeGid() == null || taskPojo.getNodeGid() < 1
                || StringUtils.isBlank(taskPojo.getName())
                || StringUtils.isBlank(taskPojo.getLocation())
                || StringUtils.isBlank(taskPojo.getScriptName())
                || StringUtils.isBlank(taskPojo.getOperatorName())) {
            if (taskPojo == null) throw new RuntimeException("param is null");
            throw new RuntimeException("task required field[" +
                    (taskPojo.getId() == null ? "taskId," : "") +
                    (StringUtils.isBlank(taskPojo.getName()) ? "name," : "") +
                    (taskPojo.getGroupId() == null || taskPojo.getGroupId() < 1 ? "groupId," : "") +
                    (taskPojo.getNodeGid() == null || taskPojo.getNodeGid() < 1 ? "nodeGid," : "") +
                    (StringUtils.isBlank(taskPojo.getLocation()) ? "location," : "") +
                    (StringUtils.isBlank(taskPojo.getScriptName()) ? "scriptName," : "") +
                    (StringUtils.isBlank(taskPojo.getOperatorName()) ? "operatorName" : "") +
                    "] cannot be empty, please check it.");
        }

        // 检查状态是不是可以修改
        TbClockworkTask oldTask = tbClockworkTaskMapper.selectByPrimaryKey(taskPojo.getId());
        if (oldTask == null) {
            throw new RuntimeException("task info is null on db, please check it,task id = " + taskPojo.getId());
        }

        if (StringUtils.isBlank(oldTask.getStatus())) {
            throw new RuntimeException("task status is null,please check it,task id = " + taskPojo.getId());
        }

        // 非完结状态不能修改
        if (!TaskStatusUtil.canBeUpdateCurrentTaskStatus(oldTask.getStatus())) {
            LOG.info("[checkRequiredFieldForUpdateTask]task can not be updated,because status = {}", oldTask.getStatus());
            throw new RuntimeException("task status is [" + oldTask.getStatus() + "], "
                    + "can't be updated,please check it.");
        }

        // 则判断是否重名
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andNameEqualTo(taskPojo.getName()).andIdNotEqualTo(taskPojo.getId());
        long counter = tbClockworkTaskMapper.countByExample(example);
        if (counter > 0) {
            throw new RuntimeException("[checkRequiredFieldForUpdateTask]task name already exist, name = ["
                    + oldTask.getName() + "], can't be updated,please check it.");
        } else {
            LOG.info("[checkRequiredFieldForUpdateTask]Check same name of task passed，task name = {}", taskPojo.getName());
        }

        if (StringUtils.startsWith(taskPojo.getScriptName(), "sh ")) {
            throw new RuntimeException("task script name don.t need add 'sh ' prefix,please check it.");
        }

        // 作业脚本所在的文件夹全路径，必须以指定的配置路径为前缀
        String locationValidate = org.apache.commons.lang3.StringUtils.endsWith(
                taskPojo.getLocation(), File.separator) ? taskPojo.getLocation() : taskPojo.getLocation() + File.separator;

        if (!StringUtil.locationStartsWithListPrefix(locationValidate, uploadPathPrefix)) {
            throw new RuntimeException("task location must be in [" + uploadPathPrefix + "] "
                    + "as prefix, please check it, task location is [" + locationValidate + "]");
        }
    }


    private void addTasksWhenAddTaskGroupAndTask(TaskGroupAndTasks taskGroupAndTasks) {
        // 存储外部系统和调度系统的任务ID对应信息
        Map<String, TbClockworkTaskPojo> externalTaskAndTssTaskMapping = new HashMap<>();

        for (TbClockworkTaskPojo addTask : taskGroupAndTasks.getTasks()) {
            LOG.info("[addTasksWhenAddTaskGroupAndTask][0]begin]task name = {}", addTask.getName());
            addTask.setCreateUser(taskGroupAndTasks.getOperator());
            addTask.setGroupId(taskGroupAndTasks.getTaskGroup().getId());
            // 临时加入，用于先添加任务到数据库获取ID
            addTask.setDependencyId("-1");
            addTask.setUpdateTime(new Date());
            addTask(addTask);
            externalTaskAndTssTaskMapping.put(addTask.getExternalSystemTaskId(), addTask);
            LOG.info("[addTasksWhenAddTaskGroupAndTask][0][end]task name = {}", addTask.getName());
        }

        // 修改父任务依赖关系
        List<Integer> taskIds = new ArrayList<Integer>();
        for (TbClockworkTaskPojo addTask : taskGroupAndTasks.getTasks()) {
            // 存储调度系统内部的父任务ID集合
            List<Integer> tssSystemFatherTaskIds = new ArrayList<>();
            boolean hasCrossGroupFathers = false;

            // 本组外依赖的父任务处理：如果有依赖的其它任务组的父任务，则将其它任务组的父任务ID加入当前任务依赖的父任务集合中
            Integer taskId = addTask.getId();
            String taskName = addTask.getName();
            taskIds.add(taskId);
            if (StringUtils.isNotBlank(addTask.getTaskFatherIdsCrossTaskGroup())) {
                String[] clockworkFatherIdsCrossTaskGroup = addTask.getTaskFatherIdsCrossTaskGroup().split(",");

                if (clockworkFatherIdsCrossTaskGroup != null && clockworkFatherIdsCrossTaskGroup.length > 0) {
                    // 删除任务依赖关系表的关系，并重新插入关系
                    TbClockworkTaskRelationExample TbClockworkTaskRelationExample = new TbClockworkTaskRelationExample();
                    TbClockworkTaskRelationExample.createCriteria().andTaskIdEqualTo(taskId);
                    tbClockworkTaskRelationMapper.deleteByExample(TbClockworkTaskRelationExample);

                    hasCrossGroupFathers = true;

                    for (String clockworkFatherIdCrossTaskGroup : clockworkFatherIdsCrossTaskGroup) {
                        // 父任务ID转换成INT
                        int clockworkFatherIdCrossTaskGroupInt = Integer.parseInt(clockworkFatherIdCrossTaskGroup);
                        // 添加到依赖的父任务统计队列
                        tssSystemFatherTaskIds.add(clockworkFatherIdCrossTaskGroupInt);

                        TbClockworkTaskRelation TbClockworkTaskRelation = new TbClockworkTaskRelation();
                        TbClockworkTaskRelation.setTaskId(taskId);
                        TbClockworkTaskRelation.setTaskName(taskName);
                        // 设置父任务ID
                        TbClockworkTaskRelation.setFatherTaskId(clockworkFatherIdCrossTaskGroupInt);
                        // 设置父任务名称
                        TbClockworkTaskRelation.setFatherTaskName(
                                tbClockworkTaskMapper.selectByPrimaryKey(clockworkFatherIdCrossTaskGroupInt).getName());
                        tbClockworkTaskRelationMapper.insertSelective(TbClockworkTaskRelation);
                        LOG.info("[addTasksWhenAddTaskGroupAndTask]add cross group father task id = {}," +
                                        "current task id = {},current task name = {}," +
                                        "external system task id = {},task group name = {}",
                                clockworkFatherIdCrossTaskGroup, taskId, taskName,
                                addTask.getExternalSystemTaskId(), taskGroupAndTasks.getTaskGroup().getName());
                    }
                    // 更新调度系统的父任务依赖关系
                    String tssSystemFatherTaskIdsStr = StringUtils.join(tssSystemFatherTaskIds, ",");
                    TbClockworkTask TbClockworkTask = new TbClockworkTask();
                    TbClockworkTask.setId(taskId);
                    TbClockworkTask.setDependencyId(tssSystemFatherTaskIdsStr);
                    tbClockworkTaskMapper.updateByPrimaryKeySelective(TbClockworkTask);

                    LOG.info("[addTasksWhenAddTaskGroupAndTaskCrossTaskGroupFathersFinished]current task id = {}, "
                                    + "external system task id = {}, tss system father task Ids = {}",
                            taskId, addTask.getExternalSystemTaskId(), tssSystemFatherTaskIdsStr);

                }
            } else {
                LOG.info("[addTasksWhenAddTaskGroupAndTask]no found cross group father id info," +
                                "current task id = {},current task name = {}," +
                                "external system task id = {},task group name = {}",
                        taskId, taskName,
                        addTask.getExternalSystemTaskId(), taskGroupAndTasks.getTaskGroup().getName());
            }

            // 本组内依赖的父任务处理：外部系统传递的任务依赖关系，多个父任务依赖逗号分隔
            String externalSystemTaskDependencyId = addTask.getExternalSystemTaskDependencyId();
            if (StringUtils.isBlank(externalSystemTaskDependencyId)) {
                LOG.info("[addTasksWhenAddTaskGroupAndTask][1]" +
                                "ExternalSystemTaskDependencyId field is null,task id = {},external system task id = {}",
                        taskId, addTask.getExternalSystemTaskId());

                // 本组内没有父任务，外组内也没有父任务，则检查是否有Cron表达式
                if (StringUtils.isBlank(addTask.getTaskFatherIdsCrossTaskGroup())) {
                    // 没有父任务依赖，必须有cron exp，否则任务运行不起来，也就是触发模式必须为时间触发
                    if (StringUtils.isBlank(addTask.getCronExp())) {
                        throw new RuntimeException("[addTasksWhenAddTaskGroupAndTask]" +
                                "Cron exp field id null, task id = " + taskId);
                    }

                    // 更新触发模式为时间触发
                    TbClockworkTask TbClockworkTask = new TbClockworkTask();
                    TbClockworkTask.setId(taskId);
                    TbClockworkTask.setTriggerMode(TaskTriggerModel.TIME.getValue());
                    tbClockworkTaskMapper.updateByPrimaryKeySelective(TbClockworkTask);
                } else {
                    LOG.info("[addTasksWhenAddTaskGroupAndTask][1] don't need check cron exp,because of has cross group father." +
                                    " task id = {}, TaskFatherIdsCrossTaskGroup = {}",
                            taskId, addTask.getTaskFatherIdsCrossTaskGroup());
                }
                continue;
            }

            // 获取当前外部系统指定的任务依赖关系
            String[] externalSystemFatherIds = externalSystemTaskDependencyId.split(",");
            if (externalSystemFatherIds == null || externalSystemFatherIds.length < 1) {
                LOG.info("[addTasksWhenAddTaskGroupAndTask][2]" +
                                "ExternalSystemTaskDependencyId field is null,task id = {},external system task id = {}",
                        taskId, addTask.getExternalSystemTaskId());
                continue;
            }


            for (String externalSystemFatherId : externalSystemFatherIds) {
                TbClockworkTaskPojo fatherTask = externalTaskAndTssTaskMapping.get(externalSystemFatherId);
                if (fatherTask == null) {
                    throw new RuntimeException("[addTasksWhenAddTaskGroupAndTask]" +
                            "No found task id by externalSystemFatherId that value is " + externalSystemFatherId);
                }
                tssSystemFatherTaskIds.add(fatherTask.getId());
            }

            // 如果没有跨组依赖的父任务，此处删除任务依赖关系表的关系，并重新插入关系
            if (!hasCrossGroupFathers) {
                TbClockworkTaskRelationExample TbClockworkTaskRelationExample = new TbClockworkTaskRelationExample();
                TbClockworkTaskRelationExample.createCriteria().andTaskIdEqualTo(taskId);
                tbClockworkTaskRelationMapper.deleteByExample(TbClockworkTaskRelationExample);
                LOG.info("[addTasksWhenAddTaskGroupAndTask]need deleted relation,task id = {},hasCrossGroupFathers = {}",
                        taskId, hasCrossGroupFathers);
            } else {
                LOG.info("[addTasksWhenAddTaskGroupAndTask]don't need deleted relation,task id = {},hasCrossGroupFathers = {}",
                        taskId, hasCrossGroupFathers);
            }

            // 添加
            for (String externalSystemFatherId : externalSystemFatherIds) {
                TbClockworkTaskRelation taskRelation = new TbClockworkTaskRelation();
                taskRelation.setTaskId(taskId);
                taskRelation.setTaskName(taskName);
                taskRelation.setFatherTaskId(externalTaskAndTssTaskMapping.get(externalSystemFatherId).getId());
                taskRelation.setFatherTaskName(externalTaskAndTssTaskMapping.get(externalSystemFatherId).getName());
                taskRelation.setIsEffective(TaskRelationTakeEffectStatus.ONLINE.getValue());
                tbClockworkTaskRelationMapper.insertSelective(taskRelation);
            }

            String tssSystemFatherTaskIdsStr = StringUtils.join(tssSystemFatherTaskIds, ",");

            LOG.info("[addTasksWhenAddTaskGroupAndTaskAllFathers]begin add dependency id, task id = {}," +
                            "external system task id = {}, tss system father task Ids = {}, external system father task Ids = {}",
                    taskId, addTask.getExternalSystemTaskId(), tssSystemFatherTaskIdsStr, externalSystemTaskDependencyId);

            // 更新调度系统的父任务依赖关系
            TbClockworkTask TbClockworkTask = new TbClockworkTask();
            TbClockworkTask.setId(taskId);
            TbClockworkTask.setDependencyId(tssSystemFatherTaskIdsStr);

            tbClockworkTaskMapper.updateByPrimaryKeySelective(TbClockworkTask);
        }

        // 重新构建依赖
        dagService.buildDagIdForTaskIds(taskIds);
    }

    private void updateTasksWhenUpdateTaskGroupAndTask(
            TaskGroupAndTasks taskGroupAndTasks, List<TbClockworkTaskPojo> addTasks, List<TbClockworkTaskPojo> updateTasks) {
        Map<String, TbClockworkTaskPojo> externalTaskAndTssTaskMapping = new HashMap<>();
        List<Integer> updateTasksId = new ArrayList<>();

        for (TbClockworkTaskPojo updateTask : updateTasks) {
            externalTaskAndTssTaskMapping.put(updateTask.getExternalSystemTaskId(), updateTask);
            updateTasksId.add(updateTask.getId());
            LOG.info("[updateTasksWhenUpdateTaskGroupAndTask]task info = {}", updateTask.toString());
        }

        if (CollectionUtils.isNotEmpty(addTasks)) {
            for (TbClockworkTaskPojo addTask : addTasks) {
                externalTaskAndTssTaskMapping.put(addTask.getExternalSystemTaskId(), addTask);
            }
        }

        // 删除当前所有修改任务的依赖关系
        TbClockworkTaskRelationExample deleteDependencyExample = new TbClockworkTaskRelationExample();
        deleteDependencyExample.createCriteria().andTaskIdIn(updateTasksId);
        tbClockworkTaskRelationMapper.deleteByExample(deleteDependencyExample);

        // 修改父任务依赖关系
        List<Integer> taskIds = new ArrayList<Integer>();
        for (TbClockworkTaskPojo updateTask : updateTasks) {
            updateTask.setUpdateTime(new Date());

            // 找出对应调度系统内部的任务ID
            List<Integer> tssSystemFatherTaskIds = new ArrayList<>();

            //本组外依赖的父任务处理：如果有依赖的其它任务组的父任务，则将其它任务组的父任务ID加入当前任务依赖的父任务集合中
            Integer taskId = updateTask.getId();
            String taskName = updateTask.getName();
            taskIds.add(taskId);
            if (StringUtils.isNotBlank(updateTask.getTaskFatherIdsCrossTaskGroup())) {
                String[] clockworkFatherIdsCrossTaskGroup = updateTask.getTaskFatherIdsCrossTaskGroup().split(",");
                if (clockworkFatherIdsCrossTaskGroup != null && clockworkFatherIdsCrossTaskGroup.length > 0) {
                    for (String clockworkFatherIdCrossTaskGroup : clockworkFatherIdsCrossTaskGroup) {
                        // 父任务ID转换成INT
                        int clockworkFatherIdCrossTaskGroupInt = Integer.parseInt(clockworkFatherIdCrossTaskGroup);
                        // 添加到依赖的父任务统计队列
                        tssSystemFatherTaskIds.add(clockworkFatherIdCrossTaskGroupInt);

                        TbClockworkTaskRelation TbClockworkTaskRelation = new TbClockworkTaskRelation();
                        TbClockworkTaskRelation.setTaskId(taskId);
                        TbClockworkTaskRelation.setTaskName(taskName);
                        // 设置父任务ID
                        TbClockworkTaskRelation.setFatherTaskId(clockworkFatherIdCrossTaskGroupInt);
                        // 设置父任务名称
                        TbClockworkTaskRelation.setFatherTaskName(
                                tbClockworkTaskMapper.selectByPrimaryKey(clockworkFatherIdCrossTaskGroupInt).getName());
                        tbClockworkTaskRelationMapper.insertSelective(TbClockworkTaskRelation);
                        LOG.info("[updateTasksWhenUpdateTaskGroupAndTask]add cross group father task id = {}, " +
                                        "current task id = {}, current task name = {}, " +
                                        "external system task id = {}, task group name = {}",
                                clockworkFatherIdCrossTaskGroup, taskId, taskName,
                                updateTask.getExternalSystemTaskId(), taskGroupAndTasks.getTaskGroup().getName());
                    }

                    String tssSystemFatherTaskIdsStr = StringUtils.join(tssSystemFatherTaskIds, ",");

                    LOG.info("[updateTasksWhenUpdateTaskGroupAndTaskCrossTaskGroupFathers]begin add dependency id, " +
                                    "current task id = {}, external system task id = {}, tss system father task Ids = {}",
                            taskId, updateTask.getExternalSystemTaskId(), tssSystemFatherTaskIdsStr);
                }
            } else {
                LOG.info("[updateTasksWhenUpdateTaskGroupAndTask]no found cross group father id info, " +
                                "current task id = {}, current task name = {}, " +
                                "external system task id = {}, task group name = {}",
                        taskId, taskName,
                        updateTask.getExternalSystemTaskId(), taskGroupAndTasks.getTaskGroup().getName());
            }

            // 外部系统传递的任务依赖关系，多个父任务依赖逗号分隔
            String externalSystemTaskDependencyId = updateTask.getExternalSystemTaskDependencyId();
            if (StringUtils.isBlank(externalSystemTaskDependencyId)) {
                updateTask.setDependencyId(StringUtils.join(tssSystemFatherTaskIds, ","));
                updateTask(updateTask);
                continue;
            }
            // 获取当前外部系统指定的任务依赖关系
            String[] externalSystemFatherIds = externalSystemTaskDependencyId.split(",");
            if (externalSystemFatherIds == null || externalSystemFatherIds.length < 1) {
                updateTask.setDependencyId(StringUtils.join(tssSystemFatherTaskIds, ","));
                updateTask(updateTask);
                continue;
            }

            for (String externalSystemFatherId : externalSystemFatherIds) {
                TbClockworkTaskPojo externalSystemFatherTask = externalTaskAndTssTaskMapping.get(externalSystemFatherId);
                if (externalSystemFatherTask == null) {
                    throw new RuntimeException("[updateTasksWhenUpdateTaskGroupAndTask]" +
                            "No found task id by externalSystemFatherId that value is " + externalSystemFatherId);
                }
                tssSystemFatherTaskIds.add(externalSystemFatherTask.getId());
            }

            for (String externalSystemFatherId : externalSystemFatherIds) {
                TbClockworkTaskRelation TbClockworkTaskRelation = new TbClockworkTaskRelation();
                TbClockworkTaskRelation.setTaskId(taskId);
                TbClockworkTaskRelation.setTaskName(taskName);
                TbClockworkTaskRelation.setFatherTaskId(externalTaskAndTssTaskMapping.get(externalSystemFatherId).getId());
                TbClockworkTaskRelation.setFatherTaskName(externalTaskAndTssTaskMapping.get(externalSystemFatherId).getName());
                tbClockworkTaskRelationMapper.insertSelective(TbClockworkTaskRelation);
            }
            // 重新设置父任务依赖关系后再修改。
            updateTask.setDependencyId(StringUtils.join(tssSystemFatherTaskIds, ","));
            updateTask(updateTask);
        }

        // 重新构建依赖
        dagService.buildDagIdForTaskIds(taskIds);
    }


    private void addTasksWhenUpdateTaskGroupAndTask(
            TaskGroupAndTasks taskGroupAndTasks, List<TbClockworkTaskPojo> addTasks, List<TbClockworkTaskPojo> updateTasks) {
        // 存储外部系统和调度系统的任务ID对应信息
        Map<String, TbClockworkTaskPojo> externalTaskAndTssTaskMapping = new HashMap<>();

        for (TbClockworkTaskPojo addTask : addTasks) {
            LOG.info("[addTasksWhenUpdateTaskGroupAndTask][0]task name = {}", addTask.getName());
            addTask.setCreateUser(taskGroupAndTasks.getOperator());
            addTask.setGroupId(taskGroupAndTasks.getTaskGroup().getId());
            addTask.setUpdateTime(new Date());
            addTask.setCreateTime(new Date());
            // 临时加入，用于先添加任务到数据库获取ID
            addTask.setDependencyId("-1");
            addTask(addTask);
            externalTaskAndTssTaskMapping.put(addTask.getExternalSystemTaskId(), addTask);
            LOG.info("[addTasksWhenUpdateTaskGroupAndTask][0]task name = {}", addTask.getName());
        }

        // 只是用来查找新增任务的父任务
        if (CollectionUtils.isNotEmpty(updateTasks)) {
            for (TbClockworkTaskPojo updateTask : updateTasks) {
                externalTaskAndTssTaskMapping.put(updateTask.getExternalSystemTaskId(), updateTask);
            }
        }

        // 修改父任务依赖关系
        for (TbClockworkTaskPojo addTask : addTasks) {
            // 找出对应调度系统内部的任务ID
            List<Integer> tssSystemFatherTaskIds = new ArrayList<>();

            boolean hasCrossGroupFathers = false;

            //本组外依赖的父任务处理：如果有依赖的其它任务组的父任务，则将其它任务组的父任务ID加入当前任务依赖的父任务集合中
            if (StringUtils.isNotBlank(addTask.getTaskFatherIdsCrossTaskGroup())) {
                String[] clockworkFatherIdsCrossTaskGroup = addTask.getTaskFatherIdsCrossTaskGroup().split(",");
                if (clockworkFatherIdsCrossTaskGroup != null && clockworkFatherIdsCrossTaskGroup.length > 0) {
                    // 删除任务依赖关系表的关系，并重新插入关系
                    TbClockworkTaskRelationExample TbClockworkTaskRelationExample = new TbClockworkTaskRelationExample();
                    TbClockworkTaskRelationExample.createCriteria().andTaskIdEqualTo(addTask.getId());
                    tbClockworkTaskRelationMapper.deleteByExample(TbClockworkTaskRelationExample);

                    hasCrossGroupFathers = true;

                    for (String clockworkFatherIdCrossTaskGroup : clockworkFatherIdsCrossTaskGroup) {
                        // 父任务ID转换成INT
                        int clockworkFatherIdCrossTaskGroupInt = Integer.parseInt(clockworkFatherIdCrossTaskGroup);
                        // 添加到依赖的父任务统计队列
                        tssSystemFatherTaskIds.add(clockworkFatherIdCrossTaskGroupInt);

                        TbClockworkTaskRelation TbClockworkTaskRelation = new TbClockworkTaskRelation();
                        TbClockworkTaskRelation.setTaskId(addTask.getId());
                        TbClockworkTaskRelation.setTaskName(addTask.getName());
                        // 设置父任务ID
                        TbClockworkTaskRelation.setFatherTaskId(clockworkFatherIdCrossTaskGroupInt);
                        // 设置父任务名称
                        TbClockworkTaskRelation.setFatherTaskName(
                                tbClockworkTaskMapper.selectByPrimaryKey(clockworkFatherIdCrossTaskGroupInt).getName());
                        tbClockworkTaskRelationMapper.insertSelective(TbClockworkTaskRelation);
                        LOG.info("[addTasksWhenUpdateTaskGroupAndTask]add cross group father task id = {}, " +
                                        "current task id = {}, current task name = {}, " +
                                        "external system task id = {}, task group name = {}",
                                clockworkFatherIdCrossTaskGroup, addTask.getId(), addTask.getName(),
                                addTask.getExternalSystemTaskId(), taskGroupAndTasks.getTaskGroup().getName());
                    }

                    String tssSystemFatherTaskIdsStr = StringUtils.join(tssSystemFatherTaskIds, ",");

                    // 更新调度系统的父任务依赖关系
                    TbClockworkTask TbClockworkTask = new TbClockworkTask();
                    TbClockworkTask.setId(addTask.getId());
                    TbClockworkTask.setDependencyId(tssSystemFatherTaskIdsStr);
                    tbClockworkTaskMapper.updateByPrimaryKeySelective(TbClockworkTask);

                    LOG.info("[addTasksWhenUpdateTaskGroupAndTaskCrossTaskGroupFathers] begin add dependency id, " +
                                    "current task id = {}, external system task id = {}, tss system father task Ids = {}",
                            addTask.getId(), addTask.getExternalSystemTaskId(), tssSystemFatherTaskIdsStr);
                }
            } else {
                LOG.info("[addTasksWhenUpdateTaskGroupAndTask]no found cross group father id info, " +
                                "current task id = {}, current task name = {}, " +
                                "external system task id = {}, task group name = {}",
                        addTask.getId(), addTask.getName(),
                        addTask.getExternalSystemTaskId(), taskGroupAndTasks.getTaskGroup().getName());
            }

            // 外部系统传递的任务依赖关系，多个父任务依赖逗号分隔
            String externalSystemTaskDependencyId = addTask.getExternalSystemTaskDependencyId();
            if (StringUtils.isBlank(externalSystemTaskDependencyId)) {
                LOG.info("[addTasksWhenUpdateTaskGroupAndTask][1]" +
                                "ExternalSystemTaskDependencyId field is null,task id = {},external system task id = {}",
                        addTask.getId(), addTask.getExternalSystemTaskId());

                if (StringUtils.isBlank(addTask.getTaskFatherIdsCrossTaskGroup())) {
                    // 没有父任务依赖，必须有cron exp，否则任务运行不起来，也就是触发模式必须为时间触发
                    if (StringUtils.isBlank(addTask.getCronExp())) {
                        throw new RuntimeException("[addTasksWhenUpdateTaskGroupAndTask]" +
                                "Cron exp field id null, task id = " + addTask.getId());
                    }

                    // 更新触发模式为时间触发
                    TbClockworkTask TbClockworkTask = new TbClockworkTask();
                    TbClockworkTask.setId(addTask.getId());
                    TbClockworkTask.setTriggerMode(TaskTriggerModel.TIME.getValue());
                    tbClockworkTaskMapper.updateByPrimaryKeySelective(TbClockworkTask);
                } else {
                    LOG.info("[addTasksWhenUpdateTaskGroupAndTask][1]don't need check cron exp,because of has cross "
                                    + "group father. task id = {}, TaskFatherIdsCrossTaskGroup = {}",
                            addTask.getId(), addTask.getTaskFatherIdsCrossTaskGroup());
                }
                continue;
            }

            // 获取当前外部系统指定的任务依赖关系
            String[] externalSystemFatherIds = externalSystemTaskDependencyId.split(",");
            if (externalSystemFatherIds == null || externalSystemFatherIds.length < 1) {
                LOG.info("[addTasksWhenUpdateTaskGroupAndTask][2]" +
                                "ExternalSystemTaskDependencyId field is null,task id = {},external system task id = {}",
                        addTask.getId(), addTask.getExternalSystemTaskId());
                continue;
            }

            for (String externalSystemFatherId : externalSystemFatherIds) {
                TbClockworkTaskPojo externalSystemFatherTask = externalTaskAndTssTaskMapping.get(externalSystemFatherId);
                if (externalSystemFatherTask == null) {
                    throw new RuntimeException("[addTasksWhenUpdateTaskGroupAndTask]" +
                            "No found task id by externalSystemFatherId that value is " + externalSystemFatherId);
                }
                tssSystemFatherTaskIds.add(externalSystemFatherTask.getId());
            }

            // 如果没有跨组依赖的父任务，此处删除任务依赖关系表的关系，并重新插入关系
            if (!hasCrossGroupFathers) {
                TbClockworkTaskRelationExample TbClockworkTaskRelationExample = new TbClockworkTaskRelationExample();
                TbClockworkTaskRelationExample.createCriteria().andTaskIdEqualTo(addTask.getId());
                tbClockworkTaskRelationMapper.deleteByExample(TbClockworkTaskRelationExample);
            }

            for (String externalSystemFatherId : externalSystemFatherIds) {
                TbClockworkTaskRelation TbClockworkTaskRelation = new TbClockworkTaskRelation();
                TbClockworkTaskRelation.setTaskId(addTask.getId());
                TbClockworkTaskRelation.setTaskName(addTask.getName());
                TbClockworkTaskRelation.setFatherTaskId(externalTaskAndTssTaskMapping.get(externalSystemFatherId).getId());
                TbClockworkTaskRelation.setFatherTaskName(externalTaskAndTssTaskMapping.get(externalSystemFatherId).getName());
                tbClockworkTaskRelationMapper.insertSelective(TbClockworkTaskRelation);
            }

            String tssSystemTaskIdsStr = StringUtils.join(tssSystemFatherTaskIds, ",");

            LOG.info("[addTasksWhenUpdateTaskGroupAndTask]begin add dependency id, task id = {}," +
                            "external system task id = {}, tss system father task Ids = {}," +
                            "external system father task Ids = {}", addTask.getId(),
                    addTask.getExternalSystemTaskId(), tssSystemTaskIdsStr, externalSystemTaskDependencyId);
            // 更新调度系统的父任务依赖关系
            TbClockworkTask TbClockworkTask = new TbClockworkTask();
            TbClockworkTask.setId(addTask.getId());
            TbClockworkTask.setDependencyId(tssSystemTaskIdsStr);
            tbClockworkTaskMapper.updateByPrimaryKeySelective(TbClockworkTask);
        }
    }

    @Override
    public boolean updateTaskChildrensDelayStatusAndRefreshSlots(Integer taskId,Integer taskRerunType) {
        List<TbClockworkTaskPojo> childrenNotIncludeSelf = new ArrayList<TbClockworkTaskPojo>();
        if (taskRerunType == TaskReRunType.ALL_CHILDREN_NOT_SELF.getCode()){
          childrenNotIncludeSelf = taskRelationService.getTaskAllChildrenNotIncludeSelf(taskId);
        }
        if (taskRerunType == TaskReRunType.ALL_CHILDREN_AND_SELF.getCode()){
          childrenNotIncludeSelf = taskRelationService.getTaskAllChildrenIncludeSelf(taskId);
        }
        if (CollectionUtils.isEmpty(childrenNotIncludeSelf)) {
            LOG.info("[updateTaskChildrensDelayStatusAndRefreshSlots] task id = {}, no find childrens. ", taskId);
            return true;
        }
        for (TbClockworkTaskPojo taskPojo : childrenNotIncludeSelf) {
//            更新任务的 延迟策略 和 刷新时钟
            taskPojo.setDelayStatus(TaskDelayStatus.FATHER_DELAYED_RECOVERY.getCode());
            tbClockworkTaskMapper.updateByPrimaryKeySelective(taskPojo);
            if (StringUtils.isNotBlank(taskPojo.getCronExp())) {
                loopClockService.addTaskToLoopClockSlot(taskPojo, 1);
                LOG.info("[updateTaskChildrensDelayStatusAndRefreshSlots] task id = {}, task cron exp = {} ",
                  taskPojo.getId(), taskPojo.getCronExp());
            }
        }
        return true;
    }
}
