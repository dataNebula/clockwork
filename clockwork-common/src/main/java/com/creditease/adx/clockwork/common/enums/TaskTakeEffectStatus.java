package com.creditease.adx.clockwork.common.enums;
/**
 * 任务生效状态
 */
public enum TaskTakeEffectStatus {
	// 在线
    ONLINE(true),
    // 离线
    OFFLINE(false),
    // 前端的全部选项，包含上线和下线标识
    ONLINE_AND_OFFLINE(null);

    private Boolean value;

    TaskTakeEffectStatus(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

}
