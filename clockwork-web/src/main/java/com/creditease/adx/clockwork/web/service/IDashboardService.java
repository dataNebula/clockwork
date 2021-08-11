package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.entity.dashboard.BarChartEntity;
import com.creditease.adx.clockwork.common.entity.dashboard.TaskOperationEntity;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:17 上午 2020/9/27
 * @ Description：
 * @ Modified By：
 */
public interface IDashboardService {



    /**
     * 总任务运行情况
     *
     * @param createUser 用户名
     * @param roleName   用户角色
     * @return
     */
    TaskOperationEntity getTaskTotalRunStatus(String createUser, String roleName);


    /**
     * 今日任务运行情况（当天）
     *
     * @param createUser 用户名
     * @param roleName   用户角色
     * @return
     */
    TaskOperationEntity getTaskTodayRunStatus(String createUser, String roleName);

    /**
     * 任务每小时成功数（当天）
     *
     * @param userName 用户名
     * @return
     */
    List<Map<String, Integer>> getTaskHourSuccess(String userName, String roleName);


    /**
     * 任务每小时失败数（当天）
     *
     * @param userName 用户名
     * @return
     */
    List<Map<String, Integer>> getTaskHourFailed(String userName, String roleName);

    /**
     * 查询任务节点运行情况（当天）
     *
     * @param userName 用户名
     * @return
     */
    List<Map<String, Object>> getTaskNodeRunCount(String userName, String roleName);


    /**
     * 各个节点每小时运行任务情况（当天）
     *
     * @param userName 用户名
     * @param roleName 角色
     * @return
     */
    BarChartEntity getTaskNodeHourRun(String userName, String roleName);

    List<TbClockworkTaskPojo> getWaitForRunTask(int size);

    List<TbClockworkTaskPojo> getLatelyFailedTask(int size);

}

