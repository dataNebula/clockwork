package com.creditease.adx.clockwork.api.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.api.service.ITaskDelayLogService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDelayLog;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:04 下午 2020/9/1
 * @ Description：
 * @ Modified By：
 */
@RestController
@RequestMapping("/clockwork/api/task/delayLog")
public class TaskDelayLogController {

    private final Logger LOG = LoggerFactory.getLogger(TaskDelayLogController.class);

    @Autowired
    private ITaskDelayLogService taskDelayLogService;

    /**
     * 添加任务延迟日志
     *
     * @param taskId      任务id
     * @param taskName    任务名
     * @param delayStatus 延迟状态
     * @return
     */
    @PostMapping(value = "/addTaskDelayLog")
    public Map<String, Object> addTaskDelayLog(
            @RequestParam(value = "taskId") Integer taskId,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "delayStatus") Integer delayStatus) {
        try {
            if (taskId == null || delayStatus == null) {
                return Response.fail("task id or delay status is null, please check it.");
            }
            return Response.success(taskDelayLogService.addTaskDelayLog(taskId, taskName, delayStatus));
        } catch (Exception e) {
            LOG.error("addTaskDelayLog Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 批量添加
     *
     * @param taskDelayLogList
     * @return
     */
    @PostMapping(value = "/addTaskDelayLogBatch")
    public Map<String, Object> addTaskDelayLogBatch(@RequestBody List<TbClockworkTaskDelayLog> taskDelayLogList) {
        LOG.info("addTaskDelayLogBatch, taskDelayLogList.size = {}", taskDelayLogList.size());
        try {
            return Response.success(taskDelayLogService.addTaskDelayLogBatch(taskDelayLogList));
        } catch (Exception e) {
            LOG.error("addTaskDelayLogBatch, Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
