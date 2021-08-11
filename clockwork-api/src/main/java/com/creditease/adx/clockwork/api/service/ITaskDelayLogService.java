package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDelayLog;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:48 下午 2020/9/1
 * @ Description：
 * @ Modified By：
 */
public interface ITaskDelayLogService {

    boolean addTaskDelayLog(int taskId, String taskName, int delayStatus);

    boolean addTaskDelayLogBatch(List<TbClockworkTaskDelayLog> taskDelayLogList);

}
