package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.IGraphService;
import com.creditease.adx.clockwork.api.service.ITaskRelationService;
import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.common.entity.RelationChildrenFather;
import com.creditease.adx.clockwork.common.entity.RelationFatherChildren;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelationExample;
import com.creditease.adx.clockwork.common.enums.TaskRelationTakeEffectStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import com.creditease.adx.clockwork.common.util.MapUtil;
import com.creditease.adx.clockwork.dao.mapper.TaskRelationMapper;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:56 上午 2020/8/21
 * @ Description：Task Graph相关service
 * @ Modified By：
 */
@Service
public class GraphService implements IGraphService {

    private static final Logger LOG = LoggerFactory.getLogger(GraphService.class);

    @Autowired
    private ITaskService taskService;

    @Autowired
    private ITaskRelationService taskRelationService;

    @Autowired
    private TaskRelationMapper taskRelationMapper;


    /**
     * 获取整个dag图的所有关系
     *
     * @param taskId taskId
     * @return relations
     */
    @Override
    public List<TbClockworkTaskRelation> getGraphAllRelationByTaskId(Integer taskId) {
        if (taskId == null || taskId < 1) {
            return null;
        }
        // 找到这个图的所有节点taskIds
        List<Integer> taskIds = getGraphAllTaskIdsByTaskId(taskId);
        if (CollectionUtils.isEmpty(taskIds)) {
            return null;
        }
        // 如果<当前节点>还有父节点，再去找父节点
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria().andTaskIdIn(taskIds).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> relationsFather = taskRelationService.getMapper().selectByExample(example);
        if (CollectionUtils.isEmpty(relationsFather)) {
            relationsFather = new ArrayList<>();
        }

        // 查询子节点
        example = new TbClockworkTaskRelationExample();
        if (relationsFather.size() > 0) {
            example.createCriteria()
                    .andFatherTaskIdIn(taskIds).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue())
                    .andIdNotIn(relationsFather.stream().map(TbClockworkTaskRelation::getId).collect(Collectors.toList()));
        } else {
            example.createCriteria().andFatherTaskIdIn(taskIds).andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        }

