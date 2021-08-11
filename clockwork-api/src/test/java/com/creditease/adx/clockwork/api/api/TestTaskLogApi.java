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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.TaskLogSearchPageEntity;
import com.creditease.adx.clockwork.common.util.HttpUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:10 2019-12-30
 * @ Description：
 * @ Modified By：
 */
public class TestTaskLogApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestTaskLogApi.class);

    @Test
    public void searchPageList() throws IOException {

        PageParam param = new PageParam();
        param.setPageNum(1);
        param.setPageSize(5);
        param.setUserName("xuandongtang@clockwork.com");
//        param.setRole("admin");

        TaskLogSearchPageEntity searchPageEntity = new TaskLogSearchPageEntity();
        searchPageEntity.setTaskName("test_1");
        searchPageEntity.setStatus("success");
        searchPageEntity.setExecuteType(1);
        param.setCondition(JSONObject.toJSONString(searchPageEntity));

        System.out.println(param.getCondition());
//        param.setCondition("{\"startTime\":\"2020-03-24 11:12:12\",\"endTime\":\"2020-03-25 19:12:12\",\"taskAliasName\":\"test\"}");



        LOG.info("==================");
        String result = HttpUtil.postByBody("http://t1:9005/clockwork/api/task/log/searchPageList", null,
                JSONObject.toJSONString(param));
        LOG.info(result);
        LOG.info("==================");

    }
}
