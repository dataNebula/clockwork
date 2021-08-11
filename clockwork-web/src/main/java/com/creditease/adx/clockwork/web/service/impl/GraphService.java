package com.creditease.adx.clockwork.web.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.creditease.adx.clockwork.client.service.DagClientService;
import com.creditease.adx.clockwork.client.service.GraphClientService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.entity.graph.LinkPosition;
import com.creditease.adx.clockwork.common.entity.graph.LinkRadial;
import com.creditease.adx.clockwork.common.entity.graph.NodePosition;
import com.creditease.adx.clockwork.common.entity.graph.NodeRadial;
import com.creditease.adx.clockwork.common.util.graphutils.DagGraphUtils;
import com.creditease.adx.clockwork.common.util.graphutils.GraphRadialBuilder;
import com.creditease.adx.clockwork.common.util.graphutils.GraphSearchTaskAdapter;
import com.creditease.adx.clockwork.web.service.*;
import com.creditease.adx.clockwork.common.util.graphutils.GraphPositionBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:56 上午 2020/8/21
 * @ Description：task graph相关service
 * @ Modified By：
 */
@Service
public class GraphService implements IGraphService {

    private static final Logger LOG = LoggerFactory.getLogger(GraphService.class);

    @Autowired
    private ITaskService taskService;

    @Autowired
    private DagClientService dagClientService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ILdapService ldapService;

    @Autowired
    private GraphClientService graphClientService;

    /**
     * 构建辐射DAG图, 通过dagId
     *
     * @param dagId dagId
     * @return Graph
     */
    @Override
    public Map<String, Object> buildDagGraphForRadialByDagId(Integer dagId) {
        TbClockworkDag dag = dagClientService.refreshDagInfoById(dagId);
        if (dag == null) {
            throw new RuntimeException("该dagID已经不存在，请重新选择！");
        }
        // 获取task节点
        List<TbClockworkTaskPojo> tasks = taskService.getTasksByDagId(dagId);
        if (CollectionUtils.isEmpty(tasks)) {
            throw new RuntimeException("该dagID没有任务，可能已经重新构建了新的dag, 请重新选择！");
        }
        // 获取节点关系
        List<TbClockworkTaskRelation> relations
                = graphClientService.getGraphAllRelationByTaskId(tasks.get(0).getId());

        // 构建节点关系图
        Set<NodeRadial> nodes = GraphRadialBuilder.buildNodeForRadials(tasks);
        Set<LinkRadial> links = GraphRadialBuilder.buildLinkForByRadial(relations);

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", new ArrayList<NodeRadial>(nodes));
        result.put("edges", new ArrayList<LinkRadial>(links));
        LOG.info("buildDagGraphForRadialByDagId, result = {}", result);
        return result;
    }

    /**
     * 构建附带位置的DAG图, 根据dagId
     *
     * @param dagId dagId
     * @return Graph
     */
    @Override
    public Map<String, Object> buildDagGraphForPositionByDagId(Integer dagId) {
        TbClockworkDag dag = dagClientService.refreshDagInfoById(dagId);
        if (dag == null) {
            throw new RuntimeException("该dagID已经不存在，请重新选择！");
        }
        // 获取节点以及关系
        List<TbClockworkTaskPojo> taskPojoList = taskService.getTasksByDagId(dagId);
        if (CollectionUtils.isEmpty(taskPojoList)) {
            throw new RuntimeException("该dagID没有任务，可能已经重新构建了新的dag, 请重新选择！");
        }
        List<TbClockworkTaskRelation> relations
                = graphClientService.getGraphAllRelationByTaskId(taskPojoList.get(0).getId());
        Set<NodePosition> nodes = GraphPositionBuilder.buildNodes(taskPojoList);
        Set<LinkPosition> links = GraphPositionBuilder.buildLinks(relations);

        // 用找到的nodes和links来分层画图
        DagGraphUtils dagGraphUtil = new DagGraphUtils(new ArrayList<NodePosition>(nodes), new ArrayList<LinkPosition>(links));
        Map<String, Object> dagGraphMap = dagGraphUtil.drawDagGraph();
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", JSONArray.toJSONString(dagGraphMap.get("nodes")));
        result.put("links", JSONArray.toJSONString(dagGraphMap.get("links")));
        LOG.info("buildDagGraphForPositionByDagId, result = {}", result);
        return result;
    }


