/**
 * This file is automatically generated by MyBatis Generator, do not modify.
 */
package com.creditease.adx.clockwork.dao.mapper.clockwork;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerunRelation;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerunRelationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TbClockworkTaskRerunRelationMapper {
    long countByExample(TbClockworkTaskRerunRelationExample example);

    int deleteByExample(TbClockworkTaskRerunRelationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TbClockworkTaskRerunRelation record);

    int insertSelective(TbClockworkTaskRerunRelation record);

    List<TbClockworkTaskRerunRelation> selectByExample(TbClockworkTaskRerunRelationExample example);

    TbClockworkTaskRerunRelation selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TbClockworkTaskRerunRelation record, @Param("example") TbClockworkTaskRerunRelationExample example);

    int updateByExample(@Param("record") TbClockworkTaskRerunRelation record, @Param("example") TbClockworkTaskRerunRelationExample example);

    int updateByPrimaryKeySelective(TbClockworkTaskRerunRelation record);

    int updateByPrimaryKey(TbClockworkTaskRerunRelation record);

    int batchInsert(List<TbClockworkTaskRerunRelation> records);
}