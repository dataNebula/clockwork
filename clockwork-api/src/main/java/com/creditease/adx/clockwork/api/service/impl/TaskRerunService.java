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
import com.creditease.adx.clockwork.api.service.ITaskRerunService;
import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.common.entity.gen.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRerun4PagePojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskRerunMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRerunMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRerunRelationMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:05 2019-11-05
 * @ Description：taskRerunService
 * @ Modified By：
 */
@Service(value = "taskRerunService")
public class TaskRerunService implements ITaskRerunService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRerunService.class);

    @Autowired
    private ITaskService taskService;

    @Autowired
    private TbClockworkTaskRerunRelationMapper tbClockworkTaskRerunRelationMapper;

    @Autowired
    private TbClockworkTaskRerunMapper tbClockworkTaskRerunMapper;

    @Autowired
    private TaskRerunMapper taskRerunMapper;

    /**
     * 获得当前任务的父任务
     *
     * @param taskId
     * @param rerunBatchNumber
     * @return
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskRerunFather(int taskId, Long rerunBatchNumber) {
        List<Integer> fatherTaskIds = getTaskRerunFatherIds(taskId, rerunBatchNumber);
        List<TbClockworkTask> TbClockworkTasks = taskService.getTbClockworkTasksByIds(fatherTaskIds);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }
        return PojoUtil.convertList(TbClockworkTasks, TbClockworkTaskPojo.class);
    }

    @Override
    public List<Integer> getTaskRerunFatherIds(int taskId, Long rerunBatchNumber) {
        // 当前批次的依赖关系
        TbClockworkTaskRerunRelationExample example = new TbClockworkTaskRerunRelationExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber).andTaskIdEqualTo(taskId);
        List<TbClockworkTaskRerunRelation> father = tbClockworkTaskRerunRelationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(father)) {
            return null;
        }

        List<Integer> fatherTaskIds = new ArrayList<>();
        for (TbClockworkTaskRerunRelation TbClockworkTaskRelation : father) {
            fatherTaskIds.add(TbClockworkTaskRelation.getFatherTaskId());
        }

        List<TbClockworkTask> TbClockworkTasks = taskService.getTbClockworkTasksByIds(fatherTaskIds);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }

        return fatherTaskIds;
    }

    /**
     * 获得当前任务的所有直接孩子，不包含自己
     *
     * @param taskId
     * @param rerunBatchNumber
     * @return
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskRerunChild(int taskId, Long rerunBatchNumber) {
        // 当前批次的依赖关系
        TbClockworkTaskRerunRelationExample example = new TbClockworkTaskRerunRelationExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber).andFatherTaskIdEqualTo(taskId);
        List<TbClockworkTaskRerunRelation> taskRerunDependencies = tbClockworkTaskRerunRelationMapper
                .selectByExample(example);

        if (CollectionUtils.isEmpty(taskRerunDependencies)) {
            return null;
        }

        List<Integer> childTaskIds = new ArrayList<>();
        for (TbClockworkTaskRerunRelation taskRerunDependency : taskRerunDependencies) {
            childTaskIds.add(taskRerunDependency.getTaskId());
        }

        List<TbClockworkTaskPojo> taskPojos = getTaskRerunByTaskIds(childTaskIds, rerunBatchNumber);
        if (CollectionUtils.isEmpty(taskPojos)) {
            return null;
        }
        LOG.info("[TaskProcessorController][getTaskRerunChild-getTaskRerunByTaskIds]" +
                "Rerun task info, taskPojos.size = {}", taskPojos.size());
        return taskPojos;
    }

    /**
     * 获取重跑的对象
     *
     * @param id
     * @param rerunBatchNumber
     * @return
     */
    @Override
    public TbClockworkTaskPojo getTaskRerunByTaskId(Integer id, Long rerunBatchNumber) {
        // 当前批次的依赖关系
        TbClockworkTaskRerunExample example = new TbClockworkTaskRerunExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber).andTaskIdEqualTo(id);

        List<TbClockworkTaskRerun> taskReruns = tbClockworkTaskRerunMapper.selectByExampleWithBLOBs(example);
        if (taskReruns != null && taskReruns.size() == 1) {
            TbClockworkTaskRerun taskRerun = taskReruns.get(0);
            String taskJson = taskRerun.getTaskJson();
            String parameter = taskRerun.getParameter();
            TbClockworkTaskPojo tbClockworkTask = JSONObject.parseObject(taskJson, TbClockworkTaskPojo.class);
            tbClockworkTask.setParameter(parameter);
            LOG.info("[TaskRerunService]getTaskRerunByTaskIds success, batchNum = {}, id = {}, taskJson = {}, parameter = {}",
                    rerunBatchNumber, id, taskJson, parameter);
            return tbClockworkTask;
        }
        LOG.info("[TaskRerunService]getTaskRerunByTaskIds failed, batchNum = {}, id = {}", rerunBatchNumber, id);
        return null;
    }

    /**
     * 获取重跑的对象List
     *
     * @param ids
     * @param rerunBatchNumber
     * @return
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskRerunByTaskIds(List<Integer> ids, Long rerunBatchNumber) {
        // 当前批次的依赖关系
        List<TbClockworkTaskPojo> result = new ArrayList<>();
        TbClockworkTaskRerunExample example = new TbClockworkTaskRerunExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber).andTaskIdIn(ids);

        List<TbClockworkTaskRerun> taskReruns = tbClockworkTaskRerunMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(taskReruns)) {
            for (TbClockworkTaskRerun taskRerun : taskReruns) {
                String taskJson = taskRerun.getTaskJson();
                String parameter = taskRerun.getParameter();
                TbClockworkTaskPojo tbClockworkTask = JSONObject.parseObject(taskJson, TbClockworkTaskPojo.class);
                tbClockworkTask.setParameter(parameter);
                LOG.info("[TaskRerunService]getTaskRerunByTaskIds, task = {}, taskPojo = {}", taskJson, tbClockworkTask);
                result.add(tbClockworkTask);
            }
        } else {
            LOG.error("[TaskRerunService]getTaskRerunByTaskIds, Error getTaskRerun is null. rerunBatchNumber = {}, ids = {}",
                    rerunBatchNumber, ids);
        }
        LOG.info("[TaskRerunService]getTaskRerunByTaskIds, batchNum = {}, ids = {}, ids.size = {}, result.size = {}",
                rerunBatchNumber, ids, ids.size(), result.size());
        return result;
    }

    /**
     * 获取该批次的Root task ID
     *
     * @param rerunBatchNumber
     * @return
     */
    @Override
    public List<Integer> getTaskRerunRootTaskIds(Long rerunBatchNumber) {
        TbClockworkTaskRerunExample example = new TbClockworkTaskRerunExample();
        example.createCriteria().andRerunBatchNumberEqualTo(rerunBatchNumber).andIsFirstEqualTo(true);

        List<TbClockworkTaskRerun> taskReruns = tbClockworkTaskRerunMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(taskReruns)) {
            return taskReruns.stream().map(TbClockworkTaskRerun::getTaskId).collect(Collectors.toList());
        }
        return null;
    }

    public int getAllTaskRerunByPageParamCount(TbClockworkTaskRerun4PagePojo taskRerun) {
        taskRerun.setRoleName(StringUtil.spiltAndAppendSingleCitation(taskRerun.getRoleName()));
        HashMap<String, Object> param = new HashMap<>();
        param.put("taskId", taskRerun.getTaskId());
        param.put("taskName", taskRerun.getTaskName());
        param.put("operatorName", taskRerun.getOperatorName());
        param.put("rerunBatchNumber", taskRerun.getRerunBatchNumber());
        param.put("executeType", taskRerun.getExecuteType());
        param.put("createUser", taskRerun.getCreateUser());
        param.put("startTime", DateUtil.dateToString(taskRerun.getStartTime()));
        param.put("endTime", DateUtil.dateToString(taskRerun.getEndTime()));
        param.put("roleName", taskRerun.getRoleName());
        return taskRerunMapper.countAllTaskRerunByPageParam(param);
    }

    /**
     * 分页查询
     *
     * @param taskRerun  taskRerun
     * @param pageNumber number
     * @param pageSize   size
     * @return
     */
    public List<TbClockworkTaskRerun4PagePojo> getAllTaskRerunByPageParam(
            TbClockworkTaskRerun4PagePojo taskRerun, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("taskId", taskRerun.getTaskId());
        param.put("taskName", taskRerun.getTaskName());
        param.put("operatorName", taskRerun.getOperatorName());
        param.put("rerunBatchNumber", taskRerun.getRerunBatchNumber());
        param.put("executeType", taskRerun.getExecuteType());
        param.put("createUser", taskRerun.getCreateUser());
        param.put("startTime", DateUtil.dateToString(taskRerun.getStartTime()));
        param.put("endTime", DateUtil.dateToString(taskRerun.getEndTime()));
        param.put("roleName", taskRerun.getRoleName());
        return taskRerunMapper.selectAllTaskRerunByPageParam(param);
    }

}
