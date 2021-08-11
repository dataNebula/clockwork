package com.creditease.adx.clockwork.web.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.creditease.adx.clockwork.common.enums.TokenStatus;
import com.creditease.adx.clockwork.web.config.FilterConfig;
import com.creditease.adx.clockwork.web.service.impl.TokenService;

/**
 * 登陆token校验
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:40 下午 2020/8/10
 * @ Description：异常返回401
 * @ Modified By：
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(TokenInterceptor.class);

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return false;
        }

        // 从cookie中获取token
        String token = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(FilterConfig.COOKIE_ACCESS_TOKEN_KEY)) {
                token = cookie.getValue();
                break;
            }
        }

        String tokenStatus = TokenStatus.ERROR.getValue();

        try {
            // TOKEN-校验
            // AdxCommonConstants.TokenStatus（SUCCESS、ERROR、TOKEN_EXPIRED）
            TokenStatus tokenStatusEnum = tokenService.checkToken(token);

            // TOKEN成功：SUCCESS
            tokenStatus = tokenStatusEnum.getValue();
            if (TokenStatus.SUCCESS.getValue().equals(tokenStatus)) {
                return true;
            }
            // TOKEN失效：ACCESS_TOKEN_EXPIRED
            if (TokenStatus.TOKEN_EXPIRED.getValue().equals(tokenStatus)) {
                tokenStatus = TokenStatus.ACCESS_TOKEN_EXPIRED.getValue();
            }
        } catch (Exception e) {
            LOG.error("TokenInterceptor Error {}", e.getMessage(), e);
        }
        // TOKEN错误或失效返回401
        response.setStatus(401);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(tokenStatus);
        LOG.error("TokenInterceptor-preHandle error, path = {}", request.getContextPath());
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
