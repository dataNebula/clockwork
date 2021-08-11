package com.creditease.adx.clockwork.common.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:31 下午 2020/10/13
 * @ Description：
 * @ Modified By：
 */
public class StringUtil {

    /**
     * 给字符添加单引号
     *
     * @param str 字符串
     * @return
     */
    public static String spiltAndAppendSingleCitation(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String[] temp = str.split(",");
        for (String s : temp) {
            if (!"".equals(s) && s != null)
                sb.append("'").append(s).append("',");
        }
        String result = sb.toString();
        return result.endsWith(",") ? result.substring(0, result.length() - 1) : result;
    }

    /**
     * 将List<String>集合 转化为String
     * 如{"ab","bc"} To 'ab','bc'
     *
     * @param strList list
     * @return
     */
    public static String convertListToString(List<String> strList) {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(strList)) {
            for (int i = 0; i < strList.size(); i++) {
                if (i == 0) {
                    sb.append("'").append(strList.get(i)).append("'");
                } else {
                    sb.append(",").append("'").append(strList.get(i)).append("'");
                }
            }
        }
        return sb.toString();

    }


    /**
     * 字符串以list中的数据开头
     *
     * @param location         location
     * @param uploadPathPrefix prefix
     * @return
     */
    public static boolean locationStartsWithListPrefix(String location, String[] uploadPathPrefix) {
        if (uploadPathPrefix == null) {
            return true;
        }

        for (String pathPrefix : uploadPathPrefix) {
            if (location.startsWith(pathPrefix)) {
                return true;
            }
        }
        return false;

    }
}
