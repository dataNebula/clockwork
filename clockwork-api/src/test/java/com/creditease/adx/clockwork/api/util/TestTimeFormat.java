package com.creditease.adx.clockwork.api.util;

import com.creditease.adx.clockwork.common.util.DateUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 12:57 下午 2020/12/9
 * @ Description：
 * @ Modified By：
 */
public class TestTimeFormat {

    private static final Logger LOG = LoggerFactory.getLogger(TestTimeFormat.class);

    @Test
    public void formatDateToStringTest(){
        Date subscriptionTime = new Date();

        String subscriptionTimeStr = DateUtil.formatDateToString(subscriptionTime, DateUtil.DATE_TIME_STR);
        Date subscriptionDateTime = DateUtil.parse(subscriptionTimeStr, DateUtil.DATE_TIME_STR);

        LOG.info("getTaskSubscriptionBySubscriptionTime, subscriptionTime = {}, subscriptionTimeStr = {}, subscriptionDateTime = {}",
                subscriptionTime, subscriptionTimeStr, subscriptionDateTime);
    }
}
