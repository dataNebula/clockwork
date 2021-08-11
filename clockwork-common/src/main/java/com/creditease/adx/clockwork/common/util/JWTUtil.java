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

package com.creditease.adx.clockwork.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.creditease.adx.clockwork.common.enums.TokenStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * 生成token、校验token、通过token获取账号
 */
public class JWTUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JWTUtil.class);

    private static final String JWT_KEY = "username";

    /**
     * 通过token获取username
     *
     * @param token token
     * @return username
     */
    public static String getUsername(String token) {
        try {
            if (token == null) {
                return null;
            }
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(JWT_KEY).asString();
        } catch (JWTDecodeException e) {
            LOG.error("JWTUtil getAccount Error {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成签名（token）
     *
     * @param username    账号
     * @param password   密码
     * @param expireTime 过期时间（分钟）
     * @return token
     */
    public static String sign(String username, String password, long expireTime) {
        try {
            Date date = new Date(System.currentTimeMillis() + expireTime * 60 * 1000);
            Algorithm algorithm = Algorithm.HMAC256(password);
            return JWT.create()
                    .withClaim(JWT_KEY, username)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (UnsupportedEncodingException e) {
            LOG.error("JWTUtil sign Error {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 校验签名（token）是否正确
     *
     * @param token    密钥
     * @param username  账号
     * @param password 密码
     * @return AdxCommonConstants.TokenStatus
     */
    public static TokenStatus verify(String token, String username, String password) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(password);
            JWTVerifier verifier = JWT.require(algorithm).withClaim(JWT_KEY, username).build();
            verifier.verify(token);
            return TokenStatus.SUCCESS;
        } catch (TokenExpiredException e) {
            LOG.error("JWTUtil TokenStatus-TOKEN_EXPIRED verify Error {} ", e.getMessage(), e);
            return TokenStatus.TOKEN_EXPIRED;
        } catch (Exception e) {
            LOG.error("JWTUtil TokenStatus-ERROR verify Error {} ", e.getMessage(), e);
            return TokenStatus.ERROR;
        }
    }


}
