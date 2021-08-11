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

package com.creditease.adx.clockwork.master.monitor;

import com.creditease.adx.clockwork.client.service.LockRecordClientService;
import com.creditease.adx.clockwork.client.service.LoopClockClientService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkLockRecord;
import com.creditease.adx.clockwork.common.enums.TaskDelayStatus;
import com.creditease.adx.clockwork.common.enums.UniqueValueRecordType;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.master.service.ITaskDelayService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:13 2019-09-11
 * @ Description：Slot Monitor
 * @ Modified By：
 */
@Service
public class SlotMonitor {

    private final Logger LOG = LoggerFactory.getLogger(SlotMonitor.class);

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Resource(name = "loopClockClientService")
    private LoopClockClientService loopClockClientService;

    @Autowired
    private ITaskDelayService taskDelayService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${monitor.slot.cron.exp}")
    protected String springCronExp;

    @Value("${server.port}")
    protected String nodePort;

    /**
     * 监控下发的Slot信息
     */
    @Scheduled(cron = "${monitor.slot.cron.exp}")   // 频率不低于一分钟
    public void slotMonitor() {
        try {
            Date currentDate = new Date();          // 当前时间
            LOG.info("[SlotMonitor.slotMonitor] start...");
            // 获取执行权限
            boolean toLaunchTaskCurrentTime = lockRecordClientService.getLockAndRecord(
                    UniqueValueRecordType.MASTER_SLOT_MONITOR.getValue(), nodeIp, Integer.parseInt(nodePort));

            // 检查是否当前master执行此次下发任务
            if (!toLaunchTaskCurrentTime) {
                LOG.info("[SlotMonitor][Try get authority]Not got authority to launch task, master node ip = {}, "
                        + "master node port = {}, toLaunchTaskCurrentTime = {}", nodeIp, nodePort, false);
                LOG.info("[SlotMonitor.slotMonitor] end.");
                return;
            }
            LOG.info("[SlotMonitor][Try get authority]Got authority to launch task, master node ip = {}, "
                    + "master node port = {}, toLaunchTaskCurrentTime = {}", nodeIp, nodePort, true);

            int slotSize = DateUtil.getCurrentTimeSlot();       // 0-1439
            if (slotSize == 0) {
                return;
            }
            long slotTime = DateUtil.getCurrentTimestamp();

            // 获取当前时间，并且去掉秒（最小单位力度这里规定为1分钟）
            String currentDateStr = DateUtil.formatDate(currentDate, DateUtil.DATE_MIN_STR) + ":00";
            currentDate = DateUtil.parse(currentDateStr);

            if (!CronSequenceGenerator.isValidExpression(springCronExp)) {
                LOG.error("[TaskFailedMonitor-failedTaskNotification]The springCronExp is Valid Expression ");
                return;
            }
            CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(springCronExp);
            Date next = cronSequenceGenerator.next(currentDate);
            Date next2 = cronSequenceGenerator.next(next);
            long timeDifference = next2.getTime() - next.getTime();     // cronExp时间差
            long endSlotTime = slotTime - 60000;                        // 结束时间向前移动一分钟
            long startSlotTime = slotTime - timeDifference;            // 开始时间

            // 获取记录
            int startSlot = DateUtil.getCurrentTimeSlot(startSlotTime);
            int endSlot = DateUtil.getCurrentTimeSlot(endSlotTime);
            int size = endSlot - startSlot + 1;
            if (size == 1) return;
            LOG.info("[slotMonitor]getSlotRecentlyBySlotTime {} - {}, size = {}, currentSlot = {}.", startSlot, endSlot, size, slotSize);
            List<TbClockworkLockRecord> records = lockRecordClientService.
                    getSlotRecentlyBySlotTime(UniqueValueRecordType.MASTER_ROUTINE.getValue(), startSlotTime, endSlotTime);
            int complete = 0;
            if (CollectionUtils.isNotEmpty(records)) {
                for (TbClockworkLockRecord record : records) {
                    if (record.getIsComplete()) {
                        ++complete;
                        LOG.info("[slotMonitor]getSlotRecentlyBySlotTime not need to recovery slot = {} ", record.getSlot());
                    }
                }
                if (complete == size) return;
            }
            LOG.info("[slotMonitor]getSlotRecentlyBySlotTime need to recovery. because need size = {}, but records.size = {}, complete = {} ",
                    size, records == null ? 0 : records.size(), complete);

            Map<Integer, TbClockworkLockRecord> maps =
                    records.stream().collect(Collectors.toMap(TbClockworkLockRecord::getSlot, record -> record, (key1, key2) -> key2));


            // 恢复slot相关任务（标记为异常延迟）
            for (int slot = startSlot; slot <= endSlot; slot++) {
                slotTime = DateUtil.getCurrentTime(slot);
                if (maps.get(slot) != null && maps.get(slot).getIsComplete()) {
                    // 跳过，Skip slot
                    LOG.info("[slotMonitor]Find miss out one slot, skip slot = {}, slotTime = {}", slot, slotTime);
                    continue;
                } else {
                    // 获取当前slot下的任务，并且标记延迟状态为：EXCEPTION_DELAY 异常延迟
                    List<TbClockworkTaskPojo> currentMinuteSlotTasks = loopClockClientService.getHasCronTaskFromSlot(slot);
                    LOG.warn("[slotMonitor]Find miss out one slot, slot = {}, slotTime = {}, slotTasks.size = {}.",
                            slot, slotTime, currentMinuteSlotTasks == null ? 0 : currentMinuteSlotTasks.size());
                    if (CollectionUtils.isNotEmpty(currentMinuteSlotTasks)) {
                        for (TbClockworkTaskPojo currentMinuteSlotTask : currentMinuteSlotTasks) {
                            LOG.info("[slotMonitor]need to recovery taskId = {} ", currentMinuteSlotTask.getId());
                        }
                        taskDelayService.handleTasksDelayStatusAndRecordDelayLog(currentMinuteSlotTasks, TaskDelayStatus.EXCEPTION_DELAY.getCode());
                    }
                }
                // 重新记录slot，入库
                if (maps.get(slot) == null) {
                    lockRecordClientService.addLockRecord(UniqueValueRecordType.MASTER_ROUTINE.getValue(),
                            slot, slotTime, nodeIp, Integer.parseInt(nodePort), true);
                } else {
                    LOG.info("[slotMonitor]updateLockRecordIsComplete id = {}, slot = {}, slotTime = {}", maps.get(slot).getId(), slot, slotTime);
                    lockRecordClientService.updateLockRecordIsComplete(maps.get(slot).getId());
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            LOG.info("[SlotMonitor.slotMonitor] end.");
        }

    }

}
