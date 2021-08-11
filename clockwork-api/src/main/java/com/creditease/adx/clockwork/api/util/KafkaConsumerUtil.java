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

package com.creditease.adx.clockwork.api.util;

import com.creditease.adx.clockwork.api.config.KafkaConfig;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:45 2019-09-03
 * @ Description：用户测试
 * @ Modified By：
 */
public class KafkaConsumerUtil {

    private static Logger LOG = LoggerFactory.getLogger(KafkaConsumerUtil.class);
    private static Consumer<String, String> consumer = null;

    static {
        if (consumer == null) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.getBrokers());
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

            if (KafkaConfig.getKrb5Enable() != null && KafkaConfig.getKrb5Enable()) {
                props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, KafkaConfig.getSecurityProtocol());
            }
            consumer = new KafkaConsumer<String, String>(props);
        }
    }

    /**
     * consumer message.
     */
    public static void consumer(String topic) {
        while (true) {
            try {
                consumer.subscribe(Collections.singletonList(topic));
                ConsumerRecords<String, String> poll = consumer.poll(100);
                for (ConsumerRecord<String, String> record : poll) {
                    LOG.info("[consumer] offset={}, key={}, value={}", record.offset(), record.key(), record.value());
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

}

