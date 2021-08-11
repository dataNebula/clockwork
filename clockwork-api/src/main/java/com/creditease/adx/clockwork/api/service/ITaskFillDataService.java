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

package com.creditease.adx.clockwork.api.service;

import java.util.List;

import com.creditease.adx.clockwork.api.service.base.IBaseRdmsService;
import com.creditease.adx.clockwork.common.entity.TaskFillDataEntity;
import com.creditease.adx.clockwork.common.entity.TaskFillDataSearchPageEntity;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillData;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillDataExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskFillDataMapper;

public interface ITaskFillDataService extends IBaseRdmsService<TbClockworkTaskFillData, TbClockworkTaskFillDataPojo,
        TbClockworkTaskFillDataExample, TbClockworkTaskFillDataMapper> {

    TbClockworkTaskFillData addTbClockworkTaskFillDataRecord(TaskFillDataEntity entity, long rerunBatchNumber);

    List<TbClockworkTaskFillDataPojo> getAllTaskFillDataByPageParam(TaskFillDataSearchPageEntity pojo, int pageNumber, int pageSize);

    int getAllTaskFillDataByPageParamCount(TaskFillDataSearchPageEntity task);

    List<TbClockworkTask4PagePojo> getTasksByReRunBatchNumber(Long rerunBatchNumber);

    List<TbClockworkTaskLogPojo> getTaskLogsByReRunBNAndTaskId(Long rerunBatchNumber, Integer taskId);

    TbClockworkTaskFillData getTaskFillDataByRerunBatchNumber(Long rerunBatchNumber) ;

    int updateTaskFillDataByRerunBatchNumber(TbClockworkTaskFillDataPojo tbClockworkTaskFillDataPojo);

    int updateTaskFillDataSuccessCount(Long rerunBatchNumber) ;

    int updateTaskFillDataCurrFillDataTime(
            Long rerunBatchNumber, String CurrFillDataTime, int CurrFillDataTimeSort);

    boolean updateTaskFillDataIsRan(Long rerunBatchNumber);

}
