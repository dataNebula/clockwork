package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.impl.DagService;
import com.creditease.adx.clockwork.common.entity.Response;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:59 2020/9/20
 * @ Description：
 * @ Modified By：
 */
@Api("DAG")
@RestController
@RequestMapping(value = "/clockwork/api/dag")
public class DagController {

    private static final Logger LOG = LoggerFactory.getLogger(DagController.class);

    @Autowired
    private DagService dagService;

    /**
     * 初始化dagId（任务未设置dagId，可调用此接口设置，比如迁移数据后等情况）如果DagId存在则不会再做初始化
     * curl -X POST localhost:9005/clockwork/api/dag/init
     *
     * @return
     */
    @PostMapping(value = "/init")
    public Map<String, Object> initTaskDagId() {
        try {
            return Response.success(dagService.initTaskDagId());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取dag信息，以及更新dag信息（更新TaskCount、LeaderTaskId、LeaderTaskName、Description、UpdateTime）
     *
     * @param dagId dag id
     * @return
     */
    @GetMapping(value = "/refreshDagInfoById")
    public Map<String, Object> refreshDagInfoById(@RequestParam(value = "dagId") Integer dagId) {
        try {
            if (dagId == null || dagId < 1) {
                return Response.fail("invalid dagId " + dagId);
            }
            return Response.success(dagService.refreshDagInfoById(dagId));
        } catch (Exception e) {
            LOG.error("DagController-refreshDagInfoById Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新DAG taskCount信息
     * curl localhost:9005/clockwork/api/dag/refreshDagCount
     *
     * @return count
     */
    @GetMapping(value = "/refreshDagCount")
    public Map<String, Object> refreshDagCount() {
        try {
            return Response.success(dagService.updateDagCount());
        } catch (Exception e) {
            LOG.error("DagController-updateDagCount Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 清除空的DAG信息（没有task引用）
     * curl localhost:9005/clockwork/api/dag/cleanEmptyDagInfo
     *
     * @return 被清空的DagIds列表
     */
    @GetMapping(value = "/cleanEmptyDagInfo")
    public Map<String, Object> cleanEmptyDagInfo() {
        try {
            return Response.success(dagService.cleanEmptyDagInfo());
        } catch (Exception e) {
            LOG.error("DagController-cleanEmptyDagInfo Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 构建任务dagId
     * curl -X POST localhost:9005/clockwork/api/dag/buildDagIdForTaskId -d 'taskId=5925'
     *
     * @return
     */
    @PostMapping(value = "/buildDagIdForTaskId")
    public Map<String, Object> buildDagIdForTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                return Response.fail("invalid taskId " + taskId);
            }
            return Response.success(dagService.buildDagIdForTaskId(taskId));
        } catch (Exception e) {
            LOG.error("DagController-buildDagIdForTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
