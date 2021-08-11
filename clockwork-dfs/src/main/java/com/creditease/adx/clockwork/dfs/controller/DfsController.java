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

package com.creditease.adx.clockwork.dfs.controller;

import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.client.service.UploadFileClientService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.exception.FileDownloadException;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.dfs.service.IDfsService;
import com.creditease.adx.clockwork.dfs.service.ISyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Api("分布式文件系统Controller")
@RestController
@RequestMapping(value = "/clockwork/dfs/file")
public class DfsController {

    private static final Logger LOG = LoggerFactory.getLogger(DfsController.class);

    @Qualifier("iDfsService")
    @Autowired
    private IDfsService iDfsService;

    @Autowired
    private ISyncService syncService;

    @Autowired
    private TaskClientService taskClientService;

    @Resource(name = "uploadFileClientService")
    private UploadFileClientService uploadFileClientService;

    @Value("${hadoop.fs.username}")
    private String defaultUserName = null;

    /**
     * 上传文件
     *
     * @param file     上传的文件内容
     * @param location 上传的文件路径
     */
    @ApiOperation("上传脚本文件到HDFS")
    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadFile(@RequestPart("file") MultipartFile file,
                                          @RequestParam("location") String location,
                                          @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            // 参数校验，不能为空
            if (file == null || StringUtils.isBlank(location)) {
                return Response.fail("invalid file or location");
            }
            userName = defaultUserName;

            String fileAbsolutePath;
            if (location.trim().endsWith(File.separator)) {
                fileAbsolutePath = location + file.getOriginalFilename();
            } else {
                fileAbsolutePath = location + File.separator + file.getOriginalFilename();
            }
            String[] uploadPathPrefix = taskClientService.getTaskUploadPathPrefix();
            if (!StringUtil.locationStartsWithListPrefix(fileAbsolutePath, uploadPathPrefix)) {
                return Response.fail("Upload file path must be in [" + Arrays.toString(uploadPathPrefix) + "] as prefix");
            }

            // 上传文件动作记录到数据库
            uploadFileClientService.getUploadFileClient().uploadFileRecord(fileAbsolutePath);
            LOG.info("[RuntimeDirController-uploadFile][1]" +
                    "finished upload file record to database,file path = {},user name = {}", fileAbsolutePath, userName);

            // 上传文件到hdfs
            iDfsService.uploadFile(file, fileAbsolutePath, userName);
            LOG.info("[RuntimeDirController-uploadFile][2]" +
                    "finished upload file to hdfs,file path = {},user name = {}", fileAbsolutePath, userName);

            // 写文件到worker节点本地
            syncService.syncScriptFile(fileAbsolutePath);
            LOG.info("[RuntimeDirController-uploadFile][3]" +
                            "finished sync file to worker node,file path = {},user name = {}",
                    fileAbsolutePath, userName);

            return Response.success(true);
        } catch (Exception e) {
            LOG.error("uploadFile Error. msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 编辑文件
     *
     * @param content          编辑的文件内容
     * @param fileAbsolutePath 编辑的文件路径
     * @param userName
     */
    @ApiOperation("编辑脚本文件到HDFS")
    @PostMapping(value = "/editFile")
    public Map<String, Object> editFile(@RequestBody String content,
                                        @RequestParam("fileAbsolutePath") String fileAbsolutePath,
                                        @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {

            if (StringUtils.isBlank(content) || StringUtils.isBlank(fileAbsolutePath)
                    || StringUtils.isBlank(userName)) {
                return Response.fail("invalid content or fileAbsolutePath or userName");
            }

            fileAbsolutePath = org.apache.commons.lang.StringUtils.endsWith(fileAbsolutePath, File.separator) ?
                    fileAbsolutePath : fileAbsolutePath + File.separator;

            String[] uploadPathPrefix = taskClientService.getTaskUploadPathPrefix();
            if (!StringUtil.locationStartsWithListPrefix(fileAbsolutePath, uploadPathPrefix)) {
                return Response.fail("Edit file path must be in [" + Arrays.toString(uploadPathPrefix) + "] as prefix");
            }
            userName = defaultUserName;

            // 编辑文件动作记录到数据库
            uploadFileClientService.getUploadFileClient().editFileRecord(fileAbsolutePath);
            LOG.info("[RuntimeDirController-editFile][1]" +
                    "finished edit file record to database,file path = {},user name = {}", fileAbsolutePath, userName);

            // 上传文件到hdfs
            iDfsService.editFile(content, fileAbsolutePath, userName);
            LOG.info("[RuntimeDirController-editFile][2]" +
                    "finished edit file to hdfs,file path = {},user name = {}", fileAbsolutePath, userName);

            // 通知worker节点下载文件到worker节点本地
            syncService.syncScriptFile(fileAbsolutePath);
            LOG.info("[RuntimeDirController-editFile][3]" +
                            "finished sync file to worker node,file path = {},user name = {},\ncontent = {}",
                    fileAbsolutePath, userName, content);

            return Response.success(true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * DEL File
     *
     * @param fileAbsolutePath file directory in hdfs
     * @param userName
     */
    @ApiOperation("删除脚本文件到HDFS")
    @PostMapping(value = "/delFile")
    public Map<String, Object> delFile(@RequestParam("fileAbsolutePath") String fileAbsolutePath,
                                       @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            // 参数校验
            if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
                LOG.error("delFile, invalid fileAbsolutePath or userName");
                return Response.fail("invalid fileAbsolutePath or userName");
            }

            fileAbsolutePath = org.apache.commons.lang.StringUtils.endsWith(fileAbsolutePath, File.separator) ?
                    fileAbsolutePath : fileAbsolutePath + File.separator;

            String[] uploadPathPrefix = taskClientService.getTaskUploadPathPrefix();
            if (!StringUtil.locationStartsWithListPrefix(fileAbsolutePath, uploadPathPrefix)) {
                return Response.fail("Delete file path must be in [" + Arrays.toString(uploadPathPrefix) + "] as prefix");
            }

            userName = defaultUserName;

            LOG.info("delFile method, fileAbsolutePath={}, userName = {}", fileAbsolutePath, userName);
            boolean result = iDfsService.delFile(fileAbsolutePath, userName);

            uploadFileClientService.getUploadFileClient().deleteFileRecord(fileAbsolutePath);
            return Response.success(result);
        } catch (Exception e) {
            LOG.error("delFile Error. msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * createFile
     *
     * @param content          createFile
     * @param fileAbsolutePath file directory in hdfs
     * @param userName
     */
    @ApiOperation("创建脚本文件到HDFS")
    @PostMapping(value = "/createFile")
    public Map<String, Object> createFile(@RequestParam("content") String content,
                                          @RequestParam("fileAbsolutePath") String fileAbsolutePath,
                                          @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            // 参数
            if (StringUtils.isBlank(content) || StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
                LOG.error("createFile method :::::invalid content or fileAbsolutePath or userName");
                return Response.fail("invalid content or fileAbsolutePath or userName");
            }

            userName = defaultUserName;

            LOG.info("createFile method :::::content={}, fileAbsolutePath = {}, userName = {}",
                    content, fileAbsolutePath, userName);

            boolean result = iDfsService.createFile(content, fileAbsolutePath, userName);
            return Response.success(result);
        } catch (Exception e) {
            LOG.error("createFile Error. msg:{}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * createDir
     *
     * @param fileAbsolutePath file directory in hdfs
     * @param userName
     */
    @ApiOperation("创建文件目录到HDFS")
    @PostMapping(value = "/createDir")
    public Map<String, Object> createDir(@RequestParam("fileAbsolutePath") String fileAbsolutePath,
                                         @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            // 参数
            if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
                LOG.error("createDir method :::::invalid content or fileAbsolutePath or userName");
                return Response.fail("invalid content or fileAbsolutePath or userName");
            }

            userName = defaultUserName;
            LOG.info("createDir method :::::fileAbsolutePath={},userName={}", fileAbsolutePath, userName);

            // 创建目录
            boolean result = iDfsService.mkdirs(fileAbsolutePath, userName);
            return Response.success(result);
        } catch (Exception e) {
            LOG.error("[DfsController][createDir] Error. msg:{} ", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("打开一个HDFS文件，返回文件内容")
    @PostMapping(value = "/open")
    public Map<String, Object> open(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath,
                                    @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        // 参数检查
        if (StringUtils.isBlank(fileAbsolutePath) || StringUtils.isBlank(userName)) {
            LOG.error("[DfsController][open] invalid fileAbsolutePath or userName");
            return Response.fail("invalid fileAbsolutePath or userName.");
        }

        // open
        userName = defaultUserName;
        LOG.info("[DfsController][open] fileAbsolutePath={}, userName={}",
                fileAbsolutePath, userName);

        try {
            return Response.success(iDfsService.open(fileAbsolutePath, userName));
        } catch (Exception e) {
            LOG.error("[DfsController][open] error. msg:{} ", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation("下载文件")
    @GetMapping(value = "/downloadFile")
    public void downloadFile(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath, HttpServletResponse response,
                             @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) throws FileDownloadException {
        try {
            if (StringUtils.isBlank(fileAbsolutePath)) {
                LOG.error("[DfsController][downloadFile] error. invalid fileAbsolutePath");
                throw new FileDownloadException("[DfsController][downloadFile] error. invalid fileAbsolutePath");
            }

            userName = defaultUserName;
            LOG.info("[DfsController][downloadFile] fileAbsolutePath={}", fileAbsolutePath);

            // 文件内容写入到response ServletOutputStream
            iDfsService.downloadFile(fileAbsolutePath, response, userName);
        } catch (Exception e) {
            LOG.error("[DfsController][downloadFile] error. invalid fileAbsolutePath: {}", e.getMessage(), e);
            throw new FileDownloadException("[DfsController][downloadFile] " + e.getMessage());
        }
    }

    @ApiOperation("从HDFS下载文件到指定的服务器路径")
    @PostMapping(value = "/downloadHdfsFile2Path")
    public Map<String, Object> downloadFile2Path(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath,
                                                 @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            if (StringUtils.isBlank(fileAbsolutePath)) {
                LOG.error("[DfsController][downloadHdfsFile2Path] error. msg: {}", "invalid fileAbsolutePath.");
                return Response.fail("");
            }

            userName = defaultUserName;
            LOG.info("[DfsController][downloadHdfsFile2Path] fileAbsolutePath = {}", fileAbsolutePath);
            return Response.success(iDfsService.downloadFile2Path(fileAbsolutePath, userName));
        } catch (Exception e) {
            LOG.error("[DfsController][downloadHdfsFile2Path] error. msg: {}", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取目录下的所有文件名
     * 如果传入的是文件，则返回该文件名
     * 如果传入的是目录，则返回该目录下的所有文件名
     *
     * @param directoryPath directory path in hdfs
     */
    @ApiOperation("获取目录下的所有文件名")
    @GetMapping(value = "/getDirectoryFileNames")
    public Map<String, Object> getDirectoryFileNames(@RequestParam(value = "directoryPath") String directoryPath,
                                                     @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            if (StringUtils.isBlank(directoryPath)) {
                LOG.error("getDirectoryFileNames, invalid directoryPath");
                return Response.fail("invalid directoryPath");
            }

            LOG.info("getDirectoryFileNames, directoryPath = {}", directoryPath);

            userName = defaultUserName;
            List<String> files = iDfsService.getDirectoryFileNames(directoryPath, userName);
            if (files == null) {
                LOG.error("getDirectoryFileNames, directoryPath has no files");
                return Response.fail("directoryPath has no files");
            }
            return Response.success(files);

        } catch (Exception e) {
            LOG.error("[DfsController-getDirectoryFileNames] error. msg:{} ", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取目录下的所有文件名和目录名
     * 如果传入的是文件，则返回该文件名
     * 如果传入的是目录，则返回该目录下的所有文件名
     *
     * @param directoryPath directory path in hdfs
     * @param userName
     */
    @ApiOperation("获取目录下的所有文件名和目录名")
    @PostMapping(value = "/listDirectory")
    public Map<String, Object> listDirectory(@RequestParam(value = "directoryPath") String directoryPath,
                                             @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName) {
        try {
            // 参数
            if (StringUtils.isBlank(directoryPath) || StringUtils.isBlank(userName)) {
                LOG.error("listDirectory method :::::invalid directoryPath or userName");
                return Response.fail("invalid directoryPath or userName");
            }

            // listDirectory
            userName = defaultUserName;
            LOG.info("listDirectory method :::::directoryPath = {}, userName = {}", directoryPath, userName);
            Map<String, List<String>> files = iDfsService.listDirectory(directoryPath, userName);
            if (files == null) {
                LOG.error("listDirectory method :::::directoryPath has no files");
                return Response.fail("directoryPath has no files");
            }
            return Response.success(files);
        } catch (Exception e) {
            LOG.error("[DfsController][getDirectoryFileNames] error. msg:{} ", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

}
