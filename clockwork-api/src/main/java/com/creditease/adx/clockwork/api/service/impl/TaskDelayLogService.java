package com.creditease.adx.clockwork.api.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.api.service.ITaskDelayLogService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDelayLog;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskDelayLogBatchMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskDelayLogMapper;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:37 下午 2020/9/1
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskDelayService")
public class TaskDelayLogService implements ITaskDelayLogService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskDelayLogService.class);

    @Autowired
    private TbClockworkTaskDelayLogMapper tbClockworkTaskDelayLogMapper;

    @Autowired
    private TaskDelayLogBatchMapper taskDelayLogBatchMapper;

    @Override
    public boolean addTaskDelayLog(int taskId, String taskName, int delayStatus) {
        TbClockworkTaskDelayLog record = new TbClockworkTaskDelayLog();
        record.setTaskId(taskId);
        record.setTaskName(taskName);
        record.setDelayStatus(delayStatus);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        return tbClockworkTaskDelayLogMapper.insert(record) > 0;
    }

    /**
     * 批量添加
     *
     * @param taskDelayLogList
     * @return
     */
    @Override
    public boolean addTaskDelayLogBatch(List<TbClockworkTaskDelayLog> taskDelayLogList) {
        List<TbClockworkTaskDelayLog> list = PojoUtil.convertList(taskDelayLogList, TbClockworkTaskDelayLog.class);
        int size = taskDelayLogBatchMapper.addTaskDelayLogBatch(list);
        LOG.info("[TaskLogService-addTaskLogBatch]add finished, list.size = {}", size);
        return true;
    }

}
