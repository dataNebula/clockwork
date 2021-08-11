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

package com.creditease.adx.clockwork.api.kafka;

import com.creditease.adx.clockwork.api.util.KafkaSenderUtil;
import com.creditease.adx.clockwork.common.entity.kafka.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogFlowPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:24 2019-09-05
 * @ Description：发送记录到kafka
 * @ Modified By：
 */
@Service
public class KafkaTaskLogFlowSender implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaTaskLogFlowSender.class);

    private final BlockingQueue<TbClockworkTaskLogFlowPojo> KAFKA_LIFECYCLE_QUEUE = new LinkedBlockingQueue<>();

    @Value("${spring.kafka.task.record.topic}")
    private String TASK_RECORD_TOPIC;

    @PostConstruct
    public void setup() {
        Thread thread = new Thread(this);
        thread.setName("Kafka-TaskLogFlow-thread");
        thread.start();
        LOG.info("[KafkaTaskLogFlowSender-setup]The thread already started");
    }


    @Override
    public void run() {
        // 批量列表
        List<TbClockworkTaskLogFlowPojo> batch = new ArrayList<>();
        while (true) {
            TbClockworkTaskLogFlowPojo queue;
            try {
                if ((queue = KAFKA_LIFECYCLE_QUEUE.poll(3, TimeUnit.SECONDS)) == null) {
                    LOG.debug("[KafkaTaskLogFlowSender-Thead-run] Don't have task log flow info,skip current loop!");
                    if (!batch.isEmpty()) {
                        // 发送数据到Kafka
                        sendToKafka(batch);
                        batch.clear();
                    }
                    continue;
                }
                batch.add(queue);
                if (batch.size() > 100) {
                    // 发送数据到kafka
                    sendToKafka(batch);
                    batch.clear();
                }
            } catch (Exception e) {
                LOG.error("KafkaTaskLogFlowSender-run Error {}.", e.getMessage(), e);
            }
        }
    }

    /**
     * 发送task生命周期到kafka【task运行的每一个阶段】
     *
     * @param recordPojos
     * @return
     */
    private boolean sendToKafka(List<TbClockworkTaskLogFlowPojo> recordPojos) {
        // Schema
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("data_type", "string", false));
        fields.add(new Field("ums_ts_", "datetime", false));
        fields.add(new Field("task_id", "int", false));
        fields.add(new Field("task_name", "string", false));
        fields.add(new Field("log_id", "long", false));
        fields.add(new Field("group_id", "int", false));
        fields.add(new Field("node_id", "int", false));
        fields.add(new Field("status", "string", false));
        fields.add(new Field("operation_type", "int", false));
        fields.add(new Field("trigger_mode", "int", false));
        fields.add(new Field("start_time", "datetime", false));
        fields.add(new Field("end_time", "datetime", true));
        fields.add(new Field("duration", "int", true));
        fields.add(new Field("create_time", "datetime", false));
        fields.add(new Field("is_end", "boolean", false));

        // Payloads
        ArrayList<Payload> payloads = new ArrayList<>(recordPojos.size());
        List<String> tuple = null;
        for (TbClockworkTaskLogFlowPojo recordPojo : recordPojos) {
            tuple = new ArrayList<>();
            tuple.add("clockwork_task_life_cycle_record_report");
            tuple.add(DateUtil.getNowTime());
            tuple.add(String.valueOf(recordPojo.getTaskId()));
            tuple.add(recordPojo.getTaskName());
            tuple.add(String.valueOf(recordPojo.getLogId()));
            tuple.add(String.valueOf(recordPojo.getGroupId()));
            tuple.add(String.valueOf(recordPojo.getNodeId()));
            tuple.add(recordPojo.getStatus());
            tuple.add(String.valueOf(recordPojo.getOperationType()));
            tuple.add(String.valueOf(recordPojo.getTriggerMode()));
            tuple.add(DateUtil.dateTimeStampToString(recordPojo.getStartTime()));
            tuple.add(recordPojo.getEndTime() == null ? "" : DateUtil.dateTimeStampToString(recordPojo.getEndTime()));
            tuple.add(recordPojo.getDuration() == null ? "-1" : String.valueOf(recordPojo.getDuration()));
            tuple.add(DateUtil.dateToString(recordPojo.getCreateTime()));
            tuple.add(String.valueOf(recordPojo.getIsEnd()));
            payloads.add(new Payload(tuple));
        }
        // send data to Kafka
        try {
            KafkaSenderUtil.send(TASK_RECORD_TOPIC, new KafkaMsg(
                    new Protocol("clockwork_task_life_cycle_record_report"),
                    new Schema(fields),
                    payloads));
        } catch (Exception e) {
            LOG.error("send kafka clockwork_task_life_cycle_record_report error:", e);
            return false;
        }
        return true;
    }

    public BlockingQueue<TbClockworkTaskLogFlowPojo> getKafkaTaskLogFlowQueue() {
        return KAFKA_LIFECYCLE_QUEUE;
    }
}
