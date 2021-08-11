package com.creditease.adx.clockwork.common.enums;
/**
 * 上传文件的操作类型
 */
public enum UploadFileOperateType {
	// 删除
    DELETED("deleted"),
    // 新增
    ADD("add"),
    // 修改
    UPDATE("update");

    private String value;

    UploadFileOperateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
