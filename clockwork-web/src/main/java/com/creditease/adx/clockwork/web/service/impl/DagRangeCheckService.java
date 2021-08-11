package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagRangeCheck;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagRangeCheckExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagRangeCheckPojo;
import com.creditease.adx.clockwork.dao.mapper.DagRangeCheckMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkDagRangeCheckMapper;
import com.creditease.adx.clockwork.web.service.IDagRangeCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: DagRangeCheckService
 * @Author: ltb
 * @Date: 2021/3/10:11:35 上午
 * @Description:
 */
@Service(value = "dagRangeCheckService")
public class DagRangeCheckService implements IDagRangeCheckService {

    @Autowired
    TbClockworkDagRangeCheckMapper tbClockworkDagRangeCheckMapper;

    @Autowired
    DagRangeCheckMapper dagRangeCheckMapper;

    @Override
    public int getCountAllLogsByPageParam(TbClockworkDagRangeCheckPojo checkPojo) {
        return dagRangeCheckMapper.countAllLogsByPageParam(checkPojo);
    }

    @Override
    public List<TbClockworkDagRangeCheck> getAllLogsByPageParam(TbClockworkDagRangeCheckPojo pojo, int pageNumber, int pageSize) {
        TbClockworkDagRangeCheckExample checkExample = new TbClockworkDagRangeCheckExample();
        TbClockworkDagRangeCheckExample.Criteria criteria = checkExample.createCriteria();
        if (null != pojo.getDagId()) {
            criteria.andDagIdEqualTo(pojo.getDagId());
        }
        if (!(null == pojo.getOperator() || pojo.getOperator().equals(""))) {
            criteria.andOperatorEqualTo(pojo.getOperator());
        }
        if (null != pojo.getIsRange()) {
            criteria.andIsRangeEqualTo(pojo.getIsRange());
        }
        if (null != pojo.getBeginDate()) {
            criteria.andCreateTimeGreaterThanOrEqualTo(pojo.getBeginDate());
        }
        if (null != pojo.getEndDate()) {
            criteria.andCreateTimeLessThanOrEqualTo(pojo.getEndDate());
        }

        checkExample.setOrderByClause("id desc");
        checkExample.setLimitStart(pageNumber - 1);
        checkExample.setLimitEnd(pageSize);
        return tbClockworkDagRangeCheckMapper.selectByExample(checkExample);
    }
}
