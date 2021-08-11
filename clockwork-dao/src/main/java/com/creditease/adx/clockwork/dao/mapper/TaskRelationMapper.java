package com.creditease.adx.clockwork.dao.mapper;

import com.creditease.adx.clockwork.common.entity.RelationChildrenFather;
import com.creditease.adx.clockwork.common.entity.RelationFatherChildren;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午7:56 2020/12/6
 * @ Description：
 * @ Modified By：
 */
@Repository
@Mapper
public interface TaskRelationMapper {

    @MapKey("taskId")
    HashMap<Integer, RelationChildrenFather> selectChildrenFather();

    @MapKey("fatherTaskId")
    HashMap<Integer, RelationFatherChildren> selectFatherChildren();

}
