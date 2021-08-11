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
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.web.service.INodeService;
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
 * @ Description：节点服务
 * @ Modified By：
 */
@Api("WEB节点服务相关接口")
@RestController
@RequestMapping("/clockwork/web/node")
public class NodeController {

    private static final Logger LOG = LoggerFactory.getLogger(NodeController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();


    @Autowired
    private INodeService nodeService;

    /**
     * 查询所有node
     *
     * @return
     */
    @GetMapping(value = "/getAllNode")
    public Map<String, Object> getAllNode() {
        try {
            List<TbClockworkNode> tbClockworkNodes = nodeService.getAllNode();
            List<TbClockworkNodePojo> pojoList =
                    PojoUtil.convertList(tbClockworkNodes, TbClockworkNodePojo.class);
            return Response.success(pojoList);
        } catch (Exception e) {
            LOG.error("NodeController-getAllNode Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 根据主键查找node
     *
     * @param nodeId
     * @return
     */
    @GetMapping(value = "/getNodeById")
    public Map<String, Object> getNodeById(@RequestParam(value = "nodeId") Integer nodeId) {
        if (nodeId == null || nodeId < 1) {
            LOG.error("NodeController-getNodeByIp, invalid nodeId");
            return Response.fail("invalid nodeId");
        }

        try {
            TbClockworkNode tbClockworkNode = nodeService.getNodeById(nodeId);
            return Response.success(tbClockworkNode);
        } catch (Exception e) {
            LOG.error("NodeController-getNodeByIp Error {}", e.getMessage(), e);
            return Response.fail(null, e.getMessage());
        }
    }

    /**
     * 添加node
     *
     * @param tbClockworkNodePojo json string
     */
    @PostMapping(value = "/addNode")
    public Map<String, Object> addNode(@RequestBody TbClockworkNodePojo tbClockworkNodePojo) {
        try {
            return Response.success(nodeService.addNode(tbClockworkNodePojo));
        } catch (Exception e) {
            LOG.error("NodeController-addNode Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改node
     *
     * @param tbClockworkNodePojo json string
     */
    @PostMapping(value = "/updateNode")
    public Map<String, Object> updateNode(@RequestBody TbClockworkNodePojo tbClockworkNodePojo) {
        try {
            return Response.success(nodeService.updateNode(tbClockworkNodePojo));
        } catch (Exception e) {
            LOG.error("NodeController-updateNode Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除node
     *
     * @param nodeId node id
     */
    @PostMapping(value = "/deleteNode")
    public Map<String, Object> deleteNode(@RequestParam(value = "nodeId") Integer nodeId) {
        if (nodeId == null) {
            LOG.error("NodeController-deleteNode, invalid nodeId");
            return Response.fail("invalid nodeId");
        }
        try {
            return Response.success(nodeService.deleteNode(nodeId));
        } catch (Exception e) {
            LOG.error("NodeController-deleteNode Error {}.", e.getMessage(), e);
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
	@PostMapping(value = "/searchNodePageList")
    public Map<String, Object> searchNodePageList(@RequestBody PageParam pageParam) {
        LOG.info("[NodeController-searchNodePageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
            TbClockworkNodePojo node = gson.fromJson(pageParam.getCondition(), TbClockworkNodePojo.class);
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
            int total = nodeService.getAllNodeByPageParamCount(node);
            List<TbClockworkNodePojo> nodes = nodeService.getAllNodeByPageParam(node, pageNumber, pageSize);
            if (CollectionUtils.isNotEmpty(nodes)) {
                PageInfo<TbClockworkNodePojo> pageInfo = new PageInfo(nodes);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[NodeController-searchNodePageList]" +
                                "dateSize = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        nodes.size(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkNodePojo> pageInfo = new PageInfo(new ArrayList<TbClockworkNodePojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[NodeController-searchNodePageList]get taskSize = 0");
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
