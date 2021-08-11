package com.creditease.adx.clockwork.web.config;

import com.creditease.adx.clockwork.web.interceptor.AuthInterceptor;
import com.creditease.adx.clockwork.web.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:43 下午 2020/8/10
 * @ Description：简单的token&auth拦截器
 * @ Modified By：
 */
@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor()).addPathPatterns(
                "/clockwork/web/dag/**"
                ,"/clockwork/web/graph/**"
                , "/clockwork/web/dashboard/**"
                , "/clockwork/web/dfs/**"
                , "/clockwork/web/file/**"
                , "/clockwork/web/node/**"
                , "/clockwork/web/task/**"
                , "/clockwork/web/token/**"
                , "/clockwork/web/user/**"
                , "/clockwork/web/role/**"
        );
        registry.addInterceptor(authInterceptor()).addPathPatterns(
                "/clockwork/web/graph/getTaskDagGraph"
                , "/clockwork/web/task/getTaskStatisticsByUserName"
                , "/clockwork/web/task/getTaskIdAndNameByUserName"
                , "/clockwork/web/task/searchPageListTask"
                , "/clockwork/web/task/group/getTaskGroupIdAndNameByUserName"
                , "/clockwork/web/task/group/getAllTaskGroupByUserName"
        );
    }

    @Bean
    public TokenInterceptor tokenInterceptor() {
        return new TokenInterceptor();
    }

    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }
}
