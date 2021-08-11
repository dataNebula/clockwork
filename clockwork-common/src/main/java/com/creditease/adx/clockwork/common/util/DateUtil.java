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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    /**
     * 定义常量
     **/
    public static final String DATE_JFP_STR = "yyyyMM";
    public static final String YEAR_MONTH = "yyyy-MM";
    public static final String DATE_STD_STR_SIMPLE = "yyyyMMdd";
    public static final String DATE_STD_STR = "yyyy-MM-dd";
    public static final String DATE_FULL_STR = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_STAMP_STR = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_MIN_STR = "yyyy-MM-dd HH:mm";
    public static final String DATE_TIME_M_STR = "HH:mm";
    public static final String DATE_TIME_STR = "HH:mm:ss";
    public static final String DATE_SMALL_STR = "yyyy-MM-dd";
    public static final String DATE_KEY_STR = "yyMMddHHmmss";
    public static final String DATE_KEY_STR_FULL = "yyyyMMddHHmmss";
    public static final String DATE_KEY_STR_FROMART_1 = "MM/dd/yyyy HH:mm:ss";

    public static String getNowTimeSimple() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_KEY_STR_FULL);
        return df.format(new Date());
    }

    /**
     * 使用预设格式提取字符串日期
     *
     * @param strDate 日期字符串
     * @return
     */
    public static Date parse(String strDate) {
        return parse(strDate, DATE_FULL_STR);
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(Long s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FULL_STR);
        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 使用用户格式提取字符串日期
     *
     * @param strDate 日期字符串
     * @param pattern 日期格式
     * @return
     */
    public static Date parse(String strDate, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取系统当前时间的日期(Date类型)
     *
     * @return
     */
    public static Date getNowTimeDate() {
        String nowTime = getNowTime();
        Date nowDate = parse(nowTime, DATE_FULL_STR);
        return nowDate;
    }

    /**
     * 获取今天凌晨的时间
     *
     * @return
     */
    public static Date getTodayZeroPointDate() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);
        String format = df.format(new Date().getTime());
        return parse(format, DATE_SMALL_STR);
    }

    /**
     * 获取当前时间24小时前的时间
     * @return
     */
    public static Date getYesterday(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR_OF_DAY,c.get(Calendar.HOUR_OF_DAY) - 24);
        return c.getTime();
    }


    /**
     * 获取系统当前时间的日期(Date类型),精确到毫秒
     *
     * @return
     */
    public static Date getNowTimeStampDate() {
        String nowTime = getNowTime(DATE_TIME_STAMP_STR);
        return parse(nowTime, DATE_TIME_STAMP_STR);
    }


    /**
     * 两个时间比较
     *
     * @param date1
     * @return
     */
    public static int compareDateWithNow(Date date1) {
        Date date2 = new Date();
        int rnum = date1.compareTo(date2);
        return rnum;
    }

    /**
     * 两个时间比较(时间戳比较)
     *
     * @param date1
     * @return
     */
    public static int compareDateWithNow(long date1) {
        long date2 = dateToUnixTimestamp();
        if (date1 > date2) {
            return 1;
        } else if (date1 < date2) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static String getNowTime() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FULL_STR);
        return df.format(new Date());
    }

    /**
     * 获取系统当前时间
     *
     * @return
     */
    public static String getNowTime(String type) {
        SimpleDateFormat df = new SimpleDateFormat(type);
        return df.format(new Date());
    }

    /**
     * 获取指定格式的时间
     *
     * @return
     * @throws Exception
     */
    public static String getTimeByFormat(String inputDateStr, String inputDateType, String outputDateType)
            throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(inputDateType);
        Date d = sdf.parse(inputDateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        sdf = new SimpleDateFormat(outputDateType);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获得指定日期偏移量的目标日期
     *
     * @param inputDateStr
     * @param inputDateType
     * @param outputDateType
     * @param offset
     * @return
     * @throws Exception
     */
    public static String getOffsetTimeByFormat(String inputDateStr, String inputDateType, String outputDateType,
                                               int offset) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(inputDateType);
        Date d = sdf.parse(inputDateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        sdf = new SimpleDateFormat(outputDateType);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获得指定日期偏移量的目标日期
     *
     * @param date
     * @param offset
     * @return
     * @throws Exception
     */
    public static Date getOffsetDateTime(Date date, int offset) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        return calendar.getTime();
    }

    /**
     * 获得指定日期格式unix时间
     *
     * @param inputDateStr
     * @param inputDateType
     * @return
     * @throws Exception
     */
    public static long getUnixTimestamp(String inputDateStr, String inputDateType) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(inputDateType);
        Date d = sdf.parse(inputDateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取系统当前计费期
     *
     * @return
     */
    public static String getJFPTime() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_JFP_STR);
        return df.format(new Date());
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date
     * @return
     */
    public static long dateToUnixTimestamp(String date) {
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat(DATE_FULL_STR).parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date
     * @param dateFormat
     * @return
     */
    public static long dateToUnixTimestamp(String date, String dateFormat) {
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat(dateFormat).parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    /**
     * 将当前日期转换成Unix时间戳
     *
     * @return long 时间戳
     */
    public static long dateToUnixTimestamp() {
        long timestamp = new Date().getTime();
        return timestamp;
    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp
     * @return
     */
    public static String unixTimestampToDate(long timestamp) {
        SimpleDateFormat sd = new SimpleDateFormat(DATE_FULL_STR);
        sd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return sd.format(new Date(timestamp));
    }

    /**
     * 返回当前日期时间戳
     *
     * @return
     */
    public static synchronized Long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 格式化时间
     *
     * @param d
     * @param pattern
     * @return
     */
    public static String formatDate(Date d, String pattern) {
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(d);
    }

    /**
     * 将时间Date类型转换成String类型
     * 格式为 yyyy-MM-dd HH:mm:ss
     *
     * @param d
     * @return
     */
    public static String dateToString(Date d) {
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FULL_STR);
        return sdf.format(d);
    }

    public static String dateTimeStampToString(Date d) {
        if (d == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_STAMP_STR);
        return sdf.format(d);
    }

    /**
     * 毫秒数转换成时分秒字符串
     *
     * @param time 毫秒数
     * @return String 时间字符串
     */
    public static String milliToHms(int time) {
        String hms = "";
        if (time < 1000) {
            hms = time + "ms";
        } else if (time >= 1000 && time < 60000) {
            int sec = time / 1000;
            int milliSec = time - sec * 1000;
            hms = sec + "s:" + milliSec + "ms";
        } else if (time >= 60000 && time < 3600000) {
            int min = time / 60000;
            int remain = time - min * 60000;
            int sec = remain / 1000;
            int milliSec = remain - sec * 1000;
            hms = min + "min:" + sec + "sec:" + milliSec + "ms";
        } else if (time >= 3600000) {
            int hour = time / 3600000;
            int remain = time - hour * 3600000;
            int min = remain / 60000;
            int remain1 = remain - min * 60000;
            int sec = remain1 / 1000;
            int milliSec = remain1 - sec * 1000;

            hms = hour + "hour:" + min + "min:" + sec + "sec:" + milliSec + "ms";
        } else {
            hms = "-";
        }
        return hms;
    }

    public static int[] getDetailInfoOfDate(String dataStr) {
        int[] result = null;

        if (StringUtils.isEmpty(dataStr)) {
            return result;
        }

        String[] firstSeg = dataStr.trim().split(" ");
        if (firstSeg == null || firstSeg.length != 2) {
            return result;
        }

        String[] segmentOne = firstSeg[0].trim().split("-");


        if (segmentOne == null || segmentOne.length != 3) {
            return result;
        }

        String[] segmentTwo = firstSeg[1].trim().split(":");


        if (segmentTwo == null || segmentTwo.length != 3) {
            return result;
        }

        result = new int[6];

        result[0] = Integer.valueOf(segmentOne[0]);

        result[1] = Integer.valueOf(segmentOne[1].startsWith("0") ?
                segmentOne[1].substring(1, 2) : segmentOne[1].substring(0, 2));

        result[2] = Integer.valueOf(segmentOne[2].startsWith("0") ?
                segmentOne[2].substring(1, 2) : segmentOne[2].substring(0, 2));

        result[3] = Integer.valueOf(segmentTwo[0].startsWith("0") ?
                segmentTwo[0].substring(1, 2) : segmentTwo[0].substring(0, 2));

        result[4] = Integer.valueOf(segmentTwo[1].startsWith("0") ?
                segmentTwo[1].substring(1, 2) : segmentTwo[1].substring(0, 2));

        result[5] = Integer.valueOf(segmentTwo[2].startsWith("0") ?
                segmentTwo[2].substring(1, 2) : segmentTwo[2].substring(0, 2));

        return result;
    }

    public static int getWeekOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week < 0)
            week = 0;
        return week;
    }

    public static int getMonthOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public static Long getCurrentTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static Long getDayStartTimestamp(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, dayOffset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 格式化时间
     *
     * @param pattern 时间
     * @return String 日期字符串
     */
    public static String formatGreenwichTime(Long time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date(time));
    }

    /**
     * 获得目标时间和当前时间的间隔，返回为毫秒数
     *
     * @param targetTime
     * @return
     */
    public static long getTimeIntervalTargetTimeAndNow(long targetTime) {
        return System.currentTimeMillis() - targetTime;
    }

    /**
     * 当前时间Slot
     *
     * @return
     */
    public static int getCurrentTimeSlot() {
        long dayStartTimestamp = DateUtil.getDayStartTimestamp(0);
        long currentTimestamp = DateUtil.getCurrentTimestamp();
        long betweenMillis = currentTimestamp - dayStartTimestamp;
        long slot = betweenMillis % 60000 == 0 ? betweenMillis / 60000 : betweenMillis / 60000 + 1;
        return (int) slot;
    }

    /**
     * 当前时间Slot
     *
     * @param currentTimestamp currentTimestamp
     * @return
     */
    public static int getCurrentTimeSlot(long currentTimestamp) {
        long slot = ((currentTimestamp + 28800000) % 86400000) / 60000;
        return (int) slot;
    }

    /**
     * 通过slot获取当前的时间戳
     *
     * @param slot
     * @return
     */
    public static long getCurrentTime(long slot) {
        long dayStartTimestamp = DateUtil.getDayStartTimestamp(0);
        return dayStartTimestamp + (slot * 60000);
    }

    /**
     * 将日期对象格式化成yyyy-mm-dd类型的字符串
     *
     * @param date 日期对象
     * @return 格式化后的字符串，无法格式化时，返回null
     */
    public static String formatDate(Date date) {
        return formatDateToString(date, DATE_STD_STR);
    }

    /**
     * 将日期对象格式化成yyyy-MM-dd HH:mm:ss类型的字符串
     *
     * @param date 日期对象
     * @return 格式化后的字符串，无法格式化时，返回null
     */
    public static String formatDateTime(Date date) {
        return formatDateToString(date, DATE_FULL_STR);
    }

    /**
     * 将日期对象格式化成yyyy-MM
     *
     * @param date 日期对象
     * @return 格式化后的字符串，无法格式化时，返回null
     */
    public static String formatDateYM(Date date) {
        return formatDateToString(date, YEAR_MONTH);
    }

    /**
     * 将日期对象格式化成指定的格式字符串
     *
     * @param date   日期对象
     * @param format 格式
     * @return 格式化后的字符串，无法格式化时，返回null
     */
    public static String formatDateToString(Date date, String format) {
        if (date == null || Strings.isNullOrEmpty(format)) {
            return null;
        }
        try {
            return DateTimeFormat.forPattern(format).print(date.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void main(String[] args) {
        String s = formatDateToString(new Date(), DATE_TIME_M_STR);
        System.out.println(parse(s+":00", DATE_TIME_STR));
        System.out.println(parse(s+":59", DATE_TIME_STR));
    }
}
