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

package com.creditease.adx.clockwork.worker.api;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.BatchTaskInfo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:21 2019-07-22
 * @ Description：TestRestApi,需要启动clockwork-worker
 * @ Modified By：
 */
@Slf4j
public class TestRestApi {


    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static String baseUrl = "http://localhost:9008";

    /**
     * 1. TaskController.checkCustomerCanRun()
     */
    @Test
    public void TestCheckCustomerCanRun() throws Exception {

        // tasks
        ArrayList<TbClockworkTaskPojo> tasks = new ArrayList<>();

        Map<String, Object> task = new HashMap<>();
//        task.put("taskId",411);
        task.put("taskId",415);

        String taskResult = HttpUtil.post("http://t5:9005/clockwork/api/task/getTaskById", task);
        JSONObject json = JSONObject.parseObject(taskResult);

        if(json.get(Constant.CODE).equals(Constant.SUCCESS_CODE)){
            tasks.add(JSONObject.parseObject(json.getString(Constant.DATA), TbClockworkTaskPojo.class));
        }



        log.info("==================");
        String result = HttpUtil.postByBody(baseUrl + "/clockwork/worker/task/checkCustomerCanRun", null,
                gson.toJson(new BatchTaskInfo(gson.toJson(tasks), "t5", "true")));
        log.info(result);
        log.info("==================");

    }


}
