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

package com.creditease.adx.clockwork.common.pojo;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:23 2020-09-21
 * @ Description：
 * @ Modified By：
 */
public class TbClockworkTaskRerun4PagePojo extends TbClockworkTaskRerunPojo {

    /**
     * 用户角色
     */
    private String roleName;

    /**
     * 创建者
     */
    private String createUser;

    // 返回日志相关参数
    private Integer logId;
    private Integer executeType;
    private String status;
    private Integer pid;
    private Boolean isEnd;
    private Integer nodeId;
    private String logName;
    private Date startTime;
    private Date runningTime;
    private Date executeTime;
    private Date endTime;

    // 返回节点相关参数
    @ApiModelProperty("日志节点ip")
    private String nodeIp;

    @ApiModelProperty("日志节点port")
    private String nodePort;


    /**
     * @return logId
     */
    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }


    /**
     * 执行task的节点id
     *
     * @return nodeId
     */
    public Integer getNodeId() {
        return nodeId;
    }

    /**
     * 执行task的节点id
     *
     * @param nodeId
     */
    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    /**
     *
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    /**
     * 运行pid
     *
     * @return pid
     */
    public Integer getPid() {
        return pid;
    }

    /**
     * 运行pid
     *
     * @param pid
     */
    public void setPid(Integer pid) {
        this.pid = pid;
    }

    /**
     * 开始时间
     *
     * @return startTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getStartTime() {
        return startTime;
    }

    /**
     * 开始时间
     *
     * @param startTime
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * 执行时间
     *
     * @return executeTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getExecuteTime() {
        return executeTime;
    }

    /**
     * 执行时间
     *
     * @param executeTime
     */
    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    /**
     * 运行时间，暂时为保留字段
     *
     * @return runningTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getRunningTime() {
        return runningTime;
    }

    /**
     * 运行时间，暂时为保留字段
     *
     * @param runningTime
     */
    public void setRunningTime(Date runningTime) {
        this.runningTime = runningTime;
    }

    /**
     * 结束时间
     *
     * @return endTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndTime() {
        return endTime;
    }

    /**
     * 结束时间
     *
     * @param endTime
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * 自动调度1；手动执行0; 补数2
     *
     * @return executeType
     */
    public Integer getExecuteType() {
        return executeType;
    }

    /**
     * 自动调度1；手动执行0; 补数2
     *
     * @param executeType
     */
    public void setExecuteType(Integer executeType) {
        this.executeType = executeType;
    }


    /**
     * 是否结束，0否1是
     *
     * @return isEnd
     */
    public Boolean getIsEnd() {
        return isEnd;
    }

    /**
     * 是否结束，0否1是
     *
     * @param isEnd
     */
    public void setIsEnd(Boolean isEnd) {
        this.isEnd = isEnd;
    }

    public Boolean getEnd() {
        return isEnd;
    }

    public void setEnd(Boolean end) {
        isEnd = end;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
}
