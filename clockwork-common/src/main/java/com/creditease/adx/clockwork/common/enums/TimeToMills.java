package com.creditease.adx.clockwork.common.enums;

public enum TimeToMills {
	// 一分钟有多少毫秒
    MILLS_OF_SECOND(60 * 1000l),
    // 一天有多少毫秒
    MILLS_OF_DAY(24 * 3600 * 1000l),
    // 一天有多少分钟
    SECONDS_OF_DAY(60 * 24l);

    private Long value;

    TimeToMills(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
