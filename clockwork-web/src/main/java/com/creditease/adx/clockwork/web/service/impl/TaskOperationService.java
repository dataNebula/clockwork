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

package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskBatchMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import com.creditease.adx.clockwork.web.service.ITaskOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @ Author     ：JasonTom
 * @ Date       ：Created in 18:13 2019-09-11
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskOperationService")
public class TaskOperationService implements ITaskOperationService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskOperationService.class);

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Autowired
    private TaskBatchMapper taskBatchMapper;

    /**
     * 更新作业任务信息
     *
     * @param taskPojo
     * @return
     */
    @Override
    public int updateTaskInfo(TbClockworkTaskPojo taskPojo) {
        LOG.info("updateTaskStatus, taskPojo.id = {}, taskPojo.status = {}", taskPojo.getId(), taskPojo.getStatus());
        return tbClockworkTaskMapper.updateByPrimaryKeySelective(PojoUtil.convert(taskPojo, TbClockworkTask.class));
    }

    @Override
    public int updateTaskDagIdByBatch(List<Integer> taskIds, Integer tagId) {
        LOG.info("updateTaskDagIdByBatch, taskPojo.size = {}", taskIds.size());
        if (taskIds.isEmpty()) return 0;
        return taskBatchMapper.batchUpdateTaskTagId(taskIds, tagId);
    }

}
