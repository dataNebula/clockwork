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

package com.creditease.adx.clockwork.api.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:03 2019-09-04
 * @ Description：TestKafkaSender
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestKafkaConsumer {

    private static final String TEST_TOPIC = "adx_clockwork_life_cycle_record";

    @Test
    public void consumer() {
        KafkaConsumerUtil.consumer(TEST_TOPIC);
    }

}
