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

import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeGroup;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodeGroupPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.web.service.INodeGroupService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:43 下午 2020/8/10
 * @ Description：节点组服务
 * @ Modified By：
 */
@Api("WEB节点组相关接口")
@RestController
@RequestMapping("/clockwork/web/node/group")
public class NodeGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(NodeGroupController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private INodeGroupService nodeGroupService;

    /**
     * 查询所有node
     *
     * @return
     */
    @GetMapping(value = "/getAllNodeGroup")
    public Map<String, Object> getAllNodeGroup() {
        try {
            List<TbClockworkNodeGroup> tbClockworkNodes = nodeGroupService.getAllNodeGroup();
            List<TbClockworkNodeGroupPojo> pojoList =
                    PojoUtil.convertList(tbClockworkNodes, TbClockworkNodeGroupPojo.class);
            return Response.success(pojoList);
        } catch (Exception e) {
            LOG.error("NodeGroupController-getAllNodeGroup Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 添加
     *
     * @param pojo pojo
     */
    @PostMapping(value = "/addNodeGroup")
    public Map<String, Object> addNodeGroup(@RequestBody TbClockworkNodeGroupPojo pojo) {
        try {
            if (pojo == null || StringUtils.isBlank(pojo.getName())) {
                return Response.fail("NodeGroupController-deleteNodeGroup, invalid param");
            }
            if (nodeGroupService.getNodeGroupByName(pojo.getName()) != null) {
                return Response.fail("Error, group name already exists!");
            }

            return Response.success(nodeGroupService.addNodeGroup(pojo));
        } catch (Exception e) {
            LOG.error("NodeGroupController-addNodeGroup Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改
     *
     * @param pojo pojo
     */
    @PostMapping(value = "/updateNodeGroup")
    public Map<String, Object> updateNodeGroup(@RequestBody TbClockworkNodeGroupPojo pojo) {
        try {
            if (pojo == null || StringUtils.isBlank(pojo.getName())
                    || pojo.getId() == null || pojo.getId() < 1) {
                return Response.fail("NodeGroupController-updateNodeGroup, invalid param");
            }
            // name不能重复
            TbClockworkNodeGroupPojo nodeGroupByName = nodeGroupService.getNodeGroupByName(pojo.getName());
            if (nodeGroupByName != null && !nodeGroupByName.getId().equals(pojo.getId())) {
                return Response.fail("Error, group name already exists!");
            }
            return Response.success(nodeGroupService.updateNodeGroup(pojo));
        } catch (Exception e) {
            LOG.error("NodeGroupController-updateNodeGroup Error {}!", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param id node group id
     */
    @PostMapping(value = "/deleteNodeGroup")
    public Map<String, Object> deleteNodeGroup(@RequestParam(value = "id") Integer id) {
        if (id == null) {
            LOG.error("NodeGroupController-deleteNodeGroup, invalid id");
            return Response.fail("invalid nodeId");
        }
        try {
            return Response.success(nodeGroupService.deleteNodeGroup(id));
        } catch (Exception e) {
            LOG.error("NodeGroupController-deleteNodeGroup Error {}!", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 分页查询
     *
     * @param pageParam page
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping(value = "/searchNodeGroupPageList")
    public Map<String, Object> searchNodeGroupPageList(@RequestBody PageParam pageParam) {
        LOG.info("[NodeGroupController-searchNodeGroupPageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
            TbClockworkNodeGroupPojo node = gson.fromJson(pageParam.getCondition(), TbClockworkNodeGroupPojo.class);
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

            // 查询数据
            int total = nodeGroupService.getAllNodeGroupByPageParamCount(node);
            List<TbClockworkNodeGroupPojo> nodes = nodeGroupService.getAllNodeGroupByPageParam(node, pageNumber, pageSize);
            if (CollectionUtils.isNotEmpty(nodes)) {
                PageInfo<TbClockworkNodeGroupPojo> pageInfo = new PageInfo(nodes);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[NodeGroupController-searchNodeGroupPageList]" +
                                "dateSize = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        nodes.size(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkNodeGroupPojo> pageInfo = new PageInfo(new ArrayList<TbClockworkNodeGroupPojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[NodeGroupController-searchNodeGroupPageList]get taskSize = 0");
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
