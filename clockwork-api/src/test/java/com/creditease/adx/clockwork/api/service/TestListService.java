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

package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robert.vesta.service.intf.IdService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:05 2019-12-03
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestListService {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Value("${task.upload.path.prefix}")
    private String[] uploadPathPrefix = null;


    @Test
    public void uploadPathPrefixTest(){

        for (String pathPrefix : uploadPathPrefix) {
            System.out.println(pathPrefix);
        }

    }


    @Test
    public void uploadPathPrefixTest2() {

        Map<String, Object> interfaceResult = Response.success(uploadPathPrefix);
        String[] array = OBJECT_MAPPER.convertValue(
                interfaceResult.get(Constant.DATA), new TypeReference<String[]>() {
                });

        for (String s : array) {
            System.out.println(s);
        }


    }

}
