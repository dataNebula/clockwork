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
import com.creditease.adx.clockwork.common.pojo.TbClockworkFilePojo;
import com.creditease.adx.clockwork.web.service.IFileService;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:12 2020-09-21
 * @ Description：文件 服务类
 * @ Modified By：
 */
@Api("文件管理")
@RestController
@RequestMapping("/clockwork/web/file")
public class FileController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @Autowired
    private IFileService fileService;

    /**
     * 分页查询
     *
     * @param pageParam page
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping(value = "/searchFilePageList")
    public Map<String, Object> searchFilePageList(@RequestBody PageParam pageParam) {
        LOG.info("[FileController-searchFilePageList]pageParam = {}", pageParam.toString());
        try {
            // 参数处理
        	TbClockworkFilePojo file = gson.fromJson(pageParam.getCondition(), TbClockworkFilePojo.class);
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
            int total = fileService.getAllFileByPageParamCount(file);
            List<TbClockworkFilePojo> files = fileService.getAllFileByPageParam(file, pageNumber, pageSize);

            if (CollectionUtils.isNotEmpty(files)) {
                PageInfo<TbClockworkFilePojo> pageInfo = new PageInfo(files);
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                pageInfo.setPages(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
                pageInfo.setTotal(total);
                LOG.info("[FileController-searchFilePageList]" +
                                "dateSize = {}, total = {}, pageSize = {}, page number = {}, pages = {}",
                        files.size(), total, pageSize, pageNumber, pageInfo.getPages());
                return Response.success(pageInfo);
            } else {
                PageInfo<TbClockworkFilePojo> pageInfo = new PageInfo(new ArrayList<TbClockworkFilePojo>());
                pageInfo.setPageNum(pageNumber);
                pageInfo.setPageSize(pageSize);
                LOG.info("[FileController-searchFilePageList]get taskSize = 0");
                return Response.success(pageInfo);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
