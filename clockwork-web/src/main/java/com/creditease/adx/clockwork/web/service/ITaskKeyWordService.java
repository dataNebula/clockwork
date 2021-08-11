package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;
import com.github.pagehelper.PageInfo;


public interface ITaskKeyWordService {

    PageInfo<TbClockworkTaskErrorKeyword> findKeyWordByPageParam(PageParam pageParam);

    int insertKeyWord(TbClockworkTaskErrorKeyword keyword);

    int updateKeyWord(TbClockworkTaskErrorKeyword keyword);

    int deleteKeyWord(Integer keywordId);
}
