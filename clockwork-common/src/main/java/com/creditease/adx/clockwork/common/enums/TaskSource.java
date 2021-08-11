package com.creditease.adx.clockwork.common.enums;

/**
 * 任务来源类型，代表哪个系统添加进来的
 */
public enum TaskSource {

    // 中台 调度系统本身
    ADX_CLOCKWORK(0),

    // 中台 DATA_HUB
    ADX_DATA_HUB(2),

    // 中台 DATA_WORKS
    ADX_DATA_WORKS(3),

    // 中台 DDS_2.0
    DDS_2(4);

    private Integer value;

    TaskSource(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public static TaskSource getTaskSourceByValue(Integer source) {
        if (source == null) {
            return null;
        }
        TaskSource[] values = TaskSource.values();
        for (TaskSource value : values) {
            if (value.getValue().equals(source)) {
                return value;
            }
        }
        return null;
    }

}
