package com.creditease.adx.clockwork.api.api;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:38 上午 2020/11/27
 * @ Description：
 * @ Modified By：
 */
public class TestTaskStopApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestTaskStopApi.class);

    @Test
    public void stopTaskTest() {


        Map<String, Object> params = new HashMap<>();
        params.put("taskId",12181);

        LOG.info("==================");
        String result = HttpUtil.post("http://t1:9005/clockwork/api/task/stop/stopTask", params);
        LOG.info(result);
        LOG.info("==================");

    }

    @Test
    public void stopTaskListTest() throws IOException{


        ArrayList<Integer> taskIds = new ArrayList<>();
        taskIds.add(12167);
        taskIds.add(12166);
        taskIds.add(12165);
        taskIds.add(12170);
        taskIds.add(12180);
        taskIds.add(12179);
        taskIds.add(12171);
        taskIds.add(12173);
        taskIds.add(12181);

        LOG.info("==================");
        String result = HttpUtil.postByBody("http://t1:9005/clockwork/api/task/stop/stopTaskList", null,
                JSONObject.toJSONString(taskIds));
        LOG.info(result);
        LOG.info("==================");

    }




}
