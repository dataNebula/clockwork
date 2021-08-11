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

package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkRolePojo;
import com.creditease.adx.clockwork.web.service.IUserRoleService;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:12 2020-09-21
 * @ Description：角色 服务类
 * @ Modified By：
 */
@Api("角色管理")
@RestController
@RequestMapping("/clockwork/web/role")
public class UserRoleController {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private IUserRoleService userRoleService;

    /**
     * 添加Role
     *
     * @param tbClockworkRolePojo json string
     */
    @PostMapping(value = "/addRole")
    public Map<String, Object> addRole(@RequestBody TbClockworkRolePojo tbClockworkRolePojo) {
        try {
            if( tbClockworkRolePojo == null || StringUtils.isBlank(tbClockworkRolePojo.getName())){
                return Response.fail("name不能为空！");
            }
            if(userRoleService.getRoleByRoleName(tbClockworkRolePojo.getName()) != null){
                return Response.fail("角色名已经存在！");
            }

            return Response.success(userRoleService.addRole(tbClockworkRolePojo));
        } catch (Exception e) {
            LOG.error("AddRoleRole Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改Role（基本信息）
     *
     * @param tbClockworkRolePojo json string
     */
    @PostMapping(value = "/updateRole")
    public Map<String, Object> updateRole(@RequestBody TbClockworkRolePojo tbClockworkRolePojo) {
        try {
            return Response.success(userRoleService.updateRole(tbClockworkRolePojo));
        } catch (Exception e) {
            LOG.error("AddRoleRole Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除role
     *
     * @param id role primary key string
     */
    @PostMapping(value = "/deleteRole")
    public Map<String, Object> deleteRole(@RequestParam(value = "id") Integer id) {
        try {
            // 参数校验
            if (id == null || id < 1) {
                LOG.error("RoleController - DeleteRole method, invalid id");
                return Response.fail("RoleController - DeleteRole method, invalid id");
            }
            return Response.success(userRoleService.deleteRole(id));
        } catch (Exception e) {
            LOG.error("deleteRole Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取所有角色
     */
    @GetMapping(value = "/getAllRole")
    public Map<String, Object> getAllRole() {
        try {
            return Response.success(userRoleService.getAllRole());
        } catch (Exception e) {
            LOG.error("getAllRole Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取角色,根据用户id
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/getRoleByUserId")
    public Map<String, Object> getRoleByUserId(@RequestParam(value = "userId") Integer userId) {
        try {
            if( userId == null || userId < 1 ){
                return Response.fail("RoleController - getRoleByUserId, invalid userId");
            }
            return Response.success(userRoleService.getRoleByUserId(userId));
        } catch (Exception e) {
            LOG.error("getAllRole Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 分页查询
     *
     * @param pageParam page
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping(value = "/searchRolePageList")
    public Map<String, Object> searchRolePageList(@RequestBody PageParam pageParam) {
        LOG.info("[RoleController-searchRolePageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
        	TbClockworkRolePojo role = gson.fromJson(pageParam.getCondition(), TbClockworkRolePojo.class);
            int pageNumber = pageParam.getPageNum();
            if (pageNumber < 1) {
                pageNumber = 1;
            }
            int pageSize = pageParam.getPageSize();
            if (pageSize < 10) {
                pageSize = 10;
            }
            if (pageSize > 100) {
                pageSize = 100;
            }

            if (StringUtils.isBlank(pageParam.getUserName())) {
                throw new RuntimeException("userName field is null that value must be user name info.");
            }

            // 查询数据
            int total = userRoleService.getAllRoleByPageParamCount(role);
            List<TbClockworkRolePojo> roles = userRoleService.getAllRoleByPageParam(role, pageNumber, pageSize);
            if (CollectionUtils.isNotEmpty(roles)) {
                PageInfo<TbClockworkRolePojo> pageInfo = new PageInfo(roles);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[RoleController-searchRolePageList]" +
                                "dateSize = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        roles.size(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkRolePojo> pageInfo = new PageInfo(new ArrayList<TbClockworkRolePojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[RoleController-searchRolePageList]get taskSize = 0");
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
