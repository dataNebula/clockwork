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

public interface ILdapService {


    /**
     * ldap 登陆
     *
     * @param username 用户名
     * @param password 密码
     * @return
     */
    boolean ldapLogin(String username, String password);

    /**
     * 根据邮箱从LDAP中调取员工组织信息
     */
    String getOrgInfoByEmail(String email_address);

}
