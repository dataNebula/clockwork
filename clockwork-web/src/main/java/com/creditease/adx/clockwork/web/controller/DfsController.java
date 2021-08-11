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

import com.creditease.adx.clockwork.client.service.DfsClientService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:12 2019-10-17
 * @ Description：依赖于dfs服务类
 * @ Modified By：
 */
@Api("文件接口")
@RestController
@RequestMapping("/clockwork/web/dfs/file")
public class DfsController {

    private static final Logger LOG = LoggerFactory.getLogger(DfsController.class);

    @Resource(name = "dfsClientService")
    private DfsClientService dfsClientService;


    @ApiOperation("上传脚本文件到HDFS")
    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadFile(@RequestPart("file") MultipartFile file,
                                          @RequestParam("location") String location,
                                          @RequestParam(value = "isSyncFile", required = false) Boolean isSyncFile,
                                          @RequestParam(value = "taskId", required = false) Integer taskId,
                                          @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            // 参数
            LOG.info("getDirectoryFileNames, location = {}, isSyncFile = {}, taskId = {}", location, isSyncFile, taskId);

            if (isSyncFile == null || isSyncFile) {
                // 上传到文件系统
                return Response.success(dfsClientService.uploadFile(file, location, userName));
            } else {
                // 上传到磁盘
                // TODO
                return Response.success(true);
            }
        } catch (Exception e) {
            LOG.error("uploadFile Error {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取目录下的所有文件名
     *
     * @param directoryPath path
     * @param isSyncFile    是否同步（上传到文件系统）
     * @param taskId        taskId
     * @param userName
     * @return
     */
    @ApiOperation("获取目录下的所有文件名")
    @GetMapping(value = "/getDirectoryFileNames")
    public Map<String, Object> getDirectoryFileNames(@RequestParam(value = "directoryPath") String directoryPath,
                                                     @RequestParam(value = "isSyncFile", required = false) Boolean isSyncFile,
                                                     @RequestParam(value = "taskId", required = false) Integer taskId,
                                                     @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            // 参数
            LOG.info("getDirectoryFileNames, directoryPath = {}, isSyncFile = {}, taskId = {}", directoryPath, isSyncFile, taskId);
            if (StringUtils.isBlank(directoryPath)) {
                LOG.error("getDirectoryFileNames parameter directoryPath is null.");
                return Response.fail("invalid directoryPath");
            }

            if (isSyncFile == null || isSyncFile) {
                // 去文件系统取
                return dfsClientService.getDirectoryFileNames(directoryPath, userName);
            } else {
                // TODO
                return Response.success(new ArrayList<>());
            }
        } catch (Exception e) {
            LOG.error("[DfsController-getDirectoryFileNames] Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
