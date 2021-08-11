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

package com.creditease.adx.clockwork.master.basecase;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import org.junit.Test;

import it.sauronsoftware.cron4j.Predictor;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-06-09
 */
public class PredictorTest {

    @Test
    public void abc(){

        AtomicInteger RERUN_JOB_LIST_REQUEST_NUMBER = new AtomicInteger();

        for(int i = 0;i < 12;i++){

            int number = RERUN_JOB_LIST_REQUEST_NUMBER.addAndGet(1);

            int index = number % 2;

            System.out.println(number + "=" + index);

        }


    }



    @Test
    public void testCron4j() {

        Predictor p = new Predictor("0 7 1 */1 *");

        for(int i = 0;i < 6;i++){
            System.out.println(DateUtil.formatDate(p.nextMatchingDate(),DateUtil.DATE_FULL_STR));
        }

    }

    @Test
    public void testSubStr() {
        String abc = "01";
        System.out.println(abc.substring(0,2));
    }

    @Test
    public void testDateUtilsGetDetailInfoOfDate() {
        int [] result = DateUtil.getDetailInfoOfDate("2019-12-08 12:34:05");

        System.out.println(result[0]);
        System.out.println(result[1]);
        System.out.println(result[2]);
        System.out.println(result[3]);
        System.out.println(result[4]);
        System.out.println(result[5]);
    }

    @Test
    public void testCreateCronExpByTaskInfo() {

        String date = null;
        String cronExp = null;
        TbClockworkTaskPojo task = null;



        date = "2019-12-08 12:34:05";
        task = new TbClockworkTaskPojo();
        task.setTriggerTime(DateUtil.parse(date,DateUtil.DATE_FULL_STR));
        task.setTimeType("hour");
        task.setRunFrequency(1);
        cronExp = createCronExpByTaskInfo(task);
        System.out.println(cronExp);

        task = new TbClockworkTaskPojo();
        task.setTriggerTime(DateUtil.parse(date,DateUtil.DATE_FULL_STR));
        task.setTimeType("hour");
        task.setRunFrequency(2);
        cronExp = createCronExpByTaskInfo(task);
        System.out.println(cronExp);


        task = new TbClockworkTaskPojo();
        task.setTriggerTime(DateUtil.parse(date,DateUtil.DATE_FULL_STR));
        task.setTimeType("day");
        task.setRunFrequency(0);
        cronExp = createCronExpByTaskInfo(task);
        System.out.println(cronExp);


        task = new TbClockworkTaskPojo();
        task.setTriggerTime(DateUtil.parse(date,DateUtil.DATE_FULL_STR));
        task.setTimeType("day");
        task.setRunFrequency(2);
        cronExp = createCronExpByTaskInfo(task);
        System.out.println(cronExp);


        task = new TbClockworkTaskPojo();
        task.setTriggerTime(DateUtil.parse(date,DateUtil.DATE_FULL_STR));
        task.setTimeType("day");
        task.setRunFrequency(32);
        cronExp = createCronExpByTaskInfo(task);
        System.out.println(cronExp);



        task = new TbClockworkTaskPojo();
        task.setTriggerTime(DateUtil.parse(date,DateUtil.DATE_FULL_STR));
        task.setTimeType("century");
        task.setRunFrequency(2);
        cronExp = createCronExpByTaskInfo(task);
        System.out.println(cronExp);

    }


    public String createCronExpByTaskInfo(TbClockworkTaskPojo task) {
        String result = null;

        /*
         * 必须提供参数检查阶段
         */

        if (task == null) {
            return result;
        }

        if (task.getTriggerTime() == null) {
            throw new RuntimeException("trigger time is null!");
        }

        if (task.getRunFrequency() != null && task.getRunFrequency() < 0) {
            throw new RuntimeException("run frequency can not less than 0,please check it!");
        }

        // 可选值有：hour、day、week、month、year、century
        if (task.getTimeType() == null) {
            throw new RuntimeException("time type is null!");
        }

        int[] triggerTimeInfo  =  DateUtil.getDetailInfoOfDate(
                DateUtil.formatDate(task.getTriggerTime(),DateUtil.DATE_FULL_STR));

        if(triggerTimeInfo == null || triggerTimeInfo.length != 6){
            throw new RuntimeException("trigger time array is null or illegal!");
        }

        int month = triggerTimeInfo[1];
        int day = triggerTimeInfo[2];
        int hour = triggerTimeInfo[3];
        int minute = triggerTimeInfo[4];


        if(task.getTimeType().equals("century")){
            result = minute + " " + hour + " " + day + " " + month + " *";
            return result;
        }


        // 2019-12-08 12:34:05
        if(task.getTimeType().equals("hour")){

            // 最大不能超过24
            if(task.getRunFrequency() > 24){
                task.setRunFrequency(24);
            }

            // run frequency >=1 且 <= 24

            if(task.getRunFrequency() > 1){
                result = minute + " */" + task.getRunFrequency() + " * * *";
            }else if(task.getRunFrequency() == 1){
                result = minute + " * * * *";
            }else{
                throw new RuntimeException("Do not support current run frequency that value is " + task.getRunFrequency());
            }
            return result;
        }


        if(task.getTimeType().equals("day")){
            // 最大不能超过31
            if(task.getRunFrequency() > 31){
                task.setRunFrequency(31);
            }
            //34 12 */31 * *
            if(task.getRunFrequency() <= 1){
                result = minute + " " + hour  + " * * *";
            }else{
                result = minute + " " + hour + " */" + task.getRunFrequency() + " * *";
            }
            return result;
        }

        if(task.getTimeType().equals("week")){
            int currentWeek = DateUtil.getWeekOfDate(task.getTriggerTime());
            result = minute + " " + hour  + " * * " + currentWeek;
            return result;
        }

        if(task.getTimeType().equals("month")){
            int currentMonth = DateUtil.getMonthOfDate(task.getTriggerTime());
            result = minute + " " + hour  + " " + day + " " + currentMonth + " *";
            return result;
        }

        return result;
    }

}

