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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creditease.adx.clockwork.api.service.ITaskUploadFileService;
import com.creditease.adx.clockwork.common.entity.Response;

@Api("上传文件接口")
@RestController
@RequestMapping("/clockwork/api/uploadFile")
public class TaskUploadFileController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskUploadFileController.class);

    @Resource(name = "taskUploadFileService")
    private ITaskUploadFileService taskUploadFileService;

    /**
     * 上传HDFS文件记录
     *
     * @param fileAbsolutePath
     * @return
     */
    @ApiOperation("上传HDFS文件记录")
    @PostMapping(value = "/uploadFileRecord")
    public Map<String, Object> uploadFileRecord(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath) {
        try {
            if (taskUploadFileService.uploadFileRecord(fileAbsolutePath)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }

    }

    /**
     * 编辑HDFS文件记录
     *
     * @param fileAbsolutePath
     * @return
     */
    @ApiOperation("编辑HDFS文件记录")
    @PostMapping(value = "/editFileRecord")
    public Map<String, Object> editFileRecord(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath) {
        try {
            if (taskUploadFileService.editFileRecord(fileAbsolutePath)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除HDFS文件记录
     *
     * @param fileAbsolutePath
     * @return
     */
    @ApiOperation("删除HDFS文件记录")
    @PostMapping(value = "/deleteFileRecord")
    public Map<String, Object> deleteFileRecord(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath) {
        try {
            if (taskUploadFileService.deleteFileRecord(fileAbsolutePath)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }

    }

    @ApiOperation("worker调用更新已经下载同步到本地的文件为同步状态")
    @PostMapping(value = "/updateUploadFileToSyncStatus")
    public Map<String, Object> updateUploadFileToSyncStatus(@RequestParam(value = "relId") Integer relId) {
        try {
            if (taskUploadFileService.updateUploadFileToSyncStatus(relId)) {
                return Response.success(true);
            }
            return Response.fail(false);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("获得当前worker节点没有同步的文件信息列表")
    @GetMapping(value = "/getNoSyncStatusUploadFileByNode")
    public Map<String, Object> getNoSyncStatusUploadFileByNode(
            @RequestParam(value = "nodeIp") String nodeIp, @RequestParam(value = "port") String port) {
        try {
            return Response.success(taskUploadFileService.getNoSyncStatusUploadFileByNode(nodeIp, port));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("检测当前启动worker节点信息以及上产文件同步信息")
    @PostMapping(value = "/addUploadFiles2NodeRels")
    public Map<String, Object> addUploadFiles2NodeRels(
            @RequestParam(value = "nodeIp") String nodeIp, @RequestParam(value = "nodePort") String nodePort) {
        try {
            taskUploadFileService.addUploadFiles2NodeRels(nodeIp, nodePort);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
