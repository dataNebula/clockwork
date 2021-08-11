package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeywordExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskErrorKeywordMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import com.creditease.adx.clockwork.web.service.ITaskKeyWordService;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName: TaskKeyWordService
 * @Author: ltb
 * @Date: 2021/3/16:4:19 下午
 * @Description:
 */
@Service
public class TaskKeyWordService implements ITaskKeyWordService {

  private static final Logger LOG = LoggerFactory.getLogger(TaskKeyWordService.class);

  private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();

  @Autowired
  TbClockworkTaskErrorKeywordMapper keywordMapper;
  @Autowired
  TbClockworkTaskMapper taskMapper;


  @Override
  public PageInfo<TbClockworkTaskErrorKeyword> findKeyWordByPageParam(PageParam pageParam) {
    TbClockworkTaskErrorKeyword keyword =
      gson.fromJson(pageParam.getCondition(), TbClockworkTaskErrorKeyword.class);
    int pageNumber = pageParam.getPageNum();
    if (pageNumber < 1) {
      pageNumber = 1;
    }
    int pageSize = pageParam.getPageSize();
    if (0 < pageSize && pageSize < 10) {
      pageSize = 10;
    }
    if (pageSize > 100) {
      pageSize = 100;
    }
    if (StringUtils.isBlank(pageParam.getUserName())) {
      throw new RuntimeException("userName field is null that value must be user name info.");
    }
    TbClockworkTaskErrorKeywordExample example = new TbClockworkTaskErrorKeywordExample();
    TbClockworkTaskErrorKeywordExample.Criteria criteria = example.createCriteria();
    if (null != keyword.getId()) {
      criteria.andIdEqualTo(keyword.getId());
    }
    if (null != keyword.getErrorWord()) {
      criteria.andErrorWordLike("%" + keyword.getErrorWord() + "%");
    }
    int total = (int) keywordMapper.countByExample(example);
    if (0 == pageSize) {
      pageSize = total;
    }
    List<TbClockworkTaskErrorKeyword> tbClockworkTaskErrorKeywords = keywordMapper.selectByExample(example);
    if (CollectionUtils.isNotEmpty(tbClockworkTaskErrorKeywords)) {
      PageInfo<TbClockworkTaskErrorKeyword> pageInfo = new PageInfo<>(tbClockworkTaskErrorKeywords);
      pageInfo.setPageNum(pageNumber);
      pageInfo.setPageSize(pageSize);
      pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
      pageInfo.setTotal(total);
      LOG.info("[TaskKeyWordService-findKeyWordByPageParam]" +
          "dateSize = {},  total = {}, pageSize = {}, page number = {}, pages = {}",
        tbClockworkTaskErrorKeywords.size(), total, pageSize, pageNumber, pageInfo.getPages());
      return pageInfo;

    } else {
      PageInfo<TbClockworkTaskErrorKeyword> pageInfo = new PageInfo<>(new ArrayList<TbClockworkTaskErrorKeyword>());
      pageInfo.setPageNum(pageNumber);
      pageInfo.setPageSize(pageSize);
      LOG.info("[TaskKeyWordService-findKeyWordByPageParam]get keywordSize = {}.", 0);
      return pageInfo;
    }
  }

  @Override
  public int insertKeyWord(TbClockworkTaskErrorKeyword keyword) {
    if (keyword == null) {
      return -1;
    }
    keyword.setCreateTime(new Date());
    keyword.setUpdateTime(new Date());
    return keywordMapper.insertSelective(keyword);
  }

  @Override
  public int updateKeyWord(TbClockworkTaskErrorKeyword keyword) {
    if (keyword == null
      || keyword.getId() == null
      || StringUtils.isBlank(keyword.getErrorWord())) {
      return -1;
    }
    keyword.setUpdateTime(new Date());
    return keywordMapper.updateByPrimaryKeySelective(keyword);
  }

  @Override
  public int deleteKeyWord(Integer keywordId) {
//       先判断这个keyword有没有在task中绑定
    TbClockworkTaskExample taskExample = new TbClockworkTaskExample();
    taskExample.createCriteria().andErrorKeywordIdsIsNotNull();
    List<TbClockworkTask> clockworkTasks = taskMapper.selectByExample(taskExample);
    clockworkTasks.stream().forEach(
      task -> {
        if (StringUtils.isNotBlank(task.getErrorKeywordIds())) {
          boolean hasKeyword = Arrays.stream(task.getErrorKeywordIds().split(",")).anyMatch(word ->
            keywordId == Integer.parseInt(word));
          if (hasKeyword) {
            throw new RuntimeException("this task have the keyword,please update task first. taskId = " + task.getId());
          }
        }
      }
    );
    TbClockworkTaskErrorKeywordExample example = new TbClockworkTaskErrorKeywordExample();
    example.createCriteria().andIdEqualTo(keywordId);
    return keywordMapper.deleteByExample(example);
  }
}
