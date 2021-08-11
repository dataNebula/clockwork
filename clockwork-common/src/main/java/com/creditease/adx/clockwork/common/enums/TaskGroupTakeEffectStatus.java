package com.creditease.adx.clockwork.common.enums;
/**
 * 任务组生效状态
 */
public enum TaskGroupTakeEffectStatus {

    // 有效
    ENABLE("enable"),
    // 无效
    DISABLE("disable");

    private String value;

    TaskGroupTakeEffectStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
