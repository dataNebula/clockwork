package com.creditease.adx.clockwork.master.service;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:26 下午 2020/9/15
 * @ Description：
 * @ Modified By：
 */
public interface ITaskDelayService {

    /**
     * 更新任务延迟状态
     * 记录延迟状态日志
     *
     * @param delayTasks  延迟任务
     * @param delayStatus 延迟状态
     */
    void handleTasksDelayStatusAndRecordDelayLog(List<TbClockworkTaskPojo> delayTasks, Integer delayStatus);
}
