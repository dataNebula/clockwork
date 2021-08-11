package com.creditease.adx.clockwork.web.service;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:29 上午 2020/7/8
 * @ Description：
 * @ Modified By：
 */
public interface IGraphService {


    /**
     * 构建辐射DAG图，通过dagId
     *
     * @param dagId dagId
     * @return Group
     */
    Map<String, Object> buildDagGraphForRadialByDagId(Integer dagId);

    /**
     * 获取附带位置的Dag图, 根据dagId
     *
     * @param dagId dagId
     * @return Group
     */
    Map<String, Object> buildDagGraphForPositionByDagId(Integer dagId);


    /**
     * 获取任务Dag图, 根据taskId or taskName（优先taskId）
     *
     * @param taskId        taskId
     * @param taskName      taskName
     * @param userName      userName
     * @param upDeepLevel   高度
     * @param downDeepLevel 深度
     * @param showDag       是否展示整个图
     * @return Group
     */
    Map<String, Object> getTaskDagGraph(
            Integer taskId, String taskName, String userName, Integer upDeepLevel, Integer downDeepLevel, boolean showDag);



}
