package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;

import java.util.List;

public interface ITaskKeywordService {

    List<TbClockworkTaskErrorKeyword> getKeyWordInfoByKeyIds(String keyIds);

}
