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

package com.creditease.adx.clockwork.api.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.creditease.adx.clockwork.api.service.ITaskUploadFileService;
import com.creditease.adx.clockwork.common.entity.UploadFileAndNodeRel;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNodeExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUploadFile;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUploadFileAndNodeRelation;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUploadFileAndNodeRelationExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkUploadFileExample;
import com.creditease.adx.clockwork.common.enums.NodeStatus;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.common.enums.UploadFileOperateType;
import com.creditease.adx.clockwork.common.enums.UploadFileSlaveSyncStatus;
import com.creditease.adx.clockwork.common.enums.UploadFileStatus;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkNodeMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkUploadFileAndNodeRelationMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkUploadFileMapper;
import com.netflix.discovery.EurekaClient;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-06-27
 */
@Service(value="taskUploadFileService")
public class TaskUploadFileService implements ITaskUploadFileService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskUploadFileService.class);

    @Autowired
    private TbClockworkUploadFileMapper tbClockworkUploadFileMapper;

    @Autowired
    private TbClockworkNodeMapper tbClockworkNodeMapper;

    @Autowired
    private TbClockworkUploadFileAndNodeRelationMapper TbClockworkUploadFileAndNodeRelationMapper;

    @Autowired
    private EurekaClient eurekaClient;

    @Override
    public boolean uploadFileRecord(String fileAbsolutePath) {
        long count = getUploadFileRecordCount(fileAbsolutePath);
        if(count > 1){
            throw new RuntimeException("Found more than 1 upload file of the same name,[ " + fileAbsolutePath + "]");
        }

        // 无记录则新增
        if(count < 1){
            // 上传文件记录
            TbClockworkUploadFile tbClockworkUploadFile = new TbClockworkUploadFile();
            tbClockworkUploadFile.setOperateType(UploadFileOperateType.ADD.getValue());
            tbClockworkUploadFile.setOperatorEmail("NAN");
            tbClockworkUploadFile.setUploadFileAbsolutePath(fileAbsolutePath.trim());
            tbClockworkUploadFile.setStatus(UploadFileStatus.ENABLE.getValue());
            tbClockworkUploadFile.setCreateTime(new Date());
            tbClockworkUploadFileMapper.insertSelective(tbClockworkUploadFile);
            // 绑定当前上传文件和节点同步的关系信息
            addFileAndNodeRels(tbClockworkUploadFile);
            LOG.info("[uploadFileRecord] add upload file record and node relation.fileAbsolutePath = {}",fileAbsolutePath);
            return true;
        }

        // 有一条记录，则修改
        // 需要主键在查出来，后续优化
        TbClockworkUploadFile tbClockworkUploadFile = getTbClockworkUploadFileByFileAbsolutePath(fileAbsolutePath);

        // 更新一下修改时间
        TbClockworkUploadFile updateTbClockworkUploadFile = new TbClockworkUploadFile();
        updateTbClockworkUploadFile.setId(tbClockworkUploadFile.getId());
        updateTbClockworkUploadFile.setUpdateTime(new Date());
        tbClockworkUploadFileMapper.updateByPrimaryKeySelective(updateTbClockworkUploadFile);
        // 删除当前上传文件和节点同步的关系信息
        deleteFileAndNodeRels(tbClockworkUploadFile);
        // 绑定当前上传文件和节点同步的关系信息
        addFileAndNodeRels(tbClockworkUploadFile);
        LOG.info("[uploadFileRecord]update edit file record and node relation.fileAbsolutePath = {}",fileAbsolutePath);
        return true;
    }

    @Override
    public boolean editFileRecord(String fileAbsolutePath) {
        long count = getUploadFileRecordCount(fileAbsolutePath);
        // 编辑的文件数量大于1，失败处理
        if(count > 1){
            throw new RuntimeException("[editFileRecord]Found more than 1 upload file of the same name,[ "
            		+ fileAbsolutePath + "]");
        }

        // 没有发现需要编辑的文件，失败处理
        if(count < 1){
            throw new RuntimeException("[editFileRecord]Not found file of editing ,[ " + fileAbsolutePath + "]");
        }

        // 有一条记录，则修改
        // 需要主键在查出来，后续优化
        TbClockworkUploadFile tbClockworkUploadFile = getTbClockworkUploadFileByFileAbsolutePath(fileAbsolutePath);
        // 更新一下修改时间
        TbClockworkUploadFile updateTbClockworkUploadFile = new TbClockworkUploadFile();
        updateTbClockworkUploadFile.setId(tbClockworkUploadFile.getId());
        updateTbClockworkUploadFile.setUpdateTime(new Date());
        tbClockworkUploadFileMapper.updateByPrimaryKeySelective(updateTbClockworkUploadFile);
        // 删除当前上传文件和节点同步的关系信息
        deleteFileAndNodeRels(tbClockworkUploadFile);
        // 绑定当前上传文件和节点同步的关系信息
        addFileAndNodeRels(tbClockworkUploadFile);
        LOG.info("[editFileRecord]update edit file record and node relation.fileAbsolutePath = {}",fileAbsolutePath);
        return true;
    }

    @Override
    public boolean deleteFileRecord(String fileAbsolutePath) {
        long count = getUploadFileRecordCount(fileAbsolutePath);
        if(count != 1){
            throw new RuntimeException("Need deleted upload file info number not equals 1,[ " + fileAbsolutePath + "]");
        }
        // 需要主键在查出来，后续优化
        TbClockworkUploadFile tbClockworkUploadFile = getTbClockworkUploadFileByFileAbsolutePath(fileAbsolutePath);

        TbClockworkUploadFile updateTbClockworkUploadFile = new TbClockworkUploadFile();
        updateTbClockworkUploadFile.setStatus(UploadFileStatus.DELETED.getValue());
        updateTbClockworkUploadFile.setId(tbClockworkUploadFile.getId());
        updateTbClockworkUploadFile.setOperateType(UploadFileOperateType.DELETED.getValue());
        tbClockworkUploadFileMapper.updateByPrimaryKeySelective(updateTbClockworkUploadFile);
        // 删除当前文件和worker节点同步关系
        deleteFileAndNodeRels(tbClockworkUploadFile);
        return true;
    }

    @Override
    public List <UploadFileAndNodeRel> getNoSyncStatusUploadFileByNode(String nodeIp, String port){
        if(StringUtils.isBlank(nodeIp)){
            throw new RuntimeException("worker node ip is null.");
        }

        if(StringUtils.isBlank(port)){
            throw new RuntimeException("worker node port is null.");
        }

        TbClockworkUploadFileAndNodeRelationExample example = new TbClockworkUploadFileAndNodeRelationExample();

        example.createCriteria().andIpEqualTo(nodeIp).andPortEqualTo(port)
                .andStatusEqualTo(UploadFileSlaveSyncStatus.NO_SYNC.getValue());

        List<TbClockworkUploadFileAndNodeRelation> tbClockworkUploadFileAndNodeRelations
                = TbClockworkUploadFileAndNodeRelationMapper.selectByExample(example);

        if(CollectionUtils.isEmpty(tbClockworkUploadFileAndNodeRelations)){
            LOG.info("[TaskUploadFileServiceImpl][getNoSyncStatusUploadFileByNode]" +
                    "node ip = {},node port = {},no sync file number = {}", nodeIp, port, 0);
            return null;
        }

        List <UploadFileAndNodeRel> result = new ArrayList <>();
        for(TbClockworkUploadFileAndNodeRelation relation:tbClockworkUploadFileAndNodeRelations){
            UploadFileAndNodeRel uploadFileAndNodeRel = new UploadFileAndNodeRel();
            uploadFileAndNodeRel.setRelId(relation.getId());
            uploadFileAndNodeRel.setFileAbsolutePath(relation.getUploadFileAbsolutePath());
            result.add(uploadFileAndNodeRel);
        }

        LOG.info("[TaskUploadFileServiceImpl][getNoSyncStatusUploadFileByNode]" +
                "node ip = {}, node port = {}, no sync file number = {}", nodeIp, port, result.size());

        return result;
    }

    @Override
    public boolean updateUploadFileToSyncStatus(Integer relId){
        if(relId < 1){
            throw new RuntimeException("rel Id is null.");
        }
        TbClockworkUploadFileAndNodeRelation relation = new TbClockworkUploadFileAndNodeRelation();
        relation.setStatus(UploadFileSlaveSyncStatus.SYNC.getValue());
        relation.setId(relId);
        TbClockworkUploadFileAndNodeRelationMapper.updateByPrimaryKeySelective(relation);
        LOG.info("[TaskUploadFileServiceImpl][updateUploadFileToSyncStatus]rel id = {}", relId);
        return  true;
    }

    private void addFileAndNodeRels(TbClockworkUploadFile tbClockworkUploadFile){
        // 绑定文件和worker节点的同步关系，已经同步的节点就设置同步的状态
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria().andRoleEqualTo(NodeType.WORKER.getValue());
        List<TbClockworkNode> tbClockworkNodes = tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
        if(CollectionUtils.isEmpty(tbClockworkNodes)){
            throw new RuntimeException("Don't have worker node information,please check.");
        }

        for(TbClockworkNode tbClockworkNode:tbClockworkNodes){
            TbClockworkUploadFileAndNodeRelation relation = new TbClockworkUploadFileAndNodeRelation();
            relation.setCreateTime(new Date());
            relation.setNodeId(tbClockworkNode.getId());
            relation.setPort(tbClockworkNode.getPort());
            relation.setIp(tbClockworkNode.getIp());
            relation.setUploadFileId(tbClockworkUploadFile.getId());
            relation.setUploadFileAbsolutePath(tbClockworkUploadFile.getUploadFileAbsolutePath());
            // 目前由于每次上传文件都是实时通知worker节点同步文件，替换了以前worker运行任务时再同步，所有此处状态直接修改为sync
            relation.setStatus(UploadFileSlaveSyncStatus.SYNC.getValue());
            TbClockworkUploadFileAndNodeRelationMapper.insertSelective(relation);
        }
    }

    @Override
    public void addUploadFiles2NodeRels(String nodeIp, String nodePort){
        //根据 ip port 查到有效的node信息
        TbClockworkNodeExample tbClockworkNodeExample = new TbClockworkNodeExample();
        tbClockworkNodeExample.createCriteria()
                .andPortEqualTo(nodePort)
                .andIpEqualTo(nodeIp)
                .andStatusEqualTo(NodeStatus.ENABLE.getValue());
        List<TbClockworkNode> tbClockworkNodes = tbClockworkNodeMapper.selectByExample(tbClockworkNodeExample);
        if(tbClockworkNodes == null || tbClockworkNodes.size() != 1){
            throw new RuntimeException("node info not exist,node ip = " + nodeIp + ",node port = " + nodePort);
        }

        LOG.info("[TaskUploadFileService-addUploadFiles2NodeRels]node address = {}:{},node id = {},node id = {}",
                nodeIp, nodePort, tbClockworkNodes.get(0).getId());

        //判断uploadFile-node关系是否存在
        TbClockworkUploadFileAndNodeRelationExample example = new TbClockworkUploadFileAndNodeRelationExample();
        example.createCriteria().andIpEqualTo(nodeIp).andPortEqualTo(nodePort)
                .andNodeIdEqualTo(tbClockworkNodes.get(0).getId());

        List<TbClockworkUploadFileAndNodeRelation>  TbClockworkUploadFileAndNodeRelations
                = TbClockworkUploadFileAndNodeRelationMapper.selectByExample(example);

        LOG.info("[TaskUploadFileService] [addUploadFiles2NodeRels] TbClockworkUploadFileAndNodeRelations count is {}",
        		TbClockworkUploadFileAndNodeRelations == null ? 0: TbClockworkUploadFileAndNodeRelations.size());

        //列出uploadFile所有记录
        TbClockworkUploadFileExample tbClockworkUploadFileExample = new TbClockworkUploadFileExample();
        tbClockworkUploadFileExample.createCriteria().andStatusEqualTo(UploadFileStatus.ENABLE.getValue());
        List<TbClockworkUploadFile> tbClockworkUploadFiles = tbClockworkUploadFileMapper
        		.selectByExample(tbClockworkUploadFileExample);
        if(CollectionUtils.isEmpty(tbClockworkUploadFiles)){
            throw new RuntimeException("Don't have upload file information,please check.");
        }
        LOG.info("[TaskUploadFileService] [addUploadFiles2NodeRels] tbClockworkUploadFiles count is {}"
        		,tbClockworkUploadFiles.size());

        //有记录关系 并且记录条数相同
        if(CollectionUtils.isNotEmpty(TbClockworkUploadFileAndNodeRelations) &&
                tbClockworkUploadFiles.size() == TbClockworkUploadFileAndNodeRelations.size()){
            LOG.info("[TaskUploadFileService] [addUploadFiles2NodeRels] filerecords are already exist");
            return;
        }

        int count = 0;
        //添加uploadFile-node关系记录
        for(TbClockworkUploadFile tbClockworkUploadFile:tbClockworkUploadFiles){
            TbClockworkUploadFileAndNodeRelationExample example2 = new TbClockworkUploadFileAndNodeRelationExample();
            example2.createCriteria()
                    .andIpEqualTo(nodeIp)
                    .andPortEqualTo(nodePort)
                    .andNodeIdEqualTo(tbClockworkNodes.get(0).getId())
                    .andUploadFileIdEqualTo(tbClockworkUploadFile.getId());

            List<TbClockworkUploadFileAndNodeRelation> relist
                    = TbClockworkUploadFileAndNodeRelationMapper.selectByExample(example2);
            if(CollectionUtils.isEmpty(relist)){
                TbClockworkUploadFileAndNodeRelation relation = new TbClockworkUploadFileAndNodeRelation();
                relation.setNodeId(tbClockworkNodes.get(0).getId());
                relation.setPort(nodePort);
                relation.setIp(nodeIp);
                relation.setUploadFileId(tbClockworkUploadFile.getId());
                relation.setUploadFileAbsolutePath(tbClockworkUploadFile.getUploadFileAbsolutePath());
                relation.setStatus(UploadFileSlaveSyncStatus.NO_SYNC.getValue());
                relation.setCreateTime(new Date());
                TbClockworkUploadFileAndNodeRelationMapper.insertSelective(relation);
                count ++;
            }
        }
        LOG.info("[TaskUploadFileService] addUploadFiles2NodeRels  count is {}",count);
    }

    private void deleteFileAndNodeRels(TbClockworkUploadFile tbClockworkUploadFile){
        TbClockworkUploadFileAndNodeRelationExample example = new TbClockworkUploadFileAndNodeRelationExample();
        example.createCriteria().andUploadFileIdEqualTo(tbClockworkUploadFile.getId());
        TbClockworkUploadFileAndNodeRelationMapper.deleteByExample(example);
    }

    private long getUploadFileRecordCount(String fileAbsolutePath){
        if(StringUtils.isBlank(fileAbsolutePath)){
            throw new RuntimeException("file absolute path is null.");
        }

        // 检查同名文件是否存在,已经存在不可以重复添加，必须先删后加
        TbClockworkUploadFileExample tbClockworkUploadFileExample = new TbClockworkUploadFileExample();
        tbClockworkUploadFileExample.createCriteria()
                .andUploadFileAbsolutePathEqualTo(fileAbsolutePath.trim())
                .andStatusNotEqualTo(UploadFileStatus.DELETED.getValue());
        return tbClockworkUploadFileMapper.countByExample(tbClockworkUploadFileExample);
    }

    private TbClockworkUploadFile getTbClockworkUploadFileByFileAbsolutePath(String fileAbsolutePath){
        TbClockworkUploadFileExample example = new TbClockworkUploadFileExample();
        example.createCriteria()
                .andUploadFileAbsolutePathEqualTo(fileAbsolutePath.trim())
                .andStatusNotEqualTo(UploadFileStatus.DELETED.getValue());
        List<TbClockworkUploadFile> tbClockworkUploadFiles = tbClockworkUploadFileMapper.selectByExample(example);
        if(tbClockworkUploadFiles == null || tbClockworkUploadFiles.size() != 1){
            throw new RuntimeException("Upload file record not exist!file absolute path is [" + fileAbsolutePath + "]");
        }
        return tbClockworkUploadFiles.get(0);
    }

    @Override
    public void deleteNodeRelsByNodeId(Integer nodeId) {
        TbClockworkUploadFileAndNodeRelationExample example = new TbClockworkUploadFileAndNodeRelationExample();
        example.createCriteria().andNodeIdEqualTo(nodeId);
        TbClockworkUploadFileAndNodeRelationMapper.deleteByExample(example);
    }

}
