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
import com.creditease.adx.clockwork.web.service.ITokenService;
import io.swagger.annotations.Api;
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
 * token服务类
 * <p>
 * 刷新，校验功能
 */
@Api("token服务类")
@RestController
@RequestMapping("/clockwork/web/token")
public class TokenController {

    private static final Logger LOG = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private ITokenService tokenService;

    /**
     * 验证token是否过期
     *
     * @param token
     * @return
     */
    @PostMapping(value = "/checkToken")
    public Map<String, Object> checkToken(@RequestParam("token") String token) {
        if (StringUtils.isBlank(token)) {
            LOG.error("UserController-checkToken, invalid token");
            return Response.fail("invalid token, token is blank");
        }

        try {
            // 获取token
            // AdxCommonConstants.TokenStatus（SUCCESS、ERROR、TOKEN_EXPIRED）
            TokenStatus tokenStatusEnum = tokenService.checkToken(token);

            // 处理
            String tokenStatus = tokenStatusEnum.getValue();
            if (TokenStatus.SUCCESS.getValue().equals(tokenStatus)) {
                return Response.success(true);
            } else {
                // 如果token失效返回ACCESS_TOKEN_EXPIRED
                if (TokenStatus.TOKEN_EXPIRED.getValue().equals(tokenStatus)) {
                    tokenStatus = TokenStatus.ACCESS_TOKEN_EXPIRED.getValue();
                }
                return Response.fail(tokenStatus, "");
            }
        } catch (Exception e) {
            LOG.error("UserController-refreshToken Error {}", e.getMessage(), e);
            return Response.fail(TokenStatus.ERROR, e.getMessage());
        }
    }

}
