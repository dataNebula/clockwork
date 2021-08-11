package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:29 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
public interface ILoopClockService {

    /**
     * 构建所有作业环形时钟信息（所有）
     * <p>
     * 1。获取所有ONLINE并且的CronExp不为空的任务
     * 2。清空作业和槽位的关系表
     * 3。依次从新构建所有任务的环形时钟
     *
     * @return boolean
     */
    boolean buildTaskLoopClock();


    /**
     * 构建单个作业环形时钟信息（单个）
     *
     * @param taskId                 任务Id
     * @param nextMatchingTimeNumber 填充几次槽位（默认可以为1）
     * @return boolean
     */
    boolean addTaskToLoopClockSlot(Integer taskId, Integer nextMatchingTimeNumber);


    /**
     * 构建单个作业环形时钟信息（单个）
     *
     * @param TbClockworkTask        任务
     * @param nextMatchingTimeNumber 填充几次槽位
     * @return boolean
     */
    boolean addTaskToLoopClockSlot(TbClockworkTask TbClockworkTask, int nextMatchingTimeNumber);

    /**
     * 构建多个作业环形时钟信息（批量）
     *
     * @param taskPojoList taskList
     * @return boolean
     */
    boolean addTaskToLoopClockSlotByBatch(List<TbClockworkTaskPojo> taskPojoList);

    /**
     * 获取指定时间槽位的作业
     *
     * @param slotPosition 时间槽位
     * @return 时间槽位对应的任务列表
     */
    List<TbClockworkTaskPojo> getHasCronTaskFromSlot(Integer slotPosition);

}
