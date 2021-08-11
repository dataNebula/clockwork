package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.impl.TaskKeywordService;
import com.creditease.adx.clockwork.common.entity.Response;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName: TaskKeywordController
 * @Author: ltb
 * @Date: 2021/3/17:5:50 下午
 * @Description:
 */
@Api("任务失败关键词接口")
@RestController
@RequestMapping("/clockwork/api/task/keyword")
public class TaskKeywordController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskKeywordController.class);

    @Autowired
    TaskKeywordService keywordService;

    @GetMapping(value = "getKeywordInfoByKeywordIds")
    Map<String, Object> getKeyWordInfoByKeyIds(@RequestParam(value = "keyIds") String keyIds) {
        try {
            if (StringUtils.isBlank(keyIds)) {
                return Response.fail("invalid keyIds " + keyIds);
            }
            return Response.success(keywordService.getKeyWordInfoByKeyIds(keyIds));
        } catch (Exception e) {
            LOG.error("TaskKeywordController-getKeyWordInfoByTaskId Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }

    }
}
