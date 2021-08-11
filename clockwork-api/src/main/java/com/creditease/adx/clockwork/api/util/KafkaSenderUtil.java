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

import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.creditease.adx.clockwork.api.config.KafkaConfig;
import com.creditease.adx.clockwork.common.entity.kafka.KafkaMsg;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:45 2019-09-03
 * @ Description：KafkaSender
 * @ Modified By：
 */
public class KafkaSenderUtil {

    private static Logger LOG = LoggerFactory.getLogger(KafkaSenderUtil.class);
    private static Producer<String, String> producer = null;

    static {
        if (producer == null) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.getBrokers());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, 2);   // 重试次数
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 163840);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 335544320);
            props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 120000);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

            if (KafkaConfig.getKrb5Enable() != null && KafkaConfig.getKrb5Enable()) {
                props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, KafkaConfig.getSecurityProtocol());
            }
            producer = new KafkaProducer<>(props);
        }
    }

    /**
     * Send message.
     *
     * @param topic    topic
     * @param kafkaMsg msg
     */
    public static void send(String topic, KafkaMsg kafkaMsg) {
        String kafkaMsgJson = JSON.toJSONString(kafkaMsg);
        LOG.info("Kafka发送的数据：topic = {}, {}", topic, kafkaMsgJson);
        producer.send(new ProducerRecord<>(topic, kafkaMsg.getProtocol().getType(), kafkaMsgJson), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e != null) {
                    LOG.info("Kafka发送的数据：fail! topic={}", topic);
                    LOG.info(e.getMessage(), e);
                    return;
                }
                LOG.info("Kafka发送的数据：success! topic={}", topic);
            }
        });
    }

    public static void sendConsanguinityAnalysis(String topic, String kafkaMsgJson) {
        LOG.info("Kafka发送的血缘数据：topic={}, {}", topic, kafkaMsgJson);
        producer.send(new ProducerRecord<>(topic, null, kafkaMsgJson), new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e != null) {
                    LOG.info("Kafka发送的血缘数据：fail! topic = {}", topic);
                    LOG.info(e.getMessage(), e);
                    return;
                }
                LOG.info("Kafka发送的血缘数据：success! topic = {}", topic);
            }
        });
    }

}

