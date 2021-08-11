package com.creditease.adx.clockwork.common.enums;

public enum TaskGroupStatusUpdateType {
	// 更新组任务的开始时间
    WHEN_TASK_START(1),

    // 更新组任务的结束时间
    WHEN_TASK_END(2),

    // 更新组状态
    GROUP_STATUS(3);

    private Integer value;

    TaskGroupStatusUpdateType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

}
