package com.creditease.adx.clockwork.master;

import com.creditease.adx.clockwork.common.util.DateUtil;
import org.junit.Test;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午3:31 2020/12/12
 * @ Description：Test
 * @ Modified By：
 */
public class TestSpringCronExp {


    @Test
    public void springCronExpTest() {
        String str = "2020-12-12 00:00:00";
        String springCronExp = "0 */10 * * * ?";

        Date currentDate = DateUtil.parse(str);
        String currentDateStr = DateUtil.formatDate(currentDate, DateUtil.DATE_MIN_STR) + ":00";
        currentDate = DateUtil.parse(currentDateStr);

        if (!CronSequenceGenerator.isValidExpression(springCronExp)) {
            return;
        }
        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(springCronExp);
        Date next = cronSequenceGenerator.next(currentDate);
        Date next2 = cronSequenceGenerator.next(next);
        long timeDifference = next2.getTime() - next.getTime();

        long beforeTime = currentDate.getTime() - timeDifference; // cronExp时间差
        Date beforeDate = new Date(beforeTime);
        String beforeDateStr = DateUtil.formatDate(beforeDate, DateUtil.DATE_FULL_STR);

        System.out.println(beforeDateStr);
        System.out.println(currentDateStr);
        System.out.println(timeDifference);

    }


    @Test
    public void springCronExpTest2() {
//        String str = "2020-12-12 00:00:00";
//        Date currentDate = DateUtil.parse(str);
        Date currentDate = new Date();
        String springCronExp = "0 0 23 * * ?";

        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(springCronExp);
        Date next = cronSequenceGenerator.next(currentDate);
        Date next2 = cronSequenceGenerator.next(next);

        System.out.println(DateUtil.formatDate(next, DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(next2, DateUtil.DATE_FULL_STR));

    }
}
