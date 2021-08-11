package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.client.service.TaskRelationClientService;
import com.creditease.adx.clockwork.common.pojo.TbClockworkDagPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.dao.mapper.DagMapper;
import com.creditease.adx.clockwork.web.entity.DeleteNodesParams;
import com.creditease.adx.clockwork.common.entity.graph.LinkRelPic;
import com.creditease.adx.clockwork.common.entity.graph.NodeRelPic;
import com.creditease.adx.clockwork.web.entity.SelectedParams;
import com.creditease.adx.clockwork.web.service.IDagService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
    private TaskRelationClientService taskRelationClientService;

    @Autowired
    private DagMapper dagMapper;


    /**
     * 获取子任务以及关系
     *
     * @param taskId         taskId
     * @param selectedParams params
     * @return
     */
    @Override
    public Map<String, Object> getTheChildTaskRelPicArray(Integer taskId, SelectedParams selectedParams) {
        Map<String, Object> result = new HashMap<>();

        NodeRelPic resultNode = new NodeRelPic();
        List<LinkRelPic> links = new ArrayList<>();
        List<NodeRelPic> nodes = new ArrayList<>();
        List<NodeRelPic> selectedNodes = selectedParams.getSelectedNodes();

        LOG.info("getTheChildTaskRelPicArray taskId = {},  selectedParams = {}.", taskId, selectedParams);
        Map<Integer, NodeRelPic> selectedNodeMaps = new HashMap<>();
        if (CollectionUtils.isNotEmpty(selectedNodes)) {
            selectedNodeMaps = selectedNodes.stream().collect(Collectors.toMap(NodeRelPic::getId, Function.identity(), (key1, key2) -> key2));
        }

        List<TbClockworkTaskPojo> taskChildrenNotIncludeSelf = taskRelationClientService.getTaskDirectlyChildrenNotIncludeSelf(taskId);
        if (CollectionUtils.isNotEmpty(taskChildrenNotIncludeSelf)) {
            NodeRelPic node = null;
            LinkRelPic link = null;
            for (TbClockworkTaskPojo taskPojo : taskChildrenNotIncludeSelf) {
                // node
                node = new NodeRelPic();
                node.setId(taskPojo.getId());
                node.setName(taskPojo.getName());
                node.setSelected(selectedNodeMaps.containsKey(taskPojo.getId()));
                nodes.add(node);

                // links
                link = new LinkRelPic();
                link.setSource(String.valueOf(taskId));
                link.setTarget(String.valueOf(taskPojo.getId()));
                links.add(link);
            }
        }

        resultNode.setId(taskId);
        resultNode.setChildren(nodes);
        result.put("node", resultNode);
        result.put("links", links);
        result.put("selectedParams", selectedParams);
        return result;
    }


    /**
     * 获取父任务以及关系
     *
     * @param taskId         taskId
     * @param selectedParams params
     * @return
     */
    @Override
    public Map<String, Object> getTheParentTaskRelPicArray(Integer taskId, SelectedParams selectedParams) {
        Map<String, Object> result = new HashMap<>();

        NodeRelPic resultNode = new NodeRelPic();
        List<LinkRelPic> links = new ArrayList<>();
        List<NodeRelPic> nodes = new ArrayList<>();

        LOG.info("getTheParentTaskRelPicArray taskId = {},  selectedParams = {}.", taskId, selectedParams);
        List<NodeRelPic> selectedNodes = selectedParams.getSelectedNodes();
        Map<Integer, NodeRelPic> selectedNodeMaps = new HashMap<>();
        if (CollectionUtils.isNotEmpty(selectedNodes)) {
            selectedNodeMaps = selectedNodes.stream().collect(Collectors.toMap(NodeRelPic::getId, Function.identity(), (key1, key2) -> key2));
        }
        List<TbClockworkTaskPojo> taskFatherNotIncludeSelf = taskRelationClientService.getTaskDirectlyFatherNotIncludeSelf(taskId);
        if (CollectionUtils.isNotEmpty(taskFatherNotIncludeSelf)) {
            NodeRelPic node = null;
            LinkRelPic link = null;
            for (TbClockworkTaskPojo taskPojo : taskFatherNotIncludeSelf) {
                // node
                node = new NodeRelPic();
                node.setId(taskPojo.getId());
                node.setName(taskPojo.getName());
                node.setSelected(selectedNodeMaps.containsKey(taskPojo.getId()));
                nodes.add(node);

                // links
                link = new LinkRelPic();
                link.setSource(String.valueOf(taskPojo.getId()));
                link.setTarget(String.valueOf(taskId));
                links.add(link);
            }
        }
        resultNode.setId(taskId);
        resultNode.setParents(nodes);
        result.put("node", resultNode);
        result.put("links", links);
        result.put("selectedParams", selectedParams);
        return result;
    }


    /**
     * 删除节点以及关系
     *
     * @param params params
     * @return
     */
    @Override
    public Map<String, Object> deleteNodesFromPic(DeleteNodesParams params) {
        Map<String, Object> result = new HashMap<>();

        List<LinkRelPic> removedLinks = new ArrayList<>();
        List<NodeRelPic> removedNodes = new ArrayList<>();

        int rootId = params.getRootId();            // 跟节点
        int direction = params.getDirection();      // 0子节点向下，1父节点向上
        int inactiveId = params.getInactiveId();    // 需要移除的任务Id
        SelectedParams selectedParams = params.getSelectedParams();

        LOG.info("deleteNodesFromPic rootId = {}, direction = {}, inactiveId = {}, selectedParams = {}.",
                rootId, direction, inactiveId, selectedParams);

        List<LinkRelPic> selectedLinks = selectedParams.getSelectedLinks();
        NodeRelPic node = null;
        if (CollectionUtils.isNotEmpty(selectedLinks)) {
            Queue<String> queue = new LinkedList<>();
            String queueId = null;
            if (direction == 0) { // 子节点关系关系
                // 直接链接的父节点直接设置
                for (LinkRelPic selectedLink : selectedLinks) {
                    if (selectedLink.getTarget().equals(String.valueOf(inactiveId))) {
                        node = new NodeRelPic();
                        node.setId(Integer.valueOf(selectedLink.getTarget()));
                        removedNodes.add(node);
                        removedLinks.add(selectedLink);
                    } else if (selectedLink.getSource().equals(String.valueOf(inactiveId))) {
                        queue.offer(selectedLink.getTarget());
                        node = new NodeRelPic();
                        node.setId(Integer.valueOf(selectedLink.getTarget()));
                        removedNodes.add(node);
                        removedLinks.add(selectedLink);
                    }
                }

                // 往下继续查找
                while ((queueId = queue.poll()) != null) {
                    for (LinkRelPic selectedLink : selectedLinks) {
                        if (selectedLink.getSource().equals(queueId)) {
                            queue.offer(selectedLink.getTarget());
                            node = new NodeRelPic();
                            node.setId(Integer.valueOf(selectedLink.getTarget()));
                            removedNodes.add(node);
                            removedLinks.add(selectedLink);
                        }
                    }
                }
            } else if (direction == 1) { // 父节点关系关系
                // 直接链接的子节点点直接设置
                for (LinkRelPic selectedLink : selectedLinks) {
                    if (selectedLink.getSource().equals(String.valueOf(inactiveId))) {
                        node = new NodeRelPic();
                        node.setId(Integer.valueOf(selectedLink.getSource()));
                        removedNodes.add(node);
                        removedLinks.add(selectedLink);
                    } else if (selectedLink.getTarget().equals(String.valueOf(inactiveId))) {
                        queue.offer(selectedLink.getSource());
                        node = new NodeRelPic();
                        node.setId(Integer.valueOf(selectedLink.getSource()));
                        removedNodes.add(node);
                        removedLinks.add(selectedLink);
                    }
                }

                // 往上继续查找
                while ((queueId = queue.poll()) != null) {
                    for (LinkRelPic selectedLink : selectedLinks) {
                        if (selectedLink.getTarget().equals(queueId)) {
                            queue.offer(selectedLink.getSource());
                            node = new NodeRelPic();
                            node.setId(Integer.valueOf(selectedLink.getSource()));
                            removedNodes.add(node);
                            removedLinks.add(selectedLink);
                        }
                    }
                }
            } else {
                throw new RuntimeException("direction is error");
            }
        }
        result.put("removedNodes", removedNodes);
        result.put("removedLinks", removedLinks);
        return result;
    }


    /**
     * 分页查询DAG列表
     *
     * @param dag dag
     * @return count
     */
    public int getAllDagByPageParamCount(TbClockworkDagPojo dag) {
        dag.setRoleName(StringUtil.spiltAndAppendSingleCitation(dag.getRoleName()));
        return dagMapper.countAllDagByPageParam(dag);
    }

    public List<TbClockworkDagPojo> getAllDagByPageParam(TbClockworkDagPojo dag, int pageNumber, int pageSize) {
        HashMap<String, Object> param = new HashMap<>();
        // 当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
        param.put("pageNumber", (pageNumber - 1) * pageSize);
        param.put("pageSize", pageSize);
        param.put("id", dag.getId());
        param.put("name", dag.getName());
        param.put("leaderTaskId", dag.getLeaderTaskId());
        param.put("leaderTaskName", dag.getLeaderTaskName());
        param.put("createUser", dag.getCreateUser());
        param.put("roleName", dag.getRoleName());
        return dagMapper.selectAllDagByPageParam(param);
    }

}
