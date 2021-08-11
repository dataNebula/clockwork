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

import com.creditease.adx.clockwork.api.service.impl.TaskRelationService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerun;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskRerunMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:07 2019-09-18
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ImportResource(locations = {"classpath:vesta/api-vesta-service-db.xml"})
public class TestTaskRerunService {


    @Autowired
    private ITaskSubmitService taskSubmitService;

    @Autowired
    private TbClockworkTaskRerunMapper tbClockworkTaskRerunMapper;

    @Autowired
    private TaskRelationService taskRelationService;


    @Test
    public void submitReRunTaskTxTest() {
        taskSubmitService.submitReRunTaskTx(56197, -1, null, null,null);
    }


    @Test
    public void testAddTaskRerunBatch() {

        List<TbClockworkTaskRerun> taskReruns = new ArrayList<>();
        TbClockworkTaskRerun taskRerun = new TbClockworkTaskRerun();
        taskRerun.setTaskId(1);
        taskRerun.setGroupId(1);
        taskRerun.setIsFirst(true);
        taskRerun.setCreateTime(new Date());
        taskRerun.setUpdateTime(new Date());
        taskReruns.add(taskRerun);

        TbClockworkTaskRerun taskRerun1 = new TbClockworkTaskRerun();
        taskRerun1.setTaskId(2);
        taskRerun1.setGroupId(3);
        taskRerun1.setIsFirst(true);
        taskRerun1.setCreateTime(new Date());
        taskRerun1.setUpdateTime(new Date());
        taskReruns.add(taskRerun1);

        int i = tbClockworkTaskRerunMapper.batchInsert(taskReruns);
        System.out.println(i);

        for (TbClockworkTaskRerun rerun : taskReruns) {
            System.out.println(rerun.getId());
            System.out.println("=======");
        }


    }

    @Test
    public void testGetTaskAllChildrenIncludeSelf(){
        List<TbClockworkTaskPojo> taskAllChildrenIncludeSelf = taskRelationService.getTaskAllChildrenNotIncludeSelf(1687);
        for (TbClockworkTaskPojo taskPojo : taskAllChildrenIncludeSelf) {
            System.out.println(taskPojo.getId());
        }
    }

    @Test
    public void testGetTaskDirectlyChildrenNotIncludeSelf(){
        List<TbClockworkTaskPojo> taskAllChildrenIncludeSelf = taskRelationService.getTaskDirectlyChildrenNotIncludeSelf(1687);
        for (TbClockworkTaskPojo taskPojo : taskAllChildrenIncludeSelf) {
            System.out.println(taskPojo.getId());
        }
    }
}
