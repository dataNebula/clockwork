package com.creditease.adx.clockwork.common.enums;

public enum SystemStatus {
	SUCCESS("SUCEESS"),
    FAILURE("FAILURE"),
    ERROR("ERROR");

    private String value;

    SystemStatus(String value) {
        this.value = value;
    }

    public String getStatus() {
        return value;
    }

}
