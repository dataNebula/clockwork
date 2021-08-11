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

import lombok.Data;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 15:27 2019-09-10
 * @ Description：
 * @ Modified By：
 */
@Data
public class KafkaConfig {

    private static Boolean krb5Enable;

    private static String brokers;

    private static String securityProtocol;

    public static void setBrokers(String brokers) {
        KafkaConfig.brokers = brokers;
    }

    public static void setKrb5Enable(Boolean krb5Enable) {
        KafkaConfig.krb5Enable = krb5Enable;
    }

    public static void setSecurityProtocol(String securityProtocol) {
        KafkaConfig.securityProtocol = securityProtocol;
    }

    public static Boolean getKrb5Enable() {
        return krb5Enable;
    }

    public static String getBrokers() {
        return brokers;
    }

    public static String getSecurityProtocol() {
        return securityProtocol;
    }
}
