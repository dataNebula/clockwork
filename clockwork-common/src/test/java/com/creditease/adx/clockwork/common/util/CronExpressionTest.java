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

package com.creditease.adx.clockwork.common.util;

import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import it.sauronsoftware.cron4j.Predictor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:14 下午 2020/4/28
 * @ Description：
 * @ Modified By：
 */
public class CronExpressionTest {


    @Test
    public void testIsValidExpression() {
        System.out.println(CronExpression.isValidExpression("0 7 1 */1 *"));
        System.out.println(CronExpression.isValidExpression("0 * L * *"));

    }


    @Test
    public void testGetInvalidMessage() {

       CronExpression.getInvalidMessage("* 1-2 *  * * ");
        System.out.println(CronExpression.getInvalidMessage("* 1,2 * *  *"));
        System.out.println(CronExpression.getInvalidMessage("34 12 */31 * * "));
        System.out.println(CronExpression.getInvalidMessage("34 12 */32 * *"));

    }


    @Test
    public void testGtNextExecutionTime() {

        System.out.println(CronExpression.nextMatchingDate("0-59/80 0-23 * * *"));
        System.out.println(CronExpression.nextMatchingDate("14-59/18 0-23 * * *"));

        System.out.println(CronExpression.nextMatchingDate("34 12 */31 * *"));
        System.out.println(CronExpression.nextMatchingDate("34 12 */32 * *"));

    }


    @Test
    public void testCreateCronExpByTaskInfo() {

        String date = "2020-12-22 15:00:00";
        Date startDate = DateUtil.parse(date, DateUtil.DATE_FULL_STR);
        System.out.println(CronExpression.createCronExpByTriggerTime(startDate,"minute", 5));
        System.out.println(CronExpression.createCronExpByTriggerTime(startDate,"hour", 2));
        System.out.println(CronExpression.createCronExpByTriggerTime(startDate,"day", 2));
        System.out.println(CronExpression.createCronExpByTriggerTime(startDate,"month", 2));
        System.out.println(CronExpression.createCronExpByTriggerTime(startDate,"week", 2));
        System.out.println(CronExpression.createCronExpByTriggerTime(startDate,"century", 2 ));
        System.out.println(CronExpression.createCronExpByTriggerTime(startDate,"minute", 2));
    }


    @Test
    public void testGetCurCycle() {

        String date = "2020-04-28 15:00:00";
        Date startDate = DateUtil.parse(date, DateUtil.DATE_FULL_STR);
        System.out.println(CronExpression.getCurCycle(CronExpression.createCronExpByTriggerTime(startDate,"minute", 5)));
        System.out.println(CronExpression.getCurCycle(CronExpression.createCronExpByTriggerTime(startDate,"hour", 2)));
        System.out.println(CronExpression.getCurCycle(CronExpression.createCronExpByTriggerTime(startDate,"day", 2)));
        System.out.println(CronExpression.getCurCycle(CronExpression.createCronExpByTriggerTime(startDate,"month", 1)));
        System.out.println(CronExpression.getCurCycle(CronExpression.createCronExpByTriggerTime(startDate,"week", 1)));
        System.out.println(CronExpression.getCurCycle(CronExpression.createCronExpByTriggerTime(startDate,"century", 1 )));
        System.out.println(CronExpression.getCurCycle(CronExpression.createCronExpByTriggerTime(startDate,"minute", 1)));
    }



    @Test
    public void testGetCurCycle2() {

        System.out.println(CronExpression.getCurCycle("*/20 0 * * *"));
    }



    @Test
    public void testIsCurCycle() {
        String exp = "0-59/5 0-23 * * *";
        long next = CronExpression.nextMatchingTime(exp);
        long up = CronExpression.upMatchingTime(exp);
        System.out.println(next);
        System.out.println(up);
        System.out.println(DateUtil.formatGreenwichTime(next, DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatGreenwichTime(up, DateUtil.DATE_FULL_STR));

        Date cru = DateUtil.parse("2020-06-30 15:35:02");

        boolean curCycle = CronExpression.isCurCycle(cru, exp);
        System.out.println(curCycle);

    }


    @Test
    public void getHighPriorityTaskIdTest(){

        List<TbClockworkTaskPojo> task = new ArrayList<>();
        TbClockworkTaskPojo task1 = new TbClockworkTaskPojo();
        TbClockworkTaskPojo task2 = new TbClockworkTaskPojo();
        TbClockworkTaskPojo task3 = new TbClockworkTaskPojo();
        task1.setTriggerMode(TaskTriggerModel.TIME.getValue());
        task2.setTriggerMode(TaskTriggerModel.TIME.getValue());
        task3.setTriggerMode(TaskTriggerModel.TIME.getValue());

        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        task1.setCronExp("");
        task2.setCronExp("1 0 * * *");
        task3.setCronExp("");

        task.add(task1);
        task.add(task2);
        task.add(task3);

        System.out.println(TaskUtil.getHighPriorityTaskId(task));
    }

    @Test
    public void getCronExpTimeDifferenceTest(){
        String exp1 = "1 0 * * *";
        String exp2 = "0 0 * * *";
        long cronExpTimeDifference = CronExpression.getCronExpTimeDifference(exp1, exp2);
        System.out.println(cronExpTimeDifference);
    }


    @Test
    public void highPriorityTimeTest(){
        String exp1 = "1 0 * * *";
        String exp2 = "";
        String string = CronExpression.highPriorityTime(exp1, exp2);
        System.out.println(string);
    }

    @Test
    public void nextMatchingDateTest(){

        String date = "2020-12-22 01:30:00";
        Date startDate = DateUtil.parse(date, DateUtil.DATE_FULL_STR);
        String cronExp = CronExpression.createCronExpByTriggerTime(startDate, "day", 5);
        System.out.println(cronExp);

        Predictor p = new Predictor(cronExp);
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
        System.out.println(DateUtil.formatDate(p.nextMatchingDate(), DateUtil.DATE_FULL_STR));
    }
}
