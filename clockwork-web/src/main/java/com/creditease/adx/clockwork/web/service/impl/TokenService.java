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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUser;
import com.creditease.adx.clockwork.common.enums.TokenStatus;
import com.creditease.adx.clockwork.common.util.JWTUtil;
import com.creditease.adx.clockwork.web.service.ITokenService;
import com.creditease.adx.clockwork.web.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:08 下午 2020/1/13
 * @ Description：TokenService
 * @ Modified By：
 */
@Service
public class TokenService implements ITokenService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private IUserService userService;

    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    @Value("${jwt.accessToken.timeout}")
    private Long accessTokenTimeout;

    @Value("${jwt.refreshToken.timeout}")
    private Long refreshTokenTimeout;

    @Override
    public String  getAccessTokenType(){
        return ACCESS_TOKEN;
    }

    @Override
    public String  getRefreshTokenType(){
        return REFRESH_TOKEN;
    }

    /**
     * 创建token
     *
     * @param username  账号
     * @param password  密码 已经加密
     * @param tokenType 类型 ACCESS_TOKEN|REFRESH_TOKEN
     * @return
     */
    @Override
    public String createToken(String username, String password, String tokenType) {
        int index = username.indexOf('@');
        if (index != -1) username = username.substring(0, index);
        if (ACCESS_TOKEN.equals(tokenType)) {
            return JWTUtil.sign(username, password, accessTokenTimeout);
        } else {
            return JWTUtil.sign(username, password, refreshTokenTimeout);
        }
    }

    /**
     * 校验token
     *
     * @param token token
     * @return AdxCommonConstants.TokenStatus（SUCCESS、ERROR、TOKEN_EXPIRED）
     */
    @Override
    public TokenStatus checkToken(String token) {
        String username = JWTUtil.getUsername(token);
        if (username == null) {
            LOG.error("token can't get matching username, token = {}", token);
            return TokenStatus.ERROR;
        }

        // 获取用户信息
        TbClockworkUser userInfo = userService.getUserByUserName(username);
        if (userInfo == null) {
            return TokenStatus.ERROR;
        }
        // 校验token(返回以下三种情况)
        // AdxCommonConstants.TokenStatus.SUCCESS
        // AdxCommonConstants.TokenStatus.ERROR
        // AdxCommonConstants.TokenStatus.TOKEN_EXPIRED
        TokenStatus verify = JWTUtil.verify(token, username, userInfo.getPassword());
        LOG.info("TokenService-checkToken username = {}, verify token status = {}", username, verify.getValue());
        return verify;
    }

    /**
     * 根据refreshToken重新刷新token
     *
     * @param refreshToken refreshToken
     * @return accessToken__refreshToken
     */
    @Override
    public String refreshToken(String refreshToken) {
        // 检查refreshToken是否过期
        TokenStatus tokenStatusEnum = checkToken(refreshToken);

        // 没过期
        if (TokenStatus.SUCCESS.getValue().equals(tokenStatusEnum.getValue())) {
            // 则重新生成accessToken和refreshToken
            String username = JWTUtil.getUsername(refreshToken);

            // 获取用户信息
            TbClockworkUser userInfo = userService.getUserByUserName(username);
            if (userInfo == null) {
                return null;
            }

            // 重新创建token
            String accessToken = createToken(username, userInfo.getPassword(), ACCESS_TOKEN);
            refreshToken = createToken(username, userInfo.getPassword(), REFRESH_TOKEN);

            LOG.info("TokenService-refreshToken {} = {} , {} = {}", ACCESS_TOKEN, accessToken, REFRESH_TOKEN, refreshToken);
            return accessToken + "__" + refreshToken;
        } else {
            LOG.error("refreshToken failure");
            return null;
        }
    }
}
