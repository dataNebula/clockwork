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
import io.swagger.annotations.ApiModel;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 12:44 下午 2020/3/3
 * @ Description：
 * @ Modified By：
 */
@ApiModel
public class TaskLogSearchPageEntity extends TbClockworkTaskLogPojo {

    /**
     * 用户角色
     */
    private String roleName;

    /**
     * 操作者
     */
    private String operatorName;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "TaskLogSearchPageEntity{" +
                "id=" + super.getId() +
                ", taskName='" + super.getTaskName() + '\'' +
                ", taskGroupName='" + super.getTaskGroupName() + '\'' +
                ", taskAliasName='" + super.getTaskAliasName() + '\'' +
                ", taskGroupAliasName='" + super.getTaskGroupAliasName() + '\'' +
                ", executeType=" + super.getExecuteType() +
                ", operatorName='" + operatorName + '\'' +
                ", externalId=" + super.getExternalId() +
                ", rerunBatchNumber='" + super.getRerunBatchNumber() + '\'' +
                ", startTime=" + super.getStatus() +
                ", endTime=" + super.getEndTime() +
                ", status='" + super.getStatus() + '\'' +
                ", roleName='" + roleName + '\'' +
                ", createUser='" + super.getCreateUser() + '\'' +
                '}';
    }
}
