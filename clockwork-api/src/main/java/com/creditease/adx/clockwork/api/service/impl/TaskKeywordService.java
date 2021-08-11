package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.ITaskKeywordService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeywordExample;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskErrorKeywordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: TaskKeywordService
 * @Author: ltb
 * @Date: 2021/3/17:6:18 下午
 * @Description:
 */
@Service(value = "taskKeywordService")
public class TaskKeywordService implements ITaskKeywordService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskKeywordService.class);

    @Autowired
    TbClockworkTaskErrorKeywordMapper keywordMapper;

    @Override
    public List<TbClockworkTaskErrorKeyword> getKeyWordInfoByKeyIds(String keyIds) {
        List<Integer> collect = Arrays.stream(keyIds.split(",")).map(x -> Integer.parseInt(x)).collect(Collectors.toList());
        TbClockworkTaskErrorKeywordExample keywordExample = new TbClockworkTaskErrorKeywordExample();
        keywordExample.createCriteria().andIdIn(collect);
        return keywordMapper.selectByExample(keywordExample);
    }

}
