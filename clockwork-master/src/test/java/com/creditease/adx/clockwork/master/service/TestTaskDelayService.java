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

package com.creditease.adx.clockwork.master.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:26 2019-07-04
 * @ Description：TestTaskService
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTaskDelayService {

    @Autowired
    TaskClientService taskClientService;

    @Test
    public void testGetTaskByStatus() throws Exception {
        List<TbClockworkTaskPojo> tasks
                = taskClientService.getTaskByStatus(TaskStatus.FAILED.getValue());
        for (TbClockworkTaskPojo task : tasks) {
            System.out.println(task.getId());
            System.out.println(task.getName());
        }

    }


}
