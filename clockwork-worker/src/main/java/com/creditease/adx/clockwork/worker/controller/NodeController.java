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

package com.creditease.adx.clockwork.worker.controller;

import com.creditease.adx.clockwork.common.entity.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 节点
 */
@RestController
@RequestMapping("/clockwork/worker/node")
public class NodeController {

    private static final Logger LOG = LoggerFactory.getLogger(NodeController.class);

    /**
     * 判断当前节点服务是否存活
     */
    @PostMapping(value = "/status/isAlive")
    public Map<String, Object> isAlive() {
        LOG.info("I am living and can execute task.");
        return Response.success(true);
    }

    /**
     * Fetch the node's resource usage
     *
     * @return
     */
    @GetMapping(value = "/status/getNodeResourceUsage")
    public Map<String, Object> getNodeResourceUsage() {
        return Response.success(null);
    }

}
