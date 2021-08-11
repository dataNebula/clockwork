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

import com.creditease.adx.clockwork.api.service.IUserService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUser;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkUserMapper;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:12 2019-10-17
 * @ Description：User 服务类
 * @ Modified By：
 */
@Api("用户接口")
@RestController
@RequestMapping("/clockwork/api/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private TbClockworkUserMapper tbClockworkUserMapper;

    @Autowired
    private IUserService userService;

    /**
     * 获取所有用户名
     *
     * @return
     */
    @GetMapping(value = "/getAllUserName")
    public Map<String, Object> getAllUserName() {
        try {
            List<TbClockworkUser> tbClockworkUsers = tbClockworkUserMapper.selectByExample(null);
            if (!CollectionUtils.isEmpty(tbClockworkUsers)) {
                List<String> userNames =
                        tbClockworkUsers.stream().map(TbClockworkUser::getUserName).collect(Collectors.toList());
                return Response.success(userNames);
            }
        } catch (Exception e) {
            LOG.error("UserController-getAllUserName Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
        return Response.success(new ArrayList<String>());

    }

    /**
     * 根据用户名获取手机号
     *
     * @param userName userName
     * @return
     */
    @GetMapping(value = "/getMobileNumberUserName")
    public Map<String, Object> getMobileNumberUserName(@RequestParam(value = "userName") String userName) {
        try {
            if (StringUtils.isBlank(userName)) {
                return Response.fail("userName is null.");
            }
            return Response.success(userService.getMobileNumberUserName(userName));
        } catch (Exception e) {
            LOG.error("UserController-getMobileNumberUserName Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
