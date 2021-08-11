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
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface TaskMapper {

    List<TbClockworkTask4PagePojo>
    selectAllTaskUsedByAutoComplete(@Param("idOrNameSegment") String idOrNameSegment);

    List<TbClockworkTask4PagePojo> selectAllTaskByCondition(TbClockworkTask4PagePojo tbClockworkTask);

    List<TbClockworkTask4PagePojo> selectAllTaskByPageParam(HashMap<String, Object> param);

    int countAllTaskByPageParam(HashMap<String, Object> param);

    List<Map<String, Object>> selectAllIdAndName(@Param("id") Integer id);

    List<Integer> selectTaskDagIdsBySource(Integer source);

    List<Integer> selectTaskDagIdsByCrossSource(Integer source);

    List<Map<String, Object>> selectTaskIdAndNameByUserGroupName(Map<String, Object> param);

    List<TbClockworkTaskPojo> selectTaskByRunFailedStatus(Map<String, String> param);

    int updateByPrimaryKeySelective(TbClockworkTask task);
}
