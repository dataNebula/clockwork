package com.creditease.adx.clockwork.common.enums;

public enum TimeCycle {
    MINUTE("MINUTE", 60000),
    HOUR("HOUR", 3600000),
    DAY("DAY", 86400000),
    WEEK("WEEK", 604800000),
    MONTH("MONTH", 2592000000L), //30天
    YEAR("YEAR", 31536000000L);

    private String type;
    private long time;

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }

    TimeCycle(String type, long time) {
        this.type = type;
        this.time = time;
    }
}
