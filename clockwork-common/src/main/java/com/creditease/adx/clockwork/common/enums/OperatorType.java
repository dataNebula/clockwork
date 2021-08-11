package com.creditease.adx.clockwork.common.enums;
/**
 * 目前有adx 和 other两种，主要为了扩展后续中台外部系统对接
 */
public enum OperatorType {
	ADX("adx"),

    OTHER("other");

    private String value;

    OperatorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
