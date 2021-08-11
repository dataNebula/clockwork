package com.creditease.adx.clockwork.common.enums;


public enum TimeType {
	MINUTE("minute", 60000L),
    HOUR("hour", 3600000L),
    DAY("day", 86400000L),
    WEEK("week", 604800000L),
    MONTH("month", 2592000000L), // 30t
    YEAR("year", 31536000000L),
    CENTURY("century", 3153600000000L);

    private String type;
    private Long millis;

    TimeType(String type, Long millis) {
        this.type = type;
        this.millis = millis;
    }

    public static Long getMillisByType(String type) {
        for (TimeType ele : values()) {
            if (ele.getType().equals(type)) return ele.getMillis();
        }
        return null;
    }

    public static TimeType getTimeTypeEnum(String timeType) {
        for (TimeType ele : values()) {
            if (ele.getType().equals(timeType)) return ele;
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getMillis() {
        return millis;
    }

    public void setMillis(Long millis) {
        this.millis = millis;
    }
}
