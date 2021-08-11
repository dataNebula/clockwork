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

package com.creditease.adx.clockwork.dfs.service.impl.hdfs;

import com.creditease.adx.clockwork.dfs.hdfs.HDfsUtils;
import com.creditease.adx.clockwork.dfs.service.IDfsService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:08 下午 2020/1/13
 * @ Description：
 * @ Modified By：
 */
@Service
public class HDfsService implements IDfsService {

    private static final Logger LOG = LoggerFactory.getLogger(HDfsService.class);

    @Autowired
    private HDfsUtils hDfsUtils;

    /**
     * 创建文件
     *
     * @param content
     * @param fileAbsolutePath
     * @param userName
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Override
    public boolean createFile(String content, String fileAbsolutePath, String userName) throws Exception {
        // 参数校验
        if (StringUtils.isBlank(content) || StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
            LOG.error("[HDfsService]createFile Error. param is null, " +
                            "content = {}, fileAbsolutePath = {}, userName = {}",
                    content, fileAbsolutePath, userName);
            return false;
        }
        LOG.info("[HDfsService]createFile. fileAbsolutePath = {}, userName = {}", fileAbsolutePath, userName);

        // 构建inputStream & 写入到文件中
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        hDfsUtils.create(fileAbsolutePath, inputStream, userName);
        return true;
    }

    /**
     * 上传文件
     *
     * @param file             upload file
     * @param fileAbsolutePath file absolute path in hdfs
     * @param userName
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Override
    public boolean uploadFile(MultipartFile file, String fileAbsolutePath, String userName) throws Exception {
        // 参数校验
        if (file == null || StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
            LOG.error("[HDfsService]uploadFile Error. file is null, fileAbsolutePath = {}, userName = {}",
                    fileAbsolutePath, userName);
            return false;
        }
        LOG.info("[HDfsService]uploadFile. fileAbsolutePath = {}, userName = {}", fileAbsolutePath, userName);

        // 上传文件，同样调用HDfsUtil.create
        InputStream inputStream = file.getInputStream();
        hDfsUtils.create(fileAbsolutePath, inputStream, userName);
        return true;
    }

    /**
     * 编辑文件
     *
     * @param content
     * @param fileAbsolutePath file absolute path in hdfs
     * @param userName
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws URISyntaxException
     */
    @Override
    public boolean editFile(String content, String fileAbsolutePath, String userName) throws Exception {
        if (StringUtils.isBlank(content) || StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
            LOG.error("[HDfsService]editFile Error. param is null, " +
                            "content = {}, fileAbsolutePath = {}, userName = {}",
                    content, fileAbsolutePath, userName);
            return false;
        }
        LOG.info("[HDfsService]editFile. content.size = {}, fileAbsolutePath = {}, userName = {}",
                content, fileAbsolutePath, userName);

        // 编辑文件时，保留历史文件
        String hisName = fileAbsolutePath + ".his" + Calendar.getInstance().getTimeInMillis();
        boolean result = hDfsUtils.rename(
                fileAbsolutePath, hisName, userName);

        if (!result) {
            LOG.error("[HDfsService]editFile rename Error. result = {}", false);
            return false;
        }

        // create new file with old name
        // 调用 HDfsUtil.create API
        InputStream is = new ByteArrayInputStream(content.getBytes());
        hDfsUtils.create(fileAbsolutePath, is, userName);
        return true;
    }

