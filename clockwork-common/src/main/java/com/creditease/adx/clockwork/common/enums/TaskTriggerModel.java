package com.creditease.adx.clockwork.common.enums;


/**
 * 任务触发模式
 */
public enum TaskTriggerModel {

    // 依赖
    DEPENDENCY(0),
    // 时间
    TIME(1),
    // 时间和依赖
    TIME_AND_DEPENDENCY(2),
    // 信号触发
    SIGNAL(3);

    private Integer value;

    TaskTriggerModel(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static TaskTriggerModel getEnumByCode(int code) {
        for (TaskTriggerModel type : TaskTriggerModel.values()) {
            if (type.value == code) {
                return type;
            }
        }
        return null;
    }
}
