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

import com.alibaba.fastjson.JSONArray;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DataUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:07 上午 2020/5/14
 * @ Description：
 * @ Modified By：
 */
public class JsonTest {


    @Test
    public void testJson1() {
        TbClockworkTaskPojo taskPojo = new TbClockworkTaskPojo();
        String command = "";
        taskPojo.setCommand(command);
        String task = JSONObject.toJSONString(taskPojo);
        System.out.println(task);
        TbClockworkTaskPojo tbClockworkTask = JSONObject.parseObject(task, TbClockworkTaskPojo.class);
        System.out.println(JSONObject.toJSONString(tbClockworkTask));
    }


    @Test
    public void testJson() {
        String task = "";
        TbClockworkTaskPojo tbClockworkTask = JSONObject.parseObject(DataUtil.specialCharHandle(task), TbClockworkTaskPojo.class);
        System.out.println(JSONObject.toJSONString(tbClockworkTask));
    }



    @Test
    public void testJsonId() {
        String task = "";
        JSONObject jsonObject = JSONObject.parseObject(task);
        JSONArray data = jsonObject.getJSONArray("data");
        for (Object datum : data) {
            System.out.print(JSONObject.parseObject(datum.toString()).get("id")+",");
        }
    }




}
