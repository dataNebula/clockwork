package com.creditease.adx.clockwork.dao.mapper;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:54 下午 2020/12/28
 * @ Description：
 * @ Modified By：
 */
public interface ProblemTaskMapper {

    List<TbClockworkTask> selectTaskInvalidDagId();

    List<TbClockworkTask> selectTaskInvalidTriggerCondition();

}
