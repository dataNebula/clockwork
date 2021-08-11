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

import io.swagger.annotations.Api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.api.service.ITaskRerunService;
import com.creditease.adx.clockwork.api.service.IUserService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRerun4PagePojo;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:49 2019-11-07
 * @ Description：
 * @ Modified By：
 */
@RestController
@Api(value = "重启相关接口")
@RequestMapping("/clockwork/api/task/rerun")
public class TaskRerunController {

    private final Logger LOG = LoggerFactory.getLogger(TaskRerunController.class);

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();

    @Autowired
    private ITaskRerunService taskRerunService;

    @Resource(name = "userService")
    private IUserService userService;

    /**
     * 查询当前taskId的子任务, 不包含自己
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/getTaskRerunChild")
    public Map<String, Object> getTaskRerunChild(
            @RequestParam(value = "taskId") Integer taskId, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber) {
        if (taskId == null || rerunBatchNumber == null) {
            return Response.fail("taskId or rerunBatchNumber is null,please check it.");
        }
        try {
            return Response.success(taskRerunService.getTaskRerunChild(taskId, rerunBatchNumber));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询当前taskId的父任务, 不包含自己
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/getTaskRerunFather")
    public Map<String, Object> getTaskRerunFather(
            @RequestParam(value = "taskId") Integer taskId, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber) {
        if (taskId == null || taskId < 1) {
            return Response.fail("taskId is invalid,please check it.");
        }
        try {
            return Response.success(taskRerunService.getTaskRerunFather(taskId, rerunBatchNumber));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取所有父任务Id
     * @param taskId
     * @param rerunBatchNumber
     * @return
     */
    @GetMapping(value = "/getTaskRerunFatherIds")
    public Map<String, Object> getTaskRerunFatherIds(
            @RequestParam(value = "taskId") Integer taskId, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber) {
        if (taskId == null || taskId < 1) {
            return Response.fail("taskId is invalid,please check it.");
        }
        try {
            return Response.success(taskRerunService.getTaskRerunFatherIds(taskId, rerunBatchNumber));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * getTaskRerunByTaskId
     *
     * @param id
     * @param rerunBatchNumber
     */
    @GetMapping(value = "/getTaskRerunByTaskId")
    public Map<String, Object> getTaskRerunByTaskId(
            @RequestParam(value = "id") Integer id, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber) {
        if (id == null  || rerunBatchNumber == null) {
            LOG.error("Parameter exception, task id or rerunBatchNumber is null");
            return Response.fail("task id or rerunBatchNumber is null");
        }
        try {
            LOG.info("[TaskRerunController]getTaskRerunByTaskIds, rerunBatchNumber = {}, id = {}", rerunBatchNumber, id);
            return Response.success(taskRerunService.getTaskRerunByTaskId(id, rerunBatchNumber));
        } catch (Exception e) {
            LOG.error("[TaskRerunController]getTaskRerunByTaskIds msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * getTaskRerunByTaskIds
     *
     * @param ids
     * @param rerunBatchNumber
     */
    @GetMapping(value = "/getTaskRerunByTaskIds")
    public Map<String, Object> getTaskRerunByTaskIds(
            @RequestParam(value = "ids") List<Integer> ids, @RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber) {
        if (CollectionUtils.isEmpty(ids) || rerunBatchNumber == null) {
            LOG.error("Parameter exception, task ids or rerunBatchNumber is null");
            return Response.fail("task ids or rerunBatchNumber is null");
        }
        try {
            LOG.info("[TaskRerunController]getTaskRerunByTaskIds, rerunBatchNumber = {}, ids = {}, ids.size = {}",
                    rerunBatchNumber, ids, ids.size());
            return Response.success(taskRerunService.getTaskRerunByTaskIds(ids, rerunBatchNumber));
        } catch (Exception e) {
            LOG.error("[TaskRerunController]getTaskRerunByTaskIds msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询该批次的root节点
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @GetMapping(value = "/getTaskRerunRootTaskIds")
    public Map<String, Object> getTaskRerunRootTaskIds(@RequestParam(value = "rerunBatchNumber") Long rerunBatchNumber) {
        if (rerunBatchNumber == null) {
            return Response.fail("rerunBatchNumber is null,please check it.");
        }
        try {
            return Response.success(taskRerunService.getTaskRerunRootTaskIds(rerunBatchNumber));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 分页查询
     *
     * @param pageParam
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes"})
	@PostMapping(value = "/searchTaskRerunPageList")
    public Map<String, Object> searchTaskRerunPageList(@RequestBody PageParam pageParam) {
        TbClockworkTaskRerun4PagePojo taskRerun;
        List<TbClockworkTaskRerun4PagePojo> taskReruns = null;
        int total = 0;
        LOG.info("[WebTaskRerunController-searchTaskRerunPageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
            taskRerun = gson.fromJson(pageParam.getCondition(), TbClockworkTaskRerun4PagePojo.class);
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
            total = taskRerunService.getAllTaskRerunByPageParamCount(taskRerun);
            taskReruns = taskRerunService.getAllTaskRerunByPageParam(taskRerun, pageNumber, pageSize);

            if (CollectionUtils.isNotEmpty(taskReruns)) {
                PageInfo<TbClockworkTaskRerun4PagePojo> pageInfo = new PageInfo(taskReruns);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[WebTaskRerunController-searchTaskRerunPageList]" +
                                "dateSize = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        taskReruns.size(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkTaskRerun4PagePojo> pageInfo = new PageInfo(new ArrayList<TbClockworkTaskRerun4PagePojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[WebTaskRerunController-searchTaskRerunPageList]get taskSize = {}", 0 );
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
