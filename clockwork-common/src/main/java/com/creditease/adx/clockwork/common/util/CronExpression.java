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

import com.creditease.adx.clockwork.common.enums.TimeCycle;
import com.creditease.adx.clockwork.common.enums.TimeType;
import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.SchedulingPattern;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * cron表达式，最小单元为分钟
 * <p>
 * 五位分别为：分 小时 日 月 周
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:16 上午 2020/4/28
 * @ Description：
 * @ Modified By：
 */
public class CronExpression {

    private final static String CRON_EXP_SPACER = " ";          // cronExp间隔符

    /**
     * Cron表达式是否有效
     *
     * @param cronExpression cronExpression
     * @return boolean
     */
    public static boolean isValidExpression(String cronExpression) {
        try {
            new SchedulingPattern(cronExpression);
        } catch (InvalidPatternException pe) {
            pe.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 返回无效Cron表达式的错误信息
     *
     * @param cronExpression cronExpression
     * @return 无效时返回表达式错误描述, 如果有效返回null
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new SchedulingPattern(cronExpression);
            return null;
        } catch (InvalidPatternException pe) {
            return pe.getMessage();
        }
    }

    /**
     * 获取CronExp下一个执行时间
     *
     * @param cronExpression cronExpression
     * @return 下次CronExp执行时间
     */
    public static long nextMatchingTime(String cronExpression) {
        Predictor p = new Predictor(cronExpression);
        return p.nextMatchingTime();
    }

    public static Date nextMatchingDate(String cronExpression) {
        Predictor p = new Predictor(cronExpression);
        return p.nextMatchingDate();
    }

    /**
     * 获取CronExp上一个执行时间
     *
     * @param cronExpression cronExpression
     * @return 上次CronExp执行时间
     */
    public static long upMatchingTime(String cronExpression) {
        Predictor p = new Predictor(cronExpression);
        long next = p.nextMatchingTime();
        return next - (p.nextMatchingTime() - next);
    }


    /**
     * 该时间是否在当前周期
     *
     * @param cronExpression cronExp
     * @param currTime       时间
     * @return
     */
    public static boolean isCurCycle(Date currTime, String cronExpression) {
        Predictor p = new Predictor(cronExpression);
        long next = p.nextMatchingTime();
        long time = currTime.getTime();
        if (time >= next) {
            return false;
        }
        long upMatchingTime = next - (p.nextMatchingTime() - next);
        return time >= upMatchingTime;
    }

    /**
     * 获取cronExp表达式对应的周期
     *
     * @param cronExpression exp
     * @return
     */
    public static TimeCycle getCurCycle(String cronExpression) {
        Predictor p = new Predictor(cronExpression);
        long time = p.nextMatchingTime();
        long next = p.nextMatchingTime();
        long interval = next - time;

        if (interval < TimeCycle.HOUR.getTime()) {
            return TimeCycle.MINUTE;
        }

        if (interval < TimeCycle.DAY.getTime()) {
            return TimeCycle.HOUR;
        }

        if (interval >= TimeCycle.WEEK.getTime() && interval % TimeCycle.WEEK.getTime() == 0) {
            return TimeCycle.WEEK;
        }

        if (interval < TimeCycle.WEEK.getTime()) {
            return TimeCycle.DAY;
        }

        if (interval > 4 * TimeCycle.WEEK.getTime()) {
            GregorianCalendar c = new GregorianCalendar();
            c.setTimeInMillis(time);
            c.setTimeZone(TimeZone.getDefault());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int year = c.get(Calendar.YEAR);

            c.setTimeInMillis(next);
            c.setTimeZone(TimeZone.getDefault());
            int hourNext = c.get(Calendar.HOUR_OF_DAY);
            int dayOfMonthNext = c.get(Calendar.DAY_OF_MONTH);
            int monthNext = c.get(Calendar.MONTH);
            int yearNext = c.get(Calendar.YEAR);

            if (year == yearNext) {
                if (month != monthNext && dayOfMonth == dayOfMonthNext && hour == hourNext) {
                    return TimeCycle.MONTH;
                }
            }
            return TimeCycle.YEAR;
        }
        return TimeCycle.DAY;
    }

