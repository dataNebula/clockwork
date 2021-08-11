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

package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.web.service.impl.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:36 2019-06-25
 * @ Description：test
 * @ Modified By：
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(LoginController.class)
@ActiveProfiles("test")
public class TestLoginController {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LoginController loginController;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * 根据用户email 获取该用户的组织机构信息
     *
     * @throws Exception
     */
    @Test
    public void testGetOrgInfoByEmail() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.get("/clockwork/web/getOrgInfoByEmail")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .param("email", "xuandongtang@clockwork.com");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        int status = mvcResult.getResponse().getStatus();
        String content = mvcResult.getResponse().getContentAsString();

        Assert.assertEquals(200, mvcResult.getResponse().getStatus());

        System.out.println("返回结果：" + status);
        System.out.println(content);
    }


    /**
     * 验证账号和密码是否匹配
     *
     * @throws Exception
     */
    @Test
    public void testCheckUser() throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.get("/clockwork/web/checkUser")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .param("username", "xuandongtang@clockwork.com")
                .param("password", "clockwork");

        MvcResult mvcResult = mvc.perform(request).andReturn();
        int status = mvcResult.getResponse().getStatus();
        String content = mvcResult.getResponse().getContentAsString();

        Assert.assertEquals(200, mvcResult.getResponse().getStatus());

        System.out.println("返回结果：" + status);
        System.out.println(content);
    }


}
