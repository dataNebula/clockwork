package com.creditease.adx.clockwork.common.enums;
/**
 * 节点类型
 */
public enum NodeType {

    // worker 任务执行服务
    WORKER("worker"),
    // master 任务分发服务
    MASTER("master"),
    // api 对外对内提供业务接口主服务
    API("api");

    private String value;

    NodeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
