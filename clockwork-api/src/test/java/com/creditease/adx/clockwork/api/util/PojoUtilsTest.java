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

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.util.PojoUtil;

import org.junit.Test;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-10-26
 */
public class PojoUtilsTest {

    @Test
    public void testConvert() {

        TbClockworkTaskLog TbClockworkTaskLog = new TbClockworkTaskLog();
        TbClockworkTaskLog.setId(1);

        TbClockworkTaskLogPojo result = PojoUtil.convert(TbClockworkTaskLog, TbClockworkTaskLogPojo.class);

        System.out.println(result.getId());

    }


    @Test
    public void test() {

//        String str = "1229_[ITM_STORE]";
        String str = "1229_[ITM_STORE]fsdfs";
//        String str = "1229_fsdfs";
//        String str = "[ITM_STORE]";
//        String str = "[]";
//        String str = "]";
//        String str = "]fds[";
//        String str = "[ITM_STORE]]";
//        String str = "[[ITM_STORE]]";

        int index = str.indexOf("[");
        int lastIndex = str.lastIndexOf("]");
        if (index != -1 && lastIndex != -1 && index < lastIndex && lastIndex < str.length()) {
            str = str.substring(str.indexOf("[")+1, str.lastIndexOf("]"));
        }
        System.out.println(str);

    }
}
