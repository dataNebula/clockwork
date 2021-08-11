package com.creditease.adx.clockwork.common.util.graphutils;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.entity.graph.LinkPosition;
import com.creditease.adx.clockwork.common.entity.graph.NodePosition;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:34 下午 2020/12/29
 * @ Description：构建附带位置的图节点关系
 * @ Modified By：
 */
public class GraphPositionBuilder {

    /**
     * 构建位置图节点
     *
     * @param taskPojoList 节点
     * @return nodes
     */
    public static Set<NodePosition> buildNodes(List<TbClockworkTaskPojo> taskPojoList) {
        Set<NodePosition> result = new HashSet<>();
        if (CollectionUtils.isEmpty(taskPojoList)) {
            return result;
        }
        NodePosition nodes;
        for (TbClockworkTaskPojo task : taskPojoList) {
            int category = 0; // 触发方式: 0是依赖触发; 1是时间触发
            if (task.getTriggerMode() != null) {
                category = task.getTriggerMode();
            }
            nodes = new NodePosition(category, task.getName(), 0, task);
            result.add(nodes);
        }
        return result;
    }


    /**
     * 构建位置图关系
     *
     * @param taskRelations 关系
     * @return links,返会任务名称
     */
    public static Set<LinkPosition> buildLinks(List<TbClockworkTaskRelation> taskRelations) {
        Set<LinkPosition> result = new HashSet<>();
        if (CollectionUtils.isEmpty(taskRelations)) {
            return result;
        }

        LinkPosition links;
        for (TbClockworkTaskRelation taskRelation : taskRelations) {
            if (StringUtils.isBlank(taskRelation.getFatherTaskName())) {
                continue;
            }
            links = new LinkPosition(taskRelation.getFatherTaskName(), taskRelation.getTaskName(),
                    taskRelation.getFatherTaskName() + "|" + taskRelation.getTaskName());
            result.add(links);
        }
        return result;
    }


}
