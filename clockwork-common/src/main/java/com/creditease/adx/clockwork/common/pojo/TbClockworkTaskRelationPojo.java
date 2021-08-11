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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class TbClockworkTaskRelationPojo extends TbClockworkTaskRelation {

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
        return "TbClockworkTaskRelation{" +
                "id=" + super.getId() +
                ", taskId=" + super.getTaskId() +
                ", taskName=" + super.getTaskName() +
                ", fatherTaskId=" + super.getFatherTaskId() +
                ", fatherTaskName=" + super.getFatherTaskName() +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                "}";
    }

}
