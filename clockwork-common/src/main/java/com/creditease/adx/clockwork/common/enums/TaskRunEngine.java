package com.creditease.adx.clockwork.common.enums;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:53 下午 2020/12/3
 * @ Description：
 * @ Modified By：
 */
public enum TaskRunEngine {

    SHELL("shell"),
    PYTHON("python"),
    HIVE("hive"),
    MOON_BOX("moonbox"),
    SPARK("spark");

    private String code;

    TaskRunEngine(String code) {
        this.setCode(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
