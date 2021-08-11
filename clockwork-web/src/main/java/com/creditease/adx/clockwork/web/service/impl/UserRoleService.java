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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkRole;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkRoleExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUserRole;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUserRoleExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkRolePojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.UserRoleMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkRoleMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkUserRoleMapper;
import com.creditease.adx.clockwork.web.service.IUserRoleService;

@Service
public class UserRoleService implements IUserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private TbClockworkRoleMapper tbClockworkRoleMapper;

    @Autowired
    private TbClockworkUserRoleMapper tbClockworkUserRoleMapper;

    /**
     * 添加用户角色
     */
    @Override
    public int addRole(TbClockworkRole tbClockworkRole) {
        if( tbClockworkRole == null){
            return -1;
        }
        tbClockworkRole.setCreateTime(new Date());
        return tbClockworkRoleMapper.insertSelective(tbClockworkRole);
    }

    /**
     * 修改用户角色
     *
     * @param tbClockworkRole
     * @return
     */
    @Override
    public int updateRole(TbClockworkRole tbClockworkRole) {
        if (tbClockworkRole == null
                || tbClockworkRole.getId() == null
                || StringUtils.isBlank(tbClockworkRole.getName())) {
            return -1;
        }
        tbClockworkRole.setUpdateTime(new Date());
        return tbClockworkRoleMapper.updateByPrimaryKey(tbClockworkRole);
    }


    /**
     * 添加用户角色关系
     *
     * @param record
     * @return
     */
    @Override
    public int addUerRoleRelation(TbClockworkUserRole record) {
        return tbClockworkUserRoleMapper.insert(record);
    }

    /**
     * 删除用户角色关系
     *
     * @param userId
     * @return
     */
    @Override
    public int deleteUerRoleRelationByUserId(Integer userId) {
        TbClockworkUserRoleExample example = new TbClockworkUserRoleExample();
        example.createCriteria().andUserIdEqualTo(userId);
        return tbClockworkUserRoleMapper.deleteByExample(example);
    }

    /**
     * 删除用户角色关系
     *
     * @param roleId
     * @return
     */
    @Override
    public int deleteUerRoleRelationByRoleId(Integer roleId) {
        TbClockworkUserRoleExample example = new TbClockworkUserRoleExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        return tbClockworkUserRoleMapper.deleteByExample(example);
    }

    /**
     * 删除用户角色
     *
     * @param roleId roleId
     * @return
     */
    @Override
    public int deleteRole(Integer roleId) {
        if (roleId == null) {
            return -1;
        }
        int count = tbClockworkRoleMapper.deleteByPrimaryKey(roleId);
        if (count > 0) {
            deleteUerRoleRelationByRoleId(roleId);
        }
        return count;
    }

    /**
     * 查询所有角色
     */
    @Override
    public List<TbClockworkRolePojo> getAllRole() {
        List<TbClockworkRole> tbClockworkRoles = tbClockworkRoleMapper.selectByExample(null);
        return PojoUtil.convertList(tbClockworkRoles, TbClockworkRolePojo.class);
    }

    /**
     * 查询角色
     */
    @Override
    public List<TbClockworkRolePojo> getRoleByUserId(Integer userId) {
        if (userId == null || userId < 1) {
            return null;
        }
        return userRoleMapper.selectRoleByUserId(userId);
    }

    /**
     * 查询角色
     */
    @Override
    public TbClockworkRolePojo getRoleByRoleName(String roleName) {
        if (StringUtils.isBlank(roleName)) {
            return null;
        }
        TbClockworkRoleExample example = new TbClockworkRoleExample();
        example.createCriteria().andNameEqualTo(roleName);
        List<TbClockworkRole> tbClockworkRoles = tbClockworkRoleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkRoles)) {
            return PojoUtil.convert(tbClockworkRoles.get(0), TbClockworkRolePojo.class);
        }
        return null;
    }

    /**
     * 分页查询
     *
     * @param role       role
     * @param pageNumber number
     * @param pageSize   size
     * @return
     */
    public List<TbClockworkRolePojo> getAllRoleByPageParam(TbClockworkRolePojo role, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("name", role.getName());
        return userRoleMapper.selectAllRoleByPageParam(param);
    }

    public int getAllRoleByPageParamCount(TbClockworkRolePojo role) {
        return userRoleMapper.countAllRoleByPageParam(role);
    }

}
