/**
 * This file is automatically generated by MyBatis Generator, do not modify.
 */
package com.creditease.adx.clockwork.dao.mapper.clockwork;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeywordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TbClockworkTaskErrorKeywordMapper {
    long countByExample(TbClockworkTaskErrorKeywordExample example);

    int deleteByExample(TbClockworkTaskErrorKeywordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TbClockworkTaskErrorKeyword record);

    int insertSelective(TbClockworkTaskErrorKeyword record);

    List<TbClockworkTaskErrorKeyword> selectByExample(TbClockworkTaskErrorKeywordExample example);

    TbClockworkTaskErrorKeyword selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TbClockworkTaskErrorKeyword record, @Param("example") TbClockworkTaskErrorKeywordExample example);

    int updateByExample(@Param("record") TbClockworkTaskErrorKeyword record, @Param("example") TbClockworkTaskErrorKeywordExample example);

    int updateByPrimaryKeySelective(TbClockworkTaskErrorKeyword record);

    int updateByPrimaryKey(TbClockworkTaskErrorKeyword record);

    int batchInsert(List<TbClockworkTaskErrorKeyword> records);
}