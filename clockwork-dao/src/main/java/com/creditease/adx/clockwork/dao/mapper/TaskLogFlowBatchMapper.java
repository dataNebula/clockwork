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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLogFlow;
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
public interface TaskLogFlowBatchMapper {

    Logger LOG = LoggerFactory.getLogger(TaskLogFlowBatchMapper.class);

    @InsertProvider(type = Provider.class, method = "addBatchTaskLogFlow")
    int addBatchTaskLogFlow(List<TbClockworkTaskLogFlow> lifeCycleRecords);


    class Provider {

    	@SuppressWarnings({ "rawtypes", "unchecked" })
        public String addBatchTaskLogFlow(Map map) {
            List<TbClockworkTaskLogFlow> lifeCycleRecords
                    = (List<TbClockworkTaskLogFlow>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO tb_clockwork_task_log_flow " +
                    "(task_id,task_name,log_id,group_id,node_id,status,operation_type," +
                    "trigger_mode,start_time,duration,create_time,is_last,is_end) VALUES ");
            TbClockworkTaskLogFlow cycleRecord;
            for (int i = 0; i < lifeCycleRecords.size(); i++) {
                cycleRecord = lifeCycleRecords.get(i);
                sb.append("(");
                sb.append(cycleRecord.getTaskId() + ",");
                sb.append("'" + cycleRecord.getTaskName() + "',");
                sb.append(cycleRecord.getLogId() + ",");
                sb.append(cycleRecord.getGroupId() + ",");
                sb.append(cycleRecord.getNodeId() + ",");
                sb.append("'" + cycleRecord.getStatus() + "',");
                sb.append(cycleRecord.getOperationType() + ",");
                sb.append(cycleRecord.getTriggerMode() + ",");
                sb.append("'" + DateUtil.dateTimeStampToString(cycleRecord.getStartTime()) + "',");
                sb.append(cycleRecord.getDuration() + ",");
                sb.append("'" + DateUtil.dateToString(cycleRecord.getCreateTime()) + "',");
                sb.append((cycleRecord.getIsLast() ? 1 : 0) + ",");
                sb.append((cycleRecord.getIsEnd() ? 1 : 0));
                sb.append(")");

                if (i < lifeCycleRecords.size() - 1) {
                    sb.append(",");
                }
            }
            String result = sb.toString();
            LOG.info("[addBatchTaskLogFlow]sb = {}", result);
            LOG.info("[addBatchTaskLogFlow]sql byte size = {}", result.getBytes().length);
            return result;
        }

    }

}
