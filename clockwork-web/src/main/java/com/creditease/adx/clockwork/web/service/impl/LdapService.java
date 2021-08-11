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

package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.web.service.ILdapService;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.util.Hashtable;
import java.util.List;

/**
 * LdapService
 */
@Service
public class LdapService implements ILdapService {

    private static final Logger LOG = LoggerFactory.getLogger(LdapService.class);

    @Autowired
    private LdapTemplate ldapTemplate;

    @SuppressWarnings("unused")
	private DirContext dc = null;

    @Value("${spring.ldap.urls}")
    private String LDAP_URL;

    @Value("${spring.ldap.base}")
    private String BASE_DN;

    @Value("${spring.ldap.queryBase}")
    private String LDAP_QUERY_BASE;

    private final String LDAP_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    /**
     * 检查用户名和密码是否匹配
     */
    @Override
    public boolean ldapLogin(String username, String password) {
        LOG.info("[LdapService-ldapLogin] checkUser username = {}", username);

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_FACTORY);
        env.put(Context.PROVIDER_URL, LDAP_URL);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            // 初始化上下文
            dc = new InitialDirContext(env);
            return true;
        } catch (javax.naming.AuthenticationException e) {
            LOG.error("[LdapService-ldapLogin] AuthenticationException username = {}", username, e);
        } catch (Exception e) {
            LOG.error("[LdapService-ldapLogin] Exception username = {}", username, e);
        }
        return false;
    }

    /**
     * 根据邮箱从LDAP中调取员工组织信息
     */
    @Override
    public String getOrgInfoByEmail(String email_address) {
        List<String> info = ldapTemplate.search(
                LdapQueryBuilder.query().base(LDAP_QUERY_BASE).where("userPrincipalName").is(email_address),
                new AttributesMapper<String>() {
                    public String mapFromAttributes(Attributes attrs)
                            throws NamingException {
                        return attrs.get("distinguishedName").get().toString();
                    }
                });
        if (CollectionUtils.isEmpty(info)) {
            return null;
        }
        String[] orgInfoAry = info.get(0).split(",OU=");
        StringBuilder sb = new StringBuilder();
        for (int i = orgInfoAry.length - 3; i > 0; i--) {
            sb.append(orgInfoAry[i]).append("/");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

}
