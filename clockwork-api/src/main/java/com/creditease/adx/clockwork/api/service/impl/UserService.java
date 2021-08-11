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

import com.creditease.adx.clockwork.api.service.IUserService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUser;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUserExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.dao.mapper.UserMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkUserMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value = "userService")
public class UserService implements IUserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private TbClockworkUserMapper tbClockworkUserMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取手机号
     *
     * @param userName userName
     * @return
     */
    @Override
    public String getMobileNumberUserName(String userName) {
        TbClockworkUserExample tbClockworkUserExample = new TbClockworkUserExample();
        tbClockworkUserExample.createCriteria().andUserNameEqualTo(userName);
        List<TbClockworkUser> tbClockworkUsers = tbClockworkUserMapper.selectByExample(tbClockworkUserExample);
        if (CollectionUtils.isNotEmpty(tbClockworkUsers)) {
            return tbClockworkUsers.get(0).getMobileNumber();
        }
        LOG.error("UserService-getMobileNumberUserName user info is null. userName = {}", userName);
        return null;
    }

    @Override
    public TbClockworkUserPojo getUserAndRoleByUserName(String userName) {
      LOG.info("UserService-getUserAndRoleByUserName, userName = {}", userName);
      return StringUtils.isBlank(userName) ? null : userMapper.selectUserAndRoleByUserName(userName);
    }
}
