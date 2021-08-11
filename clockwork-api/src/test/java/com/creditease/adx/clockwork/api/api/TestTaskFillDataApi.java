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

package com.creditease.adx.clockwork.api.api;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.TaskFillDataEntity;
import com.creditease.adx.clockwork.common.enums.FillDataType;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:10 2019-12-30
 * @ Description：
 * @ Modified By：
 */
public class TestTaskFillDataApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestTaskFillDataApi.class);


    @Test
    public void fillData() throws IOException, ParseException {

        TaskFillDataEntity param = new TaskFillDataEntity();
        param.setOperatorName("xuandongtang");
        param.setDescription("Test_"+ DateUtil.getNowTime());
        param.setFillDataType(FillDataType.HOUR.getType());

        List<String> dates = new ArrayList<>();
        dates.add("2020-10-01 10");



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(sdf.parse("2020-10-12 10"));


        System.out.println(dates);
        List<Integer> taskIds = new ArrayList<>();
        taskIds.add(5223);
        taskIds.add(5228);
//        taskIds.add(1698);

//        taskIds.add(1838); // 10_success 依赖1839
//        taskIds.add(1839); // 15_success


        param.setFillDataTimes(dates);
        param.setTaskIds(taskIds);
        param.setTaskGroupAliasName(param.getDescription());


        LOG.info("==================");
        System.out.println(JSONObject.toJSONString(param));
        String result = HttpUtil.postByBody("http://127.0.0.1:9005/clockwork/api/task/submit/fillData", null,
                JSONObject.toJSONString(param));
        LOG.info(result);
        LOG.info("==================");

    }


    @Test
    public void searchPageList() throws IOException {

        PageParam param = new PageParam();
        param.setPageNum(1);
        param.setPageSize(10);
        param.setUserName("xuandongtang@clockwork.com");

        param.setCondition("{\"rerunBatchNumber\":178550047569346600}");


        LOG.info("==================");
        String result = HttpUtil.postByBody("http://127.0.0.1:9005/clockwork/api/task/fillData/searchPageList", null,
                JSONObject.toJSONString(param));
        LOG.info(result);
        LOG.info("==================");

    }

    @Test
    public void getTasksByReRunBatchNumber() throws IOException {

        HashMap<String, Object> parameter = new HashMap<>();
        parameter.put("rerunBatchNumber",178550047569346600L);

        LOG.info("==================");
        String result = HttpUtil.get(
                "http://127.0.0.1:9005/clockwork/api/task/fillData/getTasksByReRunBatchNumber",
                parameter,
                null);
        LOG.info(result);
        LOG.info("==================");

    }

    @Test
    public void getTaskLogsByReRunBNAndTaskId() throws IOException {

        HashMap<String, Object> parameter = new HashMap<>();
        parameter.put("rerunBatchNumber",178550047569346600L);
        parameter.put("taskId",1687);

        LOG.info("==================");
        String result = HttpUtil.get(
                "http://127.0.0.1:9005/clockwork/api/task/fillData/getTaskLogsByReRunBNAndTaskId",
                parameter,
                null);
        LOG.info(result);
        LOG.info("==================");

    }


    @Test
    public void stopFillDataTask() throws IOException {

        HashMap<String, Object> parameter = new HashMap<>();
        parameter.put("rerunBatchNumber","180233725215244328");

        LOG.info("==================");
        String result = HttpUtil.post(
                "http://127.0.0.1:9005/clockwork/api/task/stop/stopFillDataTask",
                parameter);
        LOG.info(result);
        LOG.info("==================");

    }


}
