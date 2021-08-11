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

package com.creditease.adx.clockwork.worker.service;

import com.creditease.adx.clockwork.client.service.DfsClientService;
import com.creditease.adx.clockwork.client.service.UploadFileClientService;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.UploadFileAndNodeRel;
import com.creditease.adx.clockwork.common.exception.FileDownloadException;
import com.creditease.adx.clockwork.common.util.RetryUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-07-01
 */

@Service
public class TaskScriptService {

    protected static final Logger LOG = LoggerFactory.getLogger(TaskScriptService.class);

    @Resource(name = "uploadFileClientService")
    private UploadFileClientService uploadFileClientService;

    @Resource(name = "dfsClientService")
    protected DfsClientService dfsClientService;

    protected static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected static List<Class<?>> RETRY_EXCEPTION_CLASSES = new ArrayList<>();

    static {
        RETRY_EXCEPTION_CLASSES.add(FileDownloadException.class);
        RETRY_EXCEPTION_CLASSES.add(IOException.class);
    }

    /**
     * 下载文件
     *
     * @param fileAbsolutePath
     * @param runtimeDirClientUrl
     * @return
     */
    public boolean downloadFile(String fileAbsolutePath, String runtimeDirClientUrl) {
        boolean result = false;
        String fileAbsolutePathUrl
                = String.format("http://%s/clockwork/dfs/file/downloadFile?fileAbsolutePath=%s",
                runtimeDirClientUrl, fileAbsolutePath);

        LOG.info("[TaskScriptService]fileAbsolutePathUrl = {}", fileAbsolutePathUrl);
        LOG.info("[TaskScriptService]fileAbsolutePath = {}", fileAbsolutePath);

        try {
            File file = new File(fileAbsolutePath);
            // 文件不存在，按失败处理
            if (!file.exists()) {
                LOG.error("[TaskScriptService][downloadFile]file not exists, file absolute path = {}, url = {}",
                        fileAbsolutePath, fileAbsolutePathUrl);
                return result;
            }

            // copy url response content to file
            result = RetryUtil.executeWithRetry(new Callable<Boolean>() {
                @Override
                public Boolean call() throws FileDownloadException, IOException {
                    LOG.info("[TaskScriptService][downloadFile]copyURLToFile, fileAbsolutePath = {}", fileAbsolutePath);
                    FileUtils.copyURLToFile(new URL(fileAbsolutePathUrl), file);
                    return true;
                }
            }, 2, 5000, false, RETRY_EXCEPTION_CLASSES);

            LOG.info("[TaskScriptService][downloadFile]download file success!");
        } catch (Exception e) {
            LOG.error("[TaskScriptService][downloadFile] msg: {}", e.getMessage(), e);
        }
        return result;
    }

    /**
     * worker启动时，下载指定目录下的所有文件
     *
     * @param nodeIp
     * @param nodePort
     * @return
     */
    public boolean downloadAndSyncScriptFile(String nodeIp, String nodePort) {
        try {
            Map<String, Object> interfaceFileNamesResult
                    = uploadFileClientService.getUploadFileClient().getNoSyncStatusUploadFileByNode(nodeIp, nodePort);
            String code = (String) interfaceFileNamesResult.get(Constant.CODE);

            if (!Constant.SUCCESS_CODE.equals(code)) {
                LOG.error("[TaskScriptService][downloadAndSyncScriptFile] get no sync files information failure.");
                return false;
            }

            if (interfaceFileNamesResult.get(Constant.DATA) == null) {
                LOG.info("[TaskScriptService][downloadAndSyncScriptFile][1]Don't have file need to sync.");
                return true;
            }

            List<UploadFileAndNodeRel> uploadFileAndNodeRels  = OBJECT_MAPPER.convertValue(
                    interfaceFileNamesResult.get( Constant.DATA), new TypeReference<List<UploadFileAndNodeRel>>(){});
            if (CollectionUtils.isEmpty(uploadFileAndNodeRels)) {
                LOG.info("[TaskScriptService][downloadAndSyncScriptFile][2]Don't have file need to sync.");
                return true;
            }

            LOG.info("[TaskScriptService][downloadAndSyncScriptFile]Have files need to sync, count is = {}", 
            		uploadFileAndNodeRels.size());

            int success_count = 0;
            int failure_count = 0;
            for (UploadFileAndNodeRel uploadFileAndNodeRel : uploadFileAndNodeRels) {
                try {
                    if (dfsClientService.downloadFile2Path(uploadFileAndNodeRel.getFileAbsolutePath())) {
                        success_count++;
                        // 下载到本地成功,将状态更新为"已同步"
                        uploadFileClientService .getUploadFileClient()
                        .updateUploadFileToSyncStatus(uploadFileAndNodeRel.getRelId());
                        LOG.info("[TaskScriptService][downloadAndSyncScriptFile] " +
                                        "Sync file to local success, file absolute path = {},rel id = {}",
                                uploadFileAndNodeRel.getFileAbsolutePath(), uploadFileAndNodeRel.getRelId());
                    } else {
                        failure_count++;
                        // 下载到本地失败
                        LOG.info("[TaskScriptService][downloadAndSyncScriptFile]" +
                                        "Sync file to local failure, file absolute path = {},rel id = {}",
                                uploadFileAndNodeRel.getFileAbsolutePath(), uploadFileAndNodeRel.getRelId());
                    }
                } catch (Exception e) {
                    LOG.info(e.getMessage(), e);
                }
            }
            LOG.info("[TaskScriptService] downloadHdfsFile2Path success count is {}", success_count);
            LOG.info("[TaskScriptService] downloadHdfsFile2Path failure count is {}", failure_count);
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 写入脚本文件到本地
     *
     * @param fileAbsolutePath 文件路径
     * @param inputFile        文件
     * @return
     */
    public boolean writeScriptFile(String fileAbsolutePath, ByteArrayResource inputFile) {
        if (StringUtils.isBlank(fileAbsolutePath) || inputFile == null) {
            LOG.error("[TaskScriptService-writeScriptFile] fileAbsolutePath or inputFile is null");
            return false;
        }

        boolean result = false;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        File file = new File(fileAbsolutePath);

        try {
            if (!file.exists()) {
                FileUtils.touch(file);
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(inputFile.getByteArray());

            result = true;
            LOG.info("[TaskScriptService-writeScriptFile] write file success, file absolute path = {}", fileAbsolutePath);
        } catch (Exception e) {
            LOG.error("[TaskScriptService-writeScriptFile] write file io failed, file absolute path = {}", fileAbsolutePath);
            LOG.error(e.getMessage(), e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
