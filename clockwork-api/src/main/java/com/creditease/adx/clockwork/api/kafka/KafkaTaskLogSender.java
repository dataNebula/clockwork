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
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
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
 * @ Date       ：Created in 18:13 2019-09-11
 * @ Description：发送TaskLog记录给kafka,只单纯的把队列中的数据发送到Kafka
 * @ Modified By：
 */
@Service
public class KafkaTaskLogSender implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaTaskLogSender.class);

    private final BlockingQueue<TbClockworkTaskLogPojo> KAFKA_TASK_LOG_QUEUE = new LinkedBlockingQueue<>();

    @Value("${spring.kafka.task.record.topic}")
    private String TASK_RECORD_TOPIC;

    @PostConstruct
    public void setup() {
        Thread thread = new Thread(this);
        thread.setName("Kafka-TaskLog-thread");
        thread.start();
        LOG.info("[KafkaTaskLogSender-setup]The thread already started");
    }


    @Override
    public void run() {
        // 批量列表
        List<TbClockworkTaskLogPojo> batch = new ArrayList<>();
        while (true) {
            try {
                TbClockworkTaskLogPojo queue = KAFKA_TASK_LOG_QUEUE.poll(3, TimeUnit.SECONDS);
                if (queue == null) {
                    LOG.debug("[KafkaTaskLogSender-Thead-run] Don't have task log flow info,skip current loop! batch.size = {}", batch.size());
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
                LOG.error("KafkaTaskLogSender-run Error {}.", e.getMessage(), e);
            }
        }
    }




    /**
     * 发送记录到Kafka
     *
     * @param recordPojos
     * @return
     */
    private boolean sendToKafka(List<TbClockworkTaskLogPojo> recordPojos) {
        LOG.info("[SysKafkaSenderTaskLogService]sendTaskLogToKafka! record.size={}", recordPojos.size());
        // Schema
        List<Field> fields = new ArrayList<>();
        fields.add(new Field("data_type", "string", false));
        fields.add(new Field("ums_ts_", "datetime", false));
        fields.add(new Field("id", "int", false));
        fields.add(new Field("task_id", "int", false));
        fields.add(new Field("task_name", "string", false));
        fields.add(new Field("group_id", "int", false));
        fields.add(new Field("node_id", "int", false));
        fields.add(new Field("status", "string", false));
        fields.add(new Field("pid", "int", true));
        fields.add(new Field("return_code", "int", true));
        fields.add(new Field("start_time", "datetime", false));
        fields.add(new Field("execute_time", "datetime", true));
        fields.add(new Field("running_time", "datetime", true));
        fields.add(new Field("end_time", "datetime", true));
        fields.add(new Field("duration", "int", true));
        fields.add(new Field("create_time", "datetime", false));
        fields.add(new Field("operation_type", "int", false));
        fields.add(new Field("is_end", "boolean", false));

        // Payloads
        ArrayList<Payload> payloads = new ArrayList<>(recordPojos.size());
        List<String> tuple = null;
        for (TbClockworkTaskLogPojo recordPojo : recordPojos) {
            tuple = new ArrayList<>();
            tuple.add("clockwork_task_process_status_report");
            tuple.add(DateUtil.getNowTime(DateUtil.DATE_TIME_STAMP_STR));
            tuple.add(String.valueOf(recordPojo.getId()));
            tuple.add(String.valueOf(recordPojo.getTaskId()));
            tuple.add(recordPojo.getTaskName());
            tuple.add(String.valueOf(recordPojo.getGroupId()));
            tuple.add(String.valueOf(recordPojo.getNodeId()));
            tuple.add(recordPojo.getStatus());
            tuple.add(String.valueOf(recordPojo.getPid()));
            tuple.add(String.valueOf(recordPojo.getReturnCode()));
            tuple.add(DateUtil.dateTimeStampToString(recordPojo.getStartTime()));
            tuple.add(recordPojo.getExecuteTime() == null ? "" : DateUtil.dateTimeStampToString(recordPojo.getExecuteTime()));
            tuple.add(recordPojo.getRunningTime() == null ? "" : DateUtil.dateTimeStampToString(recordPojo.getRunningTime()));
            tuple.add(recordPojo.getEndTime() == null ? "" : DateUtil.dateTimeStampToString(recordPojo.getEndTime()));
            tuple.add(recordPojo.getEndTime() == null ? "-1" :
                    String.valueOf(recordPojo.getEndTime().getTime() - recordPojo.getStartTime().getTime()));
            tuple.add(DateUtil.dateToString(recordPojo.getCreateTime()));
            tuple.add(String.valueOf(recordPojo.getOperationType()));
            tuple.add(String.valueOf(recordPojo.getIsEnd()));
            payloads.add(new Payload(tuple));
        }

        // send data to Kafka
        try {
            KafkaSenderUtil.send(TASK_RECORD_TOPIC, new KafkaMsg(
                    new Protocol("clockwork_task_process_status_report"),
                    new Schema(fields),
                    payloads));
        } catch (Exception e) {
            LOG.error("send kafka clockwork_task_process_status_report error:", e);
            return false;
        }
        return true;
    }

    /**
     * 获取队列
     *
     * @return
     */
    public BlockingQueue<TbClockworkTaskLogPojo> getKafkaTaskLogQueue() {
        return KAFKA_TASK_LOG_QUEUE;
    }

}
