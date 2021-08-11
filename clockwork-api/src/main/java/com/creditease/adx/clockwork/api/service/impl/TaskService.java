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

import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.common.entity.gen.*;
import com.creditease.adx.clockwork.common.enums.*;
import com.creditease.adx.clockwork.common.pojo.DDSAndDataWorkTaskInfoPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskDependencyScriptMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "taskService")
public class TaskService implements ITaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TbClockworkTaskDependencyScriptMapper tbClockworkTaskDependencyScriptMapper;

    @Override
    public TbClockworkTaskMapper getMapper() {
        return this.tbClockworkTaskMapper;
    }

    /**
     * 获得任务运行需要替换的脚本文件信息
     *
     * @param taskId
     * @return
     */
    @Override
    public List<TbClockworkTaskDependencyScript> getDependencyScriptFileByTaskId(Integer taskId) {
        TbClockworkTaskDependencyScriptExample example = new TbClockworkTaskDependencyScriptExample();
        example.createCriteria().andTaskIdEqualTo(taskId);
        return tbClockworkTaskDependencyScriptMapper.selectByExample(example);
    }

    /**
     * 获取任务，根据taskStatusList
     *
     * @param statusList status list
     * @return
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskListByStatusList(List<String> statusList) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andStatusIn(statusList).andOnlineEqualTo(TaskTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTask> TbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }
        return PojoUtil.convertList(TbClockworkTasks, TbClockworkTaskPojo.class);
    }

    @Override
    public List<TbClockworkTask> getTbClockworkTasksByIds(List<Integer> ids) {
        TbClockworkTaskExample TbClockworkTaskExample = new TbClockworkTaskExample();
        TbClockworkTaskExample.createCriteria().andIdIn(ids).andOnlineEqualTo(TaskTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTask> TbClockworkTasks = tbClockworkTaskMapper.selectByExample(TbClockworkTaskExample);
        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            return null;
        }
        return TbClockworkTasks;
    }

    /**
     * 根据主键查询Task
     *
     * @param taskId id
     */
    @Override
    public TbClockworkTaskPojo getTaskById(Integer taskId) {
        TbClockworkTask TbClockworkTask = tbClockworkTaskMapper.selectByPrimaryKey(taskId);
        if (TbClockworkTask != null) {
            return PojoUtil.convert(TbClockworkTask, TbClockworkTaskPojo.class);
        }
        LOG.error("[TaskService]getTaskById return is null. taskId = {}", taskId);
        return null;
    }

    /**
     * 根据name查询Task
     *
     * @param name name
     */
    @Override
    public TbClockworkTaskPojo getTaskByName(String name) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andNameEqualTo(name);
        example.setLimitStart(0);
        example.setLimitEnd(1);
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (tbClockworkTasks != null && tbClockworkTasks.size() == 1) {
            return PojoUtil.convert(tbClockworkTasks.get(0), TbClockworkTaskPojo.class);
        }
        LOG.error("[TaskService]getTaskByName return is null. name = {}", name);
        return null;
    }

    /**
     * 根据状态查询task
     *
     * @param status task status
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskByStatus(String status) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andStatusEqualTo(status);

        // 如果是enable 则需要算上成功的
        if (TaskStatus.ENABLE.getValue().equalsIgnoreCase(status)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(status);
            list.add(TaskStatus.SUCCESS.getValue());

            example = new TbClockworkTaskExample();
            example.createCriteria().andStatusIn(list);
        }

        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTaskByStatus, tbClockworkTasks.size = {}", tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    /**
     * 根据状态查询online task
     *
     * @param status task status
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskOnlineByStatus(String status) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andOnlineEqualTo(true).andStatusEqualTo(status);
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTaskOnlineByStatus, status = {}, tbClockworkTasks.size = {}", status, tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    /**
     * 根据失败状态查询一段时间内失败的task
     *
     * @param status task status
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskByRunFailedStatus(List<String> status, String beforeDateStr, String currentDateStr) {
        Map<String, String> param = new HashMap<>();
        param.put("status", StringUtil.convertListToString(status));
        param.put("beforeDateStr", beforeDateStr);
        param.put("currentDateStr", currentDateStr);
        List<TbClockworkTaskPojo> tbClockworkTasks = taskMapper.selectTaskByRunFailedStatus(param);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTaskByRunFailedStatus, status = {}, beforeDateStr = {}, currentDateStr = {}, tbClockworkTasks.size = {}",
                    status, beforeDateStr, currentDateStr, tbClockworkTasks.size());
            return tbClockworkTasks;
        }
        LOG.info("getTaskByRunFailedStatus, status = {}, beforeDateStr = {}, currentDateStr = {}, tbClockworkTasks.size 0",
                status, beforeDateStr, currentDateStr);
        return null;
    }

    /**
     * 根据delayStatus查询taskList
     *
     * @param delayStatus delay status
     */
    @Override
    public List<TbClockworkTaskPojo> getTaskByDelayStatus(int delayStatus) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        // 父延迟或者父延迟恢复，只会发生在时间&依赖触发类型的任务上
        if (TaskDelayStatus.FATHER_DELAY.getCode() == delayStatus
                || TaskDelayStatus.FATHER_DELAYED_RECOVERY.getCode() == delayStatus) {
            example.createCriteria()
                    .andDelayStatusEqualTo(delayStatus)
                    .andTriggerModeEqualTo(TaskTriggerModel.TIME_AND_DEPENDENCY.getValue());
        } else {
            example.createCriteria().andDelayStatusEqualTo(delayStatus);
        }
        // 查询
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTaskByDelayStatus, delayStatus = {},tbClockworkTasks.size = {}",
                    delayStatus, tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    /**
     * 根据dagId 查询all task
     *
     * @param dagId dagId
     */
    @Override
    public List<TbClockworkTaskPojo> getTasksByDagId(int dagId) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andDagIdEqualTo(dagId).andOnlineEqualTo(true);

        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTasksByDagId dagId = {}, task.size = {}", dagId, tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    /**
     * 根据dagId 查询all task（不包括dds）
     *
     * @param dagId dagId
     */
    @Override
    public List<TbClockworkTaskPojo> getTasksNotIncludeDDSByDagId(int dagId) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andDagIdEqualTo(dagId).andOnlineEqualTo(true)
                .andSourceNotEqualTo(TaskSource.DDS_2.getValue());

        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTasksNotIncludeDDSByDagId dagId = {}, task.size = {}", dagId, tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    /**
     * 根据source查询all task dagIds
     *
     * @param source source
     */
    @Override
    public List<Integer> getTaskDagIdsBySource(Integer source) {
        if( source == null ) return null;
        return taskMapper.selectTaskDagIdsBySource(source);
    }

    /**
     * 根据source查询DagID，并且存在dag任务跨source
     *
     * @param source source
     * @return dagId
     */
    @Override
    public List<Integer> getTaskDagIdsByCrossSource(Integer source) {
        if( source == null ) return null;
        return taskMapper.selectTaskDagIdsByCrossSource(source);
    }

    /**
     * 根据GroupId查询Task
     *
     * @param groupId Group id
     */
    @Override
    public List<TbClockworkTaskPojo> getTasksByGroupId(int groupId) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andGroupIdEqualTo(groupId).andOnlineEqualTo(true);

        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTasksByGroupId groupId = {}, task.size = {}", groupId, tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    /**
     * 获取task列表
     *
     * @param taskGroupId taskGroupId
     * @return
     */
    @Override
    public List<TbClockworkTask> getTaskByTaskGroupId(int taskGroupId) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andGroupIdEqualTo(taskGroupId);
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        LOG.info("getTaskByTaskGroupId, tasks.size = {}", tbClockworkTasks.size());
        return tbClockworkTasks;
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

    /**
     * 根据taskNames查询task
     *
     * @param names task name
     */
    @Override
    public List<TbClockworkTask> getTaskByNames(List<String> names) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andNameIn(names);
        return tbClockworkTaskMapper.selectByExample(example);
    }

    @Override
    public List<TbClockworkTask4PagePojo> getAllTaskByCondition(TbClockworkTask4PagePojo tbClockworkTask) {
        if (tbClockworkTask == null) {
            tbClockworkTask = new TbClockworkTask4PagePojo();
        }
        return taskMapper.selectAllTaskByCondition(tbClockworkTask);
    }

    @Override
    public List<TbClockworkTask4PagePojo> getAllTaskUsedByAutoComplete(String idOrNameSegment) {
        return taskMapper.selectAllTaskUsedByAutoComplete(idOrNameSegment);
    }

    /**
     * 查询当前task的状态
     *
     * @param taskId taskId
     */
    @Override
    public String getTaskStatusById(int taskId) {
        TbClockworkTask clockworkTask = tbClockworkTaskMapper.selectByPrimaryKey(taskId);
        return clockworkTask != null ? clockworkTask.getStatus() : null;
    }

    /**
     * 根据表名，获得产出这张表对应调度任务的状态
     *
     * @param businessInfo 业务保留字段
     * @return
     */
    @Override
    public Map<String, Map<Integer, String>> getTaskStatusByTableName(String businessInfo) {
        Map<String, Map<Integer, String>> result = null;
        if (StringUtils.isEmpty(businessInfo)) {
            return result;
        }

        String[] tableNames = StringUtils.split(businessInfo, ",");
        if (tableNames == null || tableNames.length < 1) {
            return result;
        }
        result = new HashMap<>();

        for (String tableName : tableNames) {
            TbClockworkTaskExample TbClockworkTaskExample = new TbClockworkTaskExample();
            TbClockworkTaskExample.createCriteria()
                    .andBusinessInfoEqualTo(tableName)
                    .andOnlineEqualTo(TaskTakeEffectStatus.ONLINE.getValue());
            List<TbClockworkTask> TbClockworkTasks = tbClockworkTaskMapper.selectByExample(TbClockworkTaskExample);
            if (CollectionUtils.isEmpty(TbClockworkTasks)) {
                continue;
            }
            // 防止一个表名有多个任务对应，用map结构，key为任务id，value为任务状态
            Map<Integer, String> taskIdAndStatus = new HashMap<Integer, String>();
            for (TbClockworkTask TbClockworkTask : TbClockworkTasks) {
                taskIdAndStatus.put(TbClockworkTask.getId(), TbClockworkTask.getStatus());
            }
            // 设置当前表名的对应任务的状态
            result.put(tableName, taskIdAndStatus);
        }
        // 打印
        for (Map.Entry<String, Map<Integer, String>> entry : result.entrySet()) {
            for (Map.Entry<Integer, String> valueEntry : entry.getValue().entrySet()) {
                LOG.info("[TaskService-getTaskStatusByTableName]tableName = {},taskId = {},status = {}",
                        entry.getKey(), valueEntry.getKey(), valueEntry.getValue());
            }
        }
        return result;
    }


    /**
     * 根据dagId 查询all task which task is online
     *
     * @param dagId dagId
     */
    @Override
    public List<TbClockworkTaskPojo> getTasksByDagIdWhereTaskIsOnline(int dagId) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andDagIdEqualTo(dagId).andOnlineEqualTo(true);

        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getTasksByDagId dagId = {}, task.size = {}", dagId, tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

      /**
     * 前端分页查询count
     *
     * @param task
     * @return
     */
    @Override
    public int getAllTaskByPageParamCount(TbClockworkTask4PagePojo task) {
      HashMap<String, Object> param = new HashMap<>();
      task.setRoleName(StringUtil.spiltAndAppendSingleCitation(task.getRoleName()));
      param.put("roleName", task.getRoleName());
      param.put("id", task.getId());
      param.put("name", task.getName());
      param.put("groupId", task.getGroupId());
      param.put("dagId", task.getDagId());
      param.put("groupName", task.getGroupName());
      param.put("status", task.getStatus());
      param.put("triggerMode", task.getTriggerMode());
      param.put("online", task.getOnline());
      param.put("source", task.getSource());
      param.put("createUser", task.getCreateUser());
      param.put("lastStartTime", DateUtil.dateToString(task.getLastStartTime()));
      param.put("lastEndTime", DateUtil.dateToString(task.getLastEndTime()));
      param.put("businessInfo", task.getBusinessInfo());

      return taskMapper.countAllTaskByPageParam(param);
    }

  /**
   * 前端分页查询
   *
   * @param task
   * @return
   */
    @Override
    public List<TbClockworkTask4PagePojo> getAllTaskByPageParam(TbClockworkTask4PagePojo task, int pageNumber, int pageSize) {
      HashMap<String, Object> param = new HashMap<>();

      // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
      param.put("pageNumber", (pageNumber - 1) * pageSize);
      param.put("pageSize", pageSize);
      param.put("roleName", task.getRoleName());
      param.put("id", task.getId());
      param.put("name", task.getName());
      param.put("groupId", task.getGroupId());
      param.put("dagId", task.getDagId());
      param.put("groupName", task.getGroupName());
      param.put("status", task.getStatus());
      param.put("triggerMode", task.getTriggerMode());
      param.put("online", task.getOnline());
      param.put("source", task.getSource());
      param.put("createUser", task.getCreateUser());
      param.put("lastStartTime", DateUtil.dateToString(task.getLastStartTime()));
      param.put("lastEndTime", DateUtil.dateToString(task.getLastEndTime()));
      param.put("businessInfo", task.getBusinessInfo());
      return taskMapper.selectAllTaskByPageParam(param);
    }

    /**
     * 获得dds和datawork需要的数据
     *
     * @param tbClockworkTask
     * @return
     */
    @Override
    public List<DDSAndDataWorkTaskInfoPojo> getDDSAndDataWorkTasks(TbClockworkTask4PagePojo tbClockworkTask) {
      if (tbClockworkTask == null) {
        tbClockworkTask = new TbClockworkTask4PagePojo();
      }
      return taskMapper.selectAllTaskByCondition(tbClockworkTask).stream().map(
        task -> new DDSAndDataWorkTaskInfoPojo(task.getId(),task.getGroupId(),task.getName(),task.getGroupName())
      ).collect(Collectors.toList());
    }
}
