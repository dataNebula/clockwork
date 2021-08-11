package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DataUtil;
import com.creditease.adx.clockwork.common.util.TaskUtil;
import com.creditease.adx.clockwork.dao.mapper.DagMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkDagMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:29 上午 2020/7/8
 * @ Description：
 * @ Modified By：
 */
@Service(value = "dagService")
public class DagService implements IDagService {

    private static final Logger LOG = LoggerFactory.getLogger(DagService.class);

    @Autowired
    private ITaskService taskService;

    @Autowired
    private ITaskOperationService taskOperationService;

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Autowired
    private TbClockworkDagMapper tbClockworkDagMapper;

    @Autowired
    private IGraphService graphService;

    @Autowired
    private DagMapper dagMapper;

    @Autowired
    private ITaskRelationService taskRelationService;

    /**
     * 初始化dagId（任务未设置dagId，可调用此接口设置，比如迁移数据后等情况）如果DagId存在则不会再做初始化
     *
     * @return boolean
     */
    @Override
    public boolean initTaskDagId() {
        TbClockworkTaskMapper tbClockworkTaskMapper = taskService.getMapper();
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andOnlineEqualTo(true).andDagIdEqualTo(-1);
        example.setLimitStart(0);
        example.setLimitEnd(1);

        // 查找出没有设置dagId的上线任务，并设置dagId
        while (true) {
            List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(tbClockworkTasks)) {
                break;
            }
            buildDagIdForTask(tbClockworkTasks.get(0));
        }
        return true;
    }

    /**
     * 刷新dag基本信息（不会重新构建dagId）
     * 获取dag信息，以及更新dag信息（更新TaskCount、LeaderTaskId、LeaderTaskName、Description、UpdateTime）
     *
     * @param dagId dagId
     * @return
     */
    @Override
    public TbClockworkDag refreshDagInfoById(int dagId) {
        TbClockworkDag record = tbClockworkDagMapper.selectByPrimaryKey(dagId);
        if (record == null) {
            LOG.error("[DagService-selectByPrimaryKey]get dag info is null. dag id = {}", dagId);
            return null;
        }

        List<TbClockworkTaskPojo> tasks = taskService.getTasksByDagId(dagId);
        int taskId = TaskUtil.getHighPriorityTaskId(tasks);
        if (taskId == -1) {
            LOG.error("[DagService-getHighPriorityTaskId]get high priority task id error. " +
                    "dag id = {}, task id = {}", dagId, taskId);
            tbClockworkDagMapper.deleteByPrimaryKey(dagId);
            return null;
        }

        // 更新Dag信息
        TbClockworkTaskPojo task = taskService.getTaskById(taskId);
        record.setTaskCount(tasks.size());
        record.setLeaderTaskId(taskId);
        record.setLeaderTaskName(task == null ? null : task.getName());
        record.setDescription(task == null ? null : task.getDescription());
        record.setUpdateTime(new Date());
        int update = tbClockworkDagMapper.updateByPrimaryKeySelective(record);
        if (update < 1) {
            LOG.error("[DagService-updateByPrimaryKeySelective]update error. dag id = {}, task id = {}", dagId, taskId);
        }
        return record;
    }


    /**
     * 清除空的DAG信息（没有task引用，则删除）
     *
     * @return 被清空的DagIds列表
     */
    @Override
    public List<Integer> cleanEmptyDagInfo() {
        List<TbClockworkDagPojo> dagPojoList = dagMapper.selectEmptyDagInfo();
        if (CollectionUtils.isEmpty(dagPojoList)) {
            LOG.info("[DagService-cleanEmptyDagInfo] Is not need to clean dag info data.");
            return null;
        }

        List<Integer> dagIds = dagPojoList.stream().map(TbClockworkDagPojo::getId).collect(Collectors.toList());
        // 删除无效的数据
        TbClockworkDagExample example = new TbClockworkDagExample();
        example.createCriteria().andIdIn(dagIds);
        tbClockworkDagMapper.deleteByExample(example);
        LOG.info("[DagService-cleanEmptyDagInfo] need to clean dag info data = {}", dagIds);
        return dagIds;
    }


    /**
     * 更新DAG taskCount信息
     *
     * @return count
     */
    @Override
    public int updateDagCount() {
        return dagMapper.updateDagCount();
    }

    /**
     * 重新构建该dagIds相关的所有任务
     *
     * @param dagIds dagIds
     * @return bool
     */
    @Override
    public boolean buildDagIdForDagIds(List<Integer> dagIds) {
        if (CollectionUtils.isEmpty(dagIds)) {
            return true;
        }
        TbClockworkTask record = new TbClockworkTask();
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        record.setDagId(-1);
        example.createCriteria().andDagIdIn(dagIds);
        int count = tbClockworkTaskMapper.updateByExampleSelective(record, example);
        if (count > 0) {
            initTaskDagId();
        }
        return true;
    }

    /**
     * 重新构建该dagId相关的所有任务
     *
     * @param dagId dagId
     * @return bool
     */
    @Override
    public boolean buildDagIdForDagId(Integer dagId) {
        if (dagId == null) {
            return true;
        }
        TbClockworkTask record = new TbClockworkTask();
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        record.setDagId(-1);
        example.createCriteria().andDagIdEqualTo(dagId);
        int count = tbClockworkTaskMapper.updateByExampleSelective(record, example);
        if (count > 0) {
            initTaskDagId();
        }
        return true;
    }


    /**
     * 构建DagId（构建单个任务）
     *
     * @param task task
     * @return dagId
     */
    @Override
    public int buildDagIdForTask(TbClockworkTask task) {
        long start = System.currentTimeMillis();
        if (task == null || !task.getOnline()) { // 下线的任务不在这儿构建，所以当任务从新上线需要再次构建
            LOG.info("[DagService-buildDagIdForTask] end, task id = {}, dagId = {}, online = {}.",
                    task == null ? null : task.getId(), -1, task == null ? null : task.getOnline());
            return -1;
        }
        Integer taskId = task.getId();
        Integer dagId = task.getDagId();
        LOG.info("[DagService-buildDagIdForTask] start, task id = {}, dagId = {}", taskId, dagId);

        // 获取上下级直接关联节点，并获取dagId
        List<TbClockworkTask> directlyRelationTask = taskRelationService.getTaskDirectlyRelationTaskNotIncludeSelf(taskId);
        if (isNeedToReBuildTheWholeDiagram(taskId, dagId, directlyRelationTask)) {
            // 构建整个DagId
            int newDagId = buildDagIdForTaskGraph(taskId, dagId);
            LOG.info("[buildDagIdForTask-End]set task dagId success, task id = {}, dagId = {}, newDagId = {}, cost time = {} ms.",
                    taskId, dagId, newDagId, System.currentTimeMillis() - start);
            return newDagId;
        } else {
            LOG.info("[buildDagIdForTask-End]Not need to set task dagId, task id = {}, dagId = {}, cost time = {} ms.",
                    taskId, dagId, System.currentTimeMillis() - start);
            return dagId;
        }
    }


    /**
     * 构建DagId（构建单个任务）【同上】
     *
     * @param taskId task id
     * @return dagId
     */
    @Override
    public int buildDagIdForTaskId(int taskId) {
        return buildDagIdForTask(taskService.getTaskById(taskId));
    }


    /**
     * 构建DagId（重新构建这一批任务）
     *
     * @param taskIds list
     */
    @Override
    public void buildDagIdForTaskIds(List<Integer> taskIds) {
        long start = System.currentTimeMillis();
        List<TbClockworkTask> tasks = taskService.getTaskByTaskIds(taskIds);
        Iterator<TbClockworkTask> iterator = tasks.iterator();
        LinkedHashSet<Integer> skip = new LinkedHashSet<>();
        while (iterator.hasNext()) {
            TbClockworkTask task = iterator.next();
            Integer taskId = task.getId();
            if (taskId == null || skip.contains(taskId)) {
                LOG.info("[DagService-buildDagIdForTaskIds]Skip task = {}.", taskId);
                continue;
            }

            // 获取整个图的所有关联节点（整个dag图）
            List<TbClockworkTask> taskGraph = graphService.getGraphAllTasksByTaskId(taskId);
            LOG.info("[DagService-buildDagIdForTaskIds-getGraphAllTasksByTaskId] end, task id = {}, cost time = {} ms.",
                    taskId, System.currentTimeMillis() - start);
            if (CollectionUtils.isEmpty(taskGraph)) {
                // 没有关联关系，不为空则插入
                if (task.getDagId() == null || task.getDagId() == -1) {
                    int dagId = addDag(Collections.singletonList(taskId));
                    taskOperationService.updateTaskDagIdByBatch(Collections.singletonList(taskId), dagId);
                    LOG.info("[DagService-buildDagIdForTaskIds] update singleton task dag, task id = {}, dagId = {},", taskId, dagId);
                }
                LOG.info("[DagService-buildDagIdForTaskIds] end. task id = {}, cost time = {} ms.", taskId, System.currentTimeMillis() - start);
                continue;
            }
            // 判断是否需要构建任务，如果需要则重新构建任务（重新生成dag，更新所有的dagId）
            LOG.info("[DagService-buildDagIdForTaskIds] task id = {} taskGraph.size = {}.", taskId, taskGraph.size());
            List<Integer> taskGraphIds = taskGraph.stream().map(TbClockworkTask::getId).collect(Collectors.toList());
            skip.addAll(taskGraphIds);
            if (isNeedToReBuildTheWholeDiagram(taskId, task.getDagId(), taskGraph)) {
                // 构建整个DagId
                int dagId = addDag(taskGraphIds);
                if (!taskGraphIds.isEmpty()) taskOperationService.updateTaskDagIdByBatch(taskGraphIds, dagId);
                LOG.info("[DagService-buildDagIdForTaskIds] end. Need to build dagId = {}, task.size = {}, taskId = {},",
                        dagId, taskGraphIds.size(), taskGraphIds);
            } else {
                LOG.info("[DagService-buildDagIdForTaskIds]Not need to build DagId for task id = {}.", taskId);
            }
        }
        LOG.info("[DagService-buildDagIdForTaskIds]End. taskIds = {}, cost time = {} ms.", taskIds, System.currentTimeMillis() - start);

    }

    /**
     * 构建DagId, task以及该任务以前的直接关联节点（任务更新、删除）
     *
     * @param taskId                 taskId
     * @param oldDirectlyRelatedTask 旧的关联关系
     * @return
     */
    @Override
    public boolean buildDagIdForTaskUpdate(Integer taskId, List<Integer> oldDirectlyRelatedTask) {

        // 获取新的关联关系
        List<Integer> newDirectlyRelatedTask = null;
        if (taskId != null)
            newDirectlyRelatedTask = taskRelationService.findDirectlyRelationTaskIdsNotIncludeSelf(taskId);

        // 不存在新的关联关系（整个图只有该节点一个，直接新生成即可，如果还有旧的另外处理）
        if (CollectionUtils.isEmpty(newDirectlyRelatedTask)) {

            LOG.info("[DagService-buildDagIdForTaskUpdate]newDirectlyRelatedTask is empty. taskId = {}", taskId);
            // 没有新的关联关系，直接写入即可
            if (taskId != null) addDag(Collections.singletonList(taskId));

            // 判断旧的关联关系
            if (CollectionUtils.isEmpty(oldDirectlyRelatedTask)) {
                LOG.info("[DagService-buildDagIdForTaskUpdate]oldDirectlyRelatedTask is empty.");
                return true;
            } else {
                // 存在旧的关联关系，构建所有旧的关联任务
                LOG.info("[DagService-buildDagIdForTaskUpdate]oldDirectlyRelatedTask is not empty. " +
                        "buildDagIdForTaskIds：oldDirectlyRelatedTask is {}", oldDirectlyRelatedTask);
                buildDagIdForTaskIds(oldDirectlyRelatedTask);
            }
        }

        // 存在新的关联关系（必须要重新构建新的关联关系，如果新节点已经包含旧节点直接跳过[已经处理]，否则重新构建旧的关联关系）
        else {

            // 构建新的关联关系
            LOG.info("[DagService-buildDagIdForTaskUpdate]newDirectlyRelatedTask is not empty. " +
                    "buildDagIdForTaskIds： newDirectlyRelatedTask is {}", newDirectlyRelatedTask);
            if (CollectionUtils.isEmpty(oldDirectlyRelatedTask)) {
                // 只有新的关关系，构建所有新的关联任务
                LOG.info("[DagService-buildDagIdForTaskUpdate]oldDirectlyRelatedTask is empty.");
                buildDagIdForTaskIds(newDirectlyRelatedTask);
                return true;
            } else {
                // 如果新的关联关系和旧的关联关系相等，则不需要构建，skip
                if (DataUtil.isEqualChild(newDirectlyRelatedTask, oldDirectlyRelatedTask)) {
                    LOG.info("[DagService-buildDagIdForTaskUpdate]newDirectlyRelatedTask is Equal. oldDirectlyRelatedTask skip.");
                    return true;
                }
                if (DataUtil.isContainsChild(newDirectlyRelatedTask, oldDirectlyRelatedTask) || DataUtil.isContainsChild(oldDirectlyRelatedTask, newDirectlyRelatedTask)) {
                    LOG.info("[DagService-buildDagIdForTaskUpdate]oldDirectlyRelatedTask is not empty. " +
                            "But newDirectlyRelatedTask or oldDirectlyRelatedTask isContains.");
                    buildDagIdForTaskIds(newDirectlyRelatedTask);
                    return true;
                }
                // 构建所有新的关联任务
                // 构建所有旧的关联任务
                LOG.info("[DagService-buildDagIdForTaskUpdate]oldDirectlyRelatedTask is not empty. " +
                        "buildDagIdForTaskIds：newDirectlyRelatedTask and oldDirectlyRelatedTask {} and {}", newDirectlyRelatedTask, oldDirectlyRelatedTask);
                // 求出并集
                newDirectlyRelatedTask.removeAll(oldDirectlyRelatedTask);
                newDirectlyRelatedTask.addAll(oldDirectlyRelatedTask);
                buildDagIdForTaskIds(newDirectlyRelatedTask);
            }
        }
        return true;

    }


    /**
     * 构建dagId（强制构建整个task图）
     *
     * @param taskId taskId（直接构建整个图）
     * @return
     */
    private int buildDagIdForTaskGraph(Integer taskId, Integer dagId) {

        if (taskId == null) return -1;
        long start = System.currentTimeMillis();

        // 获取整个图的所有关联节点（整个dag图）
        List<TbClockworkTask> taskGraph = graphService.getGraphAllTasksByTaskId(taskId);
        LOG.info("[DagService-buildDagIdForTaskId-getGraphAllTasksByTaskId] end, task id = {}, cost time = {} ms.",
                taskId, System.currentTimeMillis() - start);

        List<Integer> taskIds = taskGraph.stream().map(TbClockworkTask::getId).collect(Collectors.toList());
        LOG.info("[buildDagIdForTaskGraph]taskId = {}, taskIds.size = {}, taskIds = {} ", taskId, taskIds.size(), taskIds);
        if (CollectionUtils.isEmpty(taskIds)) {
            return -1;
        }

        if (isNeedToReBuildTheWholeDiagram(taskId, dagId, taskGraph)) {
            dagId = addDag(taskIds);
            if (!taskIds.isEmpty()) taskOperationService.updateTaskDagIdByBatch(taskIds, dagId);
            LOG.info("[DagService-buildDagIdForTaskGraph-updateTaskDagIdByBatch] needUpdateTaskIds = {}, dagId = {}",
                    taskIds, dagId);
            return dagId;
        }
        return taskService.getTaskById(taskId).getDagId();

    }


    /**
     * 添加Dag信息
     *
     * @param taskGraph task图
     * @return
     */
    private int addDag(List<Integer> taskGraph) {
        if (CollectionUtils.isEmpty(taskGraph)) {
            return -1;
        }
        TbClockworkDag record = new TbClockworkDag();
        record.setTaskCount(taskGraph.size());
        record.setUpdateTime(new Date());
        record.setCreateTime(new Date());
        tbClockworkDagMapper.insert(record);
        taskOperationService.updateTaskDagIdByBatch(taskGraph, record.getId());
        return record.getId();
    }

    /**
     * 判断是否需要重新构建整个任务（当且仅当该任务以及所有关联节点唯一切只占用该dagId时为true）
     *
     * @param taskId       taskId
     * @param dagId        dagId 该taskId的dagId
     * @param relationTask 该任务的关联节点
     * @return
     */
    private boolean isNeedToReBuildTheWholeDiagram(int taskId, Integer dagId, List<TbClockworkTask> relationTask) {
        LinkedHashSet<Integer> allDagIdList = new LinkedHashSet<Integer>();       // 所有的的DagId集合
        List<Integer> allRelationTaskNodeTaskIds = new ArrayList<>();           // 所有的依赖关系节点
        if (CollectionUtils.isNotEmpty(relationTask)) {
            for (TbClockworkTask task : relationTask) {
                if (task.getDagId() != -1) {
                    allDagIdList.add(task.getDagId());
                }
            }
            allRelationTaskNodeTaskIds = relationTask.stream().map(TbClockworkTask::getId).collect(Collectors.toList());
        }

        // 是否需要重新构建图整个图
        LOG.info("[DagService-isNeedToReBuildTheWholeDiagram] allDagIdList = {}, allDagIdList.size = {}, allRelationTaskNodeTaskIds.size = {}",
                allDagIdList, allDagIdList.size(), allRelationTaskNodeTaskIds.size());
        Integer onlyDagId = null;
        if (allDagIdList.size() == 1 && isUniqueDagId((onlyDagId = allDagIdList.iterator().next()), allRelationTaskNodeTaskIds)) {
            // allDagIdList 唯一，不需要重新构建图整个图
            LOG.info("[DagService-isUniqueDagId] oldDagIdList.size is 1 and dagId = {}, onlyDagId {} isUniqueDagId.", dagId, onlyDagId);
            List<Integer> needUpdateTaskIds = new ArrayList<>();
            if (!onlyDagId.equals(dagId)) {
                needUpdateTaskIds.add(taskId);
                LOG.info("[DagService-isUniqueDagId] updateTaskDagId taskId = {}, dagId = {}, onlyDagId = {}.", taskId, dagId, onlyDagId);
            }
            if (CollectionUtils.isNotEmpty(relationTask)) for (TbClockworkTask task : relationTask) {
                if (!onlyDagId.equals(task.getDagId())) {
                    needUpdateTaskIds.add(task.getId());
                }
            }

            if (!needUpdateTaskIds.isEmpty()) taskOperationService.updateTaskDagIdByBatch(needUpdateTaskIds, onlyDagId);
            LOG.info("[DagService-buildDagIdForTaskGraph-updateTaskDagIdByBatch] needUpdateTaskIds = {}, dagId = {}, onlyDagId = {}",
                    needUpdateTaskIds, dagId, onlyDagId);

            LOG.info("[DagService-isNeedToReBuildTheWholeDiagram] Not need to build all tasks, dagId = {}, onlyDagId = {}", dagId, onlyDagId);
            return false;
        }
        LOG.info("[DagService-isNeedToReBuildTheWholeDiagram] Need to build all tasks, dagId = {}, onlyDagId = {}", dagId, onlyDagId);
        return true;
    }

    /**
     * 查看是否是唯一的dagId，只属于该taskList
     *
     * @param dagId     dagId
     * @param inTaskIds 只属于该taskList
     * @return
     */
    private boolean isUniqueDagId(int dagId, List<Integer> inTaskIds) {
        try {
            if (dagId < 1) return false;
            long record = 0;
            TbClockworkTaskExample example = new TbClockworkTaskExample();
            if (CollectionUtils.isEmpty(inTaskIds)) {
                example.createCriteria().andDagIdEqualTo(dagId);
            } else {
                example.createCriteria().andDagIdEqualTo(dagId).andIdNotIn(inTaskIds);
            }
            record = taskService.getMapper().countByExample(example);
            boolean result = record <= 0;
            LOG.info("isUniqueDagId {}, dagId = {}, notInTaskIds = {}, record = {}", result, dagId, inTaskIds, record);
            return result;
        } catch (Exception e) {
            LOG.error("DagService-isUniqueDagId Error. dagId = {}, notInTaskIds = {}", dagId, inTaskIds);
        }
        return false;
    }

}
