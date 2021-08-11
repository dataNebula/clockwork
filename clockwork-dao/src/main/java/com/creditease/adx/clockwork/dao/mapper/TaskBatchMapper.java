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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskAndSlotRelation;
import com.creditease.adx.clockwork.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.UpdateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface TaskBatchMapper {

    Logger LOG = LoggerFactory.getLogger(TaskBatchMapper.class);

    @InsertProvider(type = Provider.class, method = "batchInsertTaskAndLoopClockSlotRel")
    int batchInsertTaskAndLoopClockSlotRel(List<TbClockworkTaskAndSlotRelation> records);

    @UpdateProvider(type = Provider.class, method = "batchUpdateTaskNextTriggerTime")
    int batchUpdateTaskNextTriggerTime(List<TbClockworkTask> records);


    @UpdateProvider(type = Provider.class, method = "batchUpdateTaskTagId")
    int batchUpdateTaskTagId(List<Integer> taskIds, Integer dagId);

    class Provider {

        public String batchUpdateTaskTagId(List<Integer> taskIds, Integer dagId) {
            String result = "UPDATE tb_clockwork_task SET dag_id = " +
                    dagId +
                    " WHERE id IN (" + StringUtils.join(taskIds, ",") + ")";
            LOG.info("[batchUpdateTaskTagId]sql = {}", result);
            return result;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
		public String batchUpdateTaskNextTriggerTime(Map map) {
            List<TbClockworkTask> TbClockworkTasks
                    = (List<TbClockworkTask>) map.get("list");
            List<Integer> taskIds = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE tb_clockwork_task SET next_trigger_time = CASE id ");
            for (TbClockworkTask task : TbClockworkTasks) {
                String triggerTimeStr = DateUtil.formatDate(task.getNextTriggerTime(), DateUtil.DATE_FULL_STR);
                sb.append("WHEN " + task.getId() + " THEN '" + triggerTimeStr + "' ");
                taskIds.add(task.getId());
            }
            sb.append("END WHERE id IN (" + StringUtils.join(taskIds, ",") + ")");
            String result = sb.toString();
            LOG.info("[batchUpdateTaskNextTriggerTime]sql byte size = {}", result.getBytes().length);
            return result;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
		public String batchInsertTaskAndLoopClockSlotRel(Map map) {
            List<TbClockworkTaskAndSlotRelation> taskAndLoopClockSlotRels
                    = (List<TbClockworkTaskAndSlotRelation>) map.get("list");
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO tb_clockwork_task_and_slot_relation " +
                    "(slot_id,task_id,group_id,task_exec_date,create_time) VALUES ");
            for (int i = 0; i < taskAndLoopClockSlotRels.size(); i++) {
                TbClockworkTaskAndSlotRelation TbClockworkTaskAndLoopClockSlotRel = taskAndLoopClockSlotRels.get(i);

                sb.append("(");
                sb.append(TbClockworkTaskAndLoopClockSlotRel.getSlotId() + ",");
                sb.append(TbClockworkTaskAndLoopClockSlotRel.getTaskId() + ",");
                sb.append(TbClockworkTaskAndLoopClockSlotRel.getGroupId() + ",");
                sb.append("'" +
                        DateUtil.formatDate(
                                TbClockworkTaskAndLoopClockSlotRel.getTaskExecDate(), DateUtil.DATE_FULL_STR) + "',");
                sb.append("'" +
                        DateUtil.formatDate(
                                TbClockworkTaskAndLoopClockSlotRel.getCreateTime(), DateUtil.DATE_FULL_STR) + "'");
                sb.append(")");

                if (i < taskAndLoopClockSlotRels.size() - 1) {
                    sb.append(",");
                }
            }
            String result = sb.toString();
            LOG.info("[batchInsertTaskAndLoopClockSlotRel]sql byte size = {}", result.getBytes().length);
            return result;
        }

    }

}
