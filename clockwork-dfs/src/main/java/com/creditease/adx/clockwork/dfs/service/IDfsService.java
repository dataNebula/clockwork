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

package com.creditease.adx.clockwork.dfs.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public interface IDfsService {

    /**
     * create a new file on hdfs
     *
     * @param content
     * @param fileAbsolutePath
     * @param userName
     * @return
     * @throws Exception
     */
    boolean createFile(String content, String fileAbsolutePath, String userName) throws Exception;

    /**
     * upload file
     *
     * @param file             upload file
     * @param fileAbsolutePath file absolute path in hdfs
     * @param userName
     * @return
     */
    boolean uploadFile(MultipartFile file, String fileAbsolutePath, String userName) throws Exception;

    /**
     * edit file
     *
     * @param content
     * @param fileAbsolutePath file absolute path in hdfs
     * @param userName
     * @return
     * @throws Exception
     */
    boolean editFile(String content, String fileAbsolutePath, String userName) throws Exception;

    /**
     * delete file on hdfs
     *
     * @param fileAbsolutePath file absolute path in hdfs
     * @param userName
     * @return 1:success  0:failure
     */
    boolean delFile(String fileAbsolutePath, String userName) throws Exception;

    /**
     * download file
     *
     * @param fileAbsolutePath file absolute path
     * @param userName
     */
    void downloadFile(String fileAbsolutePath, HttpServletResponse response, String userName) throws Exception;

    /**
     * download hdfsfile to path
     *
     * @param fileAbsolutePath
     * @param userName
     */
    byte[] downloadFile2Path(String fileAbsolutePath, String userName) throws Exception;

    /**
     * @param directoryPath directory path
     * @return all file names list in the current directory
     */
    List<String> getDirectoryFileNames(String directoryPath, String userName) throws Exception;

    /**
     * @param directoryPath directory path
     * @param userName
     * @return all file names list and directories names list in the current directory
     */
    Map<String, List<String>> listDirectory(String directoryPath, String userName) throws Exception;

    /**
     * open a file as string
     *
     * @param fileAbsolutePath
     * @param userName
     * @return the content of the current file as a string
     */
    String open(String fileAbsolutePath, String userName) throws Exception;

    /**
     * create directory
     *
     * @param fileAbsolutePath
     * @param userName
     * @return 1:success  0:failure
     */
    boolean mkdirs(String fileAbsolutePath, String userName) throws Exception;
}
