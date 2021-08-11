package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:29 上午 2020/7/8
 * @ Description：
 * @ Modified By：
 */
public interface IGraphService {

    /**
     * 获取整个dag图的所有关联关系
     *
     * @param taskId taskId
     * @return all task relation
     */
    List<TbClockworkTaskRelation> getGraphAllRelationByTaskId(Integer taskId);

    /**
     * 获取整个dag图的所有TaskIds
     *
     * @param taskId 该图的某一个taskId
     * @return all task ids
     */
    List<Integer> getGraphAllTaskIdsByTaskId(Integer taskId);

    /**
     * 获取整个dag图的所有任务
     *
     * @param taskId 该图的某一个taskId
     * @return all task
     */
    List<TbClockworkTask> getGraphAllTasksByTaskId(Integer taskId);

}
