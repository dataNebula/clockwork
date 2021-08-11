package com.creditease.adx.clockwork.common.entity.graph;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:43 下午 2020/8/10
 * @ Description：附带位置的图节点
 * @ Modified By：
 */
public class NodePosition extends Node {
    /**
     * The Category.
     */
    int category; //0 targetTable 1 task/sql 2 table

    /**
     * The Value.
     */
    int value;

    int x;

    int y;

    int level;

    TbClockworkTask task;

    /**
     * Instantiates a new Nodes.
     *
     * @param category
     * @param name
     * @param value
     * @param task
     */
    public NodePosition(int category, String name, int value, TbClockworkTask task) {
        this.category = category;
        super.setName(name);
        this.value = value;
        this.task = task;
    }


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public TbClockworkTask getTask() {
        return task;
    }

    public void setTask(TbClockworkTask task) {
        this.task = task;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodePosition nodes = (NodePosition) o;

        if (value != nodes.value) return false;
        return super.getName() != null ? super.getName().equals(nodes.getName()) : nodes.getName() == null;
    }

    @Override
    public int hashCode() {
        int result = value;
        result = 31 * result + (super.getName() != null ? super.getName().hashCode() : 0);
        result = 31 * result + value;
        return result;
    }
}
