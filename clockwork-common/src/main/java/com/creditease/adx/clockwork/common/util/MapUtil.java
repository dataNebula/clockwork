package com.creditease.adx.clockwork.common.util;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 上午11:07 2020/12/6
 * @ Description：
 * @ Modified By：
 */
public class MapUtil {


    /**
     * 过滤特殊处理后的的Key
     *
     * @param sets    start_end
     * @param filters str
     * @return str in startKey or endKey
     */
    public static Set<String> mapFilterForKey(Set<String> sets, Object filters) {
        if (sets == null) {
            return null;
        } else {
            Set<String> filterSets = sets.stream()
                    .filter((e) -> checkKey(e, filters))
                    .collect(Collectors.toSet());
            sets.removeAll(filterSets);
            return filterSets;
        }
    }

    /**
     * 匹配字符："start_end"中的（start or end）
     */
    private static boolean checkKey(String key, Object filters) {
        String[] keys = key.split("_");
        return keys.length == 2 && (keys[0].equals(String.valueOf(filters)) || keys[1].equals(String.valueOf(filters)));
    }


    public static void main(String[] args) {
        Set<String> params = new HashSet<>(16);
        params.add("12_1");
        params.add("12_2");
        params.add("12_3");
        params.add("12_4");
        params.add("13_5");
        params.add("14_5");
        params.add("15_5");
        params.add("5_12");
        params.add("16_1");
        params.add("16_2");


        HashSet<String> a = (HashSet<String>)mapFilterForKey(params, 12);
        System.out.println(JSON.toJSONString(a));

    }
}
