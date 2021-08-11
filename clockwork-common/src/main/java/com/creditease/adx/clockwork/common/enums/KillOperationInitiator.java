package com.creditease.adx.clockwork.common.enums;

/**
 * 任务停止操作的发起者类型
 */
public enum KillOperationInitiator {

    // 外部手动发起的kill命令
	EXTERNAL_CLIENT(1),

    // 系统监控任务超时发起的kill命令
    SYS_RUNNING_TASK_MONITOR(2);

    private Integer value;

    KillOperationInitiator(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

}
