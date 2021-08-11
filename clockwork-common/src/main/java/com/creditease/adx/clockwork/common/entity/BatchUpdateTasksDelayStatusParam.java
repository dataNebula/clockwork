package com.creditease.adx.clockwork.common.entity;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:08 下午 2020/11/2
 * @ Description：
 * @ Modified By：
 */
public class BatchUpdateTasksDelayStatusParam {

    private List<Integer> taskIds;
    private int delayStatus;

    @Override
    public String toString() {
        return "BatchUpdateTasksDelayStatusParam{" +
                "taskIds=" + taskIds +
                ", delayStatus=" + delayStatus +
                '}';
    }

    public List<Integer> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Integer> taskIds) {
        this.taskIds = taskIds;
    }

    public int getDelayStatus() {
        return delayStatus;
    }

    public void setDelayStatus(int delayStatus) {
        this.delayStatus = delayStatus;
    }
}
