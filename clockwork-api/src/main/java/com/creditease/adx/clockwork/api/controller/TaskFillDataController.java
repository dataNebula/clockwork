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

import com.creditease.adx.clockwork.api.service.ITaskFillDataService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.TaskFillDataSearchPageEntity;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:44 2019-12-26
 * @ Description：补数相关逻辑
 * @ Modified By：
 */
@Api("补数相关逻辑")
@RestController
@RequestMapping("/clockwork/api/task/fillData")
public class TaskFillDataController {

    private final Logger LOG = LoggerFactory.getLogger(TaskFillDataController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private ITaskFillDataService taskFillDataService;


    /**
     * 获取task列表
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @ApiOperation(value = "获取补数任务", notes = "通过批次号获取该批次的补数任务信息，不考虑分页")
    @GetMapping(value = "/getTasksByReRunBatchNumber")
    public Map<String, Object> getTasksByReRunBatchNumber(@RequestParam("rerunBatchNumber") String rerunBatchNumber) {
        try {
            if (StringUtils.isBlank(rerunBatchNumber)) {
                throw new RuntimeException("parameter rerunBatchNumber is null.");
            }
            LOG.info("[TaskFillDataController-getTasksByReRunBatchNumber]param, rerunBatchNumber = {}", rerunBatchNumber);
            List<TbClockworkTask4PagePojo> tasks =
                    taskFillDataService.getTasksByReRunBatchNumber(Long.valueOf(rerunBatchNumber));
            LOG.info("[TaskFillDataController-getTasksByReRunBatchNumber]tasks.size={}", tasks.size());
            return Response.success(tasks);
        } catch (Exception e) {
            LOG.error("[TaskFillDataController-getTasksByReRunBatchNumber]Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取日志信息 - 通过批次号
     *
     * @param rerunBatchNumber 批次号
     * @param taskId           任务ID
     * @return
     */
    @ApiOperation(value = "获取补数任务日志列表", notes = "通过批次号和任务ID获取该批次的补数任务日志信息，不考虑分页")
    @GetMapping(value = "/getTaskLogsByReRunBNAndTaskId")
    public Map<String, Object> getTaskLogsByReRunBNAndTaskId(@RequestParam(value = "rerunBatchNumber") String rerunBatchNumber,
                                                             @RequestParam("taskId") Integer taskId) {
        try {
            if (rerunBatchNumber == null || taskId == null) {
                throw new RuntimeException("parameter rerunBatchNumber is null.");
            }
            LOG.info("[TaskFillDataController-getTaskLogsByReRunBNAndTaskId]param, " +
                            "rerunBatchNumber = {}, taskId = {}", rerunBatchNumber, taskId);

            List<TbClockworkTaskLogPojo> tasks =
                    taskFillDataService.getTaskLogsByReRunBNAndTaskId(Long.valueOf(rerunBatchNumber), taskId);
            LOG.info("[TaskFillDataController-getTaskLogsByReRunBNAndTaskId]tasks.size={}", tasks.size());

            return Response.success(tasks);
        } catch (Exception e) {
            LOG.error("[TaskFillDataController-getTaskLogsByReRunBNAndTaskId]Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取对象
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @GetMapping(value = "/getTaskFillDataByRerunBatchNumber")
    public Map<String, Object> getTaskFillDataByRerunBatchNumber(
            @RequestParam(value = "rerunBatchNumber") String rerunBatchNumber) {
        try {
            if (StringUtils.isBlank(rerunBatchNumber)) {
                throw new RuntimeException("parameter rerunBatchNumber is null.");
            }
            LOG.info("[TaskFillDataController-getTaskFillDataByRerunBatchNumber]param, rerunBatchNumber = {}", rerunBatchNumber);
            return Response.success(taskFillDataService.getTaskFillDataByRerunBatchNumber(Long.valueOf(rerunBatchNumber)));
        } catch (Exception e) {
            LOG.error("[TaskFillDataController-getTaskFillDataByRerunBatchNumber]Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改TbClockworkTaskLog
     *
     * @param tbClockworkTaskFillDataPojo json string
     */
    @PostMapping(value = "/updateTaskFillDataByRerunBatchNumber")
    public Map<String, Object> updateTaskFillDataByRerunBatchNumber(
            @RequestBody TbClockworkTaskFillDataPojo tbClockworkTaskFillDataPojo) {
        try {
            return Response.success(taskFillDataService.updateTaskFillDataByRerunBatchNumber(tbClockworkTaskFillDataPojo));
        } catch (Exception e) {
            LOG.error("updateTaskFillDataByRerunBatchNumber Error. Msg: {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新成功的任务数
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @PostMapping(value = "/updateTaskFillDataSuccessCount")
    public Map<String, Object> updateTaskFillDataSuccessCount(
            @RequestParam(value = "rerunBatchNumber") String rerunBatchNumber) {
        try {
            if (rerunBatchNumber == null) {
                throw new RuntimeException("parameter rerunBatchNumber is null.");
            }
            int count = taskFillDataService.updateTaskFillDataSuccessCount(Long.valueOf(rerunBatchNumber));
            LOG.info("[TaskFillDataController-updateTaskFillDataSuccessCount] rerunBatchNumber = {}, count = {}",
                    rerunBatchNumber, count);
            return Response.success(count);
        } catch (Exception e) {
            LOG.error("[TaskFillDataController-updateTaskFillDataSuccessCount]Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新CurrFillDataTime
     *
     * @param rerunBatchNumber     批次号
     * @param CurrFillDataTime     当前周期补数时间
     * @param CurrFillDataTimeSort 当前补数时间序号
     * @return
     */
    @PostMapping(value = "/updateTaskFillDataCurrFillDataTime")
    public Map<String, Object> updateTaskFillDataCurrFillDataTime(@RequestParam(value = "rerunBatchNumber") String rerunBatchNumber,
                                                                  @RequestParam(value = "CurrFillDataTime") String CurrFillDataTime,
                                                                  @RequestParam(value = "CurrFillDataTimeSort") Integer CurrFillDataTimeSort) {
        try {
            if (rerunBatchNumber == null || CurrFillDataTime == null || CurrFillDataTimeSort == null) {
                throw new RuntimeException("parameter rerunBatchNumber or CurrFillDataTime or CurrFillDataTimeSort is null.");
            }
            int count = taskFillDataService.updateTaskFillDataCurrFillDataTime(
                    Long.valueOf(rerunBatchNumber), CurrFillDataTime, CurrFillDataTimeSort);
            LOG.info("[TaskFillDataController-updateTaskFillDataCurrFillDataTime] " +
                            "rerunBatchNumber = {}, CurrFillDataTime = {}, CurrFillDataTimeSort = {}, count = {}",
                    rerunBatchNumber, CurrFillDataTime, CurrFillDataTimeSort, count);
            return Response.success(count);
        } catch (Exception e) {
            LOG.error("[TaskFillDataController-updateTaskFillDataCurrFillDataTime]Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新补数 isRan
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @PostMapping(value = "/updateTaskFillDataIsRan")
    public Map<String, Object> updateTaskFillDataIsRan(@RequestParam(value = "rerunBatchNumber") String rerunBatchNumber) {
        try {
            if (StringUtils.isBlank(rerunBatchNumber)) {
                throw new RuntimeException("parameter rerunBatchNumber is null.");
            }
            boolean result = taskFillDataService.updateTaskFillDataIsRan(Long.valueOf(rerunBatchNumber));
            LOG.info("[TaskFillDataController-updateTaskFillDataIsRan] rerunBatchNumber = {}, result = {}",
                    rerunBatchNumber, result);
            return Response.success(result);
        } catch (Exception e) {
            LOG.error("[TaskFillDataController-updateTaskFillDataIsRan]Error. Msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 查询接口
     *
     * @param pageParam 查询参数
     */
    @ApiOperation(value = "补数列表信息", notes = "请求参数如下, 查询条件为condition Json对象：" +
            " <br>" + "{" +
            " <br>" + "  \"pageNum\": 1, // 第一页" +
            " <br>" + "  \"pageSize\": 10, // 每页10条" +
            " <br>" + "  \"role\": \"\", // 可为空" +
            " <br>" + "  \"userName\": \"\", // 可为空" +
            " <br>" + "  \"condition\"：{" +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskIds\":[1,2,3], // 任务ids，List<Integer> " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskGroupId\":123, // 任务组id " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"fillDataType\":\"day\", // 补数类型 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"fillDataTime\":\"\", // 补数时间 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskGroupName\":\"\", // 任务组名 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"taskGroupAliasName\":\"\", // 任务组别名 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"description\": \"\", // 描述 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"createUser\":\"\", // 创建人 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"operatorName\":\"\", // 最后操作人 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"createTimeStart\": \"yyyy-MM-dd HH:mm:ss\", // 开始时间" +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"createTimeEnd\": \"yyyy-MM-dd HH:mm:ss\", // 结束时间 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"externalId\":, // 扩展id " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; \"rerunBatchNumber\":, // 批次号码 " +
            " <br>" + "&nbsp;&nbsp;&nbsp;&nbsp; }" +
            " <br>" + "}")
    @PostMapping(value = "/searchPageList")
    public Map<String, Object> searchPageList(@RequestBody PageParam pageParam) {
        try {
            LOG.info("[TaskFillDataController-searchPageList]pageParam = {}", pageParam);
            if (pageParam == null) {
                throw new RuntimeException("Page param is null, please check it!");
            }
            TaskFillDataSearchPageEntity searchPageEntity = gson.fromJson(pageParam.getCondition(), TaskFillDataSearchPageEntity.class);
            if (searchPageEntity == null) searchPageEntity = new TaskFillDataSearchPageEntity();
            int pageNumber = pageParam.getPageNum();
            if (pageNumber < 1) pageNumber = 1;
            int pageSize = pageParam.getPageSize();
            if (pageSize < 10) pageSize = 10;
            if (pageSize > 100) pageSize = 100;
            // 查询数据
            int total = taskFillDataService.getAllTaskFillDataByPageParamCount(searchPageEntity);
            List<TbClockworkTaskFillDataPojo> taskFillDataPojos = taskFillDataService.getAllTaskFillDataByPageParam(searchPageEntity, pageNumber, pageSize);
            if (CollectionUtils.isNotEmpty(taskFillDataPojos)) {
                PageInfo<TbClockworkTaskFillDataPojo> pageInfo = new PageInfo<TbClockworkTaskFillDataPojo>(taskFillDataPojos);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[TaskFillDataController-searchPageList]" +
                                "dateSize = {}, roleName = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        taskFillDataPojos.size(), pageParam.getRole(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkTaskFillDataPojo> pageInfo = new PageInfo<TbClockworkTaskFillDataPojo>(new ArrayList<>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[TaskFillDataController-searchPageList]get taskSize = {}, roleName = {}",
                        0, pageParam.getRole()
                );
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
