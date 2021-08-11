package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.impl.DagCheckService;
import com.creditease.adx.clockwork.common.entity.Response;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName: DagCheckController
 * @Author: ltb
 * @Date: 2021/3/11:10:24 上午
 * @Description: Dag成环检测
 */
@Api("DAGCheck")
@RestController
@RequestMapping(value = "/clockwork/api/dag/check")
public class DagCheckController {

    private static final Logger LOG = LoggerFactory.getLogger(DagCheckController.class);

    @Autowired
    private DagCheckService dagCheckService;
    /**
     * @Description 输入一个dagid，对这个dag进行成环检测　
     * @Param [dagId,userName]
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */
    @GetMapping(value = "checkDagById")
    Map<String, Object> checkDagById(@RequestParam(value = "dagId") Integer dagId,
                                     @RequestParam(value = "userName") String userName) {
        try {
            if (dagId == null || dagId < 1) {
                return Response.fail("invalid dagId " + dagId);
            }
            return Response.success(dagCheckService.checkDagById(dagId, userName));
        } catch (Exception e) {
            LOG.error("DagCheckController-checkDagById Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description
     * @Param [userName]
     */
    @GetMapping(value = "checkAllDags")
    Map<String, Object> checkAllDags(@RequestParam(value = "userName") String userName) {
        try {
            return Response.success(dagCheckService.checkAllDags(userName));
        } catch (Exception e) {
            LOG.error("DagCheckController-checkAllDags Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }
}
