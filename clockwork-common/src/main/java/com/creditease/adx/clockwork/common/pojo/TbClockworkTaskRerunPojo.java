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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerun;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:09 2019-11-05
 * @ Description：
 * @ Modified By：
 */
public class TbClockworkTaskRerunPojo extends TbClockworkTaskRerun {

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public Long getRerunBatchNumber() {
        return super.getRerunBatchNumber();
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

}
