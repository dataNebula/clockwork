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

package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.INodeGroupService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeGroup;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodeGroupPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:43 下午 2020/8/10
 * @ Description：节点组服务
 * @ Modified By：
 */
@Api("节点组相关接口")
@RestController
@RequestMapping("/clockwork/api/node/group")
public class NodeGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(NodeGroupController.class);

    @Autowired
    private INodeGroupService nodeGroupService;

    /**
     * 查询所有节点机组
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


}
