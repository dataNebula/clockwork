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

package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.ITaskOperationService;
import com.creditease.adx.clockwork.api.service.ITaskSubmitService;
import com.creditease.adx.clockwork.api.service.impl.TaskFillDataService;
import com.creditease.adx.clockwork.client.service.TaskSubmitClientService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.creditease.adx.clockwork.common.enums.FillDataType;
import com.creditease.adx.clockwork.common.enums.TaskReRunType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.robert.vesta.service.intf.IdService;
import io.swagger.annotations.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * 构建TaskSubmit Info 分发到Master
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:49 2020-05-11
 * @ Description：任务提交相关接口
 * @ Modified By：
 */
@RestController
@Api(value = "提交任务相关接口")
@RequestMapping("/clockwork/api/task/submit")
public class TaskSubmitController {

    private final Logger LOG = LoggerFactory.getLogger(TaskSubmitController.class);

    @Autowired
    private TaskFillDataService taskFillDataService;

    @Autowired
    private IdService idService;

    @Autowired
    private ITaskSubmitService taskSubmitService;

    @Autowired
    private TaskSubmitClientService taskSubmitClientService;

    @Autowired
    private ITaskOperationService taskOperationService;

    /**
     * 重启任务（自己、所有子节点不包括自己、所有子节点包括自己）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:self,3:all_children_not_self,4:all_children_and_self]
     * @param parameter     参数
     * @param operatorName  操作人
     * @return bool
     */
    @ApiOperation(value = "重启（单个任务）", notes = "taskReRunType重启类型[-1:self,3:all_children_not_self,4:all_children_and_self]")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "需要重启的任务ID", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "taskReRunType", value = "重启类型[-1:self,3:all_children_not_self,"
                    + "4:all_children_and_self]", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "parameter", value = "重启参数json", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "operatorName", value = "操作人", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "scriptParameter", value = "重启脚本参数", required = false, dataType = "String")
    })
    @PostMapping(value = "/rerunTask")
    public Map<String, Object> submitReRunTask(@RequestParam(value = "taskId") Integer taskId,
                                               @RequestParam(value = "taskReRunType", required = false) Integer taskReRunType,
                                               @RequestParam(value = "parameter", required = false) String parameter,
                                               @RequestParam(value = "operatorName", required = false) String operatorName,
                                               @RequestParam(value = "scriptParameter", required = false) String scriptParameter) {
        try {
            if (taskId == null || taskId < 1) {
                return Response.fail("taskId invalid");
            }

            if (taskReRunType == null) {
                taskReRunType = TaskReRunType.SELF.getCode();
            }

            //                在这里清除任务的延迟策略并刷新任务时钟
            if (taskReRunType == TaskReRunType.ALL_CHILDREN_NOT_SELF.getCode()
              || taskReRunType == TaskReRunType.ALL_CHILDREN_AND_SELF.getCode()) {
                boolean isRefreshSlots =
                        taskOperationService.updateTaskChildrensDelayStatusAndRefreshSlots(taskId, taskReRunType);
                if (!isRefreshSlots) {
                    throw new RuntimeException("update task childrens delay status and refresh solt fail, taskId = " + taskId);
                }
            }
            TaskSubmitInfoRerun taskSubmitInfoRerun
                    = taskSubmitService.submitReRunTaskTx(taskId, taskReRunType, parameter, operatorName, scriptParameter);

            // 提交
            boolean result = taskSubmitClientService.submitTask(taskSubmitInfoRerun);
            if (!result) {
                throw new RuntimeException("submitReRunTaskTx is Error.");
            }
            LOG.info("TaskSubmitController-submitReRunTask submit task, taskId = {}, success.", taskId);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("[TaskSubmitController-submitReRunTask] Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 重启历史运行任务（单个任务）
     *
     * @param taskId        taskId
     * @param taskReRunType 历史执行类型[-1:normal,1:routine,0:rerun,2:fill_data]
     * @param logId         历史日志ID
     * @param parameter     参数
     * @param operatorName  操作人
     * @return task
     */
    @ApiOperation(value = "重启历史运行任务（单个任务）", notes = "taskReRunType=0:his_rerun, 1:his_routine, 2:his_fill_data")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "taskId", value = "需要重启的任务ID", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "executeType", value = "执行类型[0:his_rerun, 1:his_routine, 2:his_fill_data]",
                    required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "logId", value = "logId", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "parameter", value = "重启参数json", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "operatorName", value = "操作人", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "scriptParameter", value = "重启脚本参数", required = false, dataType = "String")
    })
    @PostMapping(value = "/rerunTaskHis")
    public Map<String, Object> submitReRunTaskHis(@RequestParam(value = "taskId") Integer taskId,
                                                  @RequestParam(value = "taskReRunType", required = false) Integer taskReRunType,
                                                  @RequestParam(value = "logId", required = false) Integer logId,
                                                  @RequestParam(value = "parameter", required = false) String parameter,
                                                  @RequestParam(value = "operatorName", required = false) String operatorName,
                                                  @RequestParam(value = "scriptParameter", required = false) String scriptParameter) {
        try {
            if (taskId == null || taskId < 1) {
                return Response.fail("taskId is invalid");
            }

            if (taskReRunType == null) {
                taskReRunType = TaskReRunType.SELF.getCode();
            }

            // 提交 - 重启任务信息
            TaskSubmitInfoRerun taskSubmitInfoRerun
                    = taskSubmitService.submitReRunTaskHisTx(taskId, taskReRunType, logId, parameter, operatorName, scriptParameter);

            // 提交信息到Master
            boolean result = taskSubmitClientService.submitTask(taskSubmitInfoRerun);
            if (!result) {
                throw new RuntimeException("taskSubmitClientService.submitTask is Error.");
            }
            LOG.info("TaskSubmitController-submitReRunTaskHis submit task, taskId = {}, success.", taskId);
            return Response.success(true);

        } catch (Exception e) {
            LOG.error("[TaskSubmitController-submitReRunTaskHis] Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    @ApiOperation(value = "通过dagId重启任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "dagId", value = "dagId", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "parameter", value = "重启参数json", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "operatorName", value = "操作人", required = false, dataType = "String")
    })
    @PostMapping(value = "/rerunTaskByDagId")
    public Map<String, Object> submitReRunTaskByDagId(@RequestParam(value = "dagId") Integer dagId,
                                                      @RequestParam(value = "parameter", required = false) String parameter,
                                                      @RequestParam(value = "operatorName", required = false) String operatorName) {
        try {
            if (dagId == null || dagId < 1) {
                return Response.fail("dagId invalid");
            }

            // 提交 - 重启任务信息
            TaskSubmitInfoRerun taskSubmitInfoRerun
                    = taskSubmitService.submitReRunTaskByDagIdTx(dagId, parameter, operatorName);
            boolean result = taskSubmitClientService.submitTask(taskSubmitInfoRerun);
            if (!result) {
                throw new RuntimeException("taskSubmitClientService.submitTask is Error.");
            }
            LOG.info("submitReRunTaskByDagId submit task, dagId = {}, success.", dagId);
            return Response.success(true);

        } catch (Exception e) {
            LOG.error("[TaskSubmitController-submitReRunTaskByDagId] Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "通过groupId重启任务")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "dagId", value = "dagId", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "parameter", value = "重启参数json", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "operatorName", value = "操作人", required = false, dataType = "String")
    })
    @PostMapping(value = "/rerunTaskByGroupId")
    public Map<String, Object> submitReRunTaskByGroupId(@RequestParam(value = "groupId") Integer groupId,
                                                        @RequestParam(value = "parameter", required = false) String parameter,
                                                        @RequestParam(value = "operatorName", required = false) String operatorName) {
        try {
            if (groupId == null || groupId < 1) {
                return Response.fail("groupId invalid");
            }

            // 提交 - 重启任务信息
            TaskSubmitInfoRerun taskSubmitInfoRerun = taskSubmitService.submitReRunTaskByGroupIdTx(groupId, parameter, operatorName);
            boolean result = taskSubmitClientService.submitTask(taskSubmitInfoRerun);
            if (!result) {
                throw new RuntimeException("taskSubmitClientService.submitTask is Error.");
            }
            LOG.info("TaskSubmitController-submitReRunTaskByGroupIdTx submit task, groupId = {}, success.", groupId);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("[TaskSubmitController-submitReRunTaskByGroupIdTx] Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 提交重启任务（批量任务）
     *
     * @param taskList 多个任务
     */
    @PostMapping(value = "/rerunTaskList")
    public Map<String, Object> submitReRunTaskList(@RequestBody List<TbClockworkTaskPojo> taskList) {
        try {
            if (CollectionUtils.isEmpty(taskList)) {
                return Response.fail("task list is null");
            }

            // 提交 - 重启任务信息
            TaskSubmitInfoRerun taskSubmitInfoRerun = taskSubmitService.submitReRunTaskListTx(taskList);
            boolean result = taskSubmitClientService.submitTask(taskSubmitInfoRerun);
            if (!result) {
                throw new RuntimeException("submitReRunTaskList is Error.");
            }
            LOG.info("TaskSubmitController-submitReRunTaskList submit task list success, task list size = {}", taskList.size());
            return Response.success(taskList.size());
        } catch (Exception e) {
            LOG.error("TaskSubmitController-submitReRunTaskList Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());

        }
    }

    /**
     * 补数
     * 1. 补数记录入库到补数记录表tb_clockwork_task_fill_data（一次一条，记录当前运行的日期，是否结束，开始结束时间等汇总信息）
     * 2. 构建一个时间链表入库tb_clockwork_task_fill_data_time_queue（批次号，当前日期，下一个日期，序号-1开始）
     * 3. 任务写入到tb_clockwork_task_rerun记录表，基于当前批次的任务构建一个依赖关系入库
     * 4. 发送第一个起始日期的任务到等待运行队列中
     * 5. 任务开始运行，运行结束后记录成功数，并触发子任务运行
     * 6. 当成功数到达任务数时表示当前时间周期所有任务运行完成，可以运行下一个时间周期的任务，直到运行完成
     *
     * @param entity entity
     * @return
     */
    @ApiOperation(value = "补数接口", notes = "根据taskIds构建一个新的DAG, 按照依赖关系执行补数操作")
    @PostMapping(value = "/fillData")
    public Map<String, Object> submitFillDataTaskList(
            @RequestBody @ApiParam(name = "补数参数", value = "传入json格式", required = true) TaskFillDataEntity entity) {
        try {

            // 参数
            if (entity == null || entity.getFillDataType() == null || entity.getFillDataTimes() == null
                    || entity.getFillDataTimes().size() == 0 || entity.getTaskIds() == null) {
                LOG.warn("[TaskSubmitController]submitFillDataTaskList param is error. param = {}", entity);
                return Response.fail("param is error. param = " + entity);
            }
            // 时间参数校验
            for (String fillDataTime : entity.getFillDataTimes()) {
                String fillDataType = entity.getFillDataType();
                SimpleDateFormat sdf;
                if (FillDataType.HOUR.getType().equals(fillDataType)) {
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH");
                } else {
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                }
                LOG.info("[TaskSubmitController]submitFillDataTaskList submit task list, " +
                                "operatorName = {}, taskIds = {}, fillDataType = {}, fillDataTime = {}",
                        entity.getOperatorName(), entity.getTaskIds(), fillDataType, sdf.parse(fillDataTime));
            }

            // 添加补数记录
            long rerunBatchNumber = idService.genId();
            TbClockworkTaskFillData fillDataRecord = taskFillDataService.addTbClockworkTaskFillDataRecord(entity, rerunBatchNumber);

            // 提交 - 补数任务信息
            TaskSubmitInfoFillData submitInfoFillData = taskSubmitService.submitFillDataTaskListTx(entity, rerunBatchNumber);
            boolean result = taskSubmitClientService.submitTask(submitInfoFillData);
            if (!result) {
                throw new RuntimeException("submitFillDataTaskList-submitTaskToTaskDistributor is Error.");
            }
            LOG.info("[submitFillDataTaskList]fillData submit task list success, operatorName = {}, taskIds = {}, fillData Id = {}",
                    entity.getOperatorName(), entity.getTaskIds(), fillDataRecord.getId());

            return Response.success(PojoUtil.convert(fillDataRecord, TbClockworkTaskFillDataPojo.class));
        } catch (Exception e) {
            LOG.error("[TaskSubmitController]submitFillDataTaskList Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 提交信号触发任务
     *
     * @param signal 信号触发参数
     * @return bool
     */
    @ApiOperation(value = "信号任务提交接口", notes = "触发信号触发模式的任务运行")
    @PostMapping(value = "/submitSignalTaskList")
    public Map<String, Object> submitSignalTaskList(
            @RequestBody @ApiParam(name = "信号触发参数", required = true) TaskSignalEntity signal) {
        try {
            if (signal == null || signal.getTaskIds() == null) {
                return Response.fail("taskId is null");
            }
            // 提交 - 信号触发任务
            TaskSubmitInfoSignal submitInfoSignal = taskSubmitService.submitSignalTaskListTx(signal);
            boolean result = taskSubmitClientService.submitTask(submitInfoSignal);
            if (!result) {
                throw new RuntimeException("submitSignalTaskListTx is Error.");
            }
            LOG.info("submitSignalTaskListTx submit task, operatorName = {}, taskId = {}, success.",
                    signal.getOperatorName(), signal.getTaskIds());
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("[TaskSubmitController-submitSignalTaskList] Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
