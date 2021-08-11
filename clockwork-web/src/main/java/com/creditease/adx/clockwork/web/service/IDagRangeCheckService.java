package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagRangeCheck;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagRangeCheckPojo;

import java.util.List;


public interface IDagRangeCheckService {

    // 分页查询
    int getCountAllLogsByPageParam(TbClockworkDagRangeCheckPojo tbClockworkDagRangeCheck);

    List<TbClockworkDagRangeCheck> getAllLogsByPageParam(TbClockworkDagRangeCheckPojo tbClockworkDagRangeCheck, int pageNumber, int pageSize);
}
