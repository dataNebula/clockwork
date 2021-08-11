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

package com.creditease.adx.clockwork.common.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-10-21
 */
public class DataUtilTest {

    @Test
    public void testAverageAssign(){


        List<Integer> data = new ArrayList <>();
        data.add(11);

        List<List<Integer>> s = DataUtil.averageAssign(data,2);

        System.out.println(s.size());

        for(List<Integer> item:s){
            System.out.println(item.size());
        }
    }


    @Test
    public void testDateUtilsGetDetailInfoOfDate() {
        int [] result = DateUtil.getDetailInfoOfDate("2019-12-08 12:34:05");

        System.out.println(result[0]);
        System.out.println(result[1]);
        System.out.println(result[2]);
        System.out.println(result[3]);
        System.out.println(result[4]);
        System.out.println(result[5]);
    }

}
