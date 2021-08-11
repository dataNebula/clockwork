package com.creditease.adx.clockwork.common.enums;


/**
 * 任务重启类型
 */
public enum TaskReRunType {
    SELF(-1, "self"),                                   // 重启自己（普通重启）
    LIST(6, "list"),                                    // 重启列表（普通重启）
    ALL_CHILDREN_NOT_SELF(3, "all_children_not_self"),  // 重启所有子任务不包括自己
    ALL_CHILDREN_AND_SELF(4, "all_children_and_self"),  // 重启所有子任务包括自己
    HIS_ROUTINE(1, "his_routine"),                      // 重启历史的例行任务
    HIS_RERUN(0, "his_rerun"),                          // 重启历史的重启任务
    HIS_FILL_DATA(2, "his_fill_data"),                  // 重启历史的补数任务
    FILL_DATA(5, "fill_data"),                          // 补数任务
    RERUN_WITH_SP(7, "rerun_with_script_parameter");   // 重启任务使用scriptparameter

    private Integer code;
    private String name;

    TaskReRunType(Integer code, String name) {
        this.setCode(code);
        this.setName(name);
    }

    public static TaskReRunType getEnumByCode(int code) {
        for (TaskReRunType type : TaskReRunType.values()) {
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
