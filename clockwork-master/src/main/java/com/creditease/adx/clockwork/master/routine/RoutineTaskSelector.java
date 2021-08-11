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

package com.creditease.adx.clockwork.master.routine;

import com.creditease.adx.clockwork.client.service.LockRecordClientService;
import com.creditease.adx.clockwork.client.service.LoopClockClientService;
import com.creditease.adx.clockwork.client.service.TaskClientService;
import com.creditease.adx.clockwork.client.service.TaskOperationClientService;
import com.creditease.adx.clockwork.common.enums.*;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.master.service.ITaskDelayService;
import com.creditease.adx.clockwork.redis.service.IRedisService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 针对有时间触发配置的任务，对其在指定时间将其调度起来，我们称之为例行作业拣选器。
 *
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-06-22
 */
@Service
public class RoutineTaskSelector extends Thread {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "redisService")
    private IRedisService redisService;

    @Resource(name = "lockRecordClientService")
    private LockRecordClientService lockRecordClientService;

    @Resource(name = "loopClockClientService")
    private LoopClockClientService loopClockClientService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskOperationClientService")
    private TaskOperationClientService taskOperationClientService;

    @Autowired
    private ITaskDelayService taskDelayService;

    @Autowired
    private RoutineTaskSubmit routineTaskSubmit;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${server.port}")
    protected String nodePort;

    @Override
    public void run() {
        // get the current time mills
        long millis = System.currentTimeMillis();
        // calculate the time of next minute
        long nextMinute = ((millis / 60000) + 1) * 60000;
        // loop every minute
        for (; ; ) {
            //this task select server runs once every minute
            long currentTime = System.currentTimeMillis();
            long sleepTime = (nextMinute - currentTime);
            LOG.info("[RoutineTaskSelector-run-safe-sleep]nextMinute = {}, nextMinute date = {}, " +
                            "currentTime = {}, currentTime date = {}, sleepTime = {}"
                    , nextMinute, DateUtil.formatGreenwichTime(nextMinute, DateUtil.DATE_FULL_STR),
                    currentTime, DateUtil.formatGreenwichTime(currentTime, DateUtil.DATE_FULL_STR),
                    sleepTime);
            if (sleepTime > 0) {
                try {
                    LOG.info("[RoutineTaskSelector-run-safe-sleep-begin] sleepTime = {}", sleepTime);
                    safeSleep(sleepTime);
                    LOG.info("[RoutineTaskSelector-run-safe-sleep-end] sleepTime = {}", sleepTime);
                } catch (Exception e) {
                    LOG.info("[RoutineTaskSelector-run-safe-sleep-error], info : {}", e.getMessage());
                    break;
                }
            }

            //the following code is used to check if the task is able to run
            millis = System.currentTimeMillis();

            boolean result = false;
            int launchTaskLockRecord = -1;
            try {
                // 获得当前分钟的槽位
                int currentTimeSlot = DateUtil.getCurrentTimeSlot();    // 0-1439

                // 获取当前master是否有下发任务的权利标识
                launchTaskLockRecord = lockRecordClientService.getLockAndRecordForRoutine(
                        UniqueValueRecordType.MASTER_ROUTINE.getValue(), nodeIp, Integer.parseInt(nodePort));

                LOG.info("[RoutineTaskSelector-run-getLockRecord]master ip = {}, port = {}, currentTimeSlot = {}, "
                        + "launchTaskLockRecord = {}", nodeIp, nodePort, currentTimeSlot, launchTaskLockRecord);

                // 判断当前master是否有任务下发的权利
                if (launchTaskLockRecord < 0) {
                    LOG.info("Not got authority to launch task.");
                    continue;
                }
                LOG.info("Got authority to launch task, currentTimeSlot = {}", currentTimeSlot);

                // 下发处理当前slot任务
                result = processingTheCurrentSlotTask(currentTimeSlot, launchTaskLockRecord);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            } finally {
                // 计算下一分钟的时间
                nextMinute = ((millis / 60000) + 1) * 60000;
                LOG.info("[RoutineTaskSelector-Run]launch task flag = {}, process task result = {}," +
                                "next minute timestamp = {} ms, next minute date = {}",
                        launchTaskLockRecord > 0, result, nextMinute, DateUtil.formatGreenwichTime(nextMinute, DateUtil.DATE_FULL_STR));
            }
        }
    }

    /**
     * It has been reported that the {@link Thread#sleep(long)} method sometimes
     * exits before the requested time has passed. This one offers an
     * alternative that sometimes could sleep a few millis more than requested,
     * but never less.
     *
     * @param millis The length of time to sleep in milliseconds.
     * @throws InterruptedException If another thread has interrupted the current thread. The
     *                              <i>interrupted status</i> of the current thread is cleared
     *                              when this exception is thrown.
     * @see Thread#sleep(long)
     */
    private void safeSleep(long millis) throws InterruptedException {
        long done = 0;
        do {
            long before = System.currentTimeMillis();
            sleep(millis - done);
            long after = System.currentTimeMillis();
            done += (after - before);
        } while (done < millis);
    }

    /**
     * 处理该slot的任务
     *
     * @param currentTimeSlot slot
     * @param launchTaskLockRecord record
     * @return bool
     */
    private boolean processingTheCurrentSlotTask(int currentTimeSlot, int launchTaskLockRecord) {
        boolean getLock = false;
        try {
            long startTime = System.currentTimeMillis();
            getLock = redisService.tryLockForSubmitTask(50, TimeUnit.SECONDS);
            if (getLock) {

                // 获取当前分钟需要下发的任务
                List<TbClockworkTaskPojo> currentMinuteSlotTasks = loopClockClientService.getHasCronTaskFromSlot(currentTimeSlot);

                // skip
                skip(currentMinuteSlotTasks);

                // 恢复父延迟、自延迟、异常延迟的任务，如果存在
                LOG.info("[RoutineTaskSelector-Run-processingTheCurrentSlotTask] " +
                        "recover father、self、exception delay task if exists, current time slot is = {}", currentTimeSlot);
                currentMinuteSlotTasks = recoverFatherDelayTaskIfExists(currentMinuteSlotTasks);
                currentMinuteSlotTasks = recoverSelfDelayTaskIfExists(currentMinuteSlotTasks);
                currentMinuteSlotTasks = recoverExceptionDelayTaskIfExists(currentMinuteSlotTasks);

                // 检查并标记任务是否父延迟、或者自延迟
                LOG.info("[RoutineTaskSelector-Run-processingTheCurrentSlotTask] " +
                        "check tasks is self delay or father delay and mark, current time slot is = {}", currentTimeSlot);
                checkTasksIsSelfDelayOrFatherDelayAndMark(currentMinuteSlotTasks);

                // 如果没有需要下发的任务，则跳过本分钟
                if (CollectionUtils.isNotEmpty(currentMinuteSlotTasks)) {
                    LOG.info("[RoutineTaskSelector-Run-processingTheCurrentSlotTask]There are tasks to submit, " +
                            "current time slot is = {}, task size = {}.", currentTimeSlot, currentMinuteSlotTasks.size());

                    // 提交任务给任务分发器
                    long phaseStartTime = System.currentTimeMillis();
                    boolean result = routineTaskSubmit.submitTask((new ArrayList<>(currentMinuteSlotTasks)));
                    LOG.info("[RoutineTaskSelector-Run-processingTheCurrentSlotTask] " +
                                    "Submit tasks to worker finished, result = {}!" +
                                    "task size = {}, slot is = {}, phase cost time = {} ms, cost time = {} ms.",
                            result, currentMinuteSlotTasks.size(), currentTimeSlot,
                            System.currentTimeMillis() - phaseStartTime, System.currentTimeMillis() - startTime);
                } else {
                    LOG.info("[RoutineTaskSelector-Run-processingTheCurrentSlotTask] There are no tasks to submit," +
                            "current time slot is = {}", currentTimeSlot);
                }
                // 更新锁记录值
                lockRecordClientService.updateLockRecordIsComplete(launchTaskLockRecord);
                LOG.info("[RoutineTaskSelector-Run-processingTheCurrentSlotTask]updateLockRecordIsComplete record = {}.", launchTaskLockRecord);
                return true;
            } else {
                LOG.info("[RoutineTaskSelector-Run-processingTheCurrentSlotTask] " +
                                "Sorry do not get lock, please check it,cost time = {} ms,current time slot is = {}",
                        System.currentTimeMillis() - startTime, currentTimeSlot);
                throw new Exception("Same transaction for task get lock faild,please try again later.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        } finally {
            try {
                if (getLock) redisService.releaseLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }


    /**
     * skip 某些特殊情况
     *
     * @param currentMinuteSlotTasks tasks
     */
    private void skip(List<TbClockworkTaskPojo> currentMinuteSlotTasks) {
        if (CollectionUtils.isEmpty(currentMinuteSlotTasks)) {
            return;
        }

        // 跳过状态不能运行的任务
        Iterator<TbClockworkTaskPojo> iterator = currentMinuteSlotTasks.iterator();
        while (iterator.hasNext()) {
            TbClockworkTaskPojo next = iterator.next();
            // 如果状态是重启调度准备，则跳过
            if (next.getStatus() == null || TaskStatus.RERUN_SCHEDULE_PREP.getValue().equals(next.getStatus())) {
                LOG.info("skip task id = {}, because status = {}.", next.getId(), next.getStatus());

                // 更新当前任务环境时钟
                if (StringUtils.isNotBlank(next.getCronExp())) {
                    loopClockClientService.addTaskToLoopClockSlot(next.getId(), 1);
                }
                iterator.remove();
            }
        }
    }


    /**
     * 检测任务是否自延迟、父延迟
     * 如果任务延迟，标记延迟状态，并记录日志
     *
     * @param taskList 待检测任务列表
     */
    private void checkTasksIsSelfDelayOrFatherDelayAndMark(List<TbClockworkTaskPojo> taskList) {
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }
        Iterator<TbClockworkTaskPojo> iterator = taskList.iterator();
        List<TbClockworkTaskPojo> selfDelayTasks = new ArrayList<>();
        List<TbClockworkTaskPojo> fatherDelayTasks = new ArrayList<>();
        while (iterator.hasNext()) {
            TbClockworkTaskPojo next = iterator.next();
            // 时间触发或者时间&依赖触发的任务，如果状态没有结束则标记为自延迟
            if (TaskTriggerModel.TIME.getValue().intValue() == next.getTriggerMode().intValue()
                    || TaskTriggerModel.TIME_AND_DEPENDENCY.getValue().intValue() == next.getTriggerMode().intValue()) {
                if (TaskStatusUtil.isStartedTaskStatus(next.getStatus())) {
                    selfDelayTasks.add(next);
                    LOG.info("markTasksIsSelfDelay currentMinuteSlotTasks.size = {}, selfDelayTasks.size = {}, task id = {}",
                            taskList.size(), selfDelayTasks.size(), next.getId());
                    iterator.remove();
                    continue;
                } else {
                    LOG.debug("markTasksIsSelfDelay finished, task id = {}, status = {}, task = {}",
                            next.getId(), next.getStatus(), next);
                }
            } else {
                LOG.warn("Error, exception task id = {}, trigger mode = {}", next.getId(), next.getTriggerMode());
            }
            // 时间&依赖触发的任务的任务，如果状态没有结束则父延迟
            if (TaskTriggerModel.TIME_AND_DEPENDENCY.getValue().intValue() == next.getTriggerMode().intValue()) {
                if (!taskOperationClientService.checkParentsSuccess(next)) {
                    fatherDelayTasks.add(next);
                    LOG.info("markTasksIsFatherDelay currentMinuteSlotTasks.size = {}, fatherDelayTasks.size = {}, task id = {}",
                            taskList.size(), fatherDelayTasks.size(), next.getId());
                    iterator.remove();
                }
            }
        }

        // 标记为自延迟和父延迟
        taskDelayService.handleTasksDelayStatusAndRecordDelayLog(selfDelayTasks, TaskDelayStatus.SELF_DELAY.getCode());
        taskDelayService.handleTasksDelayStatusAndRecordDelayLog(fatherDelayTasks, TaskDelayStatus.FATHER_DELAY.getCode());
    }

    /**
     * 恢复自延迟任务，如果存在
     *
     * @param currentMinuteSlotTasks 例行任务列表
     * @return 新的任务列表
     */
    private List<TbClockworkTaskPojo> recoverSelfDelayTaskIfExists(List<TbClockworkTaskPojo> currentMinuteSlotTasks) {
        // 获取自延迟相关任务，如果不存在直接返回
        List<TbClockworkTaskPojo> selfDelayTasks = taskClientService.getTaskByDelayStatus(TaskDelayStatus.SELF_DELAY.getCode());
        if (CollectionUtils.isEmpty(selfDelayTasks)) {
            return currentMinuteSlotTasks;
        }
        LOG.info("recoverSelfDelayTaskIfExists selfDelayTasks.size = {}", selfDelayTasks.size());

        // 任务自己仍在运行，移除，延迟状态暂时无需恢复
        selfDelayTasks.removeIf(taskPojo -> TaskStatusUtil.isStartedTaskStatus(taskPojo.getStatus()));
        if (!selfDelayTasks.isEmpty()) {
            // 如果存在需要恢复的任务，更新任务延迟状态，为自延迟恢复并且记录日志
            taskDelayService.handleTasksDelayStatusAndRecordDelayLog(
                    selfDelayTasks, TaskDelayStatus.SELF_DELAYED_RECOVERY.getCode());

            // 需要恢复的任务加入到任务列表中，等待下发恢复
            // 如果恢复任务已经存在例行任务列表中，需要移除
            if (CollectionUtils.isEmpty(currentMinuteSlotTasks)) {
                LOG.info("recoverSelfDelayTaskIfExists currentMinuteSlotTasks is null, selfDelayTasks.size = {}",
                        selfDelayTasks.size());
                return selfDelayTasks;
            }
            LOG.info("recoverSelfDelayTaskIfExists currentMinuteSlotTasks.size = {}, selfDelayTasks.size = {}",
                    currentMinuteSlotTasks.size(), selfDelayTasks.size());
            currentMinuteSlotTasks.addAll(selfDelayTasks);
            Set<TbClockworkTaskPojo> currentMinuteSlotTasksSet = new HashSet<>(currentMinuteSlotTasks);
            return new ArrayList<>(currentMinuteSlotTasksSet);
        }
        LOG.info("recoverSelfDelayTaskIfExists currentMinuteSlotTasks.size = {}, selfDelayTasks.size is null",
                currentMinuteSlotTasks == null ? 0 : currentMinuteSlotTasks.size());
        return currentMinuteSlotTasks;
    }

    /**
     * 恢复异常延迟任务，如果存在
     *
     * @param currentMinuteSlotTasks 例行任务列表
     * @return 新的任务列表
     */
    private List<TbClockworkTaskPojo> recoverExceptionDelayTaskIfExists(List<TbClockworkTaskPojo> currentMinuteSlotTasks) {
        // 获取异常延迟相关任务，如果不存在直接返回
        List<TbClockworkTaskPojo> exceptionDelayTasks
                = taskClientService.getTaskByDelayStatus(TaskDelayStatus.EXCEPTION_DELAY.getCode());
        if (CollectionUtils.isEmpty(exceptionDelayTasks)) {
            return currentMinuteSlotTasks;
        }
        LOG.info("recoverExceptionDelayTaskIfExists exceptionDelayTasks.size = {}", exceptionDelayTasks.size());

        // 任务自己仍在运行，移除，延迟状态暂时无需恢复
        exceptionDelayTasks.removeIf(taskPojo -> TaskStatusUtil.isStartedTaskStatus(taskPojo.getStatus()));
        if (!exceptionDelayTasks.isEmpty()) {
            // 如果存在需要恢复的任务，更新任务延迟状态，为异常延迟恢复并且记录日志
            taskDelayService.handleTasksDelayStatusAndRecordDelayLog(
                    exceptionDelayTasks, TaskDelayStatus.EXCEPTION_DELAY_RECOVERY.getCode());

            // 需要恢复的任务加入到任务列表中，等待下发恢复
            // 如果恢复任务已经存在例行任务列表中，需要移除
            if (CollectionUtils.isEmpty(currentMinuteSlotTasks)) {
                LOG.info("recoverExceptionDelayTaskIfExists currentMinuteSlotTasks is null, exceptionDelayTasks.size = {}",
                        exceptionDelayTasks.size());
                return exceptionDelayTasks;
            }
            LOG.info("recoverExceptionDelayTaskIfExists currentMinuteSlotTasks.size = {}, exceptionDelayTasks.size = {}",
                    currentMinuteSlotTasks.size(), exceptionDelayTasks.size());
            currentMinuteSlotTasks.addAll(exceptionDelayTasks);
            Set<TbClockworkTaskPojo> currentMinuteSlotTasksSet = new HashSet<>(currentMinuteSlotTasks);
            return new ArrayList<>(currentMinuteSlotTasksSet);
        }
        LOG.info("recoverExceptionDelayTaskIfExists currentMinuteSlotTasks.size = {}, exceptionDelayTasks.size is null",
                currentMinuteSlotTasks == null ? 0 : currentMinuteSlotTasks.size());
        return currentMinuteSlotTasks;
    }

    /**
     * 恢复父延迟任务, 如果存在
     *
     * @param currentMinuteSlotTasks 例行任务列表
     * @return 新的任务列表
     */
    private List<TbClockworkTaskPojo> recoverFatherDelayTaskIfExists(List<TbClockworkTaskPojo> currentMinuteSlotTasks) {
        // 获取父延迟相关任务，如果不存在直接返回
        List<TbClockworkTaskPojo> fatherDelayTasks
                = taskClientService.getTaskByDelayStatus(TaskDelayStatus.FATHER_DELAY.getCode());
        if (CollectionUtils.isEmpty(fatherDelayTasks)) {
            return currentMinuteSlotTasks;
        }
        LOG.info("recoverFatherDelayTaskIfExists fatherDelayTasks.size = {}", fatherDelayTasks.size());

        // 父节点不成功，移除，延迟暂时无需恢复
        fatherDelayTasks.removeIf(taskPojo -> !taskOperationClientService.checkParentsSuccess(taskPojo));
        if (!fatherDelayTasks.isEmpty()) {
            // 如果存在需要恢复的任务，更新任务延迟状态，为父延迟恢复并且记录日志
            taskDelayService.handleTasksDelayStatusAndRecordDelayLog(
                    fatherDelayTasks, TaskDelayStatus.FATHER_DELAYED_RECOVERY.getCode());

            // 需要恢复的任务加入到任务列表中等待下发恢复, 如果恢复任务已经存在例行任务列表中需要移除
            if (CollectionUtils.isEmpty(currentMinuteSlotTasks)) {
                LOG.info("recoverFatherDelayTaskIfExists currentMinuteSlotTasks is null, fatherDelayTasks.size = {}",
                        fatherDelayTasks.size());
                return fatherDelayTasks;
            }
            LOG.info("recoverFatherDelayTaskIfExists currentMinuteSlotTasks.size = {}, fatherDelayTasks.size = {}",
                    currentMinuteSlotTasks.size(), fatherDelayTasks.size());
            currentMinuteSlotTasks.addAll(fatherDelayTasks);
            Set<TbClockworkTaskPojo> currentMinuteSlotTasksSet = new HashSet<>(currentMinuteSlotTasks);
            return new ArrayList<>(currentMinuteSlotTasksSet);
        }
        LOG.info("recoverFatherDelayTaskIfExists currentMinuteSlotTasks.size = {}, fatherDelayTasks.size is null",
                currentMinuteSlotTasks == null ? 0 : currentMinuteSlotTasks.size());
        return currentMinuteSlotTasks;
    }

}
