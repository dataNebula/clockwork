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

package com.creditease.adx.clockwork.api.controller;

import com.creditease.adx.clockwork.api.service.ILockRecordService;
import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkLockRecordPojo;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Api("锁记录")
@RestController
@RequestMapping("/clockwork/api/lock/record")
public class LockRecordController {

    private static final Logger LOG = LoggerFactory.getLogger(LockRecordController.class);

    @Resource(name = "lockRecordService")
    private ILockRecordService lockRecordService;

    /**
     * 添加锁记录
     *
     * @param lockRecordPojo entity
     */
    @PostMapping(value = "/addLockRecord")
    public Map<String, Object> addLockRecord(@RequestBody TbClockworkLockRecordPojo lockRecordPojo) {
        try {
            LOG.info("LockRecordController-addLockRecord success,type = {}, slotTime = {}",
                    lockRecordPojo.getType(), lockRecordPojo.getSlotTime());
            return Response.success(lockRecordService.addLockRecord(lockRecordPojo));
        } catch (Exception e) {
            if (e instanceof DuplicateKeyException) {
                LOG.info("LockRecordController-addLockRecord duplicate key, type = {}, slotTime = {}",
                        lockRecordPojo.getType(), lockRecordPojo.getSlotTime());
            } else {
                LOG.error("LockRecordController-addLockRecord Error {}", e.getMessage());
            }
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 获取锁记录
     *
     * @param type     类型
     * @param slot     slot
     * @param slotTime time
     * @return list
     */
    @GetMapping(value = "/getLockRecordBySlot")
    public Map<String, Object> getLockRecordBySlot(@RequestParam(value = "type") String type,
                                                   @RequestParam(value = "slot") Integer slot,
                                                   @RequestParam(value = "slotTime") Long slotTime) {
        try {
            if (type == null || slot == null || slotTime == null) {
                return Response.fail("Parameter invalid！");
            }
            return Response.success(lockRecordService.getLockRecordBySlot(type, slot, slotTime));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 更新锁记录为完成状态
     *
     * @param id 主键
     */
    @PostMapping(value = "/updateLockRecordIsComplete")
    public Map<String, Object> updateLockRecordIsComplete(@RequestParam(value = "id") Integer id) {
        try {
            if (id == null) {
                return Response.fail("Parameter id invalid！");
            }
            lockRecordService.updateLockRecordIsComplete(id);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询指定类型的记录值
     *
     * @param type 类型
     * @return list
     */
    @PostMapping(value = "/countLockRecordByType")
    public Map<String, Object> countLockRecordByType(@RequestParam(value = "type") String type) {
        try {
            if (type == null) {
                return Response.fail("Parameter invalid！");
            }
            return Response.success(lockRecordService.countLockRecordByType(type));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 统计一段时间内的记录
     *
     * @param type          类型
     * @param startSlotTime 开始时间
     * @param endSlotTime   结束时间
     * @return list
     */
    @PostMapping(value = "/countLoopClockBySlotTime")
    public Map<String, Object> countLoopClockBySlotTime(@RequestParam(value = "type") String type,
                                                        @RequestParam(value = "startSlotTime") Long startSlotTime,
                                                        @RequestParam(value = "endSlotTime") Long endSlotTime) {
        try {
            if (type == null || startSlotTime == null || endSlotTime == null) {
                return Response.fail("Parameter invalid！");
            }
            return Response.success(countLoopClockBySlotTime(type, startSlotTime, endSlotTime));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 查询一段时间内的记录
     *
     * @param type          类型
     * @param startSlotTime 开始时间
     * @param endSlotTime   结束时间
     * @return list
     */
    @GetMapping(value = "/getSlotRecentlyBySlotTime")
    public Map<String, Object> getSlotRecentlyBySlotTime(@RequestParam(value = "type") String type,
                                                         @RequestParam(value = "startSlotTime") Long startSlotTime,
                                                         @RequestParam(value = "endSlotTime") Long endSlotTime) {
        try {
            if (type == null || startSlotTime == null || endSlotTime == null) {
                return Response.fail("Parameter invalid！");
            }
            return Response.success(lockRecordService.getSlotRecentlyBySlotTime(type, startSlotTime, endSlotTime));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 删除小于该slotTime的记录（定时清表数据）
     *
     * @param slotTime time（毫秒）
     * @return bool
     */
    @PostMapping(value = "/deleteSlotRecentlyBySlotTime")
    public Map<String, Object> deleteSlotRecentlyBySlotTime(@RequestParam(value = "slotTime") Long slotTime) {
        try {
            if (slotTime == null) {
                return Response.fail("Parameter slotTime invalid！");
            }
            lockRecordService.deleteSlotRecentlyBySlotTime(slotTime);
            return Response.success(true);
        } catch (Exception e) {
            LOG.error("deleteSlotRecentlyBySlotTime Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
