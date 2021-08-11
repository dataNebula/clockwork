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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 替换变量
 */
public class ReplaceParameterUtil {

    protected static final Logger LOG = LoggerFactory.getLogger(ReplaceParameterUtil.class);

    // 假设当前时间为：2020-02-03 12:09:12
    private static final String BEFORE_YESTERDAY = "beforeyesterday";       // 前天 eg:2020-02-01
    private static final String YESTERDAY = "yesterday";                    // 昨天 eg:2020-02-02
    private static final String TODAY = "today";                            // 今天 eg:2020-02-03
    private static final String THIS_MONTH_BEG = "thismonthbeg";            // 当前月的第一天，  eg:2020-02-01
    private static final String THIS_MONTH_END = "thismonthend";            // 当前月的最后一天，eg:2020-02-29
    private static final String THIS_MONTH = "thismonth";                   // 这个月 eg：2020-02
    private static final String LAST_MONTH_BEG = "lastmonthbeg";            // 上一个月的第一天，  eg:2020-01-01
    private static final String LAST_MONTH_END = "lastmonthend";            // 上一个月的最后一天，eg:2020-01-31
    private static final String LAST_MONTH = "lastmonth";                   // 上个月 eg:2020-01
    private static final String TWO_MONTHS_AGO = "twomonthsago";            // 上上个月 eg:2019-12
    private static final String THIS_WEEK_BEG = "thisweekbeg";              // 本周的第一天        eg:2020-02-03
    private static final String THIS_WEEK_END = "thisweekend";              // 本周的最后一天      eg:2020-02-09
    private static final String THIS_WEEK_SUN = "thisweeksun";              // 本周的第一天 (星期天) eg:2020-02-02
    private static final String THIS_WEEK_SAT = "thisweeksat";              // 本周的最后一天(星期六) eg:2020-02-08
    private static final String LAST_WEEK_BEG = "lastweekbeg";              // 上一周的第一天   eg:2020-01-27
    private static final String LAST_WEEK_END = "lastweekend";              // 上一周的最后一天 eg:2020-02-02
    private static final String LAST_WEEK_TODAY = "lastweektoday";          // 一周前(7天前)   eg:2020-01-27
    private static final String LAST_WEEK_YESTERDAY = "lastweekyesterday";  // T+1一周前（昨天的7天前）eg:2020-01-26
    private static final String HALF_MONTH_TODAY = "halfmonthtoday";        // 半个月前(15天前)   eg:2020-01-19
    private static final String HALF_MONTH_YESTERDAY = "halfmonthyestoday"; // T+1半个月前(昨天的15天前) eg:2020-01-18
    private static final String LAST_HOUR = "lasthour";                     // 当前时间的上一个小时，eg:当前12:09，改值为11
    private static final String THIS_QUARTER_BEG = "thisquarterbeg";        // 这季度第一天，eg:2020-01-01
    private static final String THIS_QUARTER_END = "thisquarterend";        // 这季度最后一天，eg:2020-03-31
    private static final String LAST_DAY_OF_MONTH_END = "lastdayofmonthend";// 昨天月的最后一天，eg:2020-02-29

