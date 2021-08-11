package com.creditease.adx.clockwork.master.service.impl;

import com.creditease.adx.clockwork.client.service.TaskDelayLogClientService;
import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.master.service.ITaskDelayService;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:26 下午 2020/9/15
 * @ Description：
 * @ Modified By：
 */
@Service
public class TaskDelayService implements ITaskDelayService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskDelayService.class);

    @Autowired
    private TaskDelayLogClientService taskDelayLogClientService;

    @Autowired
    private TaskOperationClientService taskOperationClientService;

    /**
     * 更新任务延迟状态
     * 记录延迟状态日志
     *
     * @param delayTasks  延迟任务
     * @param delayStatus 延迟状态
     */
    public void handleTasksDelayStatusAndRecordDelayLog(List<TbClockworkTaskPojo> delayTasks, Integer delayStatus) {
        if (CollectionUtils.isEmpty(delayTasks)|| delayStatus == null) {
            return;
        }
        // 更新任务延迟状态
        // 记录延迟状态日志
        List<Integer> taskIds = delayTasks.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList());
        LOG.info("[TaskDelayService]handleTasksDelayStatusAndRecordDelayLog taskIds = {}, delayStatus = {}",
                taskIds, delayStatus);
        taskOperationClientService.updateTasksDelayStatusBatch(taskIds, delayStatus);
        taskDelayLogClientService.addTaskDelayLogBatch(delayTasks, delayStatus);
    }


}
