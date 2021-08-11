package com.creditease.adx.clockwork.common.entity.graph;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:03 上午 2020/12/28
 * @ Description：辐射图节点
 * @ Modified By：
 */
public class NodeRadial extends Node {

    // Id
    private Integer id;

    // 状态
    private String status ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
