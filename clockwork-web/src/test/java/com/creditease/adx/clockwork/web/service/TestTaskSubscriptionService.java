package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskSubscriptionPojo;
import com.creditease.adx.clockwork.web.service.impl.TaskSubscriptionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 12:40 下午 2020/12/14
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTaskSubscriptionService {


    @Autowired
    private TaskSubscriptionService taskSubscriptionService;


    @Test
    public void addTaskSubscriptionTest() throws Exception {

        TbClockworkTaskSubscriptionPojo pojo = new TbClockworkTaskSubscriptionPojo();
        pojo.setTaskId(12191);
        pojo.setSubscriptionTime(new Date());
        pojo.setUserName("xuandongtang");
        pojo.setUserEmail("xuandongtang@clockwork.com");
        pojo.setMobileNumber("15826008619");
        System.out.println(taskSubscriptionService.addTaskSubscription(pojo));


    }


    @Test
    public void getTaskSubscriptionByTaskIdTest() throws Exception {

        int taskId = 1687;
        List<TbClockworkTaskSubscriptionPojo> taskSubscriptions = taskSubscriptionService.getTaskSubscriptionByTaskId(taskId);
        for (TbClockworkTaskSubscriptionPojo taskSubscription : taskSubscriptions) {
            System.out.println(taskSubscription.getTaskId());
            System.out.println(taskSubscription.getSubscriptionTime());
            System.out.println(taskSubscription.getUserName());
            System.out.println(taskSubscription.getMobileNumber());
        }

    }



    @Test
    public void getTaskSubscriptionByUserNameTest() throws Exception {

        String userName = "xuandongtang";
        List<TbClockworkTaskSubscriptionPojo> taskSubscriptions = taskSubscriptionService.getTaskSubscriptionByUserName(userName);
        for (TbClockworkTaskSubscriptionPojo taskSubscription : taskSubscriptions) {
            System.out.println(taskSubscription.getTaskId());
            System.out.println(taskSubscription.getSubscriptionTime());
            System.out.println(taskSubscription.getUserName());
            System.out.println(taskSubscription.getMobileNumber());
        }

    }


}
