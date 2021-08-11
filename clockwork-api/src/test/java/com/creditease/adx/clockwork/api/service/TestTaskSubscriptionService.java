package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.api.service.impl.TaskSubscriptionService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskSubscription;
import com.creditease.adx.clockwork.common.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:14 下午 2020/12/8
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest()
public class TestTaskSubscriptionService {

    @Autowired
    private TaskSubscriptionService taskSubscriptionService;

    @Test
    public void getTaskSubscriptionBySubscriptionTimeTest() {

        Date parse = DateUtil.parse("2020-12-12 03:30:00");
        String subscriptionTimeStr = DateUtil.formatDateToString(parse, DateUtil.DATE_TIME_M_STR) + ":00";

        List<TbClockworkTaskSubscription>
                taskSubscriptionBySubscriptionTime = taskSubscriptionService.getTaskSubscriptionBySubscriptionTime(subscriptionTimeStr);
        for (TbClockworkTaskSubscription tbClockworkTaskSubscription : taskSubscriptionBySubscriptionTime) {
            System.out.println(tbClockworkTaskSubscription.getTaskId());
            System.out.println(tbClockworkTaskSubscription.getUserEmail());
        }
    }
}
