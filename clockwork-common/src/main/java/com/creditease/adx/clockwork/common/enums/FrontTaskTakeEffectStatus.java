package com.creditease.adx.clockwork.common.enums;
/**
 * 前端任务生效状态，由于历史原因，前端任务的生效和失效状态都传递的是 enable 和 disable
 */
public enum FrontTaskTakeEffectStatus {
	// 有效
    ENABLE("enable"),
    // 无效
    DISABLE("disable");

    private String value;

    FrontTaskTakeEffectStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
