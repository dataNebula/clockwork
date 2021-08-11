package com.creditease.adx.clockwork.common.entity.graph;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午5:52 2020/12/20
 * @ Description：basic node
 * @ Modified By：
 */
public class Node {

    // 节点名称
    private String name;

    //  节点颜色 【W=1，G=2, B=3】
    private int color = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                '}';
    }
}
