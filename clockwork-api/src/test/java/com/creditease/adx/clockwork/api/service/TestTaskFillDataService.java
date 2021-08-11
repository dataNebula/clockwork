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

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.entity.TaskFillDataSearchPageEntity;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTask4PagePojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:07 2020-03-26
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestTaskFillDataService {


    @Autowired
    private ITaskFillDataService taskFillDataService;


    @Test
    public void getAllTaskFillDataByPageParam() {

        TaskFillDataSearchPageEntity pojo = new TaskFillDataSearchPageEntity();
        pojo.setRerunBatchNumber("178626984392261672");
        pojo.setCreateTimeEnd(new Date());
        pojo.setExternalId(1);

        System.out.println(JSONObject.toJSONString(pojo));
        List<TbClockworkTaskFillDataPojo> allTaskLogByPageParam = taskFillDataService.getAllTaskFillDataByPageParam(pojo, 1, 2);
        int size = taskFillDataService.getAllTaskFillDataByPageParamCount(pojo);
        System.out.println(allTaskLogByPageParam.size());
        System.out.println(size);
        for (TbClockworkTaskFillDataPojo tbClockworkTaskLogPojo : allTaskLogByPageParam) {

            System.out.println(tbClockworkTaskLogPojo.toString());


        }
    }


    @Test
    public void getTasksByReRunBatchNumber() {


        List<TbClockworkTask4PagePojo> pojos = taskFillDataService.getTasksByReRunBatchNumber(173175444680474664L);
        System.out.println(pojos.size());
        for (TbClockworkTask4PagePojo pojo : pojos) {

            System.out.println(pojo.toString());

        }
    }



    @Test
    public void getTaskLogsByReRunBNAndTaskId() {

        List<TbClockworkTaskLogPojo> pojos =
                taskFillDataService.getTaskLogsByReRunBNAndTaskId(173175444680474664L, 1687);
        System.out.println(pojos.size());
        for (TbClockworkTaskLogPojo tbClockworkTaskLogPojo : pojos) {

            System.out.println(tbClockworkTaskLogPojo.toString());


        }
    }

}
