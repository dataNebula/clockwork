package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeGroup;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodeGroupPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.web.service.INodeGroupService;
import com.creditease.adx.clockwork.web.service.IProblemTaskService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:22 下午 2020/12/28
 * @ Description：
 * @ Modified By：
 */
@RestController
@RequestMapping("/clockwork/web/problem/task")
public class ProblemTaskController {

    private static final Logger LOG = LoggerFactory.getLogger(ProblemTaskController.class);

    @Autowired
    private IProblemTaskService problemTaskService;

    /**
     * 查询任务中无效的dagId信息（dag信息早已经不存在）
     *
     * @return list task
     */
    @GetMapping(value = "/getTasksByInvalidDagId")
    public Map<String, Object> getTasksByInvalidDagId() {
        try {
            List<TbClockworkTaskPojo> taskInvalid = problemTaskService.getTasksByInvalidDagId();
            return Response.success(taskInvalid);
        } catch (Exception e) {
            LOG.error("ProblemTaskController-getTasksByInvalidDagId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }




}
