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

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：Muyuan Sun
 * @ Date       ：Created in 18:25 2019-06-30
 * @ Description：例行任务运行基本单元Cell
 * @ Modified By：
 */
public class TaskRunCellRoutine extends TaskRunCell {

    public TaskRunCellRoutine(Integer nodeId, String runtimeDirClientUrl, Integer executeType, TbClockworkTaskPojo task) {
        super(nodeId, runtimeDirClientUrl, executeType, task );
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
