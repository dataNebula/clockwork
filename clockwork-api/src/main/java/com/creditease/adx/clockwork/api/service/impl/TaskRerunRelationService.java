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

import com.creditease.adx.clockwork.api.service.ITaskRelationService;
import com.creditease.adx.clockwork.api.service.ITaskRerunRelationService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerunRelation;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerunRelationExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRerunRelationPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRerunRelationMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:05 2019-11-05
 * @ Description：任务重跑的依赖关系
 * @ Modified By：
 */
@Service(value = "taskRerunRelationService")
public class TaskRerunRelationService implements ITaskRerunRelationService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRerunRelationService.class);

    @Autowired
    private TbClockworkTaskRerunRelationMapper tbClockworkTaskRerunRelationMapper;

    @Autowired
    private ITaskRelationService taskRelationService;


    /**
     * 获取任务直接子节点
     *
     * @param taskId           taskId
     * @param rerunBatchNumber rerunBatchNumber
     * @return list
     */
    @Override
    public List<Integer> findTaskDirectlyChildrenIds(Integer taskId, Long rerunBatchNumber) {
        List<TbClockworkTaskRerunRelation> rerunRelations = findTaskDirectlyChildren(taskId, rerunBatchNumber);
        if (CollectionUtils.isNotEmpty(rerunRelations)) {
            return rerunRelations.stream().map(TbClockworkTaskRerunRelation::getTaskId).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取任务直接父节点
     *
     * @param taskId           taskId
     * @param rerunBatchNumber rerunBatchNumber
     * @return list
     */
    @Override
    public List<Integer> findTaskDirectlyFatherIds(Integer taskId, Long rerunBatchNumber) {
        if (taskId == null || rerunBatchNumber == null) {
            return null;
        }
        TbClockworkTaskRerunRelationExample example = new TbClockworkTaskRerunRelationExample();
        example.createCriteria().andTaskIdEqualTo(taskId).andRerunBatchNumberEqualTo(rerunBatchNumber);
        List<TbClockworkTaskRerunRelation> rerunRelations = tbClockworkTaskRerunRelationMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(rerunRelations)) {
            return rerunRelations.stream().map(TbClockworkTaskRerunRelation::getFatherTaskId).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取任务直接子节点
     *
     * @param taskId           taskId
     * @param rerunBatchNumber rerunBatchNumber
     * @return list
     */
    @Override
    public List<TbClockworkTaskRerunRelation> findTaskDirectlyChildren(Integer taskId, Long rerunBatchNumber) {
        if (taskId == null || rerunBatchNumber == null) {
            return null;
        }
        TbClockworkTaskRerunRelationExample example = new TbClockworkTaskRerunRelationExample();
        example.createCriteria().andFatherTaskIdEqualTo(taskId).andRerunBatchNumberEqualTo(rerunBatchNumber);
        return tbClockworkTaskRerunRelationMapper.selectByExample(example);
    }

    /**
     * 获取任务直接父节点
     *
     * @param taskId           taskId
     * @param rerunBatchNumber rerunBatchNumber
     * @return list
     */
    @Override
    public List<TbClockworkTaskRerunRelation> findTaskDirectlyFather(Integer taskId, Long rerunBatchNumber) {
        List<Integer> fatherIds = findTaskDirectlyFatherIds(taskId, rerunBatchNumber);
        if (CollectionUtils.isEmpty(fatherIds)) {
            return null;
        }
        TbClockworkTaskRerunRelationExample example = new TbClockworkTaskRerunRelationExample();
        example.createCriteria().andTaskIdIn(fatherIds).andRerunBatchNumberEqualTo(rerunBatchNumber);
        return tbClockworkTaskRerunRelationMapper.selectByExample(example);
    }


    /**
     * 所有子节点（获取所有子节点）通过批次号
     *
     * @param taskId           任务Id
     * @param rerunBatchNumber 批次号
     * @return list
     */
    @Override
    public List<TbClockworkTaskRerunRelation> findTaskAllChildrenNotIncludeSelf(Integer taskId, Long rerunBatchNumber) {
        HashMap<Integer, TbClockworkTaskRerunRelation> result = new HashMap<>();
        if (taskId == null || rerunBatchNumber == null) {
            return null;
        }
        Queue<Integer> queue = new LinkedList<>();
        if (!queue.offer(taskId)) {
            throw new RuntimeException("[findTaskAllChildrenNotIncludeSelf]add task id to queue failure!");
        }
        // 队列方式获得当前任务的所有子孙后代
        Integer queueTaskId;
        long startTime = System.currentTimeMillis();
        while ((queueTaskId = queue.poll()) != null) {
            List<TbClockworkTaskRerunRelation> childTasks = findTaskDirectlyChildren(queueTaskId, rerunBatchNumber);
            if (CollectionUtils.isEmpty(childTasks)) {
                continue;
            }
            for (TbClockworkTaskRerunRelation childTask : childTasks) {
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
            ArrayList<TbClockworkTaskRerunRelation> collect = new ArrayList<>(result.values());
            LOG.info("[findTaskAllChildrenNotIncludeSelf]get task descendants size = {}, task id = {}, cost time = {} ms",
                    collect.size(), taskId, System.currentTimeMillis() - startTime);
            return collect;
        }
        return null;
    }

    /**
     * 获取TbClockworkTaskRerunRelation数据，通过批次号
     *
     * @param rerunBatchNumber rerunBatchNumber
     * @return
     */
    @Override
    public List<TbClockworkTaskRerunRelation> getTaskRerunRelationByBatchNumber(Long rerunBatchNumber) {
        if (rerunBatchNumber == null) {
            return null;
        }
        TbClockworkTaskRerunRelationExample example = new TbClockworkTaskRerunRelationExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber);
        return tbClockworkTaskRerunRelationMapper.selectByExample(example);
    }

    /**
     * 根据重跑的task 构建依赖关系
     *
     * @param tasks
     * @return
     */
    @Override
    public List<TbClockworkTaskRerunRelation> buildTaskRelation(List<TbClockworkTaskPojo> tasks, long rerunBatchNumber) {
        List<TbClockworkTaskRerunRelationPojo> result = new ArrayList<>();

        // 转为Map
        Map<Integer, TbClockworkTaskPojo> maps
                = tasks.stream().collect(Collectors.toMap(TbClockworkTask::getId, item -> item, (key1, key2) -> key2));

        // 遍历并构建当前批次的依赖
        TbClockworkTaskRerunRelationPojo taskRerunDependency;
        for (TbClockworkTaskPojo pojo : maps.values()) {
            taskRerunDependency = new TbClockworkTaskRerunRelationPojo();
            taskRerunDependency.setRerunBatchNumber(rerunBatchNumber);
            taskRerunDependency.setTaskId(pojo.getId());
            taskRerunDependency.setTaskName(pojo.getName());
            taskRerunDependency.setUpdateTime(new Date());
            taskRerunDependency.setCreateTime(new Date());

            // 获取该节点的所有父节点
            boolean hasFather = false;
            List<TbClockworkTaskRelationPojo> fathers = taskRelationService.findTaskDirectlyFather(pojo.getId());
            if (fathers != null) for (TbClockworkTaskRelationPojo father : fathers) {
                TbClockworkTaskRerunRelationPojo taskRerunDep =
                        (TbClockworkTaskRerunRelationPojo) taskRerunDependency.clone();
                Integer taskId = father.getTaskId();
                if (maps.containsKey(taskId)) {
                    // 设置父级
                    hasFather = true;
                    taskRerunDep.setFatherTaskId(taskId);
                    taskRerunDep.setFatherTaskName(father.getTaskName());
                    result.add(taskRerunDep);
                }
            }

            if (!hasFather) {
                taskRerunDependency.setFatherTaskId(-1);
                result.add(taskRerunDependency);
            }
        }

        return PojoUtil.convertList(result, TbClockworkTaskRerunRelation.class);
    }

}
