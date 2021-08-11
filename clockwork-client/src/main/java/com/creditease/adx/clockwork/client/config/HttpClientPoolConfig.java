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

package com.creditease.adx.clockwork.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:01 下午 2020/5/26
 * @ Description：Java配置的优先级低于yml配置
 * @ Modified By：
 */
@Component
@ConfigurationProperties(prefix = "spring.http-client.pool")
@Data
public class HttpClientPoolConfig {

    // 连接池的最大连接数
    private Integer maxTotalConnect ;

    // 同路由的并发数
    private Integer maxConnectPerRoute ;

    // 连接超时，默认2s
    private Integer connectTimeout = 2 * 1000;

    // 读超时，默认30s
    private Integer readTimeout = 30 * 1000;

    // 从连接池获取连接的超时时间，单位ms
    private Integer connectionRequestTimout = 200;

    // 针对不同的地址,特别设置不同的长连接保持时间
    private Map<String,Integer> keepAliveTargetHost;

    // 针对不同的地址,特别设置不同的长连接保持时间,单位 s
    private Integer keepAliveTime = 60;

    // 重试次数
    private Integer retryTimes = 3;

    private String charset = "UTF-8";

}
