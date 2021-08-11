/*-
 *  
 * Clockwork
 *  
 * Copyright (C) 2019 - 2020 adx
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 */

package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.client.service.DagClientService;
import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.web.entity.DeleteNodesParams;
import com.creditease.adx.clockwork.web.entity.GetTaskRelPicParams;
import com.creditease.adx.clockwork.web.service.IDagService;
import com.creditease.adx.clockwork.web.service.IUserService;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:12 2019-10-17
 * @ Description：
 * @ Modified By：
 */
@Api("任务相关依赖接口")
@RestController
@RequestMapping("/clockwork/web/dag")
public class DagController {

    private static final Logger LOG = LoggerFactory.getLogger(DagController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private IDagService dagService;

    @Resource(name = "dagClientService")
    private DagClientService dagClientService;

    @Resource(name = "userService")
    private IUserService userService;

    /**
     * 刷新dag信息
     *
     * @param dagId dag id
     * @return TbClockworkDag
     */
    @GetMapping(value = "/refreshDagInfoById")
    public Map<String, Object> refreshDagInfoById(@RequestParam(value = "dagId") Integer dagId) {
        try {
            return Response.success(dagClientService.refreshDagInfoById(dagId));
        } catch (Exception e) {
            LOG.error("DagController-refreshDagInfoById Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 清除空的DAG信息（没有task引用）
     *
     * @return bool
     */
    @GetMapping(value = "/cleanEmptyDagInfo")
    public Map<String, Object> cleanEmptyDagInfo() {
        try {
            return Response.success(dagClientService.cleanEmptyDagInfo());
        } catch (Exception e) {
            LOG.error("DagController-cleanEmptyDagInfo Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取子任务以及关系
     *
     * @param params params
     * @return map
     */
    @PostMapping(value = "/getTheChildTaskRelPicArray")
    public Map<String, Object> getTheChildTaskRelPicArray(@RequestBody GetTaskRelPicParams params) {
        try {
            if (params == null || params.getTaskId() == null || params.getTaskId() < 1) {
                return Response.fail("invalid params taskId.");
            }
            return Response.success(dagService.getTheChildTaskRelPicArray(params.getTaskId(), params.getSelectedParams()));
        } catch (Exception e) {
            LOG.error("DagController-getTheChildTaskRelPicArray Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取父任务以及关系
     *
     * @param params params
     * @return map
     */
    @PostMapping(value = "/getTheParentTaskRelPicArray")
    public Map<String, Object> getTheParentTaskRelPicArray(@RequestBody GetTaskRelPicParams params) {
        try {
            if (params == null || params.getTaskId() == null || params.getTaskId() < 1) {
                return Response.fail("invalid params taskId.");
            }
            return Response.success(dagService.getTheParentTaskRelPicArray(params.getTaskId(), params.getSelectedParams()));
        } catch (Exception e) {
            LOG.error("DagController-getTheParentTaskRelPicArray Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 移除关系
     *
     * @param params params
     * @return map
     */
    @PostMapping(value = "/deleteNodesFromPic")
    public Map<String, Object> deleteNodesFromPic(@RequestBody DeleteNodesParams params) {
        try {
            if (params == null || params.getInactiveId() == null
                    || params.getSelectedParams() == null
                    || params.getSelectedParams().getSelectedLinks() == null) {
                return Response.fail("invalid params.");
            }
            return Response.success(dagService.deleteNodesFromPic(params));
        } catch (Exception e) {
            LOG.error("DagController-deleteNodesFromPic Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * web 列表页
     *
     * @param pageParam page
     * @return pages
     */
    @PostMapping(value = "/searchDagPageList")
    public Map<String, Object> searchDagPageList(@RequestBody PageParam pageParam) {
        LOG.info("[DagController-searchDagPageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
            TbClockworkDagPojo dag = gson.fromJson(pageParam.getCondition(), TbClockworkDagPojo.class);
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
                dag.setCreateUser(pageParam.getUserName());
            } else if (!userPojo.getIsAdmin()) {
                dag.setRoleName(userPojo.getRoleName());
            }

            // 查询数据
            int total = dagService.getAllDagByPageParamCount(dag);
            List<TbClockworkDagPojo> dags = dagService.getAllDagByPageParam(dag, pageNumber, pageSize);

            if (CollectionUtils.isNotEmpty(dags)) {
                PageInfo<TbClockworkDagPojo> pageInfo = new PageInfo<>(dags);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[DagController-searchDagPageList]" +
                                "dateSize = {}, roleName = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        dags.size(), userPojo != null ? userPojo.getRoleName() : null, total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);

            } else {

                PageInfo<TbClockworkDagPojo> pageInfo = new PageInfo<>(new ArrayList<TbClockworkDagPojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[DagController-searchDagPageList]get taskSize = {}, roleName = {}",
                        0, userPojo != null ? userPojo.getRoleName() : null
                );

                return Response.success(pageInfo);
            }

        } catch (Exception e) {
            LOG.error("DagController-searchDagPageList Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
