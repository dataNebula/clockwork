package com.creditease.adx.clockwork.common.enums;

public enum DagCheckType {
//    自动
    AUTO("auto"),
//    手动
    MANUAL("manual");

    private String type;

    DagCheckType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