        List<TbClockworkTaskRelation> children = taskRelationService.getMapper().selectByExample(example);
        if (CollectionUtils.isNotEmpty(children)) {
            relationsFather.addAll(children);
        }
        return relationsFather;
    }


    /**
     * 获取整个dag图的所有TaskIds
     *
     * @param taskId 该图的某一个taskId
     * @return all task ids
     */
    @Override
    public List<Integer> getGraphAllTaskIdsByTaskId(Integer taskId) {
        long start = System.currentTimeMillis();
        if (taskId == null) {
            return null;
        }
        // 先根据taskId找到一个关系节点
        TbClockworkTaskRelationPojo oneOfRelation = taskRelationService.findTaskOneRelation(taskId);
        if (oneOfRelation == null) {
            return Collections.singletonList(taskId);
        }
        // 再通过某个节点，查找整个关联节点
        List<Integer> allTaskRelationNode = findAllTaskRelationNode(oneOfRelation);
        LOG.info("findAllGraphTaskIdsByTaskId-findAllTaskRelationNode taskIds.size = {}, cost time = {} ms.",
                allTaskRelationNode.size(), System.currentTimeMillis() - start);
        return allTaskRelationNode;
    }

    /**
     * 获取整个dag图的所有任务
     *
     * @param taskId 该图的某一个taskId
     * @return all task
     */
    @Override
    public List<TbClockworkTask> getGraphAllTasksByTaskId(Integer taskId) {
        if (taskId == null || taskId < 1) {
            return null;
        }
        // 找到这个图的所有节点taskIds
        List<Integer> taskIds = getGraphAllTaskIdsByTaskId(taskId);
        if (CollectionUtils.isEmpty(taskIds)) {
            return null;
        }
        return taskService.getTaskByTaskIds(taskIds);
    }

    /**
     * 查找所有的任务关联节点（最优方案）
     *
     * @param oneOfRelation 某个关系
     * @return taskIds
     */
    private List<Integer> findAllTaskRelationNode(TbClockworkTaskRelation oneOfRelation) {

        // 加载数据到内存，构建特殊结构数据
        HashMap<Integer, RelationChildrenFather> childrenFather = taskRelationMapper.selectChildrenFather();
        HashMap<Integer, RelationFatherChildren> fatherChildren = taskRelationMapper.selectFatherChildren();

        Integer taskId = oneOfRelation.getTaskId();
        HashSet<Integer> result = new HashSet<>();
        HashSet<Integer> visit = new HashSet<>();       // 访问记录
        result.add(taskId);
        Queue<Integer> queue = new LinkedList<>();
        if (!queue.offer(taskId)) {
            throw new RuntimeException("[findAllTaskRelationNode]add task id to queue failure!");
        }
        Integer queueTaskId = null;
        while ((queueTaskId = queue.poll()) != null) {
            if (visit.contains(queueTaskId)) continue;  // 过滤：已经访问过的数据
            visit.add(queueTaskId);                     // 记录：已经访问过的数据

            // childrenFather
            RelationChildrenFather relationFather = childrenFather.get(queueTaskId);
            if (relationFather != null) {
                String[] fatherIds = relationFather.getFatherTaskIds().split(",");
                for (String fatherId : fatherIds) {
                    Integer newId = Integer.valueOf(fatherId);
                    if (visit.contains(newId)) continue;
                    queue.offer(newId);
                    result.add(newId);
                }
            }

            // relationChildren
            RelationFatherChildren relationChildren = fatherChildren.get(queueTaskId);
            if (relationChildren != null) {
                String[] childrenIds = relationChildren.getTaskIds().split(",");
                for (String childrenId : childrenIds) {
                    Integer newId = Integer.valueOf(childrenId);
                    if (visit.contains(newId)) continue;
                    queue.offer(newId);
                    result.add(newId);
                }
            }
        }
        return new ArrayList<>(result);
    }


    /**
     * 查找所有的任务关联节点（不是最优方案）
     *
     * @param oneOfRelation 某个关系
     * @return taskIds
     */
    private List<Integer> findAllTaskRelationNode2(TbClockworkTaskRelation oneOfRelation) {

        // 加载数据到内存，构建特殊结构的Map
        TbClockworkTaskRelationExample example = new TbClockworkTaskRelationExample();
        example.createCriteria()
                .andTaskIdIsNotNull().andFatherTaskIdIsNotNull().andIsEffectiveEqualTo(TaskRelationTakeEffectStatus.ONLINE.getValue());
        List<TbClockworkTaskRelation> taskRelations = taskRelationService.getMapper().selectByExample(example);
        HashSet<String> allSetData = new HashSet<>();   // 所有组装的结构数据
        HashSet<Integer> visit = new HashSet<>();       // 访问记录
        for (TbClockworkTaskRelation taskRelation : taskRelations) {
            allSetData.add(taskRelation.getTaskId() + "_" + taskRelation.getFatherTaskId());
        }

        Integer taskId = oneOfRelation.getTaskId();
        HashSet<Integer> result = new HashSet<>();
        result.add(taskId);
        Queue<Integer> queue = new LinkedList<>();
        if (!queue.offer(taskId)) {
            throw new RuntimeException("[findAllTaskRelationNode]add task id to queue failure!");
        }
        Integer queueTaskId = null;
        while ((queueTaskId = queue.poll()) != null) {
            if (visit.contains(queueTaskId)) continue;  // 过滤：已经访问过的数据
            visit.add(queueTaskId);                     // 记录：已经访问过的数据
            long l = System.currentTimeMillis();
            Set<String> set = MapUtil.mapFilterForKey(allSetData, queueTaskId);
            System.out.println(System.currentTimeMillis() - l);
            for (String children_father : set) {
                String[] split = children_father.split("_"); //【children,father】
                if (split.length != 2) continue;
                // 获取关联关系taskId（newId）
                Integer newId =
                        Integer.parseInt((queueTaskId == Integer.parseInt(split[0])) ? split[1] : split[0]);
                if (visit.contains(newId)) continue;
                queue.offer(newId);
                result.add(newId);
            }
        }
        return new ArrayList<>(result);
    }

}
