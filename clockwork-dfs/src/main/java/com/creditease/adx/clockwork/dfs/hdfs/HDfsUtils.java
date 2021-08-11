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

package com.creditease.adx.clockwork.dfs.hdfs;

import com.creditease.adx.clockwork.common.util.RetryUtil;
import com.creditease.adx.clockwork.dfs.service.ICacheService;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:05 下午 2020/1/13
 * @ Description：
 * @ Modified By：
 */
@Service
public class HDfsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HDfsUtils.class);

    public static String haCacheKey = "ActiveHostname" ;

    @Value("${hadoop.fs.defaultFS.port}")
    private String port;

    @Autowired
    private ICacheService cacheService;

    private static List<Class<?>> RETRY_EXCEPTION_CLASSES = new ArrayList<>();
    static {
        RETRY_EXCEPTION_CLASSES.add(URISyntaxException.class);
        RETRY_EXCEPTION_CLASSES.add(IOException.class);
        RETRY_EXCEPTION_CLASSES.add(InterruptedException.class);
        RETRY_EXCEPTION_CLASSES.add(Exception.class);
    }

    /**
     * 获取HDfs URL
     *
     * @return
     */
    private String getHDfsUrl() {
        String URL = "hdfs://" + cacheService.getCacheValue(haCacheKey) + ":" + port;
        LOG.info("hdfs url = {}", URL);
        return URL;
    }

    /**
     * 获取 FileSystem（尝试两次，每次间隔2s, 2次失败后刷新hostname, 抛出异常）
     *
     * @param userName
     * @return FileSystem
     * @throws Exception
     */
    public FileSystem getFileSystem(String userName) throws Exception {
        return RetryUtil.executeWithRetry(new Callable<FileSystem>() {
            @Override
            public FileSystem call() throws Exception {
                try {
                    return FileSystem.get(new URI(getHDfsUrl()), new Configuration(), userName);
                } catch (URISyntaxException | IOException | InterruptedException e) {
                    LOG.error("获取FileSystem Error. 尝试重新刷新hostName", e);
                    cacheService.refresh(haCacheKey);
                    throw e;
                }
            }
        }, 1, 200, false, RETRY_EXCEPTION_CLASSES);
    }

    /**
     * open
     *
     * @param source
     * @param userName
     */
    public InputStream open(String source, String userName) throws Exception {
        // Get FileSystem
        return RetryUtil.executeWithRetry(new Callable<InputStream>() {
            @Override
            public InputStream call() throws Exception {
                try {
                    FileSystem fileSystem = getFileSystem(userName);
                    if (fileSystem == null) {
                        LOG.error("[HDfsUtils]open fileSystem is null");
                        throw new IOException("fileSystem is null");
                    }
                    // 调用open方法获取InputStream
                    LOG.info("[HDfsUtils]open source = {}", source);
                    return fileSystem.open(new Path(source));
                } catch (Exception e) {
                    LOG.error("获取FileSystem Error. 尝试重新刷新hostName", e);
                    cacheService.refresh(haCacheKey);
                    throw e;
                }
            }
        }, 1, 2000, false, RETRY_EXCEPTION_CLASSES);
    }

    /**
     * create
     *
     * @param destination
     * @param inputStream
     * @param userName
     * @throws IOException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public void create(String destination, InputStream inputStream, String userName) throws Exception {
        // Get FileSystem
        FileSystem fileSystem = getFileSystem(userName);
        if (fileSystem == null) {
            LOG.error("[HDfsUtils]rename fileSystem is null");
            return;
        }

        // 调用create方法 得到OutputStream
        OutputStream out = fileSystem.create(new Path(destination));

        // 使用Hadoop提供的IOUtils，将in的内容copy到out，
        // 设置buffSize大小，
        // 是否关闭流设置true
        IOUtils.copyBytes(inputStream, out, 4096, true);
    }

    /**
     * @param source
     * @param destination
     * @param userName
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws InterruptedException
     */
    public boolean rename(String source, String destination, String userName) throws Exception {
        // Get FileSystem
        FileSystem fileSystem = getFileSystem(userName);
        if (fileSystem == null) {
            LOG.error("[HDfsUtils]rename fileSystem is null");
            return false;
        }

        // 调用create方法
        return fileSystem.rename(new Path(source), new Path(destination));

    }

    /**
     * 文件删除
     *
     * @param target
     * @return
     */
    public boolean delete(String target, String userName) throws Exception {
        // Get FileSystem
        FileSystem fileSystem = getFileSystem(userName);
        if (fileSystem == null) {
            LOG.error("[HDfsUtils]delete fileSystem is null");
            return false;
        }
        // 调用delete方法，删除指定的文件。
        // 参数:false:表示是否递归删除
        return fileSystem.delete(new Path(target), false);
    }

    /**
     * 创建文件目录
     *
     * @param directory
     * @return
     */
    public boolean mkdir(String directory, String userName) throws Exception {
        // Get FileSystem
        FileSystem fileSystem = getFileSystem(userName);
        if (fileSystem == null) {
            LOG.error("[HDfsUtils]mkdir fileSystem is null");
            return false;
        }

        // 调用mkdirs方法，在HDFS文件服务器上创建文件夹。
        return fileSystem.mkdirs(new Path(directory));
    }

    /**
     * listStatus
     *
     * @param directory
     * @param userName
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    public FileStatus[] listStatus(String directory, String userName) throws Exception {
        // Get FileSystem
        FileSystem fileSystem = getFileSystem(userName);
        if (fileSystem == null) {
            LOG.error("[HDfsUtils]listStatus fileSystem is null");
            return null;
        }
        return fileSystem.listStatus(new Path(directory));
    }

}
