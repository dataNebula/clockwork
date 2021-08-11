package com.creditease.adx.clockwork.common.enums;
/**
 * 各位业务场景使用redis全局锁的Key名称
 */
public enum RedisLockKey {
	// 提交任务全局锁key名称
    SUBMIT_TASK_TRANSACTION("STTK"),
    // 下发子任务全局锁key名称
    LAUNCH_SUB_TASK_TRANSACTION("LSTTK"),
    // 更新任务组的状态-当任务开始-全局锁Key名称
    UPDATE_TASK_GROUP_STATUS_WHEN_TASK_START("UTGSWTS"),
    // 更新任务组的状态-当任务完成-全局锁Key名称
    UPDATE_TASK_GROUP_STATUS_WHEN_TASK_END("UTGSWTE");

    private String value;

    RedisLockKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
