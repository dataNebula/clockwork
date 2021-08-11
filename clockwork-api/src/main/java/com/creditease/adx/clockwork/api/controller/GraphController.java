package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.IGraphService;
import com.creditease.adx.clockwork.common.entity.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午9:12 2020/12/6
 * @ Description：
 * @ Modified By：
 */
@RestController
@RequestMapping(value = "/clockwork/api/graph")
public class GraphController {

    private static final Logger LOG = LoggerFactory.getLogger(DagController.class);

    @Autowired
    private IGraphService graphService;


    /**
     * 获取整个dag图的所有关系
     *
     * @param taskId 该图的某一个taskId
     * @return all task relations
     */
    @GetMapping(value = "/getGraphAllRelationByTaskId")
    public Map<String, Object> getGraphAllRelationByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                LOG.error("Get-GraphAllRelationByTaskId failure,task id is null");
                return Response.fail("task id is null");
            }
            return Response.success(graphService.getGraphAllRelationByTaskId(taskId));
        } catch (Exception e) {
            LOG.error("getGraphAllTaskIdsByTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取整个dag图的所有TaskIds
     *
     * @param taskId 该图的某一个taskId
     * @return all task ids
     */
    @GetMapping(value = "/getGraphAllTaskIdsByTaskId")
    public Map<String, Object> getGraphAllTaskIdsByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                LOG.error("Get-GraphAllTaskIdsByTaskId failure,task id is null");
                return Response.fail("task id is null");
            }
            return Response.success(graphService.getGraphAllTaskIdsByTaskId(taskId));
        } catch (Exception e) {
            LOG.error("getGraphAllTaskIdsByTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 获取整个dag图的所有任务
     *
     * @param taskId 该图的某一个taskId
     * @return all task
     */
    @GetMapping(value = "/getGraphAllTasksByTaskId")
    public Map<String, Object> getGraphAllTasksByTaskId(@RequestParam(value = "taskId") Integer taskId) {
        try {
            if (taskId == null || taskId < 1) {
                LOG.error("Get-GraphAllTasksByTaskId failure,task id is null");
                return Response.fail("task id is null");
            }
            return Response.success(graphService.getGraphAllTasksByTaskId(taskId));
        } catch (Exception e) {
            LOG.error("getGraphAllTasksByTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
