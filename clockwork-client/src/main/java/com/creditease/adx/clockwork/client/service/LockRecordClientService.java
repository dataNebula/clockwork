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

import com.creditease.adx.clockwork.client.LockRecordClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkLockRecord;
import com.creditease.adx.clockwork.common.pojo.TbClockworkLockRecordPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service(value = "lockRecordClientService")
public class LockRecordClientService {

    private static final Logger LOG = LoggerFactory.getLogger(LockRecordClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected LockRecordClient lockRecordClient;

    public LockRecordClient getLockRecordClient() {
        return lockRecordClient;
    }

    /**
     * 添加锁记录
     *
     * @param type       锁记录类型
     * @param slot       slot值
     * @param slotTime   slot时间
     * @param ip         ip
     * @param port       端口
     * @param isComplete 是否完成
     * @return
     */
    public int addLockRecord(String type, int slot, long slotTime, String ip, int port, boolean isComplete) {
        TbClockworkLockRecordPojo pojo = new TbClockworkLockRecordPojo();
        pojo.setSlot(slot);
        pojo.setSlotTime(slotTime);
        pojo.setIp(ip);
        pojo.setPort(port);
        pojo.setUpdateTime(new Date());
        pojo.setCreateTime(new Date());
        pojo.setIsComplete(isComplete);
        pojo.setType(type);
        Map<String, Object> interfaceResult = lockRecordClient.addLockRecord(pojo);
        if (HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return (Integer) interfaceResult.get(Constant.DATA);
        }
        return -1;
    }

    /**
     * 获取记录通过类型
     *
     * @param type 类型
     * @return list
     */
    public List<TbClockworkLockRecord> getSlotRecentlyBySlotTime(String type, long startSlotTime, long endSlotTime) {
        if (type == null) return null;
        Map<String, Object> interfaceResult = lockRecordClient.getSlotRecentlyBySlotTime(type, startSlotTime, endSlotTime);
        if (HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkLockRecord>>() {
                    });
        }
        return null;
    }

    /**
     * 更新锁记录为完成状态
     *
     * @param id 主键
     */
    public void updateLockRecordIsComplete(Integer id) {
        if (id == null) return ;
        lockRecordClient.updateLockRecordIsComplete(id);
    }


    /**
     * 检查指定服务角色是否可以执行某些操作逻辑
     *
     * @param type 锁记录类型
     * @param ip   ip
     * @param port 端口
     * @return bool
     */
    public boolean getLockAndRecord(String type, String ip, int port) {
        int currentTimeSlot = DateUtil.getCurrentTimeSlot();
        long slotTime = DateUtil.getCurrentTimestamp();
        try {
            // 记录锁记录
            int id = addLockRecord(type, currentTimeSlot, slotTime, ip, port, true);
            return id != -1;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查指定服务角色是否可以执行某些操作逻辑（返回ID）
     *
     * @param type 锁记录类型
     * @param ip   ip
     * @param port 端口
     * @return bool
     */
    public int getLockAndRecordForRoutine(String type, String ip, int port) {
        int currentTimeSlot = DateUtil.getCurrentTimeSlot();
        long slotTime = DateUtil.getCurrentTimestamp();
        try {
            // 记录锁记录
            return addLockRecord(type, currentTimeSlot, slotTime, ip, port, false);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return -1;
    }


}
