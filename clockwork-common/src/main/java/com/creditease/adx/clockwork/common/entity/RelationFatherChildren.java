package com.creditease.adx.clockwork.common.entity;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午7:51 2020/12/6
 * @ Description：
 * @ Modified By：
 */
public class RelationFatherChildren {

    private Integer fatherTaskId;
    private String taskIds;

    public Integer getFatherTaskId() {
        return fatherTaskId;
    }

    public void setFatherTaskId(Integer fatherTaskId) {
        this.fatherTaskId = fatherTaskId;
    }

    public String getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds;
    }
}
