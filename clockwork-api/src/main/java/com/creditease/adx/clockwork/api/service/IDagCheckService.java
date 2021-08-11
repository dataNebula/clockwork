package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.DagCheckRangeInfo;

public interface IDagCheckService {

    DagCheckRangeInfo checkDagById(Integer dagId,String userName);

    boolean checkAllDags(String userName);
}
