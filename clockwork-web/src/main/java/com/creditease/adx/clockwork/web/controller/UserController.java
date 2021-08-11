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
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.common.util.JWTUtil;
import com.creditease.adx.clockwork.web.config.FilterConfig;
import com.creditease.adx.clockwork.web.service.IUserService;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:12 2019-10-17
 * @ Description：User 服务类
 * @ Modified By：
 */
@Api("用户接口")
@RestController
@RequestMapping("/clockwork/web/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Resource(name = "userService")
    private IUserService userService;

    /**
     * 根据token获取用户信息
     *
     * @return
     */
    @GetMapping(value = "/getUserInfo")
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null || cookies.length == 0) {
                return Response.fail("Get cookies Error");
            }
            // 从cookie中获取token
            String token = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(FilterConfig.COOKIE_ACCESS_TOKEN_KEY)) {
                    token = cookie.getValue();
                    break;
                }
            }
            String username = JWTUtil.getUsername(token);
            if (username != null) {
                return Response.success(userService.getUserAndRoleByUserName(username));
            }
        } catch (Exception e) {
            LOG.error("TokenController-getUserInfo Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
        return Response.fail("GetUserInfo Error");

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
            if (org.apache.commons.lang3.StringUtils.isBlank(userName)) {
                return Response.fail("userName is null.");
            }
            return Response.success(userService.getMobileNumberUserName(userName));
        } catch (Exception e) {
            LOG.error("UserController-getMobileNumberUserName Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取用户角色，根据用户名
     *
     * @param userName 用户名
     * @return String
     */
    @GetMapping(value = "/getRoleByUserName")
    public Map<String, Object> getRoleByUserName(@RequestParam(value = "userName") String userName) {
        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("UserController-getRoleByUserName , invalid userName");
                return Response.fail("UserController-getRoleByUserName , invalid userName");
            }
            return Response.success(userService.getRoleByUserName(userName));
        } catch (Exception e) {
            LOG.error("getRoleByUserName Error!", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取用户信息，根据用户名
     *
     * @param userName 用户名
     * @return
     */
    @GetMapping(value = "/getUserByUserName")
    public Map<String, Object> getUserByUserName(@RequestParam(value = "userName") String userName) {
        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("UserController-getUserByUserName , invalid userName");
                return Response.fail("UserController-getUserByUserName , invalid userName");
            }
            return Response.success(userService.getUserByUserName(userName));
        } catch (Exception e) {
            LOG.error("getUserByUserName Error!", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 添加user
     *
     * @param tbClockworkUserPojo json string
     */
    @PostMapping(value = "/addUser")
    public Map<String, Object> addUser(@RequestBody TbClockworkUserPojo tbClockworkUserPojo) {
        try {
            if( tbClockworkUserPojo == null || StringUtils.isBlank(tbClockworkUserPojo.getUserName())){
                return Response.fail("UserName不能为空！");
            }
            if(userService.getUserByUserName(tbClockworkUserPojo.getUserName()) != null){
                return Response.fail("用户名已经存在！");
            }

            return Response.success(userService.addUser(tbClockworkUserPojo));
        } catch (Exception e) {
            LOG.error("addUser Error!", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 修改user
     *
     * @param tbClockworkUserPojo json string
     */
    @PostMapping(value = "/updateUser")
    public Map<String, Object> updateUser(@RequestBody TbClockworkUserPojo tbClockworkUserPojo) {
        try {
            return Response.success(userService.updateUser(tbClockworkUserPojo));
        } catch (Exception e) {
            LOG.error("updateUser Error!", e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除user
     *
     * @param id userRole primary key string
     */
    @PostMapping(value = "/deleteUser")
    public Map<String, Object> deleteUser(@RequestParam(value = "id") Integer id) {
        try {
            // 参数校验
            if (id == null || id < 1) {
                LOG.error("UserController - deleteUser method, invalid id");
                return Response.fail("UserController - deleteUser method, invalid id");
            }

            return Response.success(userService.deleteUser(id));
        } catch (Exception e) {
            LOG.error("deleteUser Error!", e);
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
	@PostMapping(value = "/searchUserPageList")
    public Map<String, Object> searchUserPageList(@RequestBody PageParam pageParam) {
        LOG.info("[UserController-searchUserPageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
        	TbClockworkUserPojo user = gson.fromJson(pageParam.getCondition(), TbClockworkUserPojo.class);
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
            int total = userService.getAllUserByPageParamCount(user);
            List<TbClockworkUserPojo> users = userService.getAllUserByPageParam(user, pageNumber, pageSize);
            if (CollectionUtils.isNotEmpty(users)) {
                PageInfo<TbClockworkUserPojo> pageInfo = new PageInfo(users);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[UserController-searchUserPageList]" +
                                "dateSize = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        users.size(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkUserPojo> pageInfo = new PageInfo(new ArrayList<TbClockworkUserPojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[UserController-searchUserPageList]get taskSize = 0");
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
