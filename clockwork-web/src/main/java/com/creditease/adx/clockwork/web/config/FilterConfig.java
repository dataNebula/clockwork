package com.creditease.adx.clockwork.web.config;

import com.creditease.adx.clockwork.web.interceptor.ServletRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:09 2020/8/12
 * @ Description：FilterConfig 过滤器配置
 * @ Modified By：
 */
@Configuration
public class FilterConfig {

    // ACCESS-TOKEN
    public static final String COOKIE_ACCESS_TOKEN_KEY = "A-TOKEN";

    // REFRESH-TOKE
    public static final String COOKIE_REFRESH_TOKEN_KEY = "R-TOKEN";

    /**
     * 注册过滤器
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean someFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(servletRequestFilter());
        registration.addUrlPatterns(
                "/clockwork/web/task/searchPageListTask"
                , "/clockwork/web/task/group/getAllTaskGroupByUserName"
        );
        registration.setName("servletRequestFilter");
        return registration;
    }

    @Bean
    public Filter servletRequestFilter() {
        return new ServletRequestFilter();
    }
}
