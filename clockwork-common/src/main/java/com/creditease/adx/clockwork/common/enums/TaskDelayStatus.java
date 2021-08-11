package com.creditease.adx.clockwork.common.enums;


public enum TaskDelayStatus {
	NO_DELAY(0),                    // 不延迟
    SELF_DELAY(1),                  // 自延迟
    SELF_DELAYED_RECOVERY(9),       // 自延迟恢复
    FATHER_DELAY(2),                // 父延迟
    FATHER_DELAYED_RECOVERY(8),     // 父延迟恢复
    EXCEPTION_DELAY(3),             // 异常延迟
    EXCEPTION_DELAY_RECOVERY(7);    // 异常延迟恢复

    private Integer code;

    TaskDelayStatus(Integer code) {
        this.code = code;
    }

    public static TaskDelayStatus getEnumByCode(int code) {
        for (TaskDelayStatus type : TaskDelayStatus.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }
}
