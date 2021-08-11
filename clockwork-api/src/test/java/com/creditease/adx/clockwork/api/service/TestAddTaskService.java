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

package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-09-09
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestAddTaskService {

    @Autowired
    private ITaskOperationService taskOperationService;

    @Autowired
    private RestTemplateClient restTemplateClient;


    @Test
    public void addTask() {
        System.out.println("------------------>>>> 添加任务开始");
        for (int i = 1; i < 1000; i++) {
            TbClockworkTaskPojo task = new TbClockworkTaskPojo();

            task.setName("每5分钟执行一次" + i);
            task.setDescription("test_05_" + i);
            task.setLocation("/user/adx/clockwork/dfs/shell/");
            task.setScriptName("emp.sh");
            task.setScriptType("sh");
            task.setCommand("sh /user/adx/clockwork/dfs/shell/emp.sh");
            task.setRunEngine(null);
            task.setTriggerMode(TaskTriggerModel.TIME.getValue());
            task.setTimeType("minute");
            task.setRunFrequency(10);
            task.setTriggerTime(DateUtil.parse("2020-05-20 08:00:00"));
            task.setDependencyId(null);
            task.setNodeGid(1);
            task.setGroupId(510);
            task.setCreateUser("xuandongtang@clockwork.com");
            task.setOperatorName("xuandongtang@clockwork.com");
            task.setIsPrivate(false);
            task.setEmailList("xuandongtang@clockwork.com");
            task.setRunTimeout(0);
            task.setExpiredTime(null);
            task.setParameter(null);
            task.setLastStartTime(DateUtil.parse("2020-05-20 14:30:00"));
            task.setLastEndTime(DateUtil.parse("2020-05-20 14:30:00"));
            task.setCreateTime(DateUtil.parse("2020-05-20 14:30:00"));
            task.setUpdateTime(DateUtil.parse("2020-05-20 14:30:00"));
            task.setProxyUser(null);
            task.setIsFirst(true);

            taskOperationService.addTask(task);

            System.out.println("------------------>>>> 完成任务" + i);
        }

        System.out.println("------------------>>>> 添加任务结束");
    }

    @Test
    public void addTaskBatch() {
        System.out.println("------------------>>>> 添加任务开始");
        long startTime = System.currentTimeMillis();
        int sucess = 0;
        int fail = 0;
        for (int i = 1; i <= 5000; i++) {
            TbClockworkTaskPojo task = new TbClockworkTaskPojo();

            task.setName("【压测】5分钟执行一次" + i);
            task.setDescription("5000作业压力测试_" + i);
            task.setLocation("/user/adx/clockwork/dfs/shell/");
            task.setScriptName("emp.sh");
            task.setCommand("sh /user/adx/clockwork/dfs/shell/emp.sh");
            task.setRunEngine(null);
            task.setTriggerMode(TaskTriggerModel.TIME.getValue());
            task.setTimeType("minute");
            task.setRunFrequency(5);
            task.setTriggerTime(DateUtil.parse("2020-10-27 9:30:00"));
            task.setDependencyId(null);
            task.setGroupId(516);
            task.setCreateUser("clockwork@clockwork.com");
            task.setOperatorName("clockwork@clockwork.com");
            task.setEmailList("clockwork@clockwork.com");
            task.setRunTimeout(0);
            task.setExpiredTime(null);
            task.setParameter(null);
            task.setProxyUser(null);
            task.setIsFirst(true);
            task.setIsPrivate(false);
            task.setIsReplace(false);
            task.setIsSyncFile(false);
            task.setAliasName("每5分钟执行一次" + i);
            task.setSource(0);

            String URL = "http://127.0.0.1:9005/clockwork/api/task/operation/addTask";
            Map<String, Object> interfaceResult = restTemplateClient.getResult(URL, task);
            if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
                System.out.println("------------------>>>> 添加任务失败" + i);
                System.out.println("错误信息[" + interfaceResult.get(Constant.MSG) + "]");
                fail++;
                continue;
            }

            sucess++;
            System.out.println("------------------>>>> 添加任务成功" + i);
        }

        System.out.println("------------------>>>> 添加任务结束, \n"
                + "成功数[" + sucess + "], \n"
                + "失败数[" + fail + "], \n"
                + "耗时[" + (System.currentTimeMillis() - startTime) + "]ms");
    }


}