    /**
     * 通过触发时间，频率/频率时间类型 生成cron表达式
     * 从M开始，每N小时/分钟...执行F次
     *
     * @param triggerTime 触发时间
     * @param timeType    可选值有：minute、hour、day、week、month、year、century
     * @param frequency   频率
     * @return cronExp
     */
    public static String createCronExpByTriggerTime(Date triggerTime, String timeType, Integer frequency) {
        if (triggerTime == null) {
            throw new RuntimeException("trigger time is null!");
        }

        TimeType timeTypeEnum;
        if (timeType == null ||
                (timeTypeEnum = TimeType.getTimeTypeEnum(timeType)) == null) {
            throw new RuntimeException("time type is null or Invalid!");
        }

        if (frequency == null || frequency < 0) {
            throw new RuntimeException("run frequency can not less than 0, please check it!");
        }
        // 获取有效的频率
        frequency = getValidFrequency(timeType, frequency);

        //解析triggerTime
        int[] triggerTimeInfo =
                DateUtil.getDetailInfoOfDate(DateUtil.formatDate(triggerTime, DateUtil.DATE_FULL_STR));
        if (triggerTimeInfo == null || triggerTimeInfo.length != 6) {
            throw new RuntimeException("trigger time array is null or illegal!");
        }

        int month = triggerTimeInfo[1];
        int day = triggerTimeInfo[2];
        int hour = triggerTimeInfo[3];
        int minute = triggerTimeInfo[4];

        //create  cronExp
        StringBuilder cronExp = new StringBuilder();
        switch (timeTypeEnum) {
            case MINUTE:
                // 处理MINUTE，frequency
                if (frequency > (59 - minute)) {
                    frequency = 59 - minute;
                }
                if (frequency == 1) {
                    cronExp.append(minute).append("-59").append(CRON_EXP_SPACER);
                } else {
                    cronExp.append(minute).append("-59/").append(frequency).append(CRON_EXP_SPACER);
                }
                cronExp.append(hour).append("-23").append(CRON_EXP_SPACER);
                cronExp.append("*").append(CRON_EXP_SPACER);
                cronExp.append("*").append(CRON_EXP_SPACER);
                cronExp.append("*");
                break;
            case HOUR:
                cronExp.append(minute).append(CRON_EXP_SPACER);
                // 处理HOUR，HOUR最大不能超过24
                if (frequency == 1) {
                    cronExp.append("*").append(CRON_EXP_SPACER);
//                    cronExp.append(hour).append("-23").append(CRON_EXP_SPACER);
                } else {
                    cronExp.append("*").append("/").append(frequency).append(CRON_EXP_SPACER);
//                    cronExp.append(hour).append("-23").append("/").append(frequency).append(CRON_EXP_SPACER);
                }
                cronExp.append("*").append(CRON_EXP_SPACER);
                cronExp.append("*").append(CRON_EXP_SPACER);
                cronExp.append("*");
                break;
            case DAY:
                cronExp.append(minute).append(CRON_EXP_SPACER);
                cronExp.append(hour).append(CRON_EXP_SPACER);
                // 最大不能超过31
                if (frequency == 1) {
                    cronExp.append("*").append(CRON_EXP_SPACER);
                } else {
                    cronExp.append("*/").append(frequency).append(CRON_EXP_SPACER);
                }
                cronExp.append("*").append(CRON_EXP_SPACER);
                cronExp.append("*");
                break;
            case MONTH:
                cronExp.append(minute).append(CRON_EXP_SPACER);
                cronExp.append(hour).append(CRON_EXP_SPACER);
                cronExp.append(day).append(CRON_EXP_SPACER);
                cronExp.append("*/").append(frequency).append(CRON_EXP_SPACER);
                cronExp.append("*");
                break;
            case WEEK:
                int currentWeek = DateUtil.getWeekOfDate(triggerTime);
                cronExp.append(minute).append(CRON_EXP_SPACER);
                cronExp.append(hour).append(CRON_EXP_SPACER);
                cronExp.append("*").append(CRON_EXP_SPACER);
                cronExp.append("*").append(CRON_EXP_SPACER);
                cronExp.append(currentWeek);
                break;
            case CENTURY:
                cronExp.append(minute).append(CRON_EXP_SPACER);
                cronExp.append(hour).append(CRON_EXP_SPACER);
                cronExp.append(day).append(CRON_EXP_SPACER);
                cronExp.append(month).append(CRON_EXP_SPACER);
                cronExp.append("*");
                break;
            default:
                break;
        }
        return cronExp.toString();
    }


    /**
     * Get valid frequency
     *
     * @param timeType  time type
     * @param frequency frequency
     * @return valid frequency
     */
    public static int getValidFrequency(String timeType, Integer frequency) {
        if (frequency == null || frequency < 1) {
            return 1;
        }
        TimeType timeTypeEnum;
        if (timeType == null ||
                (timeTypeEnum = TimeType.getTimeTypeEnum(timeType)) == null) {
            throw new RuntimeException("time type is null or Invalid!");
        }

        switch (timeTypeEnum) {
            case MINUTE:
                // 处理MINUTE，MINUTE最大不能超过59
                if (frequency > 59) {
                    return 59;
                }
                break;
            case HOUR:
                if (frequency > 23) {
                    return 23;
                }
                break;
            case DAY:
                if (frequency > 31) {
                    return 31;
                }
                break;
            case MONTH:
                if (frequency > 11) {
                    return 11;
                }
                break;
            case WEEK:
                if (frequency > 6) {
                    return 6;
                }
                break;
            default:
                break;
        }
        return frequency;
    }

    /**
     * 优先级计算
     *
     * @param exp1 cronExp表达式
     * @param exp2 cronExp表达式
     * @return 返回优先级高的表达式
     */
    public static String highPriorityTime(String exp1, String exp2) {
        if (StringUtils.isBlank(exp2)) {
            return exp1;
        }
        if (StringUtils.isBlank(exp1)) {
            return exp2;
        }
        if (exp1.equals(exp2)) {
            return exp1;
        }
        long current = System.currentTimeMillis();
        long day = TimeCycle.DAY.getTime();
        long zero = current / day * day - TimeZone.getDefault().getRawOffset() - 1;
        Predictor p1 = new Predictor(exp1, zero);
        Predictor p2 = new Predictor(exp2, zero);
        return p1.nextMatchingTime() > p2.nextMatchingTime() ? exp2 : exp1;
    }


    /**
     * 获取cronExp时间差
     *
     * @param exp1 cronExp
     * @param exp2 cronExp
     * @return
     */
    public static long getCronExpTimeDifference(String exp1, String exp2) {

        if (StringUtils.isBlank(exp1) || StringUtils.isBlank(exp2)) {
            return -1;
        }
        try {
            if (exp1.equals(exp2)) {
                return 0;
            }
            long current = DateUtil.parse("2020-12-10 23:23:23").getTime();
            Predictor p1 = new Predictor(exp1, current);
            Predictor p2 = new Predictor(exp2, current);
            return Math.abs(p1.nextMatchingTime() - p2.nextMatchingTime());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
