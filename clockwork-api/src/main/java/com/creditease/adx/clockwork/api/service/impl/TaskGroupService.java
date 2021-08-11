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
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroupExample;
import com.creditease.adx.clockwork.common.enums.*;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskGroupMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:29 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskGroupService")
public class TaskGroupService implements ITaskGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskGroupService.class);

    @Autowired
    private TbClockworkTaskGroupMapper tbClockworkTaskGroupMapper;

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Autowired
    private ITaskStopService taskStopService;

    @Autowired
    private ITaskRelationService taskRelationService;

    @Autowired
    private ITaskOperationService taskOperationService;

    @Autowired
    private IDagService dagService;

    @Resource(name = "redisService")
    private IRedisService redisService;

    /**
     * 添加任务组
     *
     * @param taskGroup taskGroup
     * @return
     */
    @Override
    public int addTaskGroup(TbClockworkTaskGroup taskGroup) {
        // 任务组对象不能为空
        if (taskGroup == null) {
            throw new RuntimeException("[addTaskGroup]task group info is null, please check it.");
        }

        // 任务组名称不能为空
        if (StringUtils.isEmpty(taskGroup.getName())) {
            throw new RuntimeException("[addTaskGroup]task group name is null");
        }

        // 任务组长度不能为零
        if (StringUtils.isBlank(taskGroup.getName())) {
            throw new RuntimeException("[addTaskGroup]task group name length illegal");
        }

        // 检查任务组名称是否存在，存在则不能添加
        TbClockworkTaskGroupExample TbClockworkTaskGroupExample = new TbClockworkTaskGroupExample();
        TbClockworkTaskGroupExample.createCriteria().andNameEqualTo(taskGroup.getName());
        long counter = tbClockworkTaskGroupMapper.countByExample(TbClockworkTaskGroupExample);
        if (counter > 0) {
            throw new RuntimeException("[addTaskGroup]task group name already exist, name = ["
                    + taskGroup.getName() + "], can't be added, please check it.");
        }
        taskGroup.setUpdateTime(new Date());
        taskGroup.setCreateTime(new Date());
        taskGroup.setTakeEffectStatus(TaskGroupTakeEffectStatus.ENABLE.getValue());
        return tbClockworkTaskGroupMapper.insertSelective(taskGroup);
    }

    /**
     * 更新任务组（修改任务基本信息）
     *
     * @param taskGroup taskGroup
     * @return count
     */
    @Override
    public int updateTaskGroup(TbClockworkTaskGroup taskGroup) {
        if (taskGroup == null) {
            throw new RuntimeException("[updateTaskGroupWhenTaskEnd]task group info is null, please check it.");
        }
        // 任务ID不能为空
        if (taskGroup.getId() == null || taskGroup.getId() < 1) {
            throw new RuntimeException("[updateTaskGroupWhenTaskEnd]task group id is null or illegal, id = "
                    + (taskGroup.getId() == null ? "null" : "" + taskGroup.getId()));
        }

        // 任务名称不能为空
        if (StringUtils.isEmpty(taskGroup.getName())) {
            throw new RuntimeException("[updateTaskGroupWhenTaskEnd]task group name is null");
        }

        // 任务组长度不能为零
        if (StringUtils.isBlank(taskGroup.getName())) {
            throw new RuntimeException("[updateTaskGroupWhenTaskEnd]task group name length illegal");
        }

        // 检查修改的任务组，除了自己是否名称已经存在，存在则不能添加
        TbClockworkTaskGroupExample TbClockworkTaskGroupExample = new TbClockworkTaskGroupExample();
        TbClockworkTaskGroupExample.createCriteria().andNameEqualTo(taskGroup.getName())
                .andIdNotEqualTo(taskGroup.getId());

        long counter = tbClockworkTaskGroupMapper.countByExample(TbClockworkTaskGroupExample);
        if (counter > 0) {
            throw new RuntimeException("[updateTaskGroupWhenTaskEnd]task group name already exist, name = "
                    + taskGroup.getName()
                    + "，current task group id = " + taskGroup.getId() + ", can't be added,please check it.");
        }
        taskGroup.setUpdateTime(new Date());
        taskGroup.setCreateTime(new Date());
        // 不能修改任务组的状态
        taskGroup.setStatus(null);

        return tbClockworkTaskGroupMapper.updateByPrimaryKeySelective(taskGroup);
    }


    /**
     * 启用任务组（批量修改任务相关信息）
     * 1。修改流（分组）的状态为ENABLE
     * 2。获取该任务组下面的所有任务
     * 3。修改这个分组下面所有的作业为上线、设置节点关系为online、重新构建dagId、批量更新子任务的TaskDependencyId字段
     *
     * @param taskGroupId task group id
     * @return count
     */
    @Override
    public int enableTaskGroup(int taskGroupId) {
        LOG.info("TaskGroupService-enableTaskGroup, task GroupId Id = {}", taskGroupId);
        // 1。修改流（分组）的状态为ENABLE
        TbClockworkTaskGroup updateTbClockworkTaskGroup = new TbClockworkTaskGroup();
        updateTbClockworkTaskGroup.setId(taskGroupId);
        updateTbClockworkTaskGroup.setTakeEffectStatus(TaskGroupTakeEffectStatus.ENABLE.getValue());
        int count = tbClockworkTaskGroupMapper.updateByPrimaryKeySelective(updateTbClockworkTaskGroup);
        LOG.info("disableTaskGroup-enableTaskGroup, task Group Id = {}, status = {},",
                taskGroupId, TaskGroupTakeEffectStatus.ENABLE.getValue());

        // 2。获取该任务组下面的所有任务
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andGroupIdEqualTo(taskGroupId);
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);

        // 3。修改这个分组下面所有的作业为上线、设置节点关系为online、重新构建dagId、批量更新子任务的TaskDependencyId字段
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            List<Integer> taskIds = tbClockworkTasks.stream().map(TbClockworkTask::getId).collect(Collectors.toList());

            // 修改这个分组下面所有的作业为上线
            TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
            updateTbClockworkTask.setDagId(-1);
            updateTbClockworkTask.setOnline(TaskTakeEffectStatus.ONLINE.getValue());
            tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
            LOG.info("enableTaskGroup-updateByExampleSelective taskGroupId = {}, taskId.size = {}, taskId = {}, dagId = -1, online = {}.",
                    taskGroupId, taskIds.size(), taskIds, TaskTakeEffectStatus.ONLINE.getValue());

            // 关系设置为online
            taskRelationService.updateTasksRelationIsEffective(taskIds, TaskRelationTakeEffectStatus.ONLINE.getValue());
            LOG.info("enableTaskGroup-updateTasksRelationIsEffective taskGroupId = {}, isEffective = {}.",
                    taskGroupId, TaskRelationTakeEffectStatus.ONLINE.getValue());

            try {
                // 重新构建DagId, 因为重新上线的dagId为-1，只需要调用初始化方法即可构建online状态并且dag=-1的任务
                dagService.initTaskDagId();
                LOG.info("enableTaskGroup-initTaskDagId taskGroupId = {}, taskId = {}", taskGroupId, taskIds);

            } catch (Exception e) {
                LOG.error("TaskGroupService-enableTaskGroup taskGroupId = {}, Error {}.", taskGroupId, e.getMessage(), e);
            }

            // 批量更新子任务的TaskDependencyId字段
            taskOperationService.updateChildrenTaskDependencyIfFatherTaskIdsChange(taskIds);
        }
        LOG.info("TaskGroupService-enableTaskGroup, taskGroupId = {}, count = {}", taskGroupId, count);
        return count;
    }

    /**
     * 禁用任务组（批量修改任务相关信息）
     * 0。检查流里面是否还有运行的任务，如果有则不能disable
     * 1。修改流（分组）的状态为DISABLE
     * 2。获取该任务组下面的所有任务
     * 3。从队列中移除、修改任务组下面的作业的状态为下线、节点关系设置为offline、重新构建相关dag、批量更新子任务的TaskDependencyId字段
     *
     * @param taskGroupId task group id
     * @return count
     */
    @Override
    public int disableTaskGroupTx(int taskGroupId) {
        LOG.info("TaskGroupService-disableTaskGroup, task Group Id = {}", taskGroupId);

        // 0。检查流里面是否还有运行的任务，如果有则不能disable
        checkRunningTaskInProcess(taskGroupId);

        // 1。修改流（分组）的状态为DISABLE
        TbClockworkTaskGroup updateTbClockworkTaskGroup = new TbClockworkTaskGroup();
        updateTbClockworkTaskGroup.setId(taskGroupId);
        updateTbClockworkTaskGroup.setTakeEffectStatus(TaskGroupTakeEffectStatus.DISABLE.getValue());
        int count = tbClockworkTaskGroupMapper.updateByPrimaryKeySelective(updateTbClockworkTaskGroup);
        LOG.info("disableTaskGroup-updateTbClockworkTaskGroup, task Group Id = {}, status = {},",
                taskGroupId, TaskGroupTakeEffectStatus.DISABLE.getValue());

        // 2。获取该任务组下面的所有任务
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andGroupIdEqualTo(taskGroupId);
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tbClockworkTasks)) {
            LOG.info("TaskGroupService-disableTaskGroup success, taskGroupId = {}, count = {}, tasks.size = 0", taskGroupId, count);
            return count;
        }

        // 3。从队列中移除、修改任务组下面的作业的状态为下线、节点关系设置为offline、重新构建相关dag、批量更新子任务的TaskDependencyId字段
        // 获取到所有DagIds、获取到该组所有的taskIds
        HashSet<Integer> dagIdsSet = new HashSet<>();
        for (TbClockworkTask tbClockworkTask : tbClockworkTasks) dagIdsSet.add(tbClockworkTask.getDagId());
        List<Integer> dagIds = new ArrayList<>(dagIdsSet);
        List<Integer> taskIds = tbClockworkTasks.stream().map(TbClockworkTask::getId).collect(Collectors.toList());

        // 如果队列中存在，需要移除
        taskStopService.stopTaskListAndRemoveFromQueue(taskIds);
        LOG.info("disableTaskGroup-stopTaskListAndRemoveFromQueue, taskGroupId = {}, taskIds = {}", taskGroupId, taskIds);
        boolean getLock = false;
        try {
            // 分布式锁环境下安全进行
            getLock = redisService.tryLockForSubmitTask(1, TimeUnit.SECONDS);
            if (getLock) {
                // 修改任务组下面的作业的状态为下线
                TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
                updateTbClockworkTask.setOnline(TaskTakeEffectStatus.OFFLINE.getValue());
                updateTbClockworkTask.setDagId(-1);
                tbClockworkTaskMapper.updateByExampleSelective(updateTbClockworkTask, example);
                LOG.info("disableTaskGroup-updateTbClockworkTask, taskGroupId = {}, taskIds.size = {}, taskIds = {}, dagId = -1, online = {}",
                        taskGroupId, taskIds.size(), taskIds, TaskTakeEffectStatus.OFFLINE.getValue());

                // 节点关系设置为offline
                taskRelationService.updateTasksRelationIsEffective(taskIds, TaskRelationTakeEffectStatus.OFFLINE.getValue());
                LOG.info("disableTaskGroup-updateTasksRelationIsEffective, taskGroupId = {}, taskIds.size = {}, isEffective = {}",
                        taskGroupId, taskIds.size(), TaskRelationTakeEffectStatus.OFFLINE.getValue());
            } else {
                throw new RuntimeException("Same transaction for update task is running,please try again later.");
            }
        } finally {
            if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
        }

        // 重新构建相关dag
        dagService.buildDagIdForDagIds(dagIds);
        LOG.info("disableTaskGroup-buildDagIdForDagIds, taskGroupId = {}, dagIds = {}", taskGroupId, dagIds);

        // 批量更新子任务的TaskDependencyId字段
        taskOperationService.updateChildrenTaskDependencyIfFatherTaskIdsChange(taskIds);
        LOG.info("TaskGroupService-disableTaskGroup success, taskGroupId = {}, count = {}", taskGroupId, count);
        return count;
    }

    /**
     * 任务组是否存在
     *
     * @param taskGroupName name
     * @return
     */
    @Override
    public boolean taskGroupIsExists(String taskGroupName) {
        TbClockworkTaskGroupExample example = new TbClockworkTaskGroupExample();
        example.createCriteria().andNameEqualTo(taskGroupName);
        long count = tbClockworkTaskGroupMapper.countByExample(example);
        return count > 0;
    }

    private void checkRunningTaskInProcess(Integer taskGroupId) {
        // 检查流里面是否还有运行的任务，如果有则不能disable
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria()
                .andGroupIdEqualTo(taskGroupId)
                .andOnlineEqualTo(TaskTakeEffectStatus.ONLINE.getValue())
                .andStatusEqualTo(TaskStatus.RUNNING.getValue());
        List<TbClockworkTask> runningTbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(runningTbClockworkTasks)) {
            List<String> runningTbClockworkTasksId = new ArrayList<>();
            for (TbClockworkTask runningTbClockworkTask : runningTbClockworkTasks) {
                runningTbClockworkTasksId.add(String.valueOf(runningTbClockworkTask.getId()));
            }
            String runningTasksId = StringUtils.join(runningTbClockworkTasksId, ",");
            throw new RuntimeException("Can't disable stream,because have task that status is running in the stream, " +
                    "tasks id = " + runningTasksId + ", task group id = " + taskGroupId);
        }
    }

    @Override
    public TbClockworkTaskGroup getTaskGroupById(int id) {
        return tbClockworkTaskGroupMapper.selectByPrimaryKey(id);
    }
}
