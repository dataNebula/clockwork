package com.creditease.adx.clockwork.common.entity.graph;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:07 下午 2020/12/3
 * @ Description：
 * @ Modified By：
 */
public class NodeRelPic extends Node {

    // Id
    private Integer id;

    // isSelected
    private  Boolean isSelected;

    private List<NodeRelPic> parents;

    private List<NodeRelPic> children;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public List<NodeRelPic> getParents() {
        return parents;
    }

    public void setParents(List<NodeRelPic> parents) {
        this.parents = parents;
    }

    public List<NodeRelPic> getChildren() {
        return children;
    }

    public void setChildren(List<NodeRelPic> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", name='" + super.getName() + '\'' +
                ", isSelected=" + isSelected +
                ", parents=" + parents +
                ", children=" + children +
                '}';
    }
}
