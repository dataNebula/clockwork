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

package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.web.service.IGraphService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
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
 * @ Date       ：Created in 11:12 2019-10-17
 * @ Description：
 * @ Modified By：
 */
@Api("图相关接口")
@RestController
@RequestMapping("/clockwork/web/graph")
public class GraphController {

    private static final Logger LOG = LoggerFactory.getLogger(GraphController.class);

    @Autowired
    private IGraphService graphService;

    /**
     * 构建辐射DAG图，通过dagId
     *
     * @param dagId dagId
     * @return Graph
     */
    @GetMapping(value = "/buildDagGraphForRadialByDagId")
    public Map<String, Object> buildDagGraphForRadialByDagId(@RequestParam(value = "dagId") Integer dagId) {
        if (dagId == null || dagId < 1) {
            LOG.error("GraphController-buildDagGraphForRadialByDagId, dagId not be empty!");
            return Response.fail("dagId not be empty!");
        }
        try {
            return Response.success(graphService.buildDagGraphForRadialByDagId(dagId));
        } catch (Exception e) {
            LOG.error("GraphController-buildDagGraphForRadialByDagId Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取附带位置的DAG图, 通过DagId
     *
     * @param dagId dagId
     * @return Graph
     */
    @GetMapping(value = "/buildDagGraphForPositionByDagId")
    public Map<String, Object> buildDagGraphForPositionByDagId(@RequestParam(value = "dagId") Integer dagId) {
        if (dagId == null || dagId < 1) {
            LOG.error("GraphController-buildDagGraphForPositionByDagId, dagId not be empty!");
            return Response.fail("dagId not be empty!");
        }
        try {
            return Response.success(graphService.buildDagGraphForPositionByDagId(dagId));
        } catch (Exception e) {
            LOG.error("GraphController-buildDagGraphForPositionByDagId Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 根据查询条件，获得task依赖关系dag图
     *
     * @param taskId        task id
     * @param taskName      taskName
     * @param upDeepLevel   向上查找多少级（showDag==false）
     * @param downDeepLevel 向下查找多少级（showDag==false）
     * @param userName      用户名
     * @param showDag       是否展示整个dag图
     * @return
     */
    @ApiOperation("根据查询task name 获得task依赖关系dag图")
    @GetMapping(value = "/getTaskDagGraph")
    public Map<String, Object> getTaskDagGraph(@RequestParam(value = "taskId", required = false) Integer taskId,
                                               @RequestParam(value = "taskName", required = false) String taskName,
                                               @RequestParam(value = "upDeepLevel", required = false) Integer upDeepLevel,
                                               @RequestParam(value = "downDeepLevel", required = false) Integer downDeepLevel,
                                               @RequestParam(value = "userName") String userName,
                                               @RequestParam(value = "showDag") Boolean showDag) {
        if (StringUtils.isBlank(taskName) && (taskId == null || taskId < 1)) {
            LOG.error("GraphController-getTaskDagGraph, taskId and taskName must not be empty at the same time!");
            return Response.fail("taskId and taskName must not be empty at the same time!");
        }
        try {
            return Response.success(graphService.getTaskDagGraph(
                    taskId, taskName, userName, upDeepLevel, downDeepLevel, showDag));
        } catch (Exception e) {
            LOG.error("GraphController-getTaskDagGraph Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
