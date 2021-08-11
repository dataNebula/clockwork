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

package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.web.service.ILdapService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:26 2020-02-07
 * @ Description：TestLdapService
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestLdapService {


    @Autowired
    private ILdapService ldapService;

    @Test
    public void testCheckUser() throws Exception {
        String email = "adx@clockwork.com";
        String password = "clockwork";
        boolean b = ldapService.ldapLogin(email, password);
        System.out.println(b);

    }


    @Test
    public void getOrgInfoByEmail() throws Exception {
        String orgInfoByEmail = ldapService.getOrgInfoByEmail("xuandongtang@clockwork.com");
        System.out.println(orgInfoByEmail);

    }


}
