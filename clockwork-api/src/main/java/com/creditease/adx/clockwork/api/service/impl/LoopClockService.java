package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.ILoopClockService;
import com.creditease.adx.clockwork.common.entity.gen.*;
import com.creditease.adx.clockwork.common.enums.TaskTakeEffectStatus;
import com.creditease.adx.clockwork.common.enums.TimeToMills;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.CronExpression;
import com.creditease.adx.clockwork.common.util.DataUtil;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskBatchMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskAndSlotRelationMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskLoopClockSlotMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:29 下午 2020/8/4
 * @ Description：
 * @ Modified By：
 */
@Service(value = "loopClockService")
public class LoopClockService implements ILoopClockService {

    private static final Logger LOG = LoggerFactory.getLogger(LoopClockService.class);

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Autowired
    private TaskBatchMapper taskBatchMapper;

    @Autowired
    private TbClockworkTaskLoopClockSlotMapper tbClockworkTaskLoopClockSlotMapper;

    @Autowired
    private TbClockworkTaskAndSlotRelationMapper tbClockworkTaskAndLoopClockSlotRelMapper;

    // 环形时钟槽位元数据缓存
    private static Cache<Integer, Integer> LOOP_CLOCK_META_CACHE = null;

    @Value("${task.batch.add.loopclock.slot.num}")
    private int taskBatchAddLoopClockSlotNum;

