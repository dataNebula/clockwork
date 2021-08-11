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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.common.entity.Response;
import com.robert.vesta.service.intf.IdService;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:11 2019-12-03
 * @ Description：
 * @ Modified By：
 */
@RestController
@RequestMapping(value = "/clockwork/api/id")
public class IdController {

    private static final Logger LOG = LoggerFactory.getLogger(IdController.class);

    @Autowired
    private IdService idService;

    /**
     * 获取唯一值
     *
     * @return
     */
    @GetMapping(value = "/getUuid")
    public Map<String, Object> getId() {
        try {
            return Response.success(idService.genId());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
