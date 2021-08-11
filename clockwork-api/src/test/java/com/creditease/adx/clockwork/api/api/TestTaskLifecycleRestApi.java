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

package com.creditease.adx.clockwork.api.api;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.enums.TaskLifeCycleOpType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogFlowPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:21 2019-09-09
 * @ Description：TaskController,需要启动adx-clockwork-api
 * @ Modified By：
 */
public class TestTaskLifecycleRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestTaskLifecycleRestApi.class);

    /**
     * 1. TaskLifeCycleRecordController.addTaskLogFlow()
     */
    @Test
    public void addTaskLogFlow() throws IOException {
        //        String status = Constant.JOB_STATUS_SUBMIT;
//        String status = Constant.JOB_STATUS_ENABLE;
        String status = TaskStatus.SUCCESS.getValue();

        TbClockworkTaskPojo task = new TbClockworkTaskPojo();
        task.setGroupId(2);
        task.setId(2);
        task.setName("test1");
        task.setTriggerMode(TaskTriggerModel.TIME.getValue());
        int logId = 2;
        int nodeId = 1;


        TbClockworkTaskLogFlowPojo cycleRecord = new TbClockworkTaskLogFlowPojo();

        cycleRecord.setStatus(status);
        cycleRecord.setGroupId(task.getGroupId());
        cycleRecord.setTaskId(task.getId());
        cycleRecord.setTaskName(task.getName());
        cycleRecord.setTriggerMode(task.getTriggerMode());
        cycleRecord.setLogId(logId);
        cycleRecord.setNodeId(nodeId);
        cycleRecord.setCreateTime(DateUtil.getNowTimeStampDate());
        cycleRecord.setStartTime(DateUtil.getNowTimeStampDate());
        cycleRecord.setIsLast(true);
        cycleRecord.setOperationType(TaskLifeCycleOpType.BASE.getValue());
        cycleRecord.setDuration(-1);

        // 生命周期是否结束
        if (TaskStatus.ENABLE.getValue().equals(status)
                || TaskStatus.SUBMIT.getValue().equals(status)
                || TaskStatus.RUNNING.getValue().equals(status)) {
            cycleRecord.setIsEnd(false);
        } else {
            cycleRecord.setIsEnd(true);
        }


        LOG.info("==================");
        String result = HttpUtil.postByBody("http://localhost:9005//clockwork/api/task/log/flow/addTaskLogFlow", null,
                JSONObject.toJSONString(cycleRecord));
        LOG.info(result);
        LOG.info("==================");

    }


    @Test
    public void testGetTbClockworkTaskLifeCycleInfo() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("groupId", 303);
        params.put("startTime", "2019-09-12 17:00:16");

        LOG.info("==================");
        String result = HttpUtil.post("http://127.0.0.1:9005/clockwork/api/taskLifeCycleRecord/getTbClockworkTaskLifeCycleInfo", params);
//        String result = HttpUtils.post("http://localhost:9005/clockwork/api/taskLifeCycleRecord/getTbClockworkTaskLifeCycleInfo", params);
        LOG.info(result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String data = jsonObject.getString("data");
        System.out.println(data);
        LOG.info("==================");


    }

}
