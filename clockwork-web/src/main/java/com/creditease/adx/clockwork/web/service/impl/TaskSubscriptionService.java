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

package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskSubscription;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskSubscriptionExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskSubscriptionPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskSubscriptionMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskSubscriptionMapper;
import com.creditease.adx.clockwork.web.service.ITaskSubscriptionService;
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


    /**
     * 添加订阅信息
     *
     * @param pojo pojo
     * @return bool
     */
    @Override
    public boolean addTaskSubscription(TbClockworkTaskSubscriptionPojo pojo) {
        // 校验
        if (pojo.getSubscriptionTime() == null || StringUtils.isBlank(pojo.getMobileNumber())
                || StringUtils.isBlank(pojo.getUserName())) {
            LOG.warn("addTaskSubscription param is null, mobileNumber = {}, subscriptionTime = {}, userName = {}.",
                    pojo.getMobileNumber(), pojo.getSubscriptionTime(), pojo.getUserName());
            return false;
        }
        LOG.info("addTaskSubscription mobileNumber = {}, subscriptionTime = {}, userName = {}.",
                pojo.getMobileNumber(), pojo.getSubscriptionTime(), pojo.getUserName());
        TbClockworkTaskSubscription record = PojoUtil.convert(pojo, TbClockworkTaskSubscription.class);
        record.setCreateTime(new Date());
        return tbClockworkTaskSubscriptionMapper.insertSelective(record) > 0;

    }


    /**
     * 获取订阅信息，通过用户名
     *
     * @param userName userName
     * @return list
     */
    @Override
    public List<TbClockworkTaskSubscriptionPojo> getTaskSubscriptionByUserName(String userName) {
        // 校验
        if (StringUtils.isBlank(userName)) {
            LOG.warn("getTaskSubscriptionByUserName param userName is null");
            return null;
        }

        TbClockworkTaskSubscriptionExample example = new TbClockworkTaskSubscriptionExample();
        example.createCriteria().andUserNameEqualTo(userName);

        List<TbClockworkTaskSubscription> taskSubscriptions = tbClockworkTaskSubscriptionMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(taskSubscriptions)) {
            return PojoUtil.convertList(taskSubscriptions, TbClockworkTaskSubscriptionPojo.class);
        }
        return null;
    }


    /**
     * 获取订阅信息，通过taskId
     *
     * @param taskId taskId
     * @return list
     */
    @Override
    public List<TbClockworkTaskSubscriptionPojo> getTaskSubscriptionByTaskId(Integer taskId) {
        // 校验
        if (taskId == null || taskId <= 0) {
            LOG.warn("getTaskSubscriptionByTaskId param taskId is null");
            return null;
        }

        TbClockworkTaskSubscriptionExample example = new TbClockworkTaskSubscriptionExample();
        example.createCriteria().andTaskIdEqualTo(taskId);

        List<TbClockworkTaskSubscription> taskSubscriptions = tbClockworkTaskSubscriptionMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(taskSubscriptions)) {
            return PojoUtil.convertList(taskSubscriptions, TbClockworkTaskSubscriptionPojo.class);
        }
        return null;
    }


}
