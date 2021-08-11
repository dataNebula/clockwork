package com.creditease.adx.clockwork.common.enums;
/**
 * 节点状态
 */
public enum NodeStatus {
	// 有效
    ENABLE("enable"),
    // 无效
    DISABLE("disable");

    private String value;

    NodeStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