    @Override
    public Map<String, Object> getTaskDagGraph(
            Integer taskId, String taskName, String userName, Integer upDeepLevel, Integer downDeepLevel, boolean showDag) {
        LOG.info("getTaskDagGraph, taskId = {}, taskName = {}, upDeepLevel = {}, downDeepLevel = {}, showDag = {}",
                taskId, taskName, upDeepLevel, downDeepLevel, showDag);
        // 获得初始task
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        if (StringUtils.isNotBlank(taskName)) example.createCriteria().andNameEqualTo(taskName);
        if (taskId != null) example.createCriteria().andIdEqualTo(taskId);

        List<TbClockworkTask> searchTasks = taskService.getMapper().selectByExample(example);
        if (searchTasks == null || searchTasks.size() != 1) {
            throw new RuntimeException("不存在该任务，taskId" + taskId);
        }
        // 根据初始taskId选取graph中的一个节点，作为起步节点
        TbClockworkTask searchTask = searchTasks.get(0);

        List<TbClockworkTaskRelation> relations = graphClientService.getGraphAllRelationByTaskId(searchTask.getId());
        List<TbClockworkTaskPojo> tasks = PojoUtil.convertList(graphClientService.getGraphAllTasksByTaskId(searchTask.getId()), TbClockworkTaskPojo.class);

        // 得到graph里的所有节点
        if (CollectionUtils.isEmpty(tasks)) {
            throw new RuntimeException("该dagID没有任务，可能已经重新构建了新的dag, 请重新选择！");
        }

        Set<NodePosition> nodes = new HashSet<>();
        Set<LinkPosition> links = new HashSet<>();
        // showDag = true 时返回全graph
        if (showDag) {
            nodes = GraphPositionBuilder.buildNodes(tasks);
            links = GraphPositionBuilder.buildLinks(relations);
        } else {
            // 用graph的nodes和links构建一个graphSearch工具类，方便我们按层来查找节点
            GraphSearchTaskAdapter upGraphSearchTaskAdapter = new GraphSearchTaskAdapter(
                    GraphPositionBuilder.buildNodes(tasks), GraphPositionBuilder.buildLinks(relations), upDeepLevel, false);
            GraphSearchTaskAdapter downGraphSearchTaskAdapter = new GraphSearchTaskAdapter(
                    GraphPositionBuilder.buildNodes(tasks), GraphPositionBuilder.buildLinks(relations), downDeepLevel, true);
            // 从start节点开始往上查找几层
            upGraphSearchTaskAdapter.widthFirstSeachWithDeepLevel(taskName);
            nodes.addAll(upGraphSearchTaskAdapter.getReturnNodesSet());
            links.addAll(upGraphSearchTaskAdapter.getReturnLinksSet());

            // 从start节点开始往下查找几层
            downGraphSearchTaskAdapter.widthFirstSeachWithDeepLevel(taskName);
            nodes.addAll(downGraphSearchTaskAdapter.getReturnNodesSet());
            links.addAll(downGraphSearchTaskAdapter.getReturnLinksSet());
        }
        // 用找到的nodes和links来分层画图
        DagGraphUtils dagGraphUtil = new DagGraphUtils(new ArrayList<NodePosition>(nodes), new ArrayList<LinkPosition>(links));
        Map<String, Object> dagGraphMap = dagGraphUtil.drawDagGraph();
        Map<String, Object> result = new HashMap<>();
        result.put("nodes", JSONArray.toJSONString(dagGraphMap.get("nodes")));
        result.put("links", JSONArray.toJSONString(dagGraphMap.get("links")));
        return result;
    }



    /**
     * 过滤掉其他人的TaskDependency节点，只留下自己的
     *
     * @param taskRelations
     * @param userName
     * @return
     */
    @SuppressWarnings("unused")
    private List<TbClockworkTaskRelationPojo> filterByUserName(List<TbClockworkTaskRelationPojo> taskRelations, String userName) {
        // 管理员可以看所有task
        if (userService.getRoleByUserName(userName).equals("admin")) {
            return taskRelations;
        } else {
            // 查出用户所属组
            String userGroupName = ldapService.getOrgInfoByEmail(userName);
            // 查出所有公有task+所属组下的task
            List<Map<String, Object>> tasks = taskService.getTaskIdAndNameByUserGroupName(userName, userGroupName, null);
            // 把taskId添加到set里
            Set<String> taskIdSet = new HashSet<>();
            for (Map<String, Object> task : tasks) {
                taskIdSet.add(task.get("id") + "");
            }
            // 过滤ArrayList<TbClockworkTaskRelation> ，只留下set里有id的节点
            ArrayList<TbClockworkTaskRelationPojo> result = new ArrayList<>();
            for (TbClockworkTaskRelationPojo taskRelation : taskRelations) {
                if (taskIdSet.contains(taskRelation.getTaskId().toString())) {
                    result.add(taskRelation);
                }
            }
            return result;
        }
    }


}
