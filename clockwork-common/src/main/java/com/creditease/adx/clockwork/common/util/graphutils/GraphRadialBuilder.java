package com.creditease.adx.clockwork.common.util.graphutils;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.entity.graph.LinkRadial;
import com.creditease.adx.clockwork.common.entity.graph.NodeRadial;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 11:42 上午 2020/12/29
 * @ Description：构建辐射图
 * @ Modified By：
 */
public class GraphRadialBuilder {


    /**
     * 构建辐射图节点
     *
     * @param taskPojoList tasks
     * @return nodes
     */
    public static Set<NodeRadial> buildNodeForRadials(List<TbClockworkTaskPojo> taskPojoList) {
        Set<NodeRadial> result = new HashSet<>();
        if (CollectionUtils.isEmpty(taskPojoList)) {
            return result;
        }
        for (TbClockworkTaskPojo task : taskPojoList) {
            NodeRadial node = new NodeRadial();
            node.setId(task.getId());
            node.setName(task.getName());
            node.setStatus(task.getStatus());
            result.add(node);
        }
        return result;
    }


    /**
     * 构建辐射图关系
     *
     * @param taskRelations relations
     * @return links
     */
    public static Set<LinkRadial> buildLinkForByRadial(List<TbClockworkTaskRelation> taskRelations) {
        Set<LinkRadial> result = new HashSet<>();
        if (CollectionUtils.isEmpty(taskRelations)) {
            return result;
        }
        LinkRadial link;
        for (TbClockworkTaskRelation taskRelation : taskRelations) {
            if (taskRelation.getTaskId() == null || taskRelation.getFatherTaskId() == null) {
                continue;
            }
            link = new LinkRadial();
            link.setSource(String.valueOf(taskRelation.getFatherTaskId()));
            link.setTarget(String.valueOf(taskRelation.getTaskId()));
            result.add(link);
        }
        return result;
    }


}
