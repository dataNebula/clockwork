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

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:21 2019-09-09
 * @ Description：TaskController,需要启动clockwork-api
 * @ Modified By：
 */
public class TestTaskRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestTaskRestApi.class);


    /**
     * 1. TaskController.addTask()
     */
    @Test
    public void addTask() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "test_" + System.currentTimeMillis());
        params.put("description", "测试任务");
        params.put("location", "/user/adx/clockwork/dfs/shell/");
        params.put("scriptName", "test.sh");
        params.put("runEngine", "hive");
        params.put("triggerMode", 1);
        params.put("timeType", "day");
        params.put("runFrequency", 1);
        params.put("triggerTime", "2019-07-05 08:40:34");
        params.put("groupId", 242);
        params.put("createUser", "xuandongtang");
        params.put("operatorName", "xuandongtang");
        params.put("isPrivate", 1);
        params.put("emailList", "xuandongtang@clockwork.com");
        params.put("runTimeout", null);
        params.put("expiredTime", null);
        params.put("parameter", null);
        params.put("lastStartTime", "2019-07-04 08:42:02");
        params.put("lastEndTime", "2019-07-04 08:42:05");
        params.put("createTime", "2019-06-14 16:43:34");
        params.put("updateTime", "2019-07-04 08:42:05");
        params.put("dependencyId", null);
        params.put("proxyUser", "hadoop");
        params.put("isFirst", 0);
        params.put("batchNum", 1560837600068L);


        LOG.info("==================");
        String result = HttpUtil.postByBody("http://127.0.0.1:9005/clockwork/api/task/operation/addTask", null,
                JSONObject.toJSONString(params));
        LOG.info(result);
        LOG.info("==================");

    }

    /**
     * 2. TaskController.updateTask()
     */
    @Test
    public void updateTask() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 412);
        params.put("name", "test_task3");
        params.put("description", "测试任务3");
        params.put("location", "/dp/user/xuandongtang");
        params.put("command", "sh /dp/user/xuandongtang/test.sh");
        params.put("runEngine", "hive");
        params.put("triggerMode", 1);
        params.put("timeType", "day");
        params.put("runFrequency", 1);
        params.put("triggerTime", "2019-07-05 08:40:34");
        params.put("groupId", 242);
        params.put("userName", "xuandongtang@clockwork.com");
        params.put("isPrivate", 1);
        params.put("failedRetries", 0);
        params.put("emailList", "xuandongtang@clockwork.com");
        params.put("runTimeout", null);
        params.put("expiredTime", null);
        params.put("parameter", null);
        params.put("lastStartTime", "2019-07-04 08:42:02");
        params.put("lastEndTime", "2019-07-04 08:42:05");
        params.put("createTime", "2019-06-14 16:43:34");
        params.put("updateTime", "2019-07-04 08:42:05");
        params.put("dependencyId", null);
        params.put("proxyUser", "hadoop");
        params.put("isFirst", 0);
        params.put("batchNum", 1560837600068L);


        LOG.info("==================");
        String result = HttpUtil.postByBody("http://localhost:9005/clockwork/api/task/operationdateTask", null,
                JSONObject.toJSONString(params));
        LOG.info(result);
        LOG.info("==================");

    }


    /**
     * 4. TaskController.updateTaskStatusByTaskGroupId()
     * <p>
     * 相当于是更新流下面所有的task状态
     */
    @Test
    public void updateTaskStatusByTaskGroupId() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("taskGroupId", 242);
        params.put("status", TaskStatus.SUCCESS.getValue());

        LOG.info("==================");
        String result = HttpUtil.post("http://localhost:9005/clockwork/api/task/operationdateTaskStatusByTaskGroupId", params);
        LOG.info(result);
        LOG.info("==================");

    }

    /**
     * 5. TaskController.getTaskByTaskGroupId()
     */
    @Test
    public void getTaskByTaskGroupId() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("taskGroupId", 242);

        LOG.info("==================");
        String result = HttpUtil.post("http://localhost:9005/clockwork/api/task/getTaskByTaskGroupId", params);
        LOG.info(result);
        LOG.info("==================");

    }


    /**
     * 6. TaskController.getAllTaskByTaskGroupId()
     */
    @Test
    public void getAllTaskByTaskGroupId() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("condition", 242);
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        params.put("role", "admin");
        params.put("userName", "xuandongtang@clockwork.com");

        LOG.info("==================");
        String result = HttpUtil.postByBody("http://localhost:9005/clockwork/api/task/getAllTaskByTaskGroupId", null,
                JSONObject.toJSONString(params));
        LOG.info(result);
        LOG.info("==================");

    }


    /**
     * 7. TaskController.getTaskById()
     */
    @Test
    public void getTaskById() {
        Map<String, Object> params = new HashMap<>();
        params.put("taskId", "412");

        LOG.info("==================");
        String result = HttpUtil.post("http://localhost:9005/clockwork/api/task/getTaskById", params);
        LOG.info(result);
        LOG.info("==================");

    }


    /**
     * 8. TaskController.getTaskJSONObjectById()
     * 根据主键查询task
     */
    @Test
    public void getTaskJSONObjectById() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("taskId", "410");

        LOG.info("==================");
        String result = HttpUtil.get("http://localhost:9005/clockwork/api/task/getTaskJSONObjectById", params, null);
        LOG.info(result);
        LOG.info("==================");

    }

    /**
     * 10. TaskController.getTaskByStatus()
     */
    @Test
    public void getTaskByStatus() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", TaskStatus.SUCCESS.getValue());

        LOG.info("==================");
        String result = HttpUtil.post("http://localhost:9005/clockwork/api/task/getTaskByStatus", params);
        LOG.info(result);
        LOG.info("==================");

    }

    /**
     * 20. TaskController.searchPageListTask()
     */
    @Test
    public void searchPageListTask() throws IOException {
        Map<String, Object> params = new HashMap<>();

        JSONObject condition = new JSONObject();
        condition.put("id", null);
        condition.put("name", null);
        condition.put("status", null);
        condition.put("triggerMode", null);
        condition.put("userName", null);

        params.put("condition", condition.toJSONString());
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        params.put("role", "admin");
        params.put("userName", "xuandongtang@clockwork.com");

        LOG.info("==================");
        String result = HttpUtil.postByBody("http://localhost:9005/clockwork/api/task/searchPageListTask", null,
                JSONObject.toJSONString(params));
        LOG.info(result);
        LOG.info("==================");

    }

    @Value("spring.profiles.active")
    private String profiles;

    @Test
    public void status() {

        System.out.println(profiles);
        String url = String.format("http://%s:%s/%s", "127.0.0.1", "9005", "clockwork/api/node/getAllEnableNodeByRole");
        try {
            HashMap<String, Object> param = new HashMap<>();
            param.put("role", "api");


            String result = HttpUtil.post(url, param);
            System.out.println(result);

//            if (Constant.SUCCESS_CODE.equals(code)) {
//                System.out.println(code);
//            }
//
//
//            List<TbClockworkNode> nodes = gson.fromJson(
//                    gson.toJson(result.get(Constant.DATA)), new TypeToken<List<TbClockworkNode>>() {
//                    }.getType());


        } catch (Exception e) {
            LOG.info("==================");
            e.printStackTrace();
        }


    }


}
