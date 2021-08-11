package com.creditease.adx.clockwork.dao.mapper;

import com.creditease.adx.clockwork.common.pojo.TbClockworkDagRangeCheckPojo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface DagRangeCheckMapper {
    int countAllLogsByPageParam(TbClockworkDagRangeCheckPojo pojo);


}
