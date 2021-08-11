package com.creditease.adx.clockwork.common.enums;

public enum TaskGroupStatus {
	// 开始
    RUNNING("running"),
    // 成功
    SUCCESS("success"),
    // 失败
    FAILED("failed"),
    // 异常结束
    EXCEPTION("exception"),
    // 停止中
    KILLING("killing"),
    // 已杀死
    KILLED("killed");


    private String value;

    TaskGroupStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
