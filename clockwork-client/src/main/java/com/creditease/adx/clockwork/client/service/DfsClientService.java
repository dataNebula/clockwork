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

package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.DfsClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:54 2019-09-17
 * @ Description：
 * @ Modified By：
 */
@Service(value = "dfsClientService")
public class DfsClientService {

    private static final Logger LOG = LoggerFactory.getLogger(DfsClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    public DfsClient dfsClient;

    /**
     * 下载hdfs文件 并存储到 节点相应路径
     *
     * @param fileAbsolutePath
     */
    public boolean downloadFile2Path(String fileAbsolutePath) {
        boolean result = false;
        Map<String, Object> resultMap = dfsClient.downloadFile2Path(fileAbsolutePath);
        byte[] ary = OBJECT_MAPPER.convertValue(
                resultMap.get(Constant.DATA), new TypeReference<byte[]>() {
                });

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        File file = new File(fileAbsolutePath);

        try {
            if (!file.exists()) {
                FileUtils.touch(file);
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(ary);

            result = true;
            LOG.info("[RuntimeDirClientService][downloadHdfsFile2Path]download file success, " +
                    "file absolute path = {}", fileAbsolutePath);
        } catch (Exception e) {
            LOG.error("[RuntimeDirClientService][downloadHdfsFile2Path]download file io failed, " +
                    "file absolute path = {}", fileAbsolutePath);
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

    public Map<String, Object> uploadFile(MultipartFile file, String location, String userName) {
        return dfsClient.uploadFile(file, location, userName);
    }

    public Map<String, Object> getDirectoryFileNames(String directoryPath, String userName) {
        return dfsClient.getDirectoryFileNames(directoryPath, userName);
    }

}
