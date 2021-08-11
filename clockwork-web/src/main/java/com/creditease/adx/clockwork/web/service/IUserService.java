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

package com.creditease.adx.clockwork.web.service;


import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUser;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:08 下午 2020/1/13
 * @ Description：IUserService
 * @ Modified By：
 */
public interface IUserService {

    String getMd5Salt();

    /**
     * 添加用户
     */
    int addUser(TbClockworkUserPojo tbClockworkUserPojo);


    /**
     * 修改用户
     */
    int updateUser(TbClockworkUserPojo tbClockworkUserPojo);

    /**
     * 删除用户
     */
    int deleteUser(Integer id);

    /**
     * 校验用户
     *
     * @param username 用户
     * @param password 密码
     * @return
     */
    boolean checkUser(String username, String password);

    /**
     * 获取用户角色
     *
     * @param userName 用户名
     * @return
     */
    String getRoleByUserName(String userName);


    /**
     * 获取用户手机号，根据用户名
     */
    String getMobileNumberUserName(String userName);

    /**
     * 获取用户信息（包括密码）
     *
     * @param userName 用户名
     * @return
     */
    TbClockworkUser getUserByUserName(String userName);

    /**
     * 获取用户信息（包含角色信息，不包含密码）
     *
     * @param userName 用户名
     * @return
     */
    TbClockworkUserPojo getUserAndRoleByUserName(String userName);


    // 分页查询
    int getAllUserByPageParamCount(TbClockworkUserPojo dag);

    List<TbClockworkUserPojo> getAllUserByPageParam(TbClockworkUserPojo dag, int pageNumber, int pageSize);

}
