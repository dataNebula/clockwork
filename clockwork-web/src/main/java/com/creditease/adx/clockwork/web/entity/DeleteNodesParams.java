package com.creditease.adx.clockwork.web.entity;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:45 上午 2020/12/10
 * @ Description：删除节点参数
 * @ Modified By：
 */
public class DeleteNodesParams {

    // 方向，0子节点向下，1父节点向上
    private Integer direction;

    // 移除的节点
    private Integer inactiveId;

    // 跟节点
    private Integer rootId;

    // 已经选择的节点以及关系
    private SelectedParams selectedParams;

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Integer getInactiveId() {
        return inactiveId;
    }

    public void setInactiveId(Integer inactiveId) {
        this.inactiveId = inactiveId;
    }

    public Integer getRootId() {
        return rootId;
    }

    public void setRootId(Integer rootId) {
        this.rootId = rootId;
    }

    public SelectedParams getSelectedParams() {
        return selectedParams;
    }

    public void setSelectedParams(SelectedParams selectedParams) {
        this.selectedParams = selectedParams;
    }
}
