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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
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
public interface TaskLogBatchMapper {

    Logger LOG = LoggerFactory.getLogger(TaskLogBatchMapper.class);

    @InsertProvider(type = Provider.class, method = "addTaskLogBatch")
    int addBatchTaskLog(List<TbClockworkTaskLog> TbClockworkTaskLoges);


    class Provider {

        @SuppressWarnings({ "rawtypes", "unchecked" })
		public String addTaskLogBatch(Map map) {
            List<TbClockworkTaskLog> taskLogs
                    = (List<TbClockworkTaskLog>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO tb_clockwork_task_log " +
                    "(task_id,task_name,node_id,status,real_command,start_time,execute_type,run_engine," +
                    "trigger_time,group_id,batch_number,create_time,is_end) VALUES ");
            for (int i = 0; i < taskLogs.size(); i++) {
                TbClockworkTaskLog taskLog = taskLogs.get(i);

                sb.append("(");
                sb.append(taskLog.getTaskId() + ",");
                sb.append("'" + taskLog.getTaskName() + "',");
                sb.append(taskLog.getNodeId() + ",");
                sb.append("'" + taskLog.getStatus() + "',");
                sb.append("'" + taskLog.getRealCommand() + "',");
                sb.append("'" + DateUtil.dateTimeStampToString(taskLog.getStartTime()) + "',");
                sb.append(taskLog.getExecuteType() + ",");
                sb.append("'" + taskLog.getRunEngine() + "',");
                sb.append("'" + DateUtil.dateToString(taskLog.getTriggerTime()) + "',");
                sb.append(taskLog.getGroupId() + ",");
                sb.append(taskLog.getBatchNumber() + ",");
                sb.append("'" + DateUtil.dateToString(taskLog.getCreateTime()) + "',");
                sb.append((taskLog.getIsEnd() ? 1 : 0));
                sb.append(")");

                if (i < taskLogs.size() - 1) {
                    sb.append(",");
                }
            }
            String result = sb.toString();
            LOG.info("[addTaskLogBatch]sql byte size = {}", result.getBytes().length);
            return result;
        }

    }

}
