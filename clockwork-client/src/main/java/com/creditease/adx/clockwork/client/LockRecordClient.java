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

import com.creditease.adx.clockwork.common.pojo.TbClockworkLockRecordPojo;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "${api.service.name}")
public interface LockRecordClient {

    /**
     * 添加锁记录
     *
     * @param lockRecordPojo entity
     */
    @PostMapping(value = "/clockwork/api/lock/record/addLockRecord")
    Map<String, Object> addLockRecord(@RequestBody TbClockworkLockRecordPojo lockRecordPojo);

    /**
     * 获取锁记录
     *
     * @param type     类型
     * @param slot     slot
     * @param slotTime time
     * @return list
     */
    @GetMapping(value = "/clockwork/api/lock/record/getLockRecordBySlot")
    Map<String, Object> getLockRecordBySlot(@RequestParam(value = "type") String type,
                                            @RequestParam(value = "slot") Integer slot,
                                            @RequestParam(value = "slotTime") Long slotTime);

    /**
     * 更新锁记录为完成状态
     *
     * @param id 主键
     */
    @PostMapping(value = "/clockwork/api/lock/record/updateLockRecordIsComplete")
    Map<String, Object> updateLockRecordIsComplete(@RequestParam(value = "id") Integer id);


    /**
     * 查询指定类型的记录值（未使用）
     *
     * @param type 类型
     * @return list
     */
    @Deprecated
    @PostMapping(value = "/clockwork/api/lock/record/countLockRecordByType")
    Map<String, Object> countLockRecordByType(@RequestParam(value = "type") String type);

    /**
     * 统计一段时间内的记录（未使用）
     *
     * @param type          类型
     * @param startSlotTime 开始时间
     * @param endSlotTime   结束时间
     * @return list
     */
    @Deprecated
    @PostMapping(value = "/clockwork/api/lock/record/countLoopClockBySlotTime")
    Map<String, Object> countLoopClockBySlotTime(@RequestParam(value = "type") String type,
                                                 @RequestParam(value = "startSlotTime") Long startSlotTime,
                                                 @RequestParam(value = "endSlotTime") Long endSlotTime);

    /**
     * 查询一段时间内的记录
     *
     * @param type          类型
     * @param startSlotTime 开始时间
     * @param endSlotTime   结束时间
     * @return list
     */
    @GetMapping(value = "/clockwork/api/lock/record/getSlotRecentlyBySlotTime")
    Map<String, Object> getSlotRecentlyBySlotTime(@RequestParam(value = "type") String type,
                                                  @RequestParam(value = "startSlotTime") Long startSlotTime,
                                                  @RequestParam(value = "endSlotTime") Long endSlotTime);

    /**
     * 删除小于该slotTime的记录（定时清表数据）
     *
     * @param slotTime time（毫秒）
     * @return bool
     */
    @PostMapping(value = "/clockwork/api/lock/record/deleteSlotRecentlyBySlotTime")
    Map<String, Object> deleteSlotRecentlyBySlotTime(@RequestParam(value = "slotTime") Long slotTime);

}
