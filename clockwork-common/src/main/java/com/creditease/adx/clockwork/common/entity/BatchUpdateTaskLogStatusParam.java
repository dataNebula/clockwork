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

import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:11 2020-10-28
 * @ Description：
 * @ Modified By：
 */
public class BatchUpdateTaskLogStatusParam {

    private List<Integer> logIds;
    private String status;
    private Integer nodeId;     // WorkerHasReceive
    private Date executeTime;   // WorkerHasReceive
    private Integer pid;        // Running
    private String LogName;     // Running
    private int returnCode;     // End

    public List<Integer> getLogIds() {
        return logIds;
    }

    public void setLogIds(List<Integer> logIds) {
        this.logIds = logIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getLogName() {
        return LogName;
    }

    public void setLogName(String logName) {
        LogName = logName;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    @SuppressWarnings("unused")
    private BatchUpdateTaskLogStatusParam() {
    }

    /**
     * 批量修改状态为MasterHasReceive，所需要的参数
     *
     * @param logIds
     * @param status
     */
    public BatchUpdateTaskLogStatusParam(List<Integer> logIds, String status) {
        this.logIds = logIds;
        this.status = status;
    }

    /**
     * 批量修改状态为WorkerHasReceive，所需要的参数
     *
     * @param logIds ids
     * @return
     */
    public BatchUpdateTaskLogStatusParam(List<Integer> logIds, String status, Integer nodeId, Date executeTime) {
        this.logIds = logIds;
        this.status = status;
        this.nodeId = nodeId;
        this.executeTime = executeTime;
    }

    /**
     * 批量修改状态为End，所需要的参数
     *
     * @param logIds     logIds
     * @param status     状态
     * @param returnCode 状态码
     */
    public BatchUpdateTaskLogStatusParam(List<Integer> logIds, String status, int returnCode) {
        this.logIds = logIds;
        this.status = status;
        this.returnCode = returnCode;
    }
}
