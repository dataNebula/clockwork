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
import com.creditease.adx.clockwork.common.entity.TaskGroupAndTasks;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskGroupPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:43 2019-12-06
 * @ Description：
 * @ Modified By：
 */
public class TestTaskGroupApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestTaskGroupApi.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();


    @Test
    public void getAllTaskGroupByUserName() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        params.put("userName", "xuandongtang@clockwork.com");


        Map<String, Object> condition = new HashMap<>();
        condition.put("name", "xuandong_test");
//        condition.put("id", 357);
//        condition.put("status", null);  // none、running、success、failed
//        condition.put("take_effect_status", "enable"); // enable、disable
        String s = gson.toJson(condition);
        params.put("condition", s);


        LOG.info("==================");
        String result = HttpUtil.postByBody("http://127.0.0.1:9005/clockwork/api/task/group/getAllTaskGroupByUserName", null,
                JSONObject.toJSONString(params));
        LOG.info(result);
        LOG.info("==================");

    }


    @Test
    public void deleteTaskGroup() throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("taskGroupId", 365);

        LOG.info("==================");
        String result = HttpUtil.post("http://127.0.0.1:9005/clockwork/api/task/group/delete", params);
        LOG.info(result);
        LOG.info("==================");

    }


    @Test
    public void addTaskList() throws IOException {
        TaskGroupAndTasks taskGroupAndTasks = new TaskGroupAndTasks();

        TbClockworkTaskGroupPojo taskGroupPojo = new TbClockworkTaskGroupPojo();
        taskGroupPojo.setName("txd_test_group_02");
        taskGroupPojo.setAliasName("test_group_02");
        taskGroupPojo.setStatus("enable");
        taskGroupPojo.setUserName("xuandongtang@clockwork.com");
        taskGroupPojo.setTakeEffectStatus("enable");
        taskGroupPojo.setIfCalculateTime(1);
        taskGroupAndTasks.setTaskGroup(taskGroupPojo);

        List<TbClockworkTaskPojo> tasks = new ArrayList<>();

        TbClockworkTaskPojo task = new TbClockworkTaskPojo();
        task.setAliasName("test_task_02");
        task.setName("txd_test_task02");
        task.setDescription("测试任务02");
        task.setLocation("/user/adx/clockwork/dfs/shell/");
        task.setScriptName("test.sh");
        task.setRunEngine("hive");
        task.setTriggerMode(1);
        task.setTimeType("day");
        task.setRunFrequency(1);
        task.setTriggerTime(new Date());
        task.setGroupId(242);
        task.setCreateUser("xuandongtang@clockwork.com");
        task.setOperatorName("xuandongtang@clockwork.com");
        task.setIsPrivate(true);
        task.setEmailList("xuandongtang@clockwork.com");
        task.setRunTimeout(null);
        task.setExpiredTime(null);
        task.setParameter(null);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setDependencyId(null);
        task.setProxyUser("hadoop");
        task.setIsFirst(false);
        tasks.add(task);

        taskGroupAndTasks.setTasks(tasks);
        taskGroupAndTasks.setOperator("xuandongtang@clockwork.com");
        LOG.info("==================");
        String result = HttpUtil.postByBody("http://127.0.0.1:9005/clockwork/api/task/group/addTaskList", null,
                JSONObject.toJSONString(taskGroupAndTasks));
        LOG.info(result);
        LOG.info("==================");

    }

    @Test
    public void updateTaskList() throws IOException {
        TaskGroupAndTasks taskGroupAndTasks = new TaskGroupAndTasks();

        TbClockworkTaskGroupPojo taskGroupPojo = new TbClockworkTaskGroupPojo();
        taskGroupPojo.setName("txd_test_group_01");
        taskGroupPojo.setAliasName("test_group_01");
        taskGroupPojo.setStatus("enable");
        taskGroupPojo.setUserName("xuandongtang@clockwork.com");
        taskGroupPojo.setTakeEffectStatus("enable");
        taskGroupPojo.setIfCalculateTime(1);
        taskGroupPojo.setId(461);
        taskGroupAndTasks.setTaskGroup(taskGroupPojo);

        List<TbClockworkTaskPojo> tasks = new ArrayList<>();

        TbClockworkTaskPojo task = new TbClockworkTaskPojo();
        task.setId(1829);
        task.setAliasName("test_task_01");
        task.setName("txd_test_task01");
        task.setDescription("测试任务01");
        task.setLocation("/user/adx/clockwork/dfs/shell/");
        task.setScriptName("test.sh");
        task.setRunEngine("hive");
        task.setTriggerMode(1);
        task.setTimeType("day");
        task.setRunFrequency(1);
        task.setTriggerTime(new Date());
        task.setGroupId(taskGroupPojo.getId());
        task.setCreateUser("xuandongtang@clockwork.com");
        task.setOperatorName("xuandongtang@clockwork.com");
        task.setIsPrivate(true);
        task.setEmailList("xuandongtang@clockwork.com");
        task.setRunTimeout(null);
        task.setExpiredTime(null);
        task.setParameter(null);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setDependencyId(null);
        task.setProxyUser("hadoop");
        task.setIsFirst(false);
        tasks.add(task);

        taskGroupAndTasks.setTasks(tasks);
        taskGroupAndTasks.setOperator("xuandongtang@clockwork.com");

        LOG.info("==================");
        String result = HttpUtil.postByBody("http://127.0.0.1:9005/clockwork/api/task/group/updateTaskList", null,
                JSONObject.toJSONString(taskGroupAndTasks));
        LOG.info(result);
        LOG.info("==================");

    }


}
