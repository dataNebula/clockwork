package com.creditease.adx.clockwork.common.entity;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:29 下午 2020/11/26
 * @ Description：worker停止任务参数对象
 * @ Modified By：
 */
public class StopRunningTaskParam {

    // stop标记
    private Integer killSceneFlag;

    // 操作人
    private String operatorName;

    // 日志信息
    private TbClockworkTaskLogPojo taskLogPojo;

    public Integer getKillSceneFlag() {
        return killSceneFlag;
    }

    public void setKillSceneFlag(Integer killSceneFlag) {
        this.killSceneFlag = killSceneFlag;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public TbClockworkTaskLogPojo getTaskLogPojo() {
        return taskLogPojo;
    }

    public void setTaskLogPojo(TbClockworkTaskLogPojo taskLogPojo) {
        this.taskLogPojo = taskLogPojo;
    }

    public StopRunningTaskParam() {
    }

    public StopRunningTaskParam(Integer killSceneFlag, String operatorName, TbClockworkTaskLogPojo taskLogPojo) {
        this.killSceneFlag = killSceneFlag;
        this.operatorName = operatorName;
        this.taskLogPojo = taskLogPojo;
    }
}
