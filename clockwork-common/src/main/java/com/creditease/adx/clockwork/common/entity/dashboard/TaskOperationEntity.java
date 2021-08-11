package com.creditease.adx.clockwork.common.entity.dashboard;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:21 下午 2020/9/27
 * @ Description：
 * @ Modified By：
 */
public class TaskOperationEntity {

    // 任务总数
    private int total;

    // 调度准备
    private int enable;

    // 重启调度准备
    private int rerunSchedulePrep;

    // 等待运行
    private int submit;

    // 调度器进程已经接受
    private int masterHasReceived;

    // 执行器进程已经接受
    private int workerHasReceived;

    // 开始
    private int running;

    // 成功
    private int success;

    // 失败
    private int failed;

    // 异常结束
    private int exception;

    // 停止中
    private int killing;

    // 已杀死
    private int killed;

    // 运行超时停止中
    private int runTimeoutKilling;

    // 运行超时已杀死
    private int runTimeoutKilled;

    // 父任务不成功
    private int fatherNotSuccess;

    // 周期重置等待调度
    private int lifeCycleReset;

    // 上线
    private int online;

    // 离线
    private int offline;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public int getSubmit() {
        return submit;
    }

    public void setSubmit(int submit) {
        this.submit = submit;
    }

    public int getMasterHasReceived() {
        return masterHasReceived;
    }

    public void setMasterHasReceived(int masterHasReceived) {
        this.masterHasReceived = masterHasReceived;
    }

    public int getWorkerHasReceived() {
        return workerHasReceived;
    }

    public void setWorkerHasReceived(int workerHasReceived) {
        this.workerHasReceived = workerHasReceived;
    }

    public int getRunning() {
        return running;
    }

    public void setRunning(int running) {
        this.running = running;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getException() {
        return exception;
    }

    public void setException(int exception) {
        this.exception = exception;
    }

    public int getKilling() {
        return killing;
    }

    public void setKilling(int killing) {
        this.killing = killing;
    }

    public int getKilled() {
        return killed;
    }

    public void setKilled(int killed) {
        this.killed = killed;
    }

    public int getRunTimeoutKilling() {
        return runTimeoutKilling;
    }

    public void setRunTimeoutKilling(int runTimeoutKilling) {
        this.runTimeoutKilling = runTimeoutKilling;
    }

    public int getRunTimeoutKilled() {
        return runTimeoutKilled;
    }

    public void setRunTimeoutKilled(int runTimeoutKilled) {
        this.runTimeoutKilled = runTimeoutKilled;
    }

    public int getFatherNotSuccess() {
        return fatherNotSuccess;
    }

    public void setFatherNotSuccess(int fatherNotSuccess) {
        this.fatherNotSuccess = fatherNotSuccess;
    }

    public int getLifeCycleReset() {
        return lifeCycleReset;
    }

    public void setLifeCycleReset(int lifeCycleReset) {
        this.lifeCycleReset = lifeCycleReset;
    }

    public int getOffline() {
        return offline;
    }

    public void setOffline(int offline) {
        this.offline = offline;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getRerunSchedulePrep() {
        return rerunSchedulePrep;
    }

    public void setRerunSchedulePrep(int rerunSchedulePrep) {
        this.rerunSchedulePrep = rerunSchedulePrep;
    }
}
