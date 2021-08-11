package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;
import com.creditease.adx.clockwork.web.service.ITaskKeyWordService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName: TaskKeyWordController
 * @Author: ltb
 * @Date: 2021/3/16:3:44 下午
 * @Description:
 */
@Api("任务失败关键词相关接口")
@RestController
@RequestMapping("/clockwork/web/task/keyword")
public class TaskKeyWordController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskKeyWordController.class);

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();

    @Autowired
    ITaskKeyWordService taskKeyWordService;

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description 分页查询关键词
     * @Param [pageParam]
     */
    @ApiOperation("分页查询关键词")
    @PostMapping(value = "/searchPageKeyWordList")
    public Map<String, Object> searchPageTaskLogList(@RequestBody PageParam pageParam) {
        try {
            return Response.success(taskKeyWordService.findKeyWordByPageParam(pageParam));
        } catch (Exception e) {
            LOG.error("[TaskKeyWordController-searchPageKeyWordList], Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description 插入一条关键词
     * @Param [pageParam]
     */
    @ApiOperation("插入一条关键词")
    @PostMapping(value = "/createKeyWord")
    public Map<String, Object> createKeyWord(@RequestBody TbClockworkTaskErrorKeyword keyword) {
        try {
            return Response.success(taskKeyWordService.insertKeyWord(keyword));
        } catch (Exception e) {
            LOG.error("[TaskKeyWordController-createKeyWord], Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description 更新一条关键词
     * @Param [pageParam]
     */
    @ApiOperation("更新一条关键词")
    @PostMapping(value = "/updateKeyWord")
    public Map<String, Object> updateKeyWord(@RequestBody TbClockworkTaskErrorKeyword keyword) {
        try {
            if (keyword == null || StringUtils.isBlank(keyword.getErrorWord())) {
                return Response.fail("关键词不能为空！");
            }
            return Response.success(taskKeyWordService.updateKeyWord(keyword));
        } catch (Exception e) {
            LOG.error("[TaskKeyWordController-updateKeyWord], Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description 删除一条关键词
     * @Param [pageParam]
     */
    @ApiOperation("删除一条关键词")
    @GetMapping(value = "/daleteKeyWord")
    public Map<String, Object> deleteKeyWord(@RequestParam @ApiParam("keywordId") Integer keywordId) {
        try {
            if (keywordId == null || keywordId < 1) {
                LOG.error("TaskKeyWordController-deleteKeyWord], invalid id");
                return Response.fail("TaskKeyWordController-deleteKeyWord method, invalid id");
            }
            return Response.success(taskKeyWordService.deleteKeyWord(keywordId));
        } catch (Exception e) {
            LOG.error("[TaskKeyWordController-deleteKeyWord], Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
