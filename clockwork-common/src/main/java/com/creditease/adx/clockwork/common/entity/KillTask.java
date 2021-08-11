/*-
 *  
 * Clockwork
 *  
 * Copyright (C) 2019 - 2020 adx
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 */

package com.creditease.adx.clockwork.common.entity;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.Date;
import java.util.Set;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-07-01
 */
public class KillTask {

    // 需要杀死任务对应的日志对象
    private TbClockworkTaskLogPojo taskLog;

    // 标识杀死操作的业务场景种类，不同的业务场景种类终结的状态是不一样的，主要为了清晰的表达业务
    private Integer killSceneFlag;

    // 记录执行完杀死操作命令的时间，这个时刻不代表任务的状态真正已经变成了已杀死
    private Date killedOperateTime;

    // 包含当前进程已经当前进程的子进程pid
    private Set <String> pids;

    // pids 字符串，多个用逗号分隔
    private String pidsInfo;

    // 作业对象信息
    private TbClockworkTaskPojo task;

    public TbClockworkTaskLogPojo getTaskLog() {
        return taskLog;
    }

    public void setTaskLog(TbClockworkTaskLogPojo taskLog) {
        this.taskLog = taskLog;
    }

    public Integer getKillSceneFlag() {
        return killSceneFlag;
    }

    public void setKillSceneFlag(Integer killSceneFlag) {
        this.killSceneFlag = killSceneFlag;
    }

    public Date getKilledOperateTime() {
        return killedOperateTime;
    }

    public void setKilledOperateTime(Date killedOperateTime) {
        this.killedOperateTime = killedOperateTime;
    }

    public Set <String> getPids() {
        return pids;
    }

    public void setPids(Set <String> pids) {
        this.pids = pids;
    }

    public String getPidsInfo() {
        return pidsInfo;
    }

    public void setPidsInfo(String pidsInfo) {
        this.pidsInfo = pidsInfo;
    }

    public TbClockworkTaskPojo getTask() {
        return task;
    }

    public void setTask(TbClockworkTaskPojo task) {
        this.task = task;
    }

}
