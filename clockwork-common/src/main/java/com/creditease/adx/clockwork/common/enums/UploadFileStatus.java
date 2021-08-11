package com.creditease.adx.clockwork.common.enums;
/**
 * 上传文件（运行脚本、依赖jar包、参数文件等）的状态
 */
public enum UploadFileStatus {

    // 删除
    DELETED("deleted"),
    // 有效
    ENABLE("enable");

    private String value;

    UploadFileStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
