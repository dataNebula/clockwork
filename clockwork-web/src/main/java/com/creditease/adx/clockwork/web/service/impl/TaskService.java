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

package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.common.enums.TaskTakeEffectStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import com.creditease.adx.clockwork.web.service.ITaskService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(value = "taskService")
public class TaskService implements ITaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public TbClockworkTaskMapper getMapper() {
        return this.tbClockworkTaskMapper;
    }

    @Override
    public TbClockworkTaskPojo getTaskById(Integer taskId) {
        TbClockworkTask TbClockworkTask = tbClockworkTaskMapper.selectByPrimaryKey(taskId);
        if (TbClockworkTask != null) {
            return PojoUtil.convert(TbClockworkTask, TbClockworkTaskPojo.class);
        }
        LOG.error("[TbClockworkTaskPojo]getTaskById return is null. taskId = {}", taskId);
        return null;
    }


    /**
     * 获取task列表
     *
     * @param ids
     * @return
     */
    @Override
    public List<TbClockworkTask> getTaskByTaskIds(List<Integer> ids) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andIdIn(ids);
        return tbClockworkTaskMapper.selectByExample(example);
    }

    @Override
    public List<Map<String, Object>> getAllTaskIdAndNameNotInThisId(Integer id) {
        List<Map<String, Object>> tasks = taskMapper.selectAllIdAndName(id);
        LOG.info("getAllTaskIdAndNameNotInThisId, id = {}, tasks.size = {}", id, tasks.size());
        return tasks;
    }

    @Override
    public List<Map<String, Object>> getTaskIdAndNameByUserGroupName(String userName, String userGroupName, Integer id) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        param.put("userName", userName);
        param.put("userGroupName", userGroupName == null ? "-" : userGroupName);
        return taskMapper.selectTaskIdAndNameByUserGroupName(param);
    }

    @Override
    public List<TbClockworkTaskPojo> getTasksByDagId(int dagId) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andDagIdEqualTo(dagId).andOnlineEqualTo(true);

        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTasksByDagId task.size = {}", tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    /**
     * 获取task列表
     *
     * @param taskGroupId
     * @return
     */
    @Override
    public List<TbClockworkTask> getTaskByTaskGroupId(int taskGroupId) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andGroupIdEqualTo(taskGroupId);
        List<TbClockworkTask> TbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        LOG.info("getTaskByTaskGroupId method :::::tasks.size={}", TbClockworkTasks.size());
        return TbClockworkTasks;
    }

    /**
     * 通过taskName统计Task(不包含已经删除)
     *
     * @param taskName
     * @return
     */
    @Override
    public long getCountByTaskName(String taskName) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andNameEqualTo(taskName);
        return tbClockworkTaskMapper.countByExample(example);
    }

}
