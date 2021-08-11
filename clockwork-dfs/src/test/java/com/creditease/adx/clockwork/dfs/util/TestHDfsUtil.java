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

package com.creditease.adx.clockwork.dfs.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.creditease.adx.clockwork.dfs.hdfs.HDfsUtils;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:38 下午 2020/1/13
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class TestHDfsUtil {


    @Autowired
    private HDfsUtils hDfsUtils;

    @Test
    public void testMkdir() throws Exception {
        boolean mkdir = hDfsUtils.mkdir("/tmp/aa","hdfs");
        System.out.println(mkdir);
    }

    @Test
    public void testDelete() throws Exception {
        boolean mkdir = hDfsUtils.delete("/tmp/aa","hdfs");
        System.out.println(mkdir);
    }

}