    /**
     * 替换变量并返回文本内容
     *
     * @param fileAbsolutePath 文件路径
     * @param parameter        用户参数
     * @param customBaseDate   用户自定义基准日期
     * @return
     * @throws Exception
     */
    public static String replaceVariableReturnContent(String fileAbsolutePath, String parameter, String customBaseDate) throws Exception {
        LOG.info("[ReplaceParameterUtil][replaceVariableReturnContent]" +
                        "fileAbsolutePath = {}, parameter = {}, customBaseDate = {}",
                fileAbsolutePath,
                parameter,
                customBaseDate);

        // 用户设置了参数，则优先按照用户的参数进行替换脚本里面的变量；
        // 用户设置了当前的时间则按照当前的时间计算
        File file = new File(fileAbsolutePath);
        String fileContent = FileUtils.readFileToString(file);
        // 1。参数
        if (parameter != null && !"".equals(parameter)) {
            JSONObject jb = JSONObject.parseObject(parameter);
            if (jb != null) for (String key : jb.keySet()) {
                if (fileContent.indexOf(key) > 0) {
                    String value = jb.getString(key);
                    if (value != null && !value.equals("")) {
                        fileContent = fileContent.replaceAll(quoteReplacement(key), value);
                    }
                }
            }
        }

        // 2。时间, 如果用户自定义了基准时间则取用户的时间
        Date dateTimeParameter = new Date();
        if (StringUtils.isNoneBlank(customBaseDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateTimeParameter = sdf.parse(customBaseDate);
        }

        return ReplaceParameterUtil.replaceSystemDefaultVariable(dateTimeParameter, customBaseDate, fileContent);

    }

    private static String replaceSystemDefaultVariable(
            Date dateTimeParameter, String customBaseDate, String content) throws Exception {
        Calendar cal = Calendar.getInstance();

        if (content.indexOf(BEFORE_YESTERDAY) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.DATE, -2); //得到前天
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + BEFORE_YESTERDAY, time);
            content = content.replaceAll("\\$\\{" + BEFORE_YESTERDAY + "\\}", time);
        }
        if (content.indexOf(YESTERDAY) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.DATE, -1);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + YESTERDAY, time);
            content = content.replaceAll("\\$\\{" + YESTERDAY + "\\}", time);
        }
        if (content.indexOf(TODAY) > 0) {
            cal.setTime(dateTimeParameter);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + TODAY, time);
            content = content.replaceAll("\\$\\{" + TODAY + "\\}", time);
        }
        if (content.indexOf(THIS_MONTH_BEG) > 0) {
            cal.setTime(dateTimeParameter);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_MONTH_BEG, time);
            content = content.replaceAll("\\$\\{" + THIS_MONTH_BEG + "\\}", time);
        }
        if (content.indexOf(THIS_MONTH_END) > 0) {
            cal.setTime(dateTimeParameter);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_MONTH_END, time);
            content = content.replaceAll("\\$\\{" + THIS_MONTH_END + "\\}", time);
        }
        if (content.indexOf(THIS_MONTH) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.MONTH, 0);
            String time = DateUtil.formatDateYM(cal.getTime());
            content = content.replaceAll("\\$" + THIS_MONTH, time);
            content = content.replaceAll("\\$\\{" + THIS_MONTH + "\\}", time);
        }
        if (content.indexOf(LAST_MONTH_BEG) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + LAST_MONTH_BEG, time);
            content = content.replaceAll("\\$\\{" + LAST_MONTH_BEG + "\\}", time);
        }
        if (content.indexOf(LAST_MONTH_END) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + LAST_MONTH_END, time);
            content = content.replaceAll("\\$\\{" + LAST_MONTH_END + "\\}", time);
        }
        if (content.indexOf(LAST_DAY_OF_MONTH_END) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.DATE, -1);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + LAST_DAY_OF_MONTH_END, time);
            content = content.replaceAll("\\$\\{" + LAST_DAY_OF_MONTH_END + "\\}", time);
        }
        if (content.indexOf(TWO_MONTHS_AGO) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.MONTH, -2);
            String time = DateUtil.formatDateYM(cal.getTime());
            content = content.replaceAll("\\$" + TWO_MONTHS_AGO, time);
            content = content.replaceAll("\\$\\{" + TWO_MONTHS_AGO + "\\}", time);
        }
        if (content.indexOf(LAST_MONTH) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.MONTH, -1);
            String time = DateUtil.formatDateYM(cal.getTime());
            content = content.replaceAll("\\$" + LAST_MONTH, time);
            content = content.replaceAll("\\$\\{" + LAST_MONTH + "\\}", time);
        }
        if (content.indexOf(THIS_WEEK_BEG) > 0) {
            cal.setTime(dateTimeParameter);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_WEEK_BEG, time);
            content = content.replaceAll("\\$\\{" + THIS_WEEK_BEG + "\\}", time);
        }
        if (content.indexOf(THIS_WEEK_SUN) > 0) {
            cal.setTime(dateTimeParameter);
            cal.setFirstDayOfWeek(Calendar.SUNDAY);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_WEEK_SUN, time);
            content = content.replaceAll("\\$\\{" + THIS_WEEK_SUN + "\\}", time);
        }
        if (content.indexOf(THIS_WEEK_END) > 0) {
            cal.setTime(dateTimeParameter);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 6);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_WEEK_END, time);
            content = content.replaceAll("\\$\\{" + THIS_WEEK_END + "\\}", time);
        }
        if (content.indexOf(THIS_WEEK_SAT) > 0) {

            cal.setTime(dateTimeParameter);
            cal.setFirstDayOfWeek(Calendar.SUNDAY);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 6);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_WEEK_SAT, time);
            content = content.replaceAll("\\$\\{" + THIS_WEEK_SAT + "\\}", time);
        }
        if (content.indexOf(LAST_WEEK_BEG) > 0) {
            cal.setTime(dateTimeParameter);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.add(Calendar.DATE, -7);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + LAST_WEEK_BEG, time);
            content = content.replaceAll("\\$\\{" + LAST_WEEK_BEG + "\\}", time);
        }
        if (content.indexOf(LAST_WEEK_END) > 0) {
            cal.setTime(dateTimeParameter);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.add(Calendar.DATE, -7);
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 6);

            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + LAST_WEEK_END, time);
            content = content.replaceAll("\\$\\{" + LAST_WEEK_END + "\\}", time);
        }
        if (content.indexOf(LAST_WEEK_TODAY) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.DATE, -7);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + LAST_WEEK_TODAY, time);
            content = content.replaceAll("\\$\\{" + LAST_WEEK_TODAY + "\\}", time);
        }
        if (content.indexOf(LAST_WEEK_YESTERDAY) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.DATE, -8);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + LAST_WEEK_YESTERDAY, time);
            content = content.replaceAll("\\$\\{" + LAST_WEEK_YESTERDAY + "\\}", time);
        }
        if (content.indexOf(HALF_MONTH_TODAY) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.DATE, -15);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + HALF_MONTH_TODAY, time);
            content = content.replaceAll("\\$\\{" + HALF_MONTH_TODAY + "\\}", time);
        }
        if (content.indexOf(HALF_MONTH_YESTERDAY) > 0) {
            cal.setTime(dateTimeParameter);
            cal.add(Calendar.DATE, -16);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + HALF_MONTH_YESTERDAY, time);
            content = content.replaceAll("\\$\\{" + HALF_MONTH_YESTERDAY + "\\}", time);
        }
        if (content.indexOf(LAST_HOUR) > 0) {
            if (StringUtils.isNoneBlank(customBaseDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
                dateTimeParameter = sdf.parse(customBaseDate);
            }
            cal.setTime(dateTimeParameter);
            cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 1);
            String time = DateUtil.formatDateToString(cal.getTime(), "HH");
            content = content.replaceAll("\\$" + LAST_HOUR, time);
            content = content.replaceAll("\\$\\{" + LAST_HOUR + "\\}", time);
        }
        if (content.indexOf(THIS_QUARTER_BEG) > 0) {
            cal.setTime(dateTimeParameter);
            int month = getQuarterInMonth(cal.get(Calendar.MONTH) + 1, true);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_QUARTER_BEG, time);
            content = content.replaceAll("\\$\\{" + THIS_QUARTER_BEG + "\\}", time);
        }
        if (content.indexOf(THIS_QUARTER_END) > 0) {
            cal.setTime(dateTimeParameter);
            int month = getQuarterInMonth(cal.get(Calendar.MONTH) + 1, false);
            cal.set(Calendar.MONTH, month + 1);
            cal.set(Calendar.DAY_OF_MONTH, 0);
            String time = DateUtil.formatDate(cal.getTime());
            content = content.replaceAll("\\$" + THIS_QUARTER_END, time);
            content = content.replaceAll("\\$\\{" + THIS_QUARTER_END + "\\}", time);
        }

        return content;
    }

    // 返回第几个月份，不是几月
    // 季度一年四季， 第一季度：1月-3月， 第二季度：4月-6月， 第三季度：7月-9月， 第四季度：10月-12月
    private static int getQuarterInMonth(int month, boolean isQuarterStart) {
        int[] months = {0, 3, 6, 9};
        if (!isQuarterStart) {
            months = new int[]{2, 5, 8, 11};
        }
        if (month >= 1 && month <= 3)
            return months[0];
        else if (month >= 4 && month <= 6)
            return months[1];
        else if (month >= 7 && month <= 9)
            return months[2];
        else
            return months[3];
    }

    private static String quoteReplacement(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
            return s;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$' || c == '{' || c == '}') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }


    /**
     * 获取替换的key
     *
     * @param KEY
     * @return
     */
    public static String[] getRepKey(String KEY) {
        String[] result = new String[2];
        result[0] = "\\$" + KEY;
        result[1] = "\\$\\{" + KEY + "\\}";
        return result;
    }
}
