package com.creditease.adx.clockwork.common.entity;

import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:57 下午 2020/11/2
 * @ Description：
 * @ Modified By：
 */
public class BatchGetTaskLogByTaskIdsParam {

    private List<Integer> taskIds;
    private List<String> statusList;
    private Date startTime;
    private Date endTime;

    @Override
    public String toString() {
        return "BatchGetTaskLogByTaskIdsParam{" +
                "taskIds=" + taskIds +
                ", statusList=" + statusList +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public List<Integer> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Integer> taskIds) {
        this.taskIds = taskIds;
    }

    public List<String> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<String> statusList) {
        this.statusList = statusList;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
