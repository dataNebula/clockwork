package com.creditease.adx.clockwork.common.enums;
/**
 * 项目上下线状态
 */
public enum ProjectStatus {
	OFFLINE(0),
    ONLINE(1);
    private int status;

    ProjectStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
