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

import com.creditease.adx.clockwork.api.service.ITaskOperationService;
import com.creditease.adx.clockwork.api.service.ITaskRelationService;
import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelationExample;
import com.creditease.adx.clockwork.common.enums.TaskReRunType;
import com.creditease.adx.clockwork.common.enums.TaskRelationTakeEffectStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRelationMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "taskRelationService")
public class TaskRelationService implements ITaskRelationService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRelationService.class);

    @Autowired
    private TbClockworkTaskRelationMapper tbClockworkTaskRelationMapper;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private ITaskOperationService taskOperationService;

    @Override
    public TbClockworkTaskRelationMapper getMapper() {
        return this.tbClockworkTaskRelationMapper;
    }

    @Override
    public int addTaskRelation(TbClockworkTaskRelationPojo taskRelationPojo) {
        taskRelationPojo.setCreateTime(new Date());
        TbClockworkTaskRelation taskRelation = PojoUtil.convert(taskRelationPojo, TbClockworkTaskRelation.class);
        int count = tbClockworkTaskRelationMapper.insertSelective(taskRelation);
        LOG.info("TaskRelationService-addTaskRelation, taskRelation = {}, count = {}", taskRelationPojo, count);
        return count;
    }

    /**
     * 删除依赖关系
     *
     * @param taskId taskId
     * @return count
     */
    @Override
    public int deleteDependencies(int taskId) {
        int count = 0;
        // 删除该任务的父任务，依赖关系
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        count = tbClockworkTaskRelationMapper.deleteByExample(example);
        LOG.info("deleteDependencies-delete task father relation, taskId = {}, father.count = {}.", taskId, count);

        // 删除该任务的子任务，依赖关系，需要修先改该任务子任务的dependenceId
        example = new TbClockworkTaskRelationExample();
        example.createCriteria().andFatherTaskIdEqualTo(taskId);

        // 让关系离线
        int offline = updateTaskRelationIsEffective(taskId, TaskRelationTakeEffectStatus.OFFLINE.getValue());
        if (offline > 0) {

            // 先更新子任务的DependenceId字段
            taskOperationService.updateChildrenTaskDependencyIfFatherTaskIdChange(taskId);
            LOG.info("deleteDependenciesByTaskIds-updateChildrenTaskDependencyIfFatherTaskIdChange, fatherTaskId = {}.", taskId);

            // 然后在删除
            count += tbClockworkTaskRelationMapper.deleteByExample(example);
            LOG.info("deleteDependencies-delete task children relation, taskId = {}.", taskId);
        } else {
            LOG.info("deleteDependenciesByTaskIds not need delete children relation, because taskId = {}, children.count = 0.", taskId);
        }
        LOG.info("TaskRelationService-deleteDependencies, taskId = {}, count = {}.", taskId, count);
        return count;
    }


    /**
     * 批量删除依赖关系
     *
     * @param taskIds taskIds
     * @return count
     */
    @Override
    public int deleteDependenciesByTaskIds(List<Integer> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return -1;
        }
        // 删除该任务的父任务，依赖关系
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria().andTaskIdIn(taskIds);
        int count = tbClockworkTaskRelationMapper.deleteByExample(example);
        LOG.info("deleteDependenciesByTaskIds-delete task father relation, taskIds = {}, father.count = {}.", taskIds, count);

        // 删除该任务的子任务，依赖关系，需要修先改该任务子任务的dependenceId
        example = new TbClockworkTaskRelationExample();
        example.createCriteria().andFatherTaskIdIn(taskIds);

        // 让关系离线
        int offline = updateTasksRelationIsEffective(taskIds, TaskRelationTakeEffectStatus.OFFLINE.getValue());
        if (offline > 0) {
            taskOperationService.updateChildrenTaskDependencyIfFatherTaskIdsChange(taskIds);
            LOG.info("deleteDependenciesByTaskIds-updateChildrenTaskDependencyIfFatherTaskIdsChange, fatherTaskIds = {}.", taskIds);

            // 然后再删除
            LOG.info("deleteDependenciesByTaskIds-delete task children relation, taskId = {}.", taskIds);
            count += tbClockworkTaskRelationMapper.deleteByExample(example);
        } else {
            LOG.info("deleteDependenciesByTaskIds not need delete children relation, because taskIds = {}, children.count = 0.", taskIds);
        }

        LOG.info("TaskRelationService-deleteDependenciesByTaskIds, taskIds = {}, count={}", taskIds, count);
        return count;
    }

    /**
     * 更新任务有效状态
     *
     * @param taskId      需要更新的任务Id
     * @param isEffective 更新为是否有效
     * @return
     */
    @Override
    public int updateTaskRelationIsEffective(int taskId, boolean isEffective) {
        try {
            // record 需要更新的记录
            TbClockworkTaskRelation record = new TbClockworkTaskRelation();
            record.setIsEffective(isEffective);

            // example 查询条件
            TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
            example.createCriteria().andTaskIdEqualTo(taskId);

            // 更新依赖关系有效状态
            int count = tbClockworkTaskRelationMapper.updateByExampleSelective(record, example);

            example = new TbClockworkTaskRelationExample();
            example.createCriteria().andFatherTaskIdEqualTo(taskId);
            count += tbClockworkTaskRelationMapper.updateByExampleSelective(record, example);
            LOG.info("TaskRelationService-updateTaskRelationIsEffective,taskId = {}, isEffective = {}, count = {}",
                    taskId, isEffective, count);
            return count;
        } catch (Exception e) {
            LOG.error("TaskRelationService-updateTaskRelationIsEffective, taskId = {}, isEffective = {} Error {}.",
                    taskId, isEffective, e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 批量更新任务有效状态
     *
     * @param taskIds     需要更新的任务Ids
     * @param isEffective 更新为是否有效
     * @return
     */
    @Override
    public int updateTasksRelationIsEffective(List<Integer> taskIds, boolean isEffective) {
        try {
            if (CollectionUtils.isEmpty(taskIds)) {
                return -1;
            }
            // record 需要更新的记录
            TbClockworkTaskRelation record = new TbClockworkTaskRelation();
            record.setIsEffective(isEffective);

            // example 查询条件
            TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
            example.createCriteria().andTaskIdIn(taskIds);

            // 更新依赖关系有效状态
            int count = tbClockworkTaskRelationMapper.updateByExampleSelective(record, example);

            example = new TbClockworkTaskRelationExample();
            example.createCriteria().andFatherTaskIdIn(taskIds);
            count += tbClockworkTaskRelationMapper.updateByExampleSelective(record, example);
            LOG.info("TaskRelationService-updateTasksRelationIsEffective, taskIds = {}, isEffective = {}, count = {}",
                    taskIds, isEffective, count);
            return count;
        } catch (Exception e) {
            LOG.error("TaskRelationService-updateTasksRelationIsEffective, taskIds = {}, isEffective = {} Error {}.",
                    taskIds, isEffective, e.getMessage(), e);
        }
        return -1;
    }

    /**
     * 获取任意一个关联节点
     *
     * @param taskId taskId
     * @return
     */
    @Override
    public TbClockworkTaskRelationPojo findTaskOneRelation(Integer taskId) {
        // 先根据taskId找到一个关系节点
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria().andTaskIdEqualTo(taskId).andIsEffectiveEqualTo(true);
        example.setLimitStart(0);
        example.setLimitEnd(1);
        List<TbClockworkTaskRelation> rNode = tbClockworkTaskRelationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(rNode)) {
            example = new TbClockworkTaskRelationExample();
            example.createCriteria().andFatherTaskIdEqualTo(taskId).andIsEffectiveEqualTo(true);
            example.setLimitStart(0);
            example.setLimitEnd(1);
            rNode = tbClockworkTaskRelationMapper.selectByExample(example);
        }

        if (CollectionUtils.isEmpty(rNode)) {
            return null;
        }
        return PojoUtil.convert(rNode.get(0), TbClockworkTaskRelationPojo.class);
    }


    @Override
    public List<TbClockworkTaskRelationPojo> findTaskDirectlyChildren(int taskId) {
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria()
                .andFatherTaskIdEqualTo(taskId).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> children = tbClockworkTaskRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(children)) {
            return PojoUtil.convertList(children, TbClockworkTaskRelationPojo.class);
        }
        return null;
    }

    @Override
    public List<TbClockworkTaskRelationPojo> findTaskAllChildrenNotIncludeSelf(Integer taskId) {
        HashMap<Integer, TbClockworkTaskRelationPojo> result = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        if (!queue.offer(taskId)) {
            throw new RuntimeException("[findTaskAllChildrenNotIncludeSelf]add task id to queue failure!");
        }
        long startTime = System.currentTimeMillis();
        Integer queueTaskId;
        // 队列方式获得当前任务的所有子孙后代
        while ((queueTaskId = queue.poll()) != null) {
            List<TbClockworkTaskRelationPojo> childTasks = findTaskDirectlyChildren(queueTaskId);
            if (CollectionUtils.isEmpty(childTasks)) {
                continue;
            }
            for (TbClockworkTaskRelationPojo childTask : childTasks) {
                if (result.containsKey(childTask.getTaskId())) {
                    continue;
                }
                result.put(childTask.getTaskId(), childTask);
                if (!queue.offer(childTask.getTaskId())) {
                    throw new RuntimeException("[findTaskAllChildrenNotIncludeSelf]add children task id to queue failure!");
                }

            }
        }
        // 转换，结果已经去重
        if (result.size() != 0) {
            ArrayList<TbClockworkTaskRelationPojo> collect = new ArrayList<>(result.values());
            LOG.info("[findTaskAllChildrenNotIncludeSelf]get task descendants size = {}, task id = {}, cost time = {} ms",
                    collect.size(), taskId, System.currentTimeMillis() - startTime);
            return collect;
        }
        return null;
    }

    @Override
    public List<TbClockworkTaskRelationPojo> findTaskDirectlyFather(int taskId) {
        LOG.info("[TaskRelationService-findTaskDirectlyFather] taskId = {}", taskId);
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria()
                .andTaskIdEqualTo(taskId).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> relations = tbClockworkTaskRelationMapper.selectByExample(example);

        // 没有父节点
        if (relations.isEmpty()) {
            LOG.info("[TaskRelationService-findTaskDirectlyFather] No fatherTaskIds, fatherTaskIds.size = 0, "
                    + "taskId = {}", taskId);
            return null;
        }

        List<TbClockworkTaskRelationPojo> result = new ArrayList<>();
        TbClockworkTaskRelationPojo pojo = null;
        for (TbClockworkTaskRelation relation : relations) {
            pojo = new TbClockworkTaskRelationPojo();
            pojo.setTaskId(relation.getFatherTaskId());
            pojo.setTaskName(relation.getFatherTaskName());
            pojo.setFatherTaskId(-1);
            result.add(pojo);
        }

        LOG.warn("[TaskRelationService-findTaskDirectlyFather] task relations info, relations.size = {}, "
                + "father.size = {}, taskId = {}", relations.size(), relations.size(), taskId);
        return result;
    }

    /**
     * 找到直接关联节点，包括自己
     *
     * @param taskId
     * @return
     */
    @Override
    public List<Integer> findDirectlyRelationTaskIdsIncludeSelf(int taskId) {
        List<Integer> result = findDirectlyRelationTaskIdsNotIncludeSelf(taskId);
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(taskId);
        LOG.info("[TaskRelationService-findDirectlyRelationTaskIdsIncludeSelf] Find directly related to node, "
                + "taskId = {}, related node = {}", taskId, result);
        return result;
    }


    /**
     * 找到直接关联节点，不包括自己
     *
     * @param taskId
     * @return
     */
    @Override
    public List<Integer> findDirectlyRelationTaskIdsNotIncludeSelf(int taskId) {
        LOG.info("[TaskRelationService-findDirectlyRelationTaskIdsNotIncludeSelf] taskId = {}", taskId);
        List<Integer> result = new ArrayList<>();

        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria()
                .andTaskIdEqualTo(taskId).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> fathers = tbClockworkTaskRelationMapper.selectByExample(example);

        // 没有父节点
        if (CollectionUtils.isEmpty(fathers)) {
            LOG.info("[TaskRelationService-findFather] No fatherTaskIds，fatherTaskIds.size = 0, taskId = {}", taskId);
        } else {
            for (TbClockworkTaskRelation father : fathers) {
                result.add(father.getFatherTaskId());
            }
        }

        List<TbClockworkTaskRelationPojo> childrenList = findTaskDirectlyChildren(taskId);
        // 没有子节点
        if (CollectionUtils.isEmpty(childrenList)) {
            LOG.info("[TaskRelationService-findChildren] No childrenTaskIds，childrenTaskIds.size = 0, taskId = {}", taskId);
        } else {
            for (TbClockworkTaskRelationPojo children : childrenList) {
                result.add(children.getTaskId());
            }
        }
        LOG.info("[TaskRelationService-findDirectlyRelationTaskIdsIncludeSelf] Find directly related to node, "
                + "taskId = {}, related node = {}", taskId, result);
        return result;
    }

    /**
     * 获得当前任务的所有直接孩子，不包含自己
     *
     * @param taskId task id
     * @return 直接关联的子任务
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskDirectlyChildrenNotIncludeSelf(int taskId) {
        List<TbClockworkTaskRelationPojo> taskRelationPojoList = findTaskDirectlyChildren(taskId);
        if (CollectionUtils.isEmpty(taskRelationPojoList)) {
            return null;
        }

        List<Integer> childTaskIds = new ArrayList<>();
        for (TbClockworkTaskRelationPojo TbClockworkTaskRelation : taskRelationPojoList) {
            childTaskIds.add(TbClockworkTaskRelation.getTaskId());
        }

        List<TbClockworkTask> TbClockworkTasks = taskService.getTbClockworkTasksByIds(childTaskIds);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }
        // 设置每个task的需要替换文件的信息
        return PojoUtil.convertList(TbClockworkTasks, TbClockworkTaskPojo.class);
    }

    /**
     * 获得当前任务的所有直接孩子，包含自己
     *
     * @param taskId task id
     * @return 直接关联的子任务
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskDirectlyChildrenIncludeSelf(int taskId) {
        List<TbClockworkTaskRelationPojo> taskRelationPojoList = findTaskDirectlyChildren(taskId);
        if (CollectionUtils.isEmpty(taskRelationPojoList)) {
            TbClockworkTaskPojo task = taskService.getTaskById(taskId);
            return task != null ? Collections.singletonList(task) : null;
        }

        List<Integer> childTaskIds = new ArrayList<>();
        for (TbClockworkTaskRelationPojo TbClockworkTaskRelation : taskRelationPojoList) {
            childTaskIds.add(TbClockworkTaskRelation.getTaskId());
        }
        childTaskIds.add(taskId);

        List<TbClockworkTask> TbClockworkTasks = taskService.getTbClockworkTasksByIds(childTaskIds);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }
        // 设置每个task的需要替换文件的信息
        return PojoUtil.convertList(TbClockworkTasks, TbClockworkTaskPojo.class);
    }


    @Override
    public List<TbClockworkTaskPojo> getTaskDirectlyFatherNotIncludeSelf(int taskId) {
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria()
                .andTaskIdEqualTo(taskId).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> TbClockworkTaskDependencies = tbClockworkTaskRelationMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(TbClockworkTaskDependencies)) {
            return null;
        }

        List<Integer> fatherTaskIds = new ArrayList<>();
        for (TbClockworkTaskRelation TbClockworkTaskRelation : TbClockworkTaskDependencies) {
            fatherTaskIds.add(TbClockworkTaskRelation.getFatherTaskId());
        }

        List<TbClockworkTask> TbClockworkTasks = taskService.getTbClockworkTasksByIds(fatherTaskIds);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }

        return PojoUtil.convertList(TbClockworkTasks, TbClockworkTaskPojo.class);
    }

    @Override
    public List<TbClockworkTask> getTaskDirectlyRelationTaskNotIncludeSelf(int taskId) {
        List<Integer> directlyRelationTaskIdsNotIncludeSelf = findDirectlyRelationTaskIdsNotIncludeSelf(taskId);
        if (CollectionUtils.isEmpty(directlyRelationTaskIdsNotIncludeSelf)) {
            return null;
        }
        List<TbClockworkTask> TbClockworkTasks = taskService.getTbClockworkTasksByIds(directlyRelationTaskIdsNotIncludeSelf);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }
        return TbClockworkTasks;
    }

    /**
     * 获得当前作业的所有后代，包含自己本身
     *
     * @param taskId
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<TbClockworkTaskPojo> getTaskAllChildrenIncludeSelf(Integer taskId) {
        HashMap<Integer, TbClockworkTaskPojo> result = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        if (!queue.offer(taskId)) {
            throw new RuntimeException("[getTaskAllChildrenIncludeSelf]add task id to queue failure!");
        }
        long startTime = System.currentTimeMillis();
        Integer queueTaskId;
        // 返回值中第一个元素是自己
        result.put(taskId, taskService.getTaskById(taskId));
        // 队列方式获得当前任务的所有子孙后代
        while ((queueTaskId = queue.poll()) != null) {
            List<TbClockworkTaskPojo> childTasks = getTaskDirectlyChildrenNotIncludeSelf(queueTaskId);
            if (CollectionUtils.isEmpty(childTasks)) {
                continue;
            }
            for (TbClockworkTaskPojo childTask : childTasks) {
                if (result.containsKey(childTask.getId())) {
                    continue;
                }
                result.put(childTask.getId(), childTask);
                if (!queue.offer(childTask.getId())) {
                    throw new RuntimeException("[getTaskAllChildrenIncludeSelf]add children task id to queue failure!");
                }
            }
        }
        // 转换，结果已经去重
        ArrayList<TbClockworkTaskPojo> collect = new ArrayList<>(result.values());
        LOG.info("[getTaskAllChildrenIncludeSelf]get task descendants size = {}, task id = {}, cost time = {} ms",
                collect.size(), taskId, System.currentTimeMillis() - startTime);
        return collect;
    }

    /**
     * 获得当前作业的所有后代，不包含自己本身
     *
     * @param taskId
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<TbClockworkTaskPojo> getTaskAllChildrenNotIncludeSelf(Integer taskId) {
        HashMap<Integer, TbClockworkTaskPojo> result = new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();
        if (!queue.offer(taskId)) {
            throw new RuntimeException("[getTaskAllChildrenNotIncludeSelf]add task id to queue failure!");
        }
        long startTime = System.currentTimeMillis();
        Integer queueTaskId;
        // 队列方式获得当前任务的所有子孙后代
        while ((queueTaskId = queue.poll()) != null) {
            List<TbClockworkTaskPojo> childTasks = getTaskDirectlyChildrenNotIncludeSelf(queueTaskId);
            if (CollectionUtils.isEmpty(childTasks)) {
                continue;
            }
            for (TbClockworkTaskPojo childTask : childTasks) {
                if (result.containsKey(childTask.getId())) {
                    continue;
                }
                result.put(childTask.getId(), childTask);
                if (!queue.offer(childTask.getId())) {
                    throw new RuntimeException("[getTaskAllChildrenNotIncludeSelf]add children task id to queue failure!");
                }

            }
        }
        // 转换，结果已经去重
        if (result.size() != 0) {
            ArrayList<TbClockworkTaskPojo> collect = new ArrayList<>(result.values());
            LOG.info("[getTaskAllChildrenNotIncludeSelf]get task descendants size = {}, task id = {}, cost time = {} ms",
                    collect.size(), taskId, System.currentTimeMillis() - startTime);
            return collect;
        }
        return null;
    }

    @Override
    public List<Integer> getAllChildrenAndSelfIds(Integer taskId, Integer stopType) {
        List<TbClockworkTaskPojo> taskAllChildrenIncludeSelf = getTaskAllChildrenIncludeSelf(taskId);
        List<Integer> taskIds = new ArrayList<>();
        if (stopType == null) {
            stopType = TaskReRunType.ALL_CHILDREN_AND_SELF.getCode();
        }

        //                在这里清除任务的延迟策略并刷新任务时钟
        if (stopType == TaskReRunType.ALL_CHILDREN_AND_SELF.getCode()) {
            taskIds = taskAllChildrenIncludeSelf.stream().map(task -> task.getId()).collect(Collectors.toList());
        }
        if (stopType == TaskReRunType.ALL_CHILDREN_NOT_SELF.getCode()) {
            taskIds = taskAllChildrenIncludeSelf.stream().map(task -> task.getId()).filter(
                    task -> task.intValue() != taskId.intValue()
            ).collect(Collectors.toList());
        }
        return taskIds;
    }
}
