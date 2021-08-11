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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerunRelation;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:05 2019-11-05
 * @ Description：任务重跑的依赖关系
 * @ Modified By：
 */
public interface ITaskRerunRelationService {

    List<Integer> findTaskDirectlyChildrenIds(Integer taskId, Long rerunBatchNumber);

    List<Integer> findTaskDirectlyFatherIds(Integer taskId, Long rerunBatchNumber);

    List<TbClockworkTaskRerunRelation> findTaskDirectlyChildren(Integer taskId, Long rerunBatchNumber);

    List<TbClockworkTaskRerunRelation> findTaskDirectlyFather(Integer taskId, Long rerunBatchNumber);

    List<TbClockworkTaskRerunRelation> findTaskAllChildrenNotIncludeSelf(Integer taskId, Long rerunBatchNumber);

    List<TbClockworkTaskRerunRelation> getTaskRerunRelationByBatchNumber(Long rerunBatchNumber);

    List<TbClockworkTaskRerunRelation> buildTaskRelation(List<TbClockworkTaskPojo> tasks, long batchNum);

}
