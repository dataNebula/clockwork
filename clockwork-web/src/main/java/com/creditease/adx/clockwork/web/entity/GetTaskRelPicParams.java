package com.creditease.adx.clockwork.web.entity;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:20 下午 2020/12/4
 * @ Description：获取节点关系参数
 * @ Modified By：
 */
public class GetTaskRelPicParams {

    private Integer taskId;

    private SelectedParams selectedParams;

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public SelectedParams getSelectedParams() {
        return selectedParams;
    }

    public void setSelectedParams(SelectedParams selectedParams) {
        this.selectedParams = selectedParams;
    }
}
