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

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUser;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUserExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUserRole;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.UserMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkUserMapper;
import com.creditease.adx.clockwork.web.service.ILdapService;
import com.creditease.adx.clockwork.web.service.IUserRoleService;
import com.creditease.adx.clockwork.web.service.IUserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService implements IUserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private ILdapService ldapService;

    @Autowired
    private TbClockworkUserMapper tbClockworkUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IUserRoleService userRoleService;

    public static final String MD5_SALT = "salt_clockwork";

    @Value("${ldap}")
    private Boolean ldap;

    @Override
    public String getMd5Salt() {
        return MD5_SALT;
    }

    /**
     * 添加用户
     */
    @Override
    public int addUser(TbClockworkUserPojo tbClockworkUser) {
        if (tbClockworkUser == null || StringUtils.isBlank(tbClockworkUser.getUserName())) {
            return -1;
        }
        tbClockworkUser.setCreateTime(new Date());
        if (StringUtils.isNotBlank(tbClockworkUser.getPassword())) {
            tbClockworkUser.setPassword(new Md5Hash(tbClockworkUser.getPassword(), MD5_SALT, 1024).toBase64());
        }
        // 新增
        int count = tbClockworkUserMapper.insertSelective(tbClockworkUser);

        // 新增用户节点关系
        if (count > 0 && StringUtils.isNotBlank(tbClockworkUser.getRoleName())) {
            String[] rolesId = tbClockworkUser.getRoleName().split(",");
            LOG.info("addUser addUerRoleRelation userId = {}, userName = {}, rolesId = {}",
                    tbClockworkUser.getId(), tbClockworkUser.getRoleName(), rolesId);
            for (String roleId : rolesId) {
                TbClockworkUserRole record = new TbClockworkUserRole();
                record.setUserId(tbClockworkUser.getId());
                record.setRoleId(Integer.valueOf(roleId));
                userRoleService.addUerRoleRelation(record);
            }
        }
        return count;
    }

    /**
     * 更新用户
     */
    @Override
    public int updateUser(TbClockworkUserPojo tbClockworkUser) {
        if (tbClockworkUser == null || tbClockworkUser.getId() == null
                || StringUtils.isBlank(tbClockworkUser.getUserName())) {
            return -1;
        }
        tbClockworkUser.setUpdateTime(new Date());

        // 更新
        Integer userId = tbClockworkUser.getId();
        int count = tbClockworkUserMapper.updateByPrimaryKey(tbClockworkUser);

        if (count > 0) {
            // 删除旧用户节点关系
            userRoleService.deleteUerRoleRelationByUserId(userId);

            // 新增用户节点关系
            if (StringUtils.isNotBlank(tbClockworkUser.getRoleName())) {
                String[] rolesId = tbClockworkUser.getRoleName().split(",");
                LOG.info("addUser addUerRoleRelation userId = {}, userName = {}, rolesId = {}",
                        userId, tbClockworkUser.getRoleName(), rolesId);
                for (String roleId : rolesId) {
                    TbClockworkUserRole record = new TbClockworkUserRole();
                    record.setUserId(userId);
                    record.setRoleId(Integer.valueOf(roleId));
                    userRoleService.addUerRoleRelation(record);
                }
            }
        }

        return count;
    }

    /**
     * 删除用户
     */
    @Override
    public int deleteUser(Integer userId) {
        if (userId == null || userId < 1) {
            return -1;
        }
        int count = tbClockworkUserMapper.deleteByPrimaryKey(userId);
        if (count > 0) {
            // 需要删除依赖关系
            userRoleService.deleteUerRoleRelationByUserId(userId);
        }
        return count;
    }


    /**
     * 校验用户账号密码
     *
     * @param username 用户
     * @param password 密码
     * @return
     */
    @Override
    public boolean checkUser(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return false;
        }

        String pwMd5 = new Md5Hash(password, MD5_SALT, 1024).toBase64();
        // LDAP验证用户名和密码， LDAP只用于登陆校验
        if (ldap) {
            if (!ldapService.ldapLogin(username, password)) {
                return false;
            }

            // 去掉邮箱后缀
            int index = username.indexOf('@');
            if (index != -1) username = username.substring(0, index);
            TbClockworkUser record = getUserByUserName(username);
            if (record == null) {
                record = new TbClockworkUser();
                record.setPassword(pwMd5);
                record.setUserName(username);
                addUser(PojoUtil.convert(record, TbClockworkUserPojo.class));
            } else {
                // 这里保存md5密码只为了刷新token时使用
                TbClockworkUserExample example = new TbClockworkUserExample();
                example.createCriteria().andUserNameEqualTo(username);
                record.setPassword(pwMd5);
                tbClockworkUserMapper.updateByExample(record, example);
            }
            return true;
        }
        // 非LDAP, 校验密码 && isActive
        TbClockworkUser record = getUserByUserName(username);
        return record != null && pwMd5.equals(record.getPassword()) && record.getIsActive();
    }

    /**
     * 获取用户角色，根据用户名
     *
     * @param userName 用户名
     * @return
     */
    @Override
    public String getRoleByUserName(String userName) {
        LOG.info("getRoleByEmail, userName = {}", userName);
        TbClockworkUserExample tbClockworkUserExample = new TbClockworkUserExample();
        tbClockworkUserExample.createCriteria().andUserNameEqualTo(userName);
        TbClockworkUser tbClockworkUser = getUserByUserName(userName);
        String role = "";
        if (tbClockworkUser != null) {
//            role = tbClockworkUser.getRoleName();
            // todo
            return "admin";
        }
        return role;
    }

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


    /**
     * 获取用户（包含密码），根据用户名
     *
     * @param userName 用户名
     * @return
     */
    @Override
    public TbClockworkUser getUserByUserName(String userName) {
        LOG.info("UserService-getUserByUserName, userName = {}", userName);
        TbClockworkUserExample tbClockworkUserExample = new TbClockworkUserExample();
        tbClockworkUserExample.createCriteria().andUserNameEqualTo(userName);
        List<TbClockworkUser> tbClockworkUsers = tbClockworkUserMapper.selectByExample(tbClockworkUserExample);
        return CollectionUtils.isNotEmpty(tbClockworkUsers) ? tbClockworkUsers.get(0) : null;
    }

    /**
     * 获取用户信息（包含角色信息，不包含密码）
     *
     * @param userName 用户名
     * @return
     */
    @Override
    public TbClockworkUserPojo getUserAndRoleByUserName(String userName) {
        LOG.info("UserService-getUserAndRoleByUserName, userName = {}", userName);
        return StringUtils.isBlank(userName) ? null : userMapper.selectUserAndRoleByUserName(userName);
    }


    public int getAllUserByPageParamCount(TbClockworkUserPojo user) {
        return userMapper.countAllUserByPageParam(user);
    }

    /**
     * 分页查询
     *
     * @param user       user
     * @param pageNumber number
     * @param pageSize   size
     * @return
     */
    public List<TbClockworkUserPojo> getAllUserByPageParam(TbClockworkUserPojo user, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("userName", user.getUserName());
        param.put("roleName", user.getRoleName());
        return userMapper.selectAllUserByPageParam(param);
    }
}
