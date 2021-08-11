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

package com.creditease.adx.clockwork.dfs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import javax.servlet.MultipartConfigElement;
import java.io.File;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.creditease.adx.clockwork.client")
@EnableEurekaClient
@ComponentScan({
        "com.creditease.adx.clockwork.dfs",
        "com.creditease.adx.clockwork.redis",
        "com.creditease.adx.clockwork.client"
})
public class DfsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DfsApplication.class, args);
    }

    @Value("${temporary.upload.location}")
    private String tmpDir;

    /**
     * 文件上传临时路径
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        File file = new File(tmpDir);
        if(!file.exists()){
            file.mkdirs();
        }
        factory.setLocation(tmpDir);
        return factory.createMultipartConfig();
    }

}
