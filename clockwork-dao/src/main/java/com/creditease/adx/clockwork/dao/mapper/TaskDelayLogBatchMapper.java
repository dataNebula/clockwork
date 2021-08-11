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

package com.creditease.adx.clockwork.dao.mapper;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDelayLog;
import com.creditease.adx.clockwork.common.util.DateUtil;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface TaskDelayLogBatchMapper {

    Logger LOG = LoggerFactory.getLogger(TaskDelayLogBatchMapper.class);

    @InsertProvider(type = Provider.class, method = "addTaskDelayLogBatch")
    int addTaskDelayLogBatch(List<TbClockworkTaskDelayLog> taskDelayLogList);

    class Provider {

        @SuppressWarnings({ "rawtypes", "unchecked" })
		public String addTaskDelayLogBatch(Map map) {
            List<TbClockworkTaskDelayLog> taskDelayLogs
                    = (List<TbClockworkTaskDelayLog>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO tb_clockwork_task_delay_log " +
                    "(task_id,task_name,delay_status,create_time) VALUES ");
            for (int i = 0; i < taskDelayLogs.size(); i++) {
                TbClockworkTaskDelayLog taskLog = taskDelayLogs.get(i);

                sb.append("(");
                sb.append(taskLog.getTaskId() + ",");
                sb.append("'" + taskLog.getTaskName() + "',");
                sb.append(taskLog.getDelayStatus() + ",");
                sb.append("'" + DateUtil.dateToString(taskLog.getCreateTime()) + "'");
                sb.append(")");

                if (i < taskDelayLogs.size() - 1) {
                    sb.append(",");
                }
            }
            String result = sb.toString();
            LOG.info("[addTaskDelayLogBatch]sql byte size = {}", result.getBytes().length);
            return result;
        }

    }

}
