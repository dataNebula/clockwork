package com.creditease.adx.clockwork.web.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.enums.TokenStatus;
import com.creditease.adx.clockwork.common.util.JWTUtil;
import com.creditease.adx.clockwork.web.config.FilterConfig;

/**
 * 权限校验
 * 目前权限校验教简单，仅拦截web校验token和username是否匹配
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 12:39 下午 2020/8/12
 * @ Description：异常返回401
 * @ Modified By：
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private Logger LOG = LoggerFactory.getLogger(AuthInterceptor.class);

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

        // userName可能不存在于request中，则去body中取
        String userName = request.getParameter("userName");
        if (StringUtils.isBlank(userName) && isJson(request)) {
            String body = new RequestWrapper(request).getBodyString();
            PageParam pageParam = JSON.parseObject(body, PageParam.class);
            if (pageParam != null && !StringUtils.isBlank(pageParam.getUserName())) {
                userName = pageParam.getUserName();
            }
        }

        // token验证
        String tokenStatus = TokenStatus.ERROR.getValue();
        if (!StringUtils.isBlank(token) && !StringUtils.isBlank(userName)) {
            String tokenUserName = JWTUtil.getUsername(token);
            if (userName.equals(tokenUserName)) {
                return true;
            } else {
                LOG.error("AuthInterceptor-preHandle tokenError,userName = {}, tUserName = {}",
                        userName, tokenUserName);
            }
        }
        // token验证错误
        response.setStatus(401);
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(tokenStatus);
        LOG.error("AuthInterceptor-preHandle error, path = {}", request.getContextPath());
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }

    /**
     * 判断本次请求的数据类型是否为json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJson(HttpServletRequest request) {
        if (request.getContentType() != null) {
            return request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE) ||
                    request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE);
        }

        return false;
    }
}
