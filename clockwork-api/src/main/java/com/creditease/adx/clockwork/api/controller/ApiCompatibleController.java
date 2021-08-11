package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.common.entity.Response;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:12 上午 2020/11/4
 * @ Description：
 * @ Modified By：
 */
@RestController
public class ApiCompatibleController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

    @Resource(name = "taskService")
    private ITaskService taskService;

    @ApiOperation("查询Task状态按中台表名")
    @RequestMapping(value = "/triangle/meta/job/getJobStatusByTableName", method = RequestMethod.GET)
    public Map<String, Object> getTaskStatusByTableName(
            @RequestParam(value = "businessInfo", required = false) String businessInfo) {
        try {
            return Response.success(taskService.getTaskStatusByTableName(businessInfo));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("查询Task状态按中台表名")
    @RequestMapping(value = "/triangle/meta/job/getJobStatusForTableCheck", method = RequestMethod.GET)
    public Map<String, Object> getJobStatusForTableCheck(
            @RequestParam(value = "businessInfo", required = false) String businessInfo) {
        try {
            return Response.success(taskService.getTaskStatusByTableName(businessInfo));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }



}
