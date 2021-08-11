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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:34 2020-01-06
 * @ Description：补数
 * @ Modified By：
 */
public class TbClockworkTaskFillDataPojo extends TbClockworkTaskFillData {

    // 扩展ID
    private Integer externalId;

    public Integer getExternalId() {
        return externalId;
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public Long getRerunBatchNumber() {
        return super.getRerunBatchNumber();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getStartTime() {
        return super.getStartTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getExecuteTime() {
        return super.getExecuteTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Override
    public Date getEndTime() {
        return super.getEndTime();
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

    @Override
    public String toString() {
        return "TbClockworkTaskGroupPojo{" +
                "id=" + getId() +
                ", operatorName=" + super.getOperatorName() +
                ", taskIds=" + super.getTaskIds() +
                ", taskGroupAliasName=" + super.getTaskGroupAliasName() +
                ", taskCount=" + super.getTaskCount() +
                ", fillDataTime=" + super.getFillDataTime() +
                ", fillDataTimeCount=" + super.getFillDataTimeCount() +
                ", rerunBatchNumber=" + super.getRerunBatchNumber() +
                ", description=" + super.getDescription() +
                ", isEnd=" + super.getIsEnd() +
                ", status=" + getStatus() +
                ", externalId=" + externalId +
                ", startTime=" + getStartTime() +
                ", executeTime=" + getExecuteTime() +
                ", endTime=" + getEndTime() +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}
