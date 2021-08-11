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

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.enums.TokenStatus;
import com.creditease.adx.clockwork.common.util.JWTUtil;
import com.creditease.adx.clockwork.web.service.ILoginService;
import com.creditease.adx.clockwork.web.service.ITokenService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:12 2019-10-17
 * @ Description：登陆/登出服务
 * @ Modified By：
 */
@RestController
@RequestMapping("/clockwork/web/login")
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private ILoginService loginService;

    @Autowired
    private ITokenService tokenService;

    /**
     * 登录，根据用户密码 生成token。
     *
     * @param username 账号
     * @param password 密码
     * @return
     */
    @PostMapping(value = "/in")
    public Map<String, Object> login(@RequestParam("username") String username,
                                     @RequestParam("password") String password) {
        try {
            // 参数校验
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                LOG.error("LoginController-login, invalid username or password, username = {}, password = {}",
                        username, password);
                return Response.fail("invalid username or password");
            }

            // 验证用户名和密码
            String tokens = loginService.login(username, password);
            if (tokens != null) {
                return Response.success(tokens);
            } else {
                return Response.fail("username or password wrong");
            }
        } catch (Exception e) {
            LOG.error("UserController-login Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 登出，从缓存里移除user。
     *
     * @param accessToken  aToken
     * @param refreshToken rToken
     * @return
     */
    @PostMapping(value = "/out")
    public Map<String, Object> logout(@RequestParam("accessToken") String accessToken,
                                      @RequestParam("refreshToken") String refreshToken) {
        try {
            // 参数校验
            if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(refreshToken)) {
                LOG.error("LoginController-logout, invalid accessToken or refreshToken");
                return Response.fail("invalid accessToken or refreshToken");
            }

            // 获取username
            String username = JWTUtil.getUsername(accessToken);
            if (username == null) {
                LOG.error("token can't get matching username");
                return Response.fail(TokenStatus.ERROR);
            }
            return Response.success(null);
        } catch (Exception e) {
            LOG.error("UserController-getOrgInfoByEmail Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据refreshToken 创建(刷新) accessToken,refreshToken
     *
     * @param refreshToken aToken
     * @return
     */
    @PostMapping(value = "/refresh")
    public Map<String, Object> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        try {
            // 参数校验
            if (StringUtils.isBlank(refreshToken)) {
                LOG.error("TokenController-refreshToken, invalid refreshToken");
                return Response.fail("invalid refreshToken");
            }

            String tokens = tokenService.refreshToken(refreshToken);
            if (tokens != null) {
                return Response.success(tokens);
            } else {
                LOG.error("refreshToken failure");
                // 过期的话则直接返回失败，前端则需要重新登录
                return Response.fail(TokenStatus.REFRESH_TOKEN_EXPIRED);
            }
        } catch (Exception e) {
            LOG.error("refreshToken Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
