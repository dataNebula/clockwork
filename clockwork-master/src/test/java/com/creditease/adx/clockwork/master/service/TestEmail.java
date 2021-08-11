package com.creditease.adx.clockwork.master.service;

import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:06 下午 2020/12/8
 * @ Description：
 * @ Modified By：
 */
public class TestEmail {

    private static final Logger LOG = LoggerFactory.getLogger(TestEmail.class);


    @Test
    public void sendEmailTest(){
        Integer taskId = 12180;
        String content = String.format("taskId：%s, taskName：%s, status：%s, startTime：%s, endTime：%s.",
                taskId,
                "test_task_01",
                "running",
                new Date(),
                TaskStatusUtil.isFinishedTaskStatus("running") ? new Date() : null
        );

        Map<String, Object> params = new HashMap<>();
        params.put("phones", "xxxxx");
        params.put("content", content);
        String result = HttpUtil.post("http://127.0.0.1:8080/alarm/sendmessage", params);


        LOG.info("content = {} result = {}.", content, result);

    }
}
