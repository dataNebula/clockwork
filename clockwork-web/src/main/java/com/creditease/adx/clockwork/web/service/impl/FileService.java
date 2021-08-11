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

package com.creditease.adx.clockwork.web.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.common.pojo.TbClockworkFilePojo;
import com.creditease.adx.clockwork.dao.mapper.FileMapper;
import com.creditease.adx.clockwork.web.service.IFileService;

@Service
public class FileService implements IFileService {

    @Autowired
    private FileMapper fileMapper;

    /**
     * 分页查询
     *
     * @param file       file
     * @param pageNumber number
     * @param pageSize   size
     * @return
     */
    public List<TbClockworkFilePojo> getAllFileByPageParam(TbClockworkFilePojo file, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("uploadFileAbsolutePath", file.getUploadFileAbsolutePath());
        return fileMapper.selectAllFileByPageParam(param);
    }

    public int getAllFileByPageParamCount(TbClockworkFilePojo file) {
        return fileMapper.countAllFileByPageParam(file);
    }

}
