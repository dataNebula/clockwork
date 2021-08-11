package com.creditease.adx.clockwork.common.enums;
/**
 * 系统角色
 */
public enum SystemRole {

    // 管理员
    ADMIN("admin"),
    // 普通用户
    NORMAL("normal");

    private String value;

    SystemRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
