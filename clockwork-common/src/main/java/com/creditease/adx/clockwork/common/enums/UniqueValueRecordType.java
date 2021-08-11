package com.creditease.adx.clockwork.common.enums;
/**
 * 唯一值记录类型
 */
public enum UniqueValueRecordType {

    MASTER_ROUTINE("MRoutine"),
    MASTER_SLOT_MONITOR("MSlotMonitor"),
    MASTER_NODE_MONITOR("MNodeMonitor"),
    MASTER_TASK_RUNNING_MONITOR("MTaskRunningMonitor"),
    MASTER_TASK_FAILED_MONITOR("MTaskFailedMonitor"),
    MASTER_RESET_LIFECYCLE("MResetLifecycle"),
    MASTER_CLEAN_LOG_MONITOR("MCleanLogMonitor"),
    MASTER_TASK_EXCEPTION_MONITOR("MTaskExceptionMonitor"),
    MASTER_TASK_SUBSCRIPTION_MONITOR("MTaskSubMonitor"),
    KAFKA_TASK_LOG_SEND("KTaskLogSend"),
    KAFKA_LIFE_CYCLE_SEND("KTaskLogFlowSend");

    private String value;

    UniqueValueRecordType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
