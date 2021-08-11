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


import com.creditease.adx.clockwork.common.entity.gen.TbClockworkRole;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUserRole;
import com.creditease.adx.clockwork.common.pojo.TbClockworkRolePojo;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:08 下午 2020/1/13
 * @ Description：IUserRoleService
 * @ Modified By：
 */
public interface IUserRoleService {


    /**
     * 添加用户角色
     */
    int addRole(TbClockworkRole tbClockworkRole);

    /**
     * 修改用户角色
     * @param tbClockworkRole
     * @return
     */
    int updateRole(TbClockworkRole tbClockworkRole);

    /**
     * 添加用户角色关系
     *
     * @param record
     */
    int addUerRoleRelation(TbClockworkUserRole record);

    int deleteUerRoleRelationByUserId(Integer userId);


    int deleteUerRoleRelationByRoleId(Integer roleId);


    /**
     * 删除用户角色
     */
    int deleteRole(Integer id);


    List<TbClockworkRolePojo> getAllRole();

    List<TbClockworkRolePojo> getRoleByUserId(Integer userId);

    TbClockworkRolePojo getRoleByRoleName(String roleName);

    // 分页查询
    int getAllRoleByPageParamCount(TbClockworkRolePojo role);

    List<TbClockworkRolePojo> getAllRoleByPageParam(TbClockworkRolePojo role, int pageNumber, int pageSize);

}
