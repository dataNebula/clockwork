package com.creditease.adx.clockwork.common.entity.dashboard;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 9:49 上午 2020/9/28
 * @ Description：
 * @ Modified By：
 */
public class BarChartEntity {

    /**
     * ["1点", "2点", "3点"]
     */
    private String[] xData;


    /**
     * {
     * name: '节点1'
     * value: [100,200,300],
     * }
     */
    private List<Map<String, Object>> chartData;


    public String[] getxData() {
        return xData;
    }

    public void setxData(String[] xData) {
        this.xData = xData;
    }

    public List<Map<String, Object>> getChartData() {
        return chartData;
    }

    public void setChartData(List<Map<String, Object>> chartData) {
        this.chartData = chartData;
    }
}
