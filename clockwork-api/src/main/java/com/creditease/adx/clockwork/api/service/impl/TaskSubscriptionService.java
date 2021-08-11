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

package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.ITaskSubscriptionService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskSubscription;
import com.creditease.adx.clockwork.dao.mapper.TaskSubscriptionMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskSubscriptionMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:40 下午 2020/12/8
 * @ Description：
 * @ Modified By：
 */
@Service(value = "taskSubscriptionService")
public class TaskSubscriptionService implements ITaskSubscriptionService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSubscriptionService.class);

    @Autowired
    private TbClockworkTaskSubscriptionMapper tbClockworkTaskSubscriptionMapper;

    @Autowired
    private TaskSubscriptionMapper taskSubscriptionMapper;


    /**
     * 获取当前时间的订阅信息
     *
     * @param subscriptionTime date
     * @return
     */
    @Override
    public List<TbClockworkTaskSubscription> getTaskSubscriptionBySubscriptionTime(String subscriptionTime) {
        // 校验
        if (StringUtils.isBlank(subscriptionTime)) {
            LOG.warn("getTaskSubscriptionBySubscriptionTime, currentTime = {}, subscriptionTime = {}, size = 0.",
                    new Date(), subscriptionTime);
            return null;
        }

        // 获取当前时间的订阅信息
        List<TbClockworkTaskSubscription> subscriptionList
                = taskSubscriptionMapper.selectTaskSubscriptionBySubscriptionTime(subscriptionTime);
        if(CollectionUtils.isEmpty(subscriptionList)){
            LOG.info("getTaskSubscriptionBySubscriptionTime, currentTime = {}, subscriptionTime = {}, size = 0.",
                    new Date(), subscriptionTime);
            return null;
        }
        LOG.info("getTaskSubscriptionBySubscriptionTime, currentTime = {}, subscriptionTime = {}, size = {}.",
                new Date(), subscriptionTime, subscriptionList.size());
        return subscriptionList;
    }


}
