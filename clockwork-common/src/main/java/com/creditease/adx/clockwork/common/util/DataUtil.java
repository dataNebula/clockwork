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

import java.util.ArrayList;
import java.util.List;

/**
 * 数据处理相关
 */
public class DataUtil {

    /**
     * 将一个list均分成n个list,主要通过偏移量来实现的
     *
     * @param source
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<List<T>>();
        int remaider = source.size() % n;  //(先计算出余数)
        int number = source.size() / n;  //然后是商
        int offset = 0;//偏移量
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remaider--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 将一组数据固定分组，每组n个元素
     *
     * @param source 要分组的数据源
     * @param n      每组n个元素（n大于0）
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> fixedGrouping(List<T> source, int n) {
        if (null == source || source.size() == 0 || n <= 0)
            return null;
        List<List<T>> result = new ArrayList<List<T>>();

        int sourceSize = source.size();
        int size = (source.size() / n) + 1;
        for (int i = 0; i < size; i++) {
            List<T> subset = new ArrayList<T>();
            for (int j = i * n; j < (i + 1) * n; j++) {
                if (j < sourceSize) {
                    subset.add(source.get(j));
                }
            }
            if (subset.size() != 0) result.add(subset);
        }
        return result;
    }

    /**
     * 将一个list按比例分成n个list
     *
     * @param source      待切分待list
     * @param percentages 百分比集合，例如[60,30,10],降序排列
     * @return
     */
    public static <T> List<List<T>> assign(List<T> source, List<Integer> percentages) {
        List<List<T>> result = new ArrayList<List<T>>();
        int size = source.size();
        int offset = 0;//偏移量

        for (int i = 0; i < percentages.size(); i++) {
            List<T> value = new ArrayList<>();
            double j = size * percentages.get(i) / 100.0;
            if (j > 1) {
                value.addAll(source.subList(offset, offset + (int) j));
                offset = offset + (int) j;
                if (i == percentages.size() - 1 && offset < source.size()) {
                    result.get(0).addAll(source.subList(offset, source.size()));
                }
            } else {
                if (offset + 1 <= size) {
                    value.addAll(source.subList(offset, offset + 1));
                    offset = offset + 1;
                }
            }
            result.add(value);
        }
        return result;
    }

    public static String specialCharHandle(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.toCharArray()[i];
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if ((c >= 0 && c <= 31) || c == 127)//在ASCⅡ码中，第0～31号及第127号(共33个)是控制字符或通讯专用字符
                    {

                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 一个数组是否包含另外一个
     *
     * @param all   所有
     * @param child 孩子
     * @return
     */
    public static boolean isContainsChild(List<Integer> all, List<Integer> child) {
        if (all == null || child == null) return false;
        for (Integer a : child) {
            if (!all.contains(a)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个集合是否相等
     *
     * @param all   所有
     * @param child 孩子
     * @return
     */
    public static boolean isEqualChild(List<Integer> all, List<Integer> child) {
        if (all == null || child == null) return false;
        if (all.size() != child.size()) return false;
        for (Integer a : child) {
            if (!all.contains(a)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        List<Integer> all = new ArrayList<>();
        all.add(12);
        all.add(1);
        all.add(2);


        List<Integer> child = new ArrayList<>();
        child.add(12);
        child.add(2);

        System.out.println(isContainsChild(all, child));
        System.out.println(isEqualChild(all, child));


    }

}
