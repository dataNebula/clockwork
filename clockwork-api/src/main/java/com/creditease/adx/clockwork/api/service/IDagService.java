package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:29 上午 2020/7/8
 * @ Description：
 * @ Modified By：
 */
public interface IDagService {


    /**
     * 遍历dagId为空的任务，设置任务的dagId
     */
    boolean initTaskDagId();

    /**
     * 刷新dag基本信息（不会重新构建dagId）
     *
     * @param dagId dag id
     * @return
     */
    TbClockworkDag refreshDagInfoById(int dagId);

    /**
     * 刷新dag task count
     */
    int updateDagCount();

    /**
     * 清除空的DAG信息（没有task引用）
     *
     * @return 被清空的DagIds列表
     */
    List<Integer> cleanEmptyDagInfo();

    /**
     * 重新构建该dagId相关的所有任务
     *
     * @param dagId dagId
     * @return bool
     */
    boolean buildDagIdForDagId(Integer dagId);

    /**
     * 重新构建该dagIds相关的所有任务
     *
     * @param dagIds dagIds
     * @return bool
     */
    boolean buildDagIdForDagIds(List<Integer> dagIds);

    /**
     * 构建DagId（构建单个任务）
     *
     * @param task task
     * @return dagId
     */
    int buildDagIdForTask(TbClockworkTask task);


    /**
     * 构建DagId（构建单个任务）【同上】
     *
     * @param taskId taskId
     * @return dagId
     */
    int buildDagIdForTaskId(int taskId);


    /**
     * 构建DagId（重新构建这一批任务）
     *
     * @param taskIds list
     */
    void buildDagIdForTaskIds(List<Integer> taskIds);


    /**
     * 构建DagId, task以及该任务以前的直接关联节点（任务添加、更新、删除）
     *
     * @param taskId                 taskId
     * @param oldDirectlyRelatedTask 旧的关联关系
     * @return
     */
    boolean buildDagIdForTaskUpdate(Integer taskId, List<Integer> oldDirectlyRelatedTask);

}
