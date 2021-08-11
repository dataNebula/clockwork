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

package com.creditease.adx.clockwork.api.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskLog;
import com.creditease.adx.clockwork.common.enums.TimeType;
import com.creditease.adx.clockwork.common.util.DateUtil;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 17:02 2019-11-11
 * @ Description：
 * @ Modified By：
 */
public class T {

    @SuppressWarnings("unused")
	private static void getCurrCycleBoundaryValue(String time_type) {

        Long millis = TimeType.getMillisByType(time_type);
        if (millis == null) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long timeConsuming = currentTimeMillis % millis;
        System.out.println(timeConsuming);

        long startTime = currentTimeMillis - timeConsuming;
        if (TimeType.DAY.getType().equals(time_type)) {
            startTime -= 28800000;
        } else if (TimeType.WEEK.getType().equals(time_type)) {
            startTime = System.currentTimeMillis() - System.currentTimeMillis() % TimeType.DAY.getMillis();
            startTime -= 28800000;
            startTime = getFirstDayOfWeek(new Date(startTime)).getTime();
        } else if (TimeType.MONTH.getType().equals(time_type)) {
            Date monthFirstDay = getCurrentMonthFirstDay();
            Date currentMonthLastDay = getCurrentMonthLastDay();
        }


        long endTime = startTime + millis;

        System.out.println(startTime + " " + DateUtil.formatGreenwichTime(startTime, DateUtil.DATE_TIME_STAMP_STR));
        System.out.println(endTime + " " + DateUtil.formatGreenwichTime(endTime, DateUtil.DATE_TIME_STAMP_STR));
    }


    public static Date getFirstDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_WEEK, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cal.getTime();
    }

    public static Date getCurrentMonthFirstDay() {
        Calendar cale = Calendar.getInstance();
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        return cale.getTime();
    }


    public static Date getCurrentMonthLastDay() {
        Calendar cale = Calendar.getInstance();
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        return cale.getTime();
    }


    public static void main(String[] args) {

//        String str = "25,13,0d6cf3c520f34460a308522a04a5890a\n" +
//                "";
//
//        String[] split = str.split("\n");
//        for (String s : split) {
//            String[] v = s.split(",");
//            String groupId = v[0];
//            String projectId = v[1];
//            System.out.println("update tb_clockwork_task set alias_name_type=3 where group_id="+groupId+";");
//        }


        List<TbClockworkTaskLog> needTobeKilledTaskLogIsRunningList = new ArrayList<>();

        Map<Integer, TbClockworkTaskLog> needTobeKilledTaskLogIsRunningMap =
                needTobeKilledTaskLogIsRunningList.stream().collect(Collectors.toMap(TbClockworkTaskLog::getTaskId, taskLog -> taskLog, (key1, key2) -> key2));

        for (Integer integer : needTobeKilledTaskLogIsRunningMap.keySet()) {
            System.out.println(integer);
        }

    }


}
