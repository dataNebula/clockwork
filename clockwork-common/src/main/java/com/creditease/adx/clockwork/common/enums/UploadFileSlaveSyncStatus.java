package com.creditease.adx.clockwork.common.enums;
/**
 * 标记文件是否已经被执行服务所在服务器同步下载到本地
 */
public enum UploadFileSlaveSyncStatus {
	// 已同步下载
    SYNC("sync"),
    // 没有同步下载
    NO_SYNC("nosync");

    private String value;

    UploadFileSlaveSyncStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
