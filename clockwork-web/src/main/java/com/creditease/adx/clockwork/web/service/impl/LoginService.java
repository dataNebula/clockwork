package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.web.service.ILoginService;
import com.creditease.adx.clockwork.web.service.ITokenService;
import com.creditease.adx.clockwork.web.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:58 下午 2020/9/22
 * @ Description：
 * @ Modified By：
 */
@Service
public class LoginService implements ILoginService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private ITokenService tokenService;

    @Override
    public String login(String username, String password) {
        try {
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                LOG.error("LoginService-login, invalid username or password, username = {}, password = {}",
                        username, password);
                return null;
            }

            if (userService.checkUser(username, password)) {
                // Md5密码
                password = new Md5Hash(password, userService.getMd5Salt(), 1024).toBase64();

                // 创建accessToken
                String accessTokenType = tokenService.getAccessTokenType();
                String accessToken = tokenService.createToken(username, password, accessTokenType);

                // 创建refreshToken
                String refreshTokenType = tokenService.getRefreshTokenType();
                String refreshToken = tokenService.createToken(username, password, refreshTokenType);

                LOG.info("login, username = {}, {} = {}, {} = {}",
                        username, accessTokenType, accessToken, refreshTokenType, refreshToken);
                return accessToken + "__" + refreshToken;
            }
        } catch (Exception e) {
            LOG.error("UserController-login Error {}.", e.getMessage(), e);
        }
        return null;
    }


}
