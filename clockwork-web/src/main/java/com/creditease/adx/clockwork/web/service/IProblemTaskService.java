package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:58 下午 2020/12/28
 * @ Description：
 * @ Modified By：
 */
public interface IProblemTaskService {

    List<TbClockworkTaskPojo> getTasksByInvalidDagId();

}
