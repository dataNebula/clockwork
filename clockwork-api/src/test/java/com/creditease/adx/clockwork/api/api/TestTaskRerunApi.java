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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:34 2019-12-06
 * @ Description：
 * @ Modified By：
 */
public class TestTaskRerunApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestTaskRerunApi.class);

    @Test
    public void preProcessReRunTask() throws IOException {


        Map<String, Object> params = new HashMap<>();
        params.put("taskId","1698");
        params.put("taskReRunType","-1");
        params.put("operatorName","xuandongtang@clockwork.com");

        LOG.info("==================");
        String result = HttpUtil.post("http://127.0.0.1:9005/clockwork/api/task/submit/rerunTask", params);
        LOG.info(result);
        LOG.info("==================");

    }




    @Test
    public void buildReRunTaskSubmitInfoTx() throws IOException {


        String str = "[{\"id\":1782,\"name\":\"test_12\",\"description\":\"test_12\",\"location\":\"/user/adx/clockwork/dfs/shell/\",\"command\":\"sh /user/adx/clockwork/dfs/shell/test.sh\",\"scriptName\":\"test.sh\",\"scriptParameter\":\"\",\"status\":\"life_cycle_reset\",\"cronExp\":null,\"triggerMode\":0,\"dependencyId\":\"1694,1698,1687,1697,1779\",\"timeType\":null,\"runFrequency\":null,\"triggerTime\":null,\"groupId\":396,\"userName\":\"xuandongtang@clockwork.com\",\"isPrivate\":1,\"failedRetries\":0,\"emailList\":null,\"runTimeout\":null,\"lastStartTime\":\"2020-02-24 18:40:42\",\"lastEndTime\":\"2020-02-24 18:41:03\",\"runEngine\":null,\"parameter\":\"{\\\"$yesterday\\\":\\\"2020-02-01\\\"}\",\"proxyUser\":null,\"expiredTime\":null,\"isFirst\":0,\"source\":0,\"operatorType\":\"adx\",\"operatorName\":\"xuandongtang@clockwork.com\",\"operatorName\":\"xuandongtang@clockwork.com\",\"online\":1,\"createTime\":\"2020-02-23 10:34:06\",\"updateTime\":\"2020-02-24 19:05:21\",\"businessInfo\":null,\"batchNumber\":null,\"groupName\":\"test_group_1\",\"userGroupName\":null},{\"id\":1694,\"name\":\"test_2\",\"description\":\"test_2\",\"location\":\"/user/adx/clockwork/dfs/shell/\",\"command\":\"sh /user/adx/clockwork/dfs/shell/test.sh\",\"scriptName\":\"test.sh\",\"scriptParameter\":\"\",\"status\":\"success\",\"cronExp\":null,\"triggerMode\":0,\"dependencyId\":\"1687\",\"timeType\":null,\"runFrequency\":null,\"triggerTime\":null,\"groupId\":396,\"userName\":\"xuandongtang@clockwork.com\",\"isPrivate\":1,\"failedRetries\":0,\"emailList\":null,\"runTimeout\":null,\"lastStartTime\":\"2020-02-23 11:11:21\",\"lastEndTime\":\"2020-02-23 11:11:42\",\"runEngine\":null,\"parameter\":null,\"proxyUser\":null,\"expiredTime\":null,\"isFirst\":0,\"source\":0,\"operatorType\":\"adx\",\"operatorName\":\"xuandongtang@clockwork.com\",\"operatorName\":\"xuandongtang@clockwork.com\",\"online\":1,\"createTime\":\"2020-02-10 10:49:57\",\"updateTime\":\"2020-02-23 11:07:05\",\"businessInfo\":null,\"batchNumber\":null,\"groupName\":\"test_group_1\",\"userGroupName\":null}]";
        List<TbClockworkTaskPojo> taskPojos = JSONArray.parseArray(str, TbClockworkTaskPojo.class);
//        TbClockworkTaskPojo taskPojo = taskPojos.get(0);
//
//
//        JSONArray objects = new JSONArray();
//        String parameter = JSONObject.toJSONString(taskPojo.getParameter());
//
//        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(taskPojo));
//        jsonObject.put("parameter",parameter);
//
//        System.out.println(jsonObject.toJSONString());
//        System.out.println(JSONObject.toJSONString(taskPojo));



        LOG.info("==================");
        String result = HttpUtil.postByBody("http://127.0.0.1:9005/clockwork/api/task/submit/rerunTaskList", null, JSONArray.toJSONString(taskPojos));
        LOG.info(result);
        LOG.info("==================");

    }
}
