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

package com.creditease.adx.clockwork.master.api;

import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoRouTine;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:21 2019-07-22
 * @ Description：TestRestApi
 * @ Modified By：
 */
@Slf4j
public class TestRestApi {


    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static String baseUrl = "http://127.0.0.1:9006";

    @Test
    public void TestDistribute() throws Exception {

        List<TbClockworkTaskPojo> taskPojoList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {

            TbClockworkTaskPojo task = new TbClockworkTaskPojo();
            task.setId(1687 + i);
            taskPojoList.add(task);

            TaskSubmitInfoRouTine taskSubmitInfoRouTine = new TaskSubmitInfoRouTine(taskPojoList);

            log.info("==================");
            String result = HttpUtil.postByBody(
                    baseUrl + "/clockwork/master/task/distribute/routine", null, gson.toJson(taskSubmitInfoRouTine));
            log.info(result);
            log.info("==================");
        }


    }


}
