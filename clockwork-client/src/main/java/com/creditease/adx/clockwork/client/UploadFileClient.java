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

package com.creditease.adx.clockwork.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:29 2019-12-04
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface UploadFileClient {

    /**
     * worker调用更新已经下载同步到本地的文件为同步状态
     *
     * @param relId
     * @return
     */
    @PostMapping(value = "/clockwork/api/uploadFile/updateUploadFileToSyncStatus")
    Map<String, Object> updateUploadFileToSyncStatus(@RequestParam(value = "relId") Integer relId);

    /**
     * 获得当前worker节点没有同步的文件信息列表
     *
     * @param nodeIp
     * @param port
     * @return
     */
    @GetMapping(value = "/clockwork/api/uploadFile/getNoSyncStatusUploadFileByNode")
    Map<String, Object> getNoSyncStatusUploadFileByNode(
            @RequestParam(value = "nodeIp") String nodeIp, @RequestParam(value = "port") String port);

    /**
     * 同步当前worker节点和下载文件记录关系
     *
     * @param nodeIp
     * @param nodePort
     */
    @PostMapping(value = "/clockwork/api/uploadFile/addUploadFiles2NodeRels")
    Map<String, Object> addUploadFiles2NodeRels(
            @RequestParam(value = "nodeIp") String nodeIp, @RequestParam(value = "nodePort") String nodePort);

    @PostMapping(value = "/clockwork/api/uploadFile/uploadFileRecord")
    Map<String, Object> uploadFileRecord(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath);

    @PostMapping(value = "/clockwork/api/uploadFile/editFileRecord")
    Map<String, Object> editFileRecord(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath);

    @PostMapping(value = "/clockwork/api/uploadFile/deleteFileRecord")
    Map<String, Object> deleteFileRecord(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath);

}
