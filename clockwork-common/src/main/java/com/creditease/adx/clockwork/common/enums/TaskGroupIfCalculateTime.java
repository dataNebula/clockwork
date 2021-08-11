package com.creditease.adx.clockwork.common.enums;

public enum TaskGroupIfCalculateTime {
	// 计算
    YES(1),

    // 不计算
    NO(0);


    private Integer value;

    TaskGroupIfCalculateTime(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
