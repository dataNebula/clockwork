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

package com.creditease.adx.clockwork.master;

import com.creditease.adx.clockwork.client.service.LoopClockClientService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:57 2019-10-08
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan({
        "com.creditease.adx.clockwork.redis",
        "com.creditease.adx.clockwork.master",
        "com.creditease.adx.clockwork.client",
})
public class TestTimeOut {

    @Resource(name = "loopClockClientService")
    private LoopClockClientService loopClockClientService;

    @Test
    public void test() {
        boolean buildTaskLoopClockResult = loopClockClientService.buildTaskLoopClock();
        System.out.println(buildTaskLoopClockResult);
    }
}