    @Override
    public boolean delFile(String fileAbsolutePath, String userName) throws Exception {
        if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
            LOG.error("[HDfsService]delFile Error. param is null, " +
                            "fileAbsolutePath = {}, userName = {}",
                    fileAbsolutePath, userName);
            return false;
        }
        LOG.info("[HDfsService]delFile. fileAbsolutePath = {}, userName = {}",
                fileAbsolutePath, userName);
        return hDfsUtils.delete(fileAbsolutePath, userName);
    }

    @Override
    public void downloadFile(String fileAbsolutePath, HttpServletResponse response, String userName)
            throws Exception {
        InputStream in = null;
        ServletOutputStream out = null;
        try {
            if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
                LOG.error("[HDfsService]delFile Error. param is null, " +
                                "fileAbsolutePath = {}, userName = {}",
                        fileAbsolutePath, userName);
                return;
            }
            LOG.info("[HDfsService]downloadFile. fileAbsolutePath = {}, userName = {}",
                    fileAbsolutePath, userName);

            // OPEN FILE
            in = hDfsUtils.open(fileAbsolutePath, userName);
            if (in != null) {
                LOG.info("[HDfsService]downloadFile. fileAbsolutePath = {}, open success. and out put. ",
                        fileAbsolutePath);
                out = response.getOutputStream();
                FileCopyUtils.copy(in, out);
            }
            LOG.info("[HDfsService]downloadFile. fileAbsolutePath = {}  success. ", fileAbsolutePath);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public byte[] downloadFile2Path(String fileAbsolutePath, String userName) throws Exception {
        InputStream in = null;
        try {
            // 参数校验
            if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
                LOG.error("[HDfsService]delFile Error. param is null, " +
                                "fileAbsolutePath = {}, userName = {}",
                        fileAbsolutePath, userName);
                return null;
            }
            LOG.info("[HDfsService]downloadFile2Path. fileAbsolutePath = {}, userName = {}",
                    fileAbsolutePath, userName);
            // OPEN FILE
            in = hDfsUtils.open(fileAbsolutePath, userName);
            if (in != null) {
                return IOUtils.toByteArray(in);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return null;
    }

    @Override
    public List<String> getDirectoryFileNames(String directoryPath, String userName) throws Exception {
        // 参数校验
        if (StringUtils.isBlank(directoryPath) || StringUtils.isBlank(userName)) {
            LOG.error("[HDfsService]getDirectoryFileNames Error. param is null, " +
                            "directoryPath = {}, userName = {}",
                    directoryPath, userName);
            throw new RuntimeException("getDirectoryFileNames param is null.");
        }
        LOG.info("[HDfsService]getDirectoryFileNames. directoryPath = {}, userName = {}",
                directoryPath, userName);
        List<String> result = new ArrayList<>();
        FileStatus[] fileStatuses = hDfsUtils.listStatus(directoryPath, userName);
        for (FileStatus fileStatus : fileStatuses) {
            String fileName = fileStatus.getPath().getName();
            if (fileStatus.isFile()) {
                String fileAbsolutePath;
                if (directoryPath.endsWith("/")) {
                    fileAbsolutePath = directoryPath + fileName;
                } else {
                    fileAbsolutePath = directoryPath + File.separator + fileName;
                }

                if ("".equals(fileName)) {
                    fileAbsolutePath = directoryPath;
                }
                LOG.info("[HDfsService]getDirectoryFileNames fileName = {}", fileName);
                result.add(fileAbsolutePath);
            }
        }
        return result;
    }

    @Override
    public Map<String, List<String>> listDirectory(String directoryPath, String userName) throws Exception {
        // 参数校验
        if (StringUtils.isBlank(directoryPath) || StringUtils.isBlank(userName)) {
            LOG.error("[HDfsService]listDirectory Error. param is null, " +
                            "directoryPath = {}, userName = {}",
                    directoryPath, userName);
            throw new RuntimeException("listDirectory param is null.");
        }
        LOG.info("[HDfsService]listDirectory. directoryPath = {}, userName = {}",
                directoryPath, userName);

        Map<String, List<String>> result = new HashMap<>();
        List<String> files = new ArrayList<>();
        List<String> directories = new ArrayList<>();

        // listStatus get FileStatus object.
        FileStatus[] fileStatuses = hDfsUtils.listStatus(directoryPath, userName);
        for (FileStatus fileStatus : fileStatuses) {
            String fileName = fileStatus.getPath().getName();
            if (fileStatus.isFile()) {
                String fileAbsolutePath = directoryPath + File.separator + fileName;
                if ("".equals(fileName)) {
                    fileAbsolutePath = directoryPath;
                }
                LOG.info("[HDfsService]listDirectory fileName = {}", fileName);
                files.add(fileAbsolutePath);
            } else if (fileStatus.isDirectory()) {
                String fileAbsolutePath = directoryPath + File.separator + fileName;
                LOG.info("[HDfsService]listDirectory directoryName = {}", fileName);
                directories.add(fileAbsolutePath);
            }
        }
        result.put("files", files);
        result.put("directories", directories);
        return result;
    }

    @Override
    public String open(String fileAbsolutePath, String userName) throws Exception {
        InputStream in = null;
        try {
            // 参数校验
            if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
                LOG.error("[HDfsService]open Error. param is null, " +
                                "fileAbsolutePath = {}, userName = {}",
                        fileAbsolutePath, userName);
                throw new RuntimeException("open param is null.");
            }
            LOG.info("[HDfsService]open. fileAbsolutePath = {}, userName = {}",
                    fileAbsolutePath, userName);
            // open
            in = hDfsUtils.open(fileAbsolutePath, userName);
            if (in != null) {
                return IOUtils.toString(in, "utf-8");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return null;
    }

    @Override
    public boolean mkdirs(String fileAbsolutePath, String userName) throws Exception {
        // 参数校验
        if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
            LOG.error("[HDfsService]mkdirs Error. param is null, " +
                            "fileAbsolutePath = {}, userName = {}",
                    fileAbsolutePath, userName);
            throw new RuntimeException("mkdirs param is null.");
        }
        LOG.info("[HDfsService]mkdirs. fileAbsolutePath = {}, userName = {}",
                fileAbsolutePath, userName);
        return hDfsUtils.mkdir(fileAbsolutePath, userName);
    }

}
