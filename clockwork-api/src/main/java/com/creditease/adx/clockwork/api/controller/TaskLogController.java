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

import com.creditease.adx.clockwork.api.service.ITaskLogService;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.TaskLogUtil;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Api("任务日志")
@RestController
@RequestMapping("/clockwork/api/task/log")
public class TaskLogController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogController.class);

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();

    @Resource(name = "taskLogService")
    private ITaskLogService taskLogService;

    /**
     * 添加到kafka队列
     *
     * @param logPojoList
     * @return
     */
    @PostMapping(value = "/addToKafkaQueue")
    public Map<String, Object> addToKafkaQueue(@RequestBody List<TbClockworkTaskLogPojo> logPojoList) {
        try {
            if (CollectionUtils.isEmpty(logPojoList)) {
                return Response.fail("Param error.");
            }
            // 添加到kafka队列
            if (taskLogService.addToKafkaQueue(logPojoList)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("TaskLogController-addToKafkaQueue Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 添加taskLog & Send2Kafka
     *
     * @param taskLogPojo json string
     */
    @PostMapping(value = "/addTaskLog")
    public Map<String, Object> addTaskLog(@RequestBody TbClockworkTaskLogPojo taskLogPojo) {
        try {
            return Response.success(taskLogService.addTbClockworkTaskLog(taskLogPojo));
        } catch (Exception e) {
            LOG.error("TaskLogController - addTbClockworkTaskLog, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 批量添加taskLog & Send2Kafka
     *
     * @param taskLogPojo json string
     */
    @PostMapping(value = "/addBatchTaskLog")
    public Map<String, Object> addBatchTaskLog(@RequestBody List<TbClockworkTaskLog> taskLogPojo) {
        try {
            return Response.success(taskLogService.addBatchTaskLog(taskLogPojo));
        } catch (Exception e) {
            LOG.error("TaskLogController - addBatchTaskLog, Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改taskLog & Send2Kafka
     *
     * @param taskLogPojo json string
     */
    @PostMapping(value = "/updateTaskLog")
    public Map<String, Object> updateTaskLog(@RequestBody TbClockworkTaskLogPojo taskLogPojo) {
        try {
            return Response.success(taskLogService.updateTbClockworkTaskLog(taskLogPojo));
        } catch (Exception e) {
            LOG.error("TaskLogController - updateTbClockworkTaskLog, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新logName
     */
    @PostMapping(value = "/updateTaskLogLogName")
    public Map<String, Object> updateTaskLogLogName(@RequestParam(value = "id") Integer id,
                                                    @RequestParam(value = "logName") String logName) {
        try {
            return Response.success(taskLogService.updateTaskLogLogName(id, logName));
        } catch (Exception e) {
            LOG.error("TaskLogController - updateTbClockworkTaskLog, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新Pid
     */
    @PostMapping(value = "/updateTaskLogPid")
    public Map<String, Object> updateTaskLogPid(@RequestParam(value = "id") Integer id,
                                                @RequestParam(value = "pid") Integer pid) {
        try {
            return Response.success(taskLogService.updateTaskLogPid(id, pid));
        } catch (Exception e) {
            LOG.error("TaskLogController - updateTaskLogPid, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 批量更新状态
     *
     * @param updateTaskLogStatusParam
     * @return
     */
    @PostMapping(value = "/updateBatchTaskLogStatus")
    public Map<String, Object> updateBatchTaskLogStatus(@RequestBody BatchUpdateTaskLogStatusParam updateTaskLogStatusParam) {
        try {
            LOG.info("updateBatchTaskLogStatus info, taskLogIds = {}, status = {}",
                    updateTaskLogStatusParam.getLogIds(),
                    updateTaskLogStatusParam.getStatus());
            boolean updateTasksStatusResult = taskLogService.updateBatchTaskLogStatus(updateTaskLogStatusParam);
            if (updateTasksStatusResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("updateBatchTaskLogStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    @ApiOperation("获得指定ID日志信息")
    @GetMapping(value = "/getTaskLogById")
    public Map<String, Object> getTaskLogById(@RequestParam(value = "taskLogId") Integer taskLogId) {
        try {
            return Response.success(taskLogService.getTbClockworkTaskLogById(taskLogId));
        } catch (Exception e) {
            LOG.error("TaskLogController - getTaskLogById, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("获得指定状态下的任务的日志信息")
    @GetMapping(value = "/getTaskLogByTaskStatus")
    public Map<String, Object> getTaskLogByTaskStatus(@RequestParam(value = "status") String status) {
        try {
            return Response.success(taskLogService.getTaskLogByTaskStatus(status));
        } catch (Exception e) {
            LOG.error("TaskLogController - getTaskLogByTaskStatus, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("按时间段查询Task运行日志信息")
    @GetMapping(value = "/getTaskLogLogInfo")
    public Map<String, Object> getTaskLogLogInfo(@RequestParam(value = "taskId") Integer taskId,
                                                 @RequestParam(value = "taskGroupId") Integer taskGroupId,
                                                 @RequestParam(value = "startDate") String startDate,
                                                 @RequestParam(value = "endDate") String endDate) {
        try {
            List<TbClockworkTaskLog> baseBusinessDatas
                    = taskLogService.getTaskLogLogInfo(taskId, taskGroupId, startDate, endDate);
            if (CollectionUtils.isEmpty(baseBusinessDatas)) {
                return Response.success(null);
            }
            return Response.success(PojoUtil.convertList(baseBusinessDatas, TbClockworkTaskLogPojo.class));
        } catch (Exception e) {
            LOG.error("TaskLogController - getTaskLogLogInfo, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/getFillDataTaskLogByTaskIds")
    public Map<String, Object> getFillDataTaskLogByTaskIds(@RequestParam(value = "taskIds") List<Integer> taskIds,
                                                           @RequestParam(value = "rerunBatchNumber") long rerunBatchNumber,
                                                           @RequestParam(value = "fillDataTime") String fillDataTime) {
        try {
            return Response.success(taskLogService.getFillDataTaskLogByTaskIds(taskIds, rerunBatchNumber, fillDataTime));
        } catch (Exception e) {
            LOG.error("TaskLogController - getFillDataTaskLogByTaskIds, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }

    }

    @PostMapping(value = "/getTaskLogByTaskIdsAndStatusList")
    public Map<String, Object> getTaskLogByTaskIdsAndStatusList(@RequestBody BatchGetTaskLogByTaskIdsParam param) {
        try {
            if (CollectionUtils.isEmpty(param.getTaskIds())
                    || CollectionUtils.isEmpty(param.getStatusList())
                    || param.getStartTime() == null
                    || param.getEndTime() == null) {
                LOG.error("getTaskLogByTaskIdsAndStatusList RequestParam is null, param = {}", param);
                return Response.fail("RequestParam is null");
            }
            return Response.success(taskLogService.getTaskLogByTaskIdsAndStatusList(param));
        } catch (Exception e) {
            LOG.error("TaskLogController - getTaskLogByTaskIdsAndStatusList, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * @param taskId taskId
     */
    @GetMapping(value = "/getTaskLogByTaskId")
    public Map<String, Object> getTaskLogByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        if (taskId == null || taskId < 1) {
            LOG.error("TaskLogController - getTaskLogByTaskId, invalid taskId");
            return Response.fail("invalid taskId");
        }
        try {
            LOG.info("TaskLogController - getTaskLogByTaskId, taskId = {}", taskId);
            return Response.success(taskLogService.getTaskLogByTaskId(taskId));
        } catch (Exception e) {
            LOG.error("TaskLogController - getTaskLogByTaskId, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }

    }

    /**
     * 根据taskLog信息查询任务历史
     */
    @GetMapping(value = "getTaskLogByTaskGroupId")
    public Map<String, Object> getTaskLogByTaskGroupId(@RequestBody TaskLogInfo taskLogInfo) {
        if (taskLogInfo == null) {
            LOG.error("TaskLogController - getTaskLogByTaskGroupId, invalid taskLogInfo");
            return Response.fail(
                    "invalid taskLogInfo");
        }
        try {
            LOG.info("TaskLogController - getTaskLogByTaskGroupId, taskLogInfo={}", taskLogInfo);
            List<TbClockworkTaskLogPojo> taskLog = taskLogService.getTaskLogByTaskLogInfo(taskLogInfo);
            return Response.success(TaskLogUtil.getBatchTaskLogByBatchNum(taskLog));
        } catch (Exception e) {
            LOG.error("TaskLogController - getTaskLogByTaskGroupId, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据taskLog信息查询任务历史
     *
     * @param taskGroupId
     * @param batchNum
     * @return
     */
    @GetMapping(value = "/getTaskLogByBatchNum")
    public Map<String, Object> getTaskLogByBatchNum(@RequestParam(value = "taskGroupId") Integer taskGroupId,
                                                    @RequestParam(value = "batchNum") Long batchNum) {
        if (taskGroupId == null || batchNum == null) {
            LOG.error("TaskLogController - getTaskLogByBatchNum, invalid taskGroupId or batchNum");
            return Response.fail("invalid taskGroupId or batchNum");
        }
        try {
            LOG.info("TaskLogController - getTaskLogByBatchNum, taskGroupId={}, batchNum={}", taskGroupId, batchNum);
            return Response.success(taskLogService.getTaskLogByBatchNum(taskGroupId, batchNum));
        } catch (Exception e) {
            LOG.error("TaskLogController - getTaskLogByBatchNum, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 查询接口
     *
     * @param pageParam 查询参数
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ApiOperation(value = "Task日志列表", notes = "请求参数如下, 查询条件为condition Json对象：" +
            " <br>" + "{" +
            " <br>" + "  \"pageNum\": 1, // 第一页" +
            " <br>" + "  \"pageSize\": 10, // 每页10条" +
            " <br>" + "  \"role\": \"\", // 可为空" +
            " <br>" + "  \"userName\": \"\", // 可为空" +
            " <br>" + "  \"condition\"：{" +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"id\": 123, // 日志id" +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskId\":111, // 任务id " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"source\":3, // 0调度系统,2dataHub,3dataWorks,4dds " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"executeType\":1, // 执行类型0重启，1例行，2补数 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskName\":\"\", // 任务名 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskAliasName\":\"\", // 任务别名 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskGroupName\":\"\", // 任务组名 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskGroupAliasName\":\"\", // 任务组别名 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"status\":\"\", // success/failed 状态 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"isEnd\": false, // 是否结束true/false " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"createUser\":\"\", // 创建人 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"operatorName\":\"\", // 最后操作人 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"startTime\":, // 开始时间Date类型 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"endTime\":, // 结束时间Date类型 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"externalId\":, // 扩展id " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"rerunBatchNumber\":, // 批次号码 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; }" +
            " <br>" + "}")
    @PostMapping(value = "/searchPageList")
    public Map<String, Object> searchPageList(@RequestBody PageParam pageParam) {
        LOG.info("[TaskLogController-searchPageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
            TaskLogSearchPageEntity searchPageEntity = gson.fromJson(pageParam.getCondition(), TaskLogSearchPageEntity.class);
            if (searchPageEntity == null) searchPageEntity = new TaskLogSearchPageEntity();
            int pageNumber = pageParam.getPageNum();
            if (pageNumber < 1) {
                pageNumber = 1;
            }
            int pageSize = pageParam.getPageSize();
            if (pageSize < 10) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            // 查询数据
            int total = taskLogService.getAllTaskLogByPageParamCount(searchPageEntity);
            List<TbClockworkTaskLogPojo> taskLogPojos = taskLogService
                    .getAllTaskLogByPageParam(searchPageEntity, pageNumber, pageSize);

            if (CollectionUtils.isNotEmpty(taskLogPojos)) {
                PageInfo<TbClockworkTaskPojo> pageInfo = new PageInfo(taskLogPojos);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[TaskLogController-searchPageList]dateSize = {}, roleName = {}, total = {}, pageSize = {}, "
                                + "page number = {}, pages = {}",
                        taskLogPojos.size(), pageParam.getRole(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkTaskPojo> pageInfo = new PageInfo<>(new ArrayList<>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[TaskLogController-searchPageList]get taskSize = {}, roleName = {}", 0, pageParam.getRole());
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error("[TaskLogController-searchPageList] Error. msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取所有未结束的日志信息
     *
     * @return NotEndTaskLog List
     */
    @GetMapping(value = "/getAllNotEndTaskLog")
    public Map<String, Object> getAllNotEndTaskLogFlow() {
        try {
            return Response.success(taskLogService.getAllNotEndTaskLog());
        } catch (Exception e) {
            LOG.error("TaskLogController-getAllNotEndTaskLog Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取任务当前日志文件参数（最新任务日志文件对应的参数）
     *
     * @param taskId task primary key
     */
    @GetMapping(value = "/getLatestTaskLogFileParamByTaskId")
    public Map<String, Object> getLatestTaskLogFileParamByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                LOG.error("TaskLogController - getLatestTaskLogFileParamByTaskId, invalid taskId");
                return Response.fail("invalid taskId");
            }

            LogFileParam logFileParam = taskLogService.getLatestTaskLogFileParamByTaskId(taskId);
            if (logFileParam == null) {
                LOG.error("TaskLogController - getLatestTaskLogFileParamByTaskId, taskLog does not exist");
                return Response.fail("taskLog does not exist");
            }

            return Response.success(logFileParam);
        } catch (Exception e) {
            LOG.error("TaskLogController - getLatestTaskLogFileParamByTaskId, Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
