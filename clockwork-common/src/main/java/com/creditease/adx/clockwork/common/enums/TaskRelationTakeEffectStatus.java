package com.creditease.adx.clockwork.common.enums;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 上午11:40 2020/12/20
 * @ Description：
 * @ Modified By：
 */
public enum  TaskRelationTakeEffectStatus {

    // 在线
    ONLINE(true),

    // 离线
    OFFLINE(false);

    private final Boolean value;

    TaskRelationTakeEffectStatus(Boolean value) {
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }
}
