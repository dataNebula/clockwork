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

package com.creditease.adx.clockwork.dfs.config;

import com.creditease.adx.clockwork.dfs.service.impl.hdfs.HDfsService;
import com.creditease.adx.clockwork.dfs.service.IDfsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:08 下午 2020/1/10
 * @ Description：AppConfig
 * @ Modified By：Created in 4:22 下午 2020/1/15
 */
@Configuration
public class AppConfig {

    // Dfs 实现类 HDfsService
    @Bean
    @Qualifier("iDfsService")
    @ConditionalOnProperty(name = "spring.dfs.enable", havingValue = "HDfsService")
    public IDfsService getHDfsService() {
        return new HDfsService();
    }

}
