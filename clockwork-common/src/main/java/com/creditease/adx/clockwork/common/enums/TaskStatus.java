package com.creditease.adx.clockwork.common.enums;

/**
 * 任务状态
 */
public enum TaskStatus {

    // 调度准备
    ENABLE("enable", "调度准备"),
    // 重启调度准备
    RERUN_SCHEDULE_PREP("rerun_schedule_prep", "重启调度准备"),
    // 等待运行,
    SUBMIT("submit", "等待运行"),
    // 调度器进程已经接受
    MASTER_HAS_RECEIVE("master_has_received", "入队待调度"),
    // 执行器进程已经接受
    WORKER_HAS_RECEIVE("worker_has_received", "入队待执行"),
    // 开始
    RUNNING("running", "开始"),
    // 成功
    SUCCESS("success", "成功"),
    // 失败
    FAILED("failed", "失败"),
    // 异常结束
    EXCEPTION("exception", "异常结束"),
    // 停止中
    KILLING("killing", "停止中"),
    // 已杀死
    KILLED("killed", "已杀死"),
    // 运行超时停止中
    RUN_TIMEOUT_KILLING("run_timeout_killing", "运行超时停止中"),
    // 运行超时已杀死
    RUN_TIMEOUT_KILLED("run_timeout_killed", "运行超时已杀死"),
    // 父任务不成功
    FATHER_NOT_SUCCESS("father_not_success", "父任务不成功"),
    // 周期重置等待调度
    LIFE_CYCLE_RESET("life_cycle_reset", "周期重置等待调度");

    private final String value;
    private final String desc;

    TaskStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getValue() {
        return value;
    }


    /**
     * 获取状态描述信息
     *
     * @param value status str
     * @return desc
     */
    public static String getDescByValue(String value) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.getValue().equals(value)) {
                return status.getDesc();
            }
        }
        return null;
    }


}
