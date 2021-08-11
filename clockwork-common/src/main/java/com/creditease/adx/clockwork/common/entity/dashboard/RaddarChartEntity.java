package com.creditease.adx.clockwork.common.entity.dashboard;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 9:49 上午 2020/9/28
 * @ Description：
 * @ Modified By：
 */
public class RaddarChartEntity {

    /**
     * [节点1]
     */
    private String[] name;

    /**
     * [
     * {name: '1点'},
     * {name: '2点'},
     * {name: '3点'}
     * ]
     */
    private List<Map<String, Object>> chartName;

    /**
     * {
     * name: '节点1'
     * value: [1,2,3],
     * }
     */
    private List<Map<String, Object>> chartData;


    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public List<Map<String, Object>> getChartName() {
        return chartName;
    }

    public void setChartName(List<Map<String, Object>> chartName) {
        this.chartName = chartName;
    }

    public List<Map<String, Object>> getChartData() {
        return chartData;
    }

    public void setChartData(List<Map<String, Object>> chartData) {
        this.chartData = chartData;
    }
}
