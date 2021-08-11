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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.creditease.adx.clockwork.common.entity.TaskLogSearchPageEntity;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:07 2020-03-26
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestTaskLogService {


    @Autowired
    private ITaskLogService taskLogService;

    @Test
    public void getAllTaskLogByPageParam() {


        TaskLogSearchPageEntity pojo = new TaskLogSearchPageEntity();
        List<TbClockworkTaskLogPojo> allTaskLogByPageParam = taskLogService.getAllTaskLogByPageParam(pojo, 1, 2);
        for (TbClockworkTaskLogPojo tbClockworkTaskLogPojo : allTaskLogByPageParam) {

            System.out.println(tbClockworkTaskLogPojo.toString());
            System.out.println(tbClockworkTaskLogPojo.getParameter());


        }
    }


}
