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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:59 2019-12-06
 * @ Description：
 * @ Modified By：
 */
public class TbClockworkTask4PagePojo extends TbClockworkTask {

    /**
     * 用户角色，用逗号隔开
     */
    private String roleName;

    private String groupName ;

    private Long rerunBatchNumber;

    private String fillDataTimes;

    // 节点机组名称
    private String nodeGName;

    public Long getRerunBatchNumber() {
        return rerunBatchNumber;
    }

    public void setRerunBatchNumber(Long rerunBatchNumber) {
        this.rerunBatchNumber = rerunBatchNumber;
    }

    public String getFillDataTimes() {
        return fillDataTimes;
    }

    public void setFillDataTimes(String fillDataTimes) {
        this.fillDataTimes = fillDataTimes;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getTriggerTime() {
        return super.getTriggerTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getNextTriggerTime() {
        return super.getNextTriggerTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getLastStartTime() {
        return super.getLastStartTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getLastEndTime() {
        return super.getLastEndTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getExpiredTime() {
        return super.getExpiredTime();
    }


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getCreateTime() {
        return super.getCreateTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getUpdateTime() {
        return super.getUpdateTime();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getNodeGName() {
        return nodeGName;
    }

    public void setNodeGName(String nodeGName) {
        this.nodeGName = nodeGName;
    }
}
