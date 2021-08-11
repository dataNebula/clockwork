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

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.creditease.adx.clockwork.api.service.impl.LockRecordService;
import com.creditease.adx.clockwork.common.enums.UniqueValueRecordType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkLockRecordPojo;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:21 2019-10-17
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestLockRecordService {

    @Resource
    private LockRecordService lockRecordService;

    @Test
    public void addLockRecordTest() {
        long startTime = System.currentTimeMillis();

        TbClockworkLockRecordPojo recordPojo = new TbClockworkLockRecordPojo();
        recordPojo.setType(UniqueValueRecordType.MASTER_ROUTINE.getValue());
        recordPojo.setSlotTime(1570982460000L);
        recordPojo.setSlot(1);
        recordPojo.setIp("127.0.0.1");
        recordPojo.setPort(9006);
        recordPojo.setCreateTime(new Date());
        recordPojo.setUpdateTime(new Date());
        recordPojo.setIsComplete(true);
        lockRecordService.addLockRecord(recordPojo);

        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime + " .ms");
    }
}
