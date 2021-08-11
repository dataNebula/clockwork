package com.creditease.adx.clockwork.common.entity;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午7:50 2020/12/6
 * @ Description：RelationChildrenFather
 * @ Modified By：
 */
public class RelationChildrenFather {

    private Integer taskId;
    private String fatherTaskIds;

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getFatherTaskIds() {
        return fatherTaskIds;
    }

    public void setFatherTaskIds(String fatherTaskIds) {
        this.fatherTaskIds = fatherTaskIds;
    }
}
