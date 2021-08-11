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

package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.IdClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-08-30
 */
@Service(value = "idClientService")
public class IdClientService {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected IdClient idClient;

    public long getUuid() {
        Map<String, Object> interfaceResult = idClient.getUuid();

        if (HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<Long>() {
                    });
        }
        return -1;
    }
}
