package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:02 下午 2020/12/10
 * @ Description：构建和获取环形时钟相关测试
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestLoopClockService {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private ILoopClockService loopClockService;


    @Test
    public void getHasCronTaskFromSlotTest() {
        long startTime = System.currentTimeMillis();

        List<TbClockworkTaskPojo> tasks = loopClockService.getHasCronTaskFromSlot(2);
        Map<String, Object> interfaceResult = Response.success(tasks);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)) {
            return;
        }
        if (interfaceResult.get(Constant.DATA) == null) {
            return;
        }
        List<TbClockworkTaskPojo> tasks1 = OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskPojo>>() {
        });
        for (TbClockworkTaskPojo task : tasks1) {
            System.out.println(task.getId());
            System.out.println(task.getName());
        }
        long endTime = System.currentTimeMillis();
        System.out.println("------------------>>>> " + (endTime - startTime) + " .ms");
    }



    @Test
    public void addTaskToLoopClockSlotTest() {
        long startTime = System.currentTimeMillis();
        int taskId = 1234;
        loopClockService.addTaskToLoopClockSlot(taskId, 1);
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime + " .ms");
    }



    @Test
    public void addTaskToLoopClockSlotByBatchTest() {
        List<Integer> taskIds = new ArrayList<>();
        for (int i = 0; i < 30000; i++) {
            taskIds.add(5);
        }
        long startTime = System.currentTimeMillis();
        loopClockService.addTaskToLoopClockSlotByBatch(null);
        long endTime = System.currentTimeMillis();
        System.out.println("------------------>>>> " + (endTime - startTime) + " .ms");
    }



}
