package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.client.service.DagCheckClientService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagRangeCheck;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagRangeCheckPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.web.service.IDagRangeCheckService;
import com.creditease.adx.clockwork.web.service.IUserService;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: DagRangeCheckController
 * @Author: ltb
 * @Date: 2021/3/10:10:38 上午
 * @Description:
 */
@Api("DAG成环检测相关接口")
@RestController
@RequestMapping("/clockwork/web/dag/check")
public class DagRangeCheckController {
    private static final Logger LOG = LoggerFactory.getLogger(DagRangeCheckController.class);

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").disableHtmlEscaping().create();

    @Autowired
    private IUserService userService;

    @Autowired
    private IDagRangeCheckService dagRangeCheckService;

    @Autowired
    private DagCheckClientService checkClient;

    @ApiOperation("对单个dag进行成环检测")
    @GetMapping(value = "/checkDagById")
    public Map<String, Object> checkDagById(@RequestParam(value = "dagId")
                                            @ApiParam(name = "dag id") Integer dagId,
                                            @RequestParam(value = "userName")
                                            @ApiParam(name = "userName") String userName) {
        try {
            return Response.success(checkClient.checkDagById(dagId,userName));
        } catch (Exception e) {
            LOG.error("DagRangeCheckController-checkDagById Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("对全部dag进行成环检测")
    @GetMapping(value = "/checkAllDags")
    public Map<String, Object> checkAllDags(@RequestParam(value = "userName")
                                            @ApiParam(name = "userName") String userName) {
        try {
            boolean checkAllDags = checkClient.checkAllDags(userName);
            if (checkAllDags){
                return Response.success("add dags to check queue, start check...");
            }else {
                return Response.success("check queue is not empty, dont need add, please use checkDagById.");
            }
        } catch (Exception e) {
            LOG.error("DagRangeCheckController-checkAllDags Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description 分页分条件查询成环检测结果
     * @Param [pageParam]
     */
    @ApiOperation("分页查询成环记录")
    @PostMapping(value = "/searchDagCheckList")
    public Map<String, Object> searchDagCheckPageList(@RequestBody PageParam pageParam) {
        LOG.info("[DagRangeCheckController-searchDagCheckList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
            TbClockworkDagRangeCheckPojo dagRangeCheckPojo =
                    gson.fromJson(pageParam.getCondition(), TbClockworkDagRangeCheckPojo.class);
            int pageNumber = pageParam.getPageNum();
            if (pageNumber < 1) {
                pageNumber = 1;
            }
            int pageSize = pageParam.getPageSize();
            if (pageSize < 10) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }
            if (StringUtils.isBlank(pageParam.getUserName())) {
                throw new RuntimeException("userName field is null that value must be user name info.");
            }
            // 赋予查询角色权限，无角色查询自己创建的，管理员查询所有，非管理员查询该用户角色下所有用户公开的任务
            TbClockworkUserPojo userPojo = userService.getUserAndRoleByUserName(pageParam.getUserName());
            if (userPojo == null) {
                dagRangeCheckPojo.setOperator(pageParam.getUserName());
            }
//            因为非admin用户只能查看自己的数据，在这强行写回去，非admin用户这个参数无效　
            else if (!userPojo.getIsAdmin()) {
                dagRangeCheckPojo.setOperator(userPojo.getUserName());
                dagRangeCheckPojo.setRoleName(userPojo.getRoleName());
            }

            // 查询数据
            int total = dagRangeCheckService.getCountAllLogsByPageParam(dagRangeCheckPojo);
            List<TbClockworkDagRangeCheck> allLogsByUserName =
                    dagRangeCheckService.getAllLogsByPageParam(dagRangeCheckPojo, pageNumber, pageSize);

            if (CollectionUtils.isNotEmpty(allLogsByUserName)) {
                PageInfo<TbClockworkDagRangeCheck> pageInfo = new PageInfo<>(allLogsByUserName);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[DagRangeCheckController-searchDagCheckList]" +
                                "dateSize = {}, roleName = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        allLogsByUserName.size(), userPojo != null ? userPojo.getRoleName() : null, total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);

            } else {
                PageInfo<TbClockworkDagPojo> pageInfo = new PageInfo<>(new ArrayList<TbClockworkDagPojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[DagRangeCheckController-searchDagCheckList]get taskSize = {}, roleName = {}",
                        0, userPojo != null ? userPojo.getRoleName() : null
                );
                return Response.success(pageInfo);
            }

        } catch (Exception e) {
            LOG.error("DagRangeCheckController-searchDagCheckList Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
