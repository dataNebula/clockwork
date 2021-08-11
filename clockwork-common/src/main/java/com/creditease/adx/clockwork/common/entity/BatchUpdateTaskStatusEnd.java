package com.creditease.adx.clockwork.common.entity;

import com.creditease.adx.clockwork.common.enums.TaskStatus;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:39 下午 2020/12/1
 * @ Description：
 * @ Modified By：
 */
public class BatchUpdateTaskStatusEnd {

    private List<Integer> taskIds;

    private String status ;

    private boolean isStarted;

    public List<Integer> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Integer> taskIds) {
        this.taskIds = taskIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public BatchUpdateTaskStatusEnd(List<Integer> taskIds, String status, boolean isStarted) {
        this.taskIds = taskIds;
        this.status = status;
        this.isStarted = isStarted;
    }
}
