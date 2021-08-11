package com.creditease.adx.clockwork.web.entity;

import com.creditease.adx.clockwork.common.entity.graph.LinkRelPic;
import com.creditease.adx.clockwork.common.entity.graph.NodeRelPic;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:11 下午 2020/12/3
 * @ Description：
 * @ Modified By：
 */
public class SelectedParams {

    private List<NodeRelPic> selectedNodes;

    private List<LinkRelPic> selectedLinks;

    public List<NodeRelPic> getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(List<NodeRelPic> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public List<LinkRelPic> getSelectedLinks() {
        return selectedLinks;
    }

    public void setSelectedLinks(List<LinkRelPic> selectedLinks) {
        this.selectedLinks = selectedLinks;
    }

    @Override
    public String toString() {
        return "SelectedParams{" +
                "selectedNodes=" + selectedNodes +
                ", selectedLinks=" + selectedLinks +
                '}';
    }
}
