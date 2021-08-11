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

import com.creditease.adx.clockwork.api.service.impl.TaskLogFlowService;
import com.creditease.adx.clockwork.common.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:02 2019-09-09
 * @ Description：SysTaskLogFlowService
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTaskLogFlowService {


    @Autowired
    private TaskLogFlowService taskLogFlowService;

    @Test
    public void getTaskLogFlowListTest() {

        int groupId = 265;
        String startTime = "2019-10-12 00:00:00";
        String endTime = "2019-10-15 00:00:00";

        String taskMonitorInfo = taskLogFlowService.getTaskLogFlowList(
                groupId,
                DateUtil.parse(startTime),
                DateUtil.parse(endTime)
        );
        System.out.println(taskMonitorInfo);

    }

}
