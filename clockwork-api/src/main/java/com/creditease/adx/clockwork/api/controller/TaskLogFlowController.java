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

import com.creditease.adx.clockwork.api.service.ITaskLogFlowService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogFlowPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:56 2019-10-14
 * @ Description：
 * @ Modified By：
 */
@Api("TaskLogFlow 任务生命周期记录")
@RestController
@RequestMapping("/clockwork/api/task/log/flow")
public class TaskLogFlowController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogFlowController.class);

    @Resource
    private ITaskLogFlowService taskLogFlowService;

    /**
     * 添加到kafka队列
     *
     * @param logFlowPojoList
     * @return
     */
    @PostMapping(value = "/addToKafkaQueue")
    public Map<String, Object> addToKafkaQueue(@RequestBody List<TbClockworkTaskLogFlowPojo> logFlowPojoList) {
        try {
            if (CollectionUtils.isEmpty(logFlowPojoList)) {
                return Response.fail("Param error.");
            }
            // 添加到kafka队列
            if (taskLogFlowService.addToKafkaQueue(logFlowPojoList)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("TaskLogFlowController-addToKafkaQueue Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 添加生命周期记录
     *
     * @param logFlowPojo
     * @return
     */
    @PostMapping(value = "/addTaskLogFlow")
    public Map<String, Object> addTaskLogFlow(@RequestBody TbClockworkTaskLogFlowPojo logFlowPojo) {
        try {
            if (taskLogFlowService.addTaskLogFlow(logFlowPojo)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("TaskLogFlowController-addTaskLogFlow Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 批量添加生命周期记录
     *
     * @param logFlowPojoList
     * @return
     */
    @PostMapping(value = "/addBatchTaskLogFlow")
    public Map<String, Object> addBatchTaskLogFlow(@RequestBody List<TbClockworkTaskLogFlowPojo> logFlowPojoList) {
        try {
            if (CollectionUtils.isEmpty(logFlowPojoList)) {
                return Response.success(true);
            }
            if (taskLogFlowService.addBatchTaskLogFlow(logFlowPojoList)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("TaskLogFlowController-addBatchTaskLogFlow Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取所有未结束的生命周期日志信息
     *
     * @return NotEndTaskLogFlow List
     */
    @GetMapping(value = "/getAllNotEndTaskLogFlow")
    public Map<String, Object> getAllNotEndTaskLogFlow() {
        try {
            return Response.success(taskLogFlowService.getAllNotEndTaskLogFlow());
        } catch (Exception e) {
            LOG.error("TaskLogFlowController-getAllNotEndTaskLogFlow Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 通过groupId，获取时间范围内的生命周期信息.瀑布图
     *
     * @param groupId   task流ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @GetMapping(value = "/getTaskLogFlowInfo")
    public Map<String, Object> getTaskLogFlowInfo(
            @RequestParam(value = "groupId", required = true) Integer groupId,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        try {
            if (groupId == null) {
                return Response.fail("参数异常：groupId is null.");
            }

            // 默认获取前后三天的信息
            Date start = null;
            Date end = null;
            try {
                if (StringUtils.isBlank(startTime) && StringUtils.isBlank(endTime)) {
                    end = DateUtil.getNowTimeStampDate();
                    start = DateUtil.getOffsetDateTime(end, -3);
                } else if (StringUtils.isBlank(startTime)) {
                    end = DateUtil.parse(endTime);
                    start = DateUtil.getOffsetDateTime(end, -3);
                } else if (StringUtils.isBlank(endTime)) {
                    start = DateUtil.parse(startTime);
                    end = DateUtil.getOffsetDateTime(start, 3);
                } else {
                    start = DateUtil.parse(startTime);
                    end = DateUtil.parse(endTime);
                }
            } catch (Exception e) {
                return Response.fail("时间格式异常：" + e.getMessage());
            }

            String result = taskLogFlowService.getTaskLogFlowInfo(groupId, start, end);
            if (result != null) {
                return Response.success(result);
            }
            return Response.fail(result);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 通过groupId，获取时间范围内的生命周期信息.列表
     *
     * @param groupId   task流ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @GetMapping(value = "getTaskLogFlowList")
    public Map<String, Object> getTaskLogFlowList(
            @RequestParam(value = "groupId", required = true) Integer groupId,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        try {
            if (groupId == null) {
                return Response.fail("参数异常：groupId is null.");
            }
 
            // 默认获取前后三天的信息
            Date start = null;
            Date end = null;
            try {
                if (StringUtils.isBlank(startTime) && StringUtils.isBlank(endTime)) {
                    end = DateUtil.getNowTimeStampDate();
                    start = DateUtil.getOffsetDateTime(end, -3);
                } else if (StringUtils.isBlank(startTime)) {
                    end = DateUtil.parse(endTime);
                    start = DateUtil.getOffsetDateTime(end, -3);
                } else if (StringUtils.isBlank(endTime)) {
                    start = DateUtil.parse(startTime);
                    end = DateUtil.getOffsetDateTime(start, 3);
                } else {
                    start = DateUtil.parse(startTime);
                    end = DateUtil.parse(endTime);
                }
            } catch (Exception e) {
                return Response.fail("时间格式异常：" + e.getMessage());
            }

            String result = taskLogFlowService.getTaskLogFlowList(groupId, start, end);
            if (result != null) {
                return Response.success(result);
            }
            return Response.fail(result);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }
}
