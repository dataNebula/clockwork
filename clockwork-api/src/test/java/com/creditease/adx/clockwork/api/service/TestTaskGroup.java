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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:59 2019-12-09
 * @ Description：
 * @ Modified By：
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@ImportResource(locations = {"classpath:vesta/api-vesta-service-db.xml"})
public class TestTaskGroup {

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Test
    public void deleteByPrimaryKey() {

        Integer taskGroupId = 365;

        // 删除组
        int count = tbClockworkTaskMapper.deleteByPrimaryKey(taskGroupId);
        System.out.println(count);

    }

}
