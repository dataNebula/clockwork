package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkDagMapper;
import com.creditease.adx.clockwork.web.entity.DeleteNodesParams;
import com.creditease.adx.clockwork.web.entity.SelectedParams;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:29 上午 2020/7/8
 * @ Description：
 * @ Modified By：
 */
public interface IDagService {

    /**
     * 获取子任务以及关系
     *
     * @param taskId         taskId
     * @param selectedParams params
     * @return
     */
    Map<String, Object> getTheChildTaskRelPicArray(Integer taskId, SelectedParams selectedParams);

    /**
     * 获取父任务以及关系
     *
     * @param taskId         taskId
     * @param selectedParams params
     * @return
     */
    Map<String, Object> getTheParentTaskRelPicArray(Integer taskId, SelectedParams selectedParams);


    /**
     * 移除节点关系
     * @param params params
     * @return
     */
    Map<String, Object> deleteNodesFromPic(DeleteNodesParams params);


    /**
     * DAG list pag（DAG列表页）
     *
     * @param dag dag
     * @return
     */
    int getAllDagByPageParamCount(TbClockworkDagPojo dag);

    List<TbClockworkDagPojo> getAllDagByPageParam(TbClockworkDagPojo dag, int pageNumber, int pageSize);


}
