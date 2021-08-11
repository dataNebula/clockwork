package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.TaskDelayLogClient;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDelayLog;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:25 下午 2020/9/1
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskDelayLogClientService")
public class TaskDelayLogClientService {

    private final Logger LOG = LoggerFactory.getLogger(TaskDelayLogClientService.class);

    @Autowired
    private TaskDelayLogClient taskDelayLogClient;

    public boolean addTaskDelayLog(int taskId, String taskName, int delayStatus) {
        try {
            Map<String, Object> interfaceResult  = taskDelayLogClient.addTaskDelayLog(taskId, taskName, delayStatus);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("addTaskDelayLog Error {}.", e.getMessage(), e);
        }
        return true;
    }

    public boolean addTaskDelayLogBatch(List<TbClockworkTaskPojo> tasks, int delayStatus) {
        try {
            List<TbClockworkTaskDelayLog> taskDelayLogList = new ArrayList<>();
            TbClockworkTaskDelayLog taskDelayLog;
            Date date = new Date();
            for (TbClockworkTaskPojo task : tasks) {
                taskDelayLog = new TbClockworkTaskDelayLog();
                taskDelayLog.setTaskId(task.getId());
                taskDelayLog.setTaskName(task.getName());
                taskDelayLog.setDelayStatus(delayStatus);
                taskDelayLog.setCreateTime(date);

                taskDelayLogList.add(taskDelayLog);
            }
            Map<String, Object> interfaceResult = taskDelayLogClient.addTaskDelayLogBatch(taskDelayLogList);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("addTaskDelayLog Error {}.", e.getMessage(), e);
        }
        return true;
    }


}
