package com.creditease.adx.clockwork.common.enums;

public enum ClientSource {
	SELF(0, "self"),
    ADX(1, "adx"),
    OTHER(-1, "other");

    private int value;
    private String name;

    ClientSource(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
