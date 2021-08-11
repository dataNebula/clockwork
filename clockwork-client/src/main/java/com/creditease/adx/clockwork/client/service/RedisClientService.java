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

import com.creditease.adx.clockwork.redis.service.IRedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:11 2019-09-16
 * @ Description：
 * @ Modified By：
 */
@Service(value = "redisClientService")
public class RedisClientService {

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Value("${api.service.name}")
    private String apiServiceName = null;

    public long getUUID() {
        Long uuid = redisService.incrementAndGet(apiServiceName + ".UUID");
        int i = 0;
        while (uuid == null || uuid < 0) {
            try {
                Thread.sleep(i++);
                uuid = redisService.incrementAndGet(apiServiceName + ".UUID");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return uuid;
    }


}