    static {
        try {
            // 初始化缓存，缓存项再30分钟之内没有被读写将被回收
            LOOP_CLOCK_META_CACHE = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 构建作业环形时钟信息（所有）
     * <p>
     * 1。获取所有ONLINE并且的CronExp不为空的任务
     * 2。清空作业和槽位的关系表
     * 3。依次从新构建所有任务的环形时钟
     *
     * @return boolean
     */
    @Override
    public boolean buildTaskLoopClock() {
        long startTime = System.currentTimeMillis();

        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andCronExpIsNotNull().andOnlineEqualTo(TaskTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTask> clockworkTasks = tbClockworkTaskMapper.selectByExample(example);

        // 没有满足条件的任务算失败
        if (CollectionUtils.isEmpty(clockworkTasks)) {
            LOG.info("[LoopClockService-buildTaskLoopClock]no task need to build.");
            return true;
        }
        // 清空作业和槽位的关系表
        tbClockworkTaskAndLoopClockSlotRelMapper.deleteByExample(null);

        try {
            CountDownLatch latch;
            if (clockworkTasks.size() > taskBatchAddLoopClockSlotNum) {
                List<List<TbClockworkTask>> fixedGroups = DataUtil.fixedGrouping(clockworkTasks, taskBatchAddLoopClockSlotNum);
                latch = new CountDownLatch(fixedGroups.size());
                for (List<TbClockworkTask> taskList : fixedGroups) {
                    new LoopClockSlotThread(taskList, latch).start();
                }
            } else {
                latch = new CountDownLatch(1);
                new LoopClockSlotThread(clockworkTasks, latch).start();
            }
            //等待所有线程完成
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("[LoopClockService-buildTaskLoopClock] build task loop clock failed!", e);
            return false;
        }
        long endTime = System.currentTimeMillis();
        LOG.info("[LoopClockService-buildTaskLoopClock] build task loop clock finished! cost time = {} ms.", endTime - startTime);
        return true;
    }

    private class LoopClockSlotThread extends Thread {
        private List<TbClockworkTask> clockworkTasks;
        private CountDownLatch latch;

        public LoopClockSlotThread(List<TbClockworkTask> tbClockworkTasks, CountDownLatch latch) {
            this.clockworkTasks = tbClockworkTasks;
            this.latch = latch;
        }

        @Override
        public void run() {
            for (TbClockworkTask clockworkTask : clockworkTasks) {
                if (StringUtils.isEmpty(clockworkTask.getCronExp())) {
                    LOG.info("[LoopClockService-buildTaskLoopClock]The length of cron exp field is 0."
                            + "please check it and skip the task, task id = {}", clockworkTask.getId());
                    continue;
                }
                if (checkHasCronExpTaskExecutionPeriodIsSmaller(clockworkTask, TimeToMills.MILLS_OF_SECOND.getValue())) {
                    addTaskToLoopClockSlot(clockworkTask, 3);
                } else if (checkHasCronExpTaskExecutionPeriodIsSmaller(clockworkTask, TimeToMills.MILLS_OF_SECOND.getValue() * 2)) {
                    addTaskToLoopClockSlot(clockworkTask, 2);
                } else {
                    addTaskToLoopClockSlot(clockworkTask, 1);
                }
            }
            //完成操作, 计数器减一
            latch.countDown();
        }
    }

    /**
     * 构建单个作业环形时钟信息（单个）
     * <p>
     * 设置作业的下一次执行所在的执行slot信息
     *
     * @param taskId                 任务Id
     * @param nextMatchingTimeNumber 填充几次槽位（默认可以为1）
     * @return boolean
     */
    @Override
    public boolean addTaskToLoopClockSlot(Integer taskId, Integer nextMatchingTimeNumber) {
        if (taskId == null) {
            throw new RuntimeException("task id info is null.");
        }
        return addTaskToLoopClockSlot(tbClockworkTaskMapper.selectByPrimaryKey(taskId), nextMatchingTimeNumber);
    }

    /**
     * 构建单个作业环形时钟信息（单个）
     * <p>
     * 更新有cron表达式的任务到环形时钟数据结构，支持指定下一次匹配时间的次数。
     *
     * @param TbClockworkTask        任务
     * @param nextMatchingTimeNumber 填充几次槽位
     * @return boolean
     */
    @Override
    public boolean addTaskToLoopClockSlot(TbClockworkTask TbClockworkTask, int nextMatchingTimeNumber) {
        if (nextMatchingTimeNumber < 1) {
            nextMatchingTimeNumber = 1;
        }
        // 没有cron 表达式直接返回
        if (StringUtils.isEmpty(TbClockworkTask.getCronExp())) {
            return true;
        }
        //先把已经存在的槽位信息删除掉
        TbClockworkTaskAndSlotRelationExample example = new TbClockworkTaskAndSlotRelationExample();
        example.createCriteria().andTaskIdEqualTo(TbClockworkTask.getId());
        tbClockworkTaskAndLoopClockSlotRelMapper.deleteByExample(example);
        //计算并添加新的槽位
        for (int i = 0; i < nextMatchingTimeNumber; i++) {
            // 获取当前任务的下一次运行开始时间
            Long nextMatchingTime = CronExpression.nextMatchingTime(TbClockworkTask.getCronExp());

            // 更新下一次触发时间字段
            if (i == 0) {
                TbClockworkTask updateTbClockworkTask = new TbClockworkTask();
                updateTbClockworkTask.setId(TbClockworkTask.getId());
                updateTbClockworkTask.setNextTriggerTime(new Date(nextMatchingTime));
                tbClockworkTaskMapper.updateByPrimaryKeySelective(updateTbClockworkTask);
            }

            // 获取当前天的零点零分零秒
            Long dayStartTimestamp = DateUtil.getDayStartTimestamp(0);
            // 计算下一次运行时间和当前天的时间差，用来计算需要将任务放入哪个槽位
            Long betweenMillis = nextMatchingTime - dayStartTimestamp;
            // offsetDay代表跨了几天，等于零代表不跨天，是当前天
            Long offsetDay = betweenMillis / TimeToMills.MILLS_OF_DAY.getValue();
            // 组装作业和槽位的关系对象
            TbClockworkTaskAndSlotRelation record = getTaskAndLoopClockSlotRelByOffsetDay(
                    nextMatchingTime, offsetDay, TbClockworkTask.getId(), TbClockworkTask.getGroupId());
            tbClockworkTaskAndLoopClockSlotRelMapper.insertSelective(record);
        }
        return true;
    }

    /**
     * 构建多个作业环形时钟信息（批量）
     *
     * @param taskPojoList taskList
     * @return boolean
     */
    @Override
    public boolean addTaskToLoopClockSlotByBatch(List<TbClockworkTaskPojo> taskPojoList) {
        if (CollectionUtils.isEmpty(taskPojoList)) {
            return false;
        }

        // 删除的任务ID集合
        List<Integer> deleteTaskIds = new ArrayList<>();
        // 需要新添加的作业和槽位的关系集合
        List<TbClockworkTaskAndSlotRelation> addTaskAndLoopClockSlotRel = new ArrayList<>();
        // 需要更新触发时间的任务集合
        List<TbClockworkTask> updateTriggerTimeTbClockworkTasks = new ArrayList<>();

        for (TbClockworkTaskPojo taskPojo : taskPojoList) {
            // 没有cron表达式则忽略
            if (StringUtils.isEmpty(taskPojo.getCronExp())) {
                continue;
            }

            // 作业和槽位的更新采取先删除后添加的方式，所有将此任务ID加入删除的任务ID集合
            deleteTaskIds.add(taskPojo.getId());

            // 获取当前任务的下一次运行开始时间
            long nextMatchingTime = CronExpression.nextMatchingTime(taskPojo.getCronExp());

            // 设置下一次触发时间，并加入到更新集合，用于前端显示
            taskPojo.setNextTriggerTime(new Date(nextMatchingTime));
            updateTriggerTimeTbClockworkTasks.add(taskPojo);

            // 获取当前天的零点零分零秒
            Long dayStartTimestamp = DateUtil.getDayStartTimestamp(0);
            // 计算下一次运行时间和当前天的时间差，用来计算需要将任务放入哪个槽位
            Long betweenMillis = nextMatchingTime - dayStartTimestamp;
            // 此变量代表跨了几天，等于零代表不跨天，是当前天，累加以此类推
            Long offsetDay = betweenMillis / TimeToMills.MILLS_OF_DAY.getValue();
            // 组装作业和槽位的关系对象
            TbClockworkTaskAndSlotRelation record = getTaskAndLoopClockSlotRelByOffsetDay(
                    nextMatchingTime, offsetDay, taskPojo.getId(), taskPojo.getGroupId());
            // 添加到作业和槽位的关系集合
            addTaskAndLoopClockSlotRel.add(record);
        }

        // 操作数据库删除作业和槽位的关系
        if (!deleteTaskIds.isEmpty()) {
            TbClockworkTaskAndSlotRelationExample example = new TbClockworkTaskAndSlotRelationExample();
            example.createCriteria().andTaskIdIn(deleteTaskIds);
            tbClockworkTaskAndLoopClockSlotRelMapper.deleteByExample(example);
            LOG.info("[addTaskToLoopClockSlotByBatch]delete task rel size = {}", deleteTaskIds.size());
        } else {
            LOG.info("[addTaskToLoopClockSlotByBatch]delete task rel size = {}", 0);
        }

        // 操作数据库添加作业和槽位的关系
        if (!addTaskAndLoopClockSlotRel.isEmpty()) {
            taskBatchMapper.batchInsertTaskAndLoopClockSlotRel(addTaskAndLoopClockSlotRel);
            LOG.info("[addTaskToLoopClockSlotByBatch]add task rel size = {}", addTaskAndLoopClockSlotRel.size());
        } else {
            LOG.info("[addTaskToLoopClockSlotByBatch]add task rel size = {}", 0);
        }

        // 操作数据库更新下一次触发时间的作业
        if (!updateTriggerTimeTbClockworkTasks.isEmpty()) {
            taskBatchMapper.batchUpdateTaskNextTriggerTime(updateTriggerTimeTbClockworkTasks);
            LOG.info("[addTaskToLoopClockSlotByBatch]update task trigger time size = {}",
                    updateTriggerTimeTbClockworkTasks.size());
        } else {
            LOG.info("[addTaskToLoopClockSlotByBatch]update task trigger time size = {}", 0);
        }

        return true;
    }

    /**
     * 获取指定时间槽位的作业
     *
     * @param slotPosition 时间槽位
     * @return 时间槽位对应的任务列表
     */
    @Override
    public List<TbClockworkTaskPojo> getHasCronTaskFromSlot(Integer slotPosition) {
        LOG.info("[LoopClockService-getHasCronTaskFromSlot]get tasks from slot begin,slotPosition = {}", slotPosition);

        long start = System.currentTimeMillis();

        // 获得当前槽位和任务的关联信息
        List<TbClockworkTaskAndSlotRelation> TbClockworkTaskRelLoopClockSlots = getTssLoopClockSlotAndTaskIds(slotPosition, 0);

        if (CollectionUtils.isEmpty(TbClockworkTaskRelLoopClockSlots)) {
            return null;
        }

        TbClockworkTaskExample example = new TbClockworkTaskExample();
        List<Integer> taskIds = new ArrayList<>();
        for (TbClockworkTaskAndSlotRelation rel : TbClockworkTaskRelLoopClockSlots) {
            taskIds.add(rel.getTaskId());
        }
        example.createCriteria().andIdIn(taskIds).andCronExpIsNotNull().andOnlineEqualTo(TaskTakeEffectStatus.ONLINE.getValue());

        List<TbClockworkTask> TbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(TbClockworkTasks)) {
            LOG.info("[LoopClockService-getHasCronTaskFromSlot]get tasks from slot success, slotPosition = {},result size = {},"
                    + "cost time = {} ms.", slotPosition, 0, System.currentTimeMillis() - start);
            return null;
        }
        List<TbClockworkTaskPojo> result = PojoUtil.convertList(TbClockworkTasks, TbClockworkTaskPojo.class);
        LOG.info("[LoopClockService-getHasCronTaskFromSlot]get tasks from slot success, slotPosition = {},result size = {},"
                + "cost time = {} ms.", slotPosition, result.size(), System.currentTimeMillis() - start);

        return result;
    }

    private List<TbClockworkTaskAndSlotRelation> getTssLoopClockSlotAndTaskIds(int slotPosition, int dayOffset) {
        if (slotPosition < 0 || slotPosition > 1439) {
            throw new RuntimeException("param slotPosition should more than or equals 0 and less than 1440");
        }
        // get slot position id.
        TbClockworkTaskLoopClockSlotExample TbClockworkTaskLoopClockSlotExample = new TbClockworkTaskLoopClockSlotExample();
        TbClockworkTaskLoopClockSlotExample.createCriteria().andSlotEqualTo(slotPosition);
        TbClockworkTaskLoopClockSlot taskLoopClockSlot
                = tbClockworkTaskLoopClockSlotMapper.selectByExample(TbClockworkTaskLoopClockSlotExample).get(0);
        // get tasks of current slot,that is current time need to be launched to executive tasks.
        TbClockworkTaskAndSlotRelationExample example = new TbClockworkTaskAndSlotRelationExample();
        example.createCriteria().andTaskExecDateEqualTo(new Date(DateUtil.getDayStartTimestamp(0)))
                .andSlotIdEqualTo(taskLoopClockSlot.getId());
        return tbClockworkTaskAndLoopClockSlotRelMapper.selectByExample(example);
    }

    private TbClockworkTaskAndSlotRelation getTaskAndLoopClockSlotRelByOffsetDay(
            Long nextMatchingTime, Long offsetDay, Integer taskId, Integer groupId) {
        if (offsetDay < 0) {
            throw new RuntimeException("The parameter value of offset day is less than 0! please check it.");
        }

        // 获取作业执行那天的零点零分零秒
        Long offsetDayStartTimestamp = DateUtil.getDayStartTimestamp(offsetDay.intValue());
        Long offsetDayBetweenMillis = nextMatchingTime - offsetDayStartTimestamp;
        Long offsetDaySlot = offsetDayBetweenMillis % 60000 == 0 ? offsetDayBetweenMillis / 60000 : offsetDayBetweenMillis / 60000 + 1;

        // 组装作业和槽位的关系对象
        TbClockworkTaskAndSlotRelation record = new TbClockworkTaskAndSlotRelation();
        record.setTaskId(taskId);
        record.setGroupId(groupId);
        record.setSlotId(getLoopClockSlotIdBySlotPosition(offsetDaySlot.intValue()));
        record.setTaskExecDate(new Date(offsetDayStartTimestamp));
        record.setCreateTime(new Date());
        return record;
    }

    private Integer getLoopClockSlotIdBySlotPosition(int slotPosition) {
        //使用guava缓存
        Integer result = null;
        try {
            result = LOOP_CLOCK_META_CACHE.get(slotPosition, new Callable<Integer>() {
                @Override
                public Integer call() {
                    // 如果没有数据，则先初始化一遍
                    long counter = tbClockworkTaskLoopClockSlotMapper.countByExample(null);
                    if (counter < 1) {
                        for (int i = 0; i < TimeToMills.SECONDS_OF_DAY.getValue(); i++) {
                            TbClockworkTaskLoopClockSlot tssLoopClockSlot = new TbClockworkTaskLoopClockSlot();
                            tssLoopClockSlot.setSlot(i);
                            tbClockworkTaskLoopClockSlotMapper.insertSelective(tssLoopClockSlot);
                        }
                    }
                    TbClockworkTaskLoopClockSlotExample example = new TbClockworkTaskLoopClockSlotExample();
                    example.createCriteria().andSlotEqualTo(slotPosition);
                    List<TbClockworkTaskLoopClockSlot> tssLoopClockSlots
                            = tbClockworkTaskLoopClockSlotMapper.selectByExample(example);
                    return tssLoopClockSlots.get(0).getId();
                }
            });
        } catch (ExecutionException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 检查任务的下一次执行周期是否和当前时间的间距比较近，我们以两分钟为阀值举例，如果小于两分钟则返回真
     * 说明离执行时间小于两分钟，用于初始化的时候判断是否需要多填充几个槽位，
     * 避免在启动的时候拣选器错过了时间差，导致某些任务不能运行起来。
     *
     * @param TbClockworkTask
     * @param thresholdInMills
     * @return
     */
    private boolean checkHasCronExpTaskExecutionPeriodIsSmaller(TbClockworkTask TbClockworkTask, long thresholdInMills) {
        // 获取当前任务的下一次运行开始时间
        Long nextMatchingTime = CronExpression.nextMatchingTime(TbClockworkTask.getCronExp());

        // 获取当前时间
        Long currentTimestamp = DateUtil.getCurrentTimestamp();

        // 计算下一次运行时间和当前的时间差
        Long betweenMillis = nextMatchingTime - currentTimestamp;

        // 如果下一次执行时间和当前时间间隔小于阀值
        return betweenMillis < thresholdInMills;
    }

}
