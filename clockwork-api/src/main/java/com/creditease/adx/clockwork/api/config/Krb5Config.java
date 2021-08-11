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

package com.creditease.adx.clockwork.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:27 2019-09-10
 * @ Description：
 * @ Modified By：
 */
@Component
public class Krb5Config implements ApplicationRunner {

    @Value("${java.security.krb5.enable}")
    private Boolean krb5Enable;

    @Value("${java.security.krb5.conf}")
    private String krb5Conf;

    @Value("${java.security.auth.login.config}")
    private String krb5LoginConfig;

    @Value("${spring.kafka.bootstrap-servers}")
    private String brokers;

    @Value("${spring.kafka.properties.security.protocol}")
    private String securityProtocol;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (krb5Enable != null && krb5Enable) {
            System.setProperty("java.security.krb5.conf", krb5Conf);
            System.setProperty("java.security.auth.login.config", krb5LoginConfig);
        }
        KafkaConfig.setBrokers(brokers);
        KafkaConfig.setKrb5Enable(krb5Enable);
        KafkaConfig.setSecurityProtocol(securityProtocol);
    }


}
