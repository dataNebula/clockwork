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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:02 2019-09-27
 * @ Description：TestTaskService
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class TestTaskService {


    @Resource(name = "taskService")
    private ITaskService taskService;

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;


    @Test
    public void getHasCronTaskFromSlotTest() {
        long startTime = System.currentTimeMillis();
        TbClockworkTaskPojo sysTbClockworkTaskPojo = taskService.getTaskById(1687);
        long endTime = System.currentTimeMillis();
        System.out.println(sysTbClockworkTaskPojo.getName());
        System.out.println("------------------>>>> " + (endTime - startTime) + " .ms");
//        for(int i = 0;i < 1;i++){
//            sysTbClockworkTaskPojo.setId(null);
//            sysTbClockworkTaskPojo.setName("new_" + sysTbClockworkTaskPojo.getName()+ "_" + i);
//            sysTbClockworkTaskPojo.setOnline(AdxCommonConstants.TaskTakeEffectStatus.OFFLINE.getValue());
//            taskService.addTask(PojoUtil.convert(sysTbClockworkTaskPojo, TbClockworkTaskPojo.class));
//        }
    }


    @Test
    public void tbClockworkTaskTest() {

        // 恢复
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andSourceEqualTo(4).andTriggerModeEqualTo(0).andNameLike("%NEBULA_ODS%");
//        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
//        String update_sql_01 = "update tb_clockwork_task t set t.location='%s',script_name='%s',script_type='%s' where source=4 and t.online=1 and trigger_mode=0 and id=%s;";
//        for (TbClockworkTask t : tbClockworkTasks) {
//            System.out.println(String.format(update_sql_01,t.getLocation(),t.getScriptName(),t.getScriptType(),t.getId()));
//        }

        List<Integer> ids = Arrays.asList(46226);

        TbClockworkTaskExample example2 = new TbClockworkTaskExample();
        example2.createCriteria().andSourceEqualTo(4).andTriggerModeEqualTo(1).andNameLike("%NEBULA_ODS%").andDagIdNotIn(ids).andOnlineEqualTo(true);
        List<TbClockworkTask> tbClockworkTasks2 = tbClockworkTaskMapper.selectByExample(example2);

        String update_sql_02 = "update tb_clockwork_task t set t.location='%s',script_name='%s',script_type='%s',cron_exp='%s' where source=4 and t.online=1 and trigger_mode=1 and id=%s;";
        for (TbClockworkTask t : tbClockworkTasks2) {
            System.out.println(String.format(update_sql_02, t.getLocation(), t.getScriptName(), t.getScriptType(), t.getCronExp(), t.getId()));
        }


    }


}
