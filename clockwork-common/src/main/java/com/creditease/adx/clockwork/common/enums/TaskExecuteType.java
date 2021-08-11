package com.creditease.adx.clockwork.common.enums;


/**
 * 任务运行类型（模式）
 */
public enum TaskExecuteType {

    ROUTINE(1, "routine"),      // 例行
    RERUN(0, "rerun"),          // 重启
    FILL_DATA(2, "fill_data");  // 补数

    private Integer code;
    private String name;

    TaskExecuteType(Integer code, String name) {
        this.setCode(code);
        this.setName(name);
    }

    public static TaskExecuteType getEnumByCode(int code) {
        for (TaskExecuteType type : TaskExecuteType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
