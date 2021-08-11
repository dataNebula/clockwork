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

import com.creditease.adx.clockwork.client.config.FeignMultipartSupportConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;


@FeignClient(value = "${dfs.service.name}", configuration = FeignMultipartSupportConfig.class)
public interface DfsClient {

    /**
     * 下载目录下的文件
     *
     * @param fileAbsolutePath directory absolute path in hdfs
     */
    @PostMapping("/clockwork/dfs/file/downloadHdfsFile2Path")
    Map<String, Object> downloadFile2Path(@RequestParam(value = "fileAbsolutePath") String fileAbsolutePath);

    @PostMapping(value = "/clockwork/dfs/file/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("location") String location,
            @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName);

    @GetMapping("/clockwork/dfs/file/getDirectoryFileNames")
    Map<String, Object> getDirectoryFileNames(
            @RequestParam(value = "directoryPath") String directoryPath,
            @RequestParam(value = "userName", required = false, defaultValue = "hdfs") String userName);

}
