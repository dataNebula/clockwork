package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.impl.LoopClockService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:26 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
@Api("环形时钟相关接口")
@RestController
@RequestMapping("/clockwork/api/loop/clock")
public class LoopClockController {

    private final Logger LOG = LoggerFactory.getLogger(LoopClockController.class);

    @Autowired
    private LoopClockService loopClockService;

    /**
     * 构建作业环形时钟信息（所有）
     * curl -X POST http://localhost:9005/clockwork/api/loop/clock/buildTaskLoopClock
     *
     * @return
     */
    @ApiOperation("构建环形时钟信息（所有）")
    @PostMapping(value = "/buildTaskLoopClock")
    public Map<String, Object> buildTaskLoopClock() {
        try {
            boolean buildTaskLoopClockResult = loopClockService.buildTaskLoopClock();
            if (buildTaskLoopClockResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("LoopClockController-buildTaskLoopClock, Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 构建单个作业环形时钟信息（单个）
     * curl -X POST http://localhost:9005/clockwork/api/loop/clock/addTaskToLoopClockSlot -d 'nextMatchingTimeNumber=1&taskId=12345'
     *
     * @param taskId                 任务Id
     * @param nextMatchingTimeNumber 填充几次槽位（默认可以为1）
     * @return
     */
    @ApiOperation("构建环形时钟信息（单个任务）")
    @PostMapping(value = "/addTaskToLoopClockSlot")
    public Map<String, Object> addTaskToLoopClockSlot(@RequestParam(value = "taskId") Integer taskId,
                                                      @RequestParam(value = "nextMatchingTimeNumber") Integer nextMatchingTimeNumber) {
        try {
            boolean buildTaskLoopClockResult = loopClockService.addTaskToLoopClockSlot(taskId, nextMatchingTimeNumber);
            if (buildTaskLoopClockResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("LoopClockController-addTaskToLoopClockSlot, Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 构建多个作业环形时钟信息（批量）
     *
     * @param taskPojoList taskList
     * @return boolean
     */
    @ApiOperation("Worker服务调用此接口，更新作业下一次执行槽位和前端界面显示的触发时间信息，此接口采用批量方式")
    @PostMapping(value = "/addTaskToLoopClockSlotByBatch")
    public Map<String, Object> addTaskToLoopClockSlotByBatch(@RequestBody List<TbClockworkTaskPojo> taskPojoList) {
        try {
            boolean buildTaskLoopClockResult = loopClockService.addTaskToLoopClockSlotByBatch(taskPojoList);
            if (buildTaskLoopClockResult) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error("LoopClockController-addTaskToLoopClockSlotByBatch, Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询指定分钟应该执行的所有任务
     *
     * @param slotPosition（从0开始）
     * @return
     */
    @ApiOperation("获取指定分钟应该运行的任务")
    @GetMapping(value = "/getHasCronTaskFromSlot")
    public Map<String, Object> getHasCronTaskFromSlot(@RequestParam(value = "slotPosition") Integer slotPosition) {
        try {
            return Response.success(loopClockService.getHasCronTaskFromSlot(slotPosition));
        } catch (Exception e) {
            LOG.error("LoopClockController-getHasCronTaskFromSlot, Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
