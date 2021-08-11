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

import com.creditease.adx.clockwork.api.service.ILockRecordService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkLockRecord;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkLockRecordExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkLockRecordPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkLockRecordMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:01 2019-09-18
 * @ Description：
 * @ Modified By：
 */
@Service(value = "lockRecordService")
public class LockRecordService implements ILockRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskLogService.class);

    @Autowired
    private TbClockworkLockRecordMapper clockworkLockRecordMapper;

    /**
     * 添加锁记录信息
     *
     * @param lockRecordPojo lockRecord
     * @return
     */
    @Override
    public int addLockRecord(TbClockworkLockRecordPojo lockRecordPojo) {
        TbClockworkLockRecord tbClockworkLockRecord
                = PojoUtil.convert(lockRecordPojo, TbClockworkLockRecord.class);
        clockworkLockRecordMapper.insertSelective(tbClockworkLockRecord);
        LOG.info("[LockRecordService-addLockRecord] finished, id = {}, lockRecordPojo = {}",
                tbClockworkLockRecord.getId(), tbClockworkLockRecord.toString());
        return tbClockworkLockRecord.getId();
    }

    /**
     * 获取环形时钟生命周期信息
     *
     * @param type     类型
     * @param slot     slot值
     * @param slotTime slot时间毫秒
     * @return record
     */
    @Override
    public TbClockworkLockRecord getLockRecordBySlot(String type, int slot, long slotTime) {
        TbClockworkLockRecordExample example = new TbClockworkLockRecordExample();
        example.createCriteria().andTypeEqualTo(type).andSlotEqualTo(slot).andSlotTimeEqualTo(slotTime);

        List<TbClockworkLockRecord> records = clockworkLockRecordMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(records)) {
            LOG.info("[LockRecordService-getLockRecordBySlot]type={}, slot={}, slotTime={}, records.size = {}",
                    type, slot, slotTime, records.size());
            return records.get(0);
        }
        return null;
    }

    /**
     * 更新锁记录为完成状态
     *
     * @param id 主键
     */
    @Override
    public void updateLockRecordIsComplete(Integer id) {
        if (id == null) return;
        TbClockworkLockRecord record = new TbClockworkLockRecord();
        record.setId(id);
        record.setIsComplete(true);
        int count = clockworkLockRecordMapper.updateByPrimaryKeySelective(record);
        LOG.info("[LockRecordService-updateLockRecordIsComplete] id = {}, count = {}", id, count);
    }


    /**
     * 统计生命周期次数通过类型
     *
     * @param type 类型
     * @return
     */
    @Override
    public long countLockRecordByType(String type) {
        TbClockworkLockRecordExample example = new TbClockworkLockRecordExample();
        example.createCriteria().andTypeEqualTo(type);
        long count = clockworkLockRecordMapper.countByExample(example);
        LOG.info("[LockRecordService-countLockRecordByType]type={}, count = {}", type, count);
        return count;
    }

    /**
     * 统计生命周期次数通过类型和时间
     *
     * @param type          类型
     * @param startSlotTime 开始时间
     * @param endSlotTime   结束时间
     * @return long
     */
    @Override
    public long countLoopClockBySlotTime(String type, long startSlotTime, long endSlotTime) {
        TbClockworkLockRecordExample example = new TbClockworkLockRecordExample();
        example.createCriteria().andTypeEqualTo(type).andSlotTimeBetween(startSlotTime, endSlotTime);
        long count = clockworkLockRecordMapper.countByExample(example);
        LOG.info("[LockRecordService-countLoopClockBySlotTime] type={}, startSlotTime={}, endSlotTime={}, count={}",
                type, startSlotTime, endSlotTime, count);
        return count;
    }

    /**
     * 查询一段时间内的记录
     *
     * @param type          类型
     * @param startSlotTime 开始时间
     * @param endSlotTime   结束时间
     * @return
     */
    @Override
    public List<TbClockworkLockRecord> getSlotRecentlyBySlotTime(String type, long startSlotTime, long endSlotTime) {
        TbClockworkLockRecordExample example = new TbClockworkLockRecordExample();
        example.createCriteria().andTypeEqualTo(type).andSlotTimeBetween(startSlotTime, endSlotTime);
        example.setOrderByClause("slot ASC");
        List<TbClockworkLockRecord> records = clockworkLockRecordMapper.selectByExample(example);
        LOG.info("[LockRecordService-getSlotRecentlyBySlotTime] type={}, startSlotTime={}, endSlotTime={}, records.size = {}",
                type, startSlotTime, endSlotTime, records.size());
        return records;
    }


    /**
     * 删除小于该slotTime的记录（定时清表数据）
     *
     * @param slotTime time
     */
    @Override
    public void deleteSlotRecentlyBySlotTime(long slotTime) {
        TbClockworkLockRecordExample example = new TbClockworkLockRecordExample();
        example.createCriteria().andSlotTimeLessThan(slotTime);
        clockworkLockRecordMapper.deleteByExample(example);
    }
}
