package com.creditease.adx.clockwork.common.entity;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:54 下午 2020/10/26
 * @ Description：
 * @ Modified By：
 */
public class BatchResetTaskLifecycleParam {

    private List<Integer> dagIds;
    private List<Integer> taskIds;

    public List<Integer> getDagIds() {
        return dagIds;
    }

    public void setDagIds(List<Integer> dagIds) {
        this.dagIds = dagIds;
    }

    public List<Integer> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Integer> taskIds) {
        this.taskIds = taskIds;
    }
}
