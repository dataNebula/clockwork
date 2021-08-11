package com.creditease.adx.clockwork.common.enums;

/**
 * 节点构建命令的方式
 */
public enum BuildCommandType {

    // 默认的方式
    DEFAULT("default"),

    // 加载环境变量
    LOAD_ENV("loadEnv");

    private String value;

    BuildCommandType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
