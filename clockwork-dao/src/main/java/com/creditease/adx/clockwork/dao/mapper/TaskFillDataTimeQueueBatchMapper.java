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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskFillDataTimeQueue;
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
public interface TaskFillDataTimeQueueBatchMapper {

    Logger LOG = LoggerFactory.getLogger(TaskFillDataTimeQueueBatchMapper.class);

    @InsertProvider(type = Provider.class, method = "addBatchTbClockworkTaskFillDataTimeQueue")
    int addBatchTbClockworkTaskFillDataTimeQueue(List<TbClockworkTaskFillDataTimeQueue> tbClockworkTaskFillDataTimeQueues);

    class Provider {

        @SuppressWarnings({ "rawtypes", "unchecked" })
		public String addBatchTbClockworkTaskFillDataTimeQueue(Map map) {
            List<TbClockworkTaskFillDataTimeQueue> tbClockworkTaskFillDataTimeQueues
                    = (List<TbClockworkTaskFillDataTimeQueue>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO tb_clockwork_task_fill_data_time_queue " +
                    "(fill_data_type,fill_data_time,upper_fill_data_time,rerun_batch_number,sort,update_time,create_time) VALUES ");
            TbClockworkTaskFillDataTimeQueue tbClockworkTaskFillDataTimeQueue;
            for (int i = 0; i < tbClockworkTaskFillDataTimeQueues.size(); i++) {
                tbClockworkTaskFillDataTimeQueue = tbClockworkTaskFillDataTimeQueues.get(i);
                sb.append("(");
                sb.append(tbClockworkTaskFillDataTimeQueue.getFillDataType()
                        == null ? null + "," : "'" + tbClockworkTaskFillDataTimeQueue.getFillDataType() + "',");
                sb.append("'" + tbClockworkTaskFillDataTimeQueue.getFillDataTime() + "',");
                sb.append(tbClockworkTaskFillDataTimeQueue.getUpperFillDataTime()
                        == null ? null + "," : "'" + tbClockworkTaskFillDataTimeQueue.getUpperFillDataTime() + "',");
                sb.append(tbClockworkTaskFillDataTimeQueue.getRerunBatchNumber() + ",");
                sb.append(tbClockworkTaskFillDataTimeQueue.getSort() + ",");
                sb.append("'" + DateUtil.dateTimeStampToString(tbClockworkTaskFillDataTimeQueue.getUpdateTime()) + "',");
                sb.append("'" + DateUtil.dateToString(tbClockworkTaskFillDataTimeQueue.getCreateTime())+ "'");
                sb.append(")");
                if (i < tbClockworkTaskFillDataTimeQueues.size() - 1) {
                    sb.append(",");
                }
            }
            String result = sb.toString();
            LOG.info("[addBatchTbClockworkTaskFillDataTimeQueue]sb = {}", result);
            LOG.info("[addBatchTbClockworkTaskFillDataTimeQueue]sql byte size = {}", result.getBytes().length);
            return result;
        }

    }

}
