package com.creditease.adx.clockwork.common.util.graphutils;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.entity.graph.LinkSankey;
import com.creditease.adx.clockwork.common.entity.graph.Node;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:30 下午 2020/12/22
 * @ Description：
 * @ Modified By：
 */
public class GraphSankeyBuilder {

    private List<Node> nodes;
    private List<LinkSankey> links;

    public GraphSankeyBuilder(List<TbClockworkTaskPojo> tasks, List<TbClockworkTaskRelation> relations) {
        this.nodes = nodes;
        this.links = links;
    }


    public Map<String, Object> drawDagGraph() {
        Map<String, Object> result = new HashMap<>();
        for (LinkSankey link : links) {

        }
        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }



    private Set<Node> buildNodes2(List<TbClockworkTaskPojo> tasks) {
        Set<Node> result = new HashSet<>();
        for (TbClockworkTaskPojo task : tasks) {
            Node node = new Node();
            node.setName(task.getName());
            long i = task.getLastEndTime().getTime() - task.getLastStartTime().getTime();



            result.add(node);
        }
        return result;
    }

    private Set<LinkSankey> buildLinks2(List<TbClockworkTaskRelation> taskRelations) {
        Set<LinkSankey> result = new HashSet<>();
        if (CollectionUtils.isEmpty(taskRelations)) {
            return result;
        }
        for (TbClockworkTaskRelation taskRelation : taskRelations) {
            if (StringUtils.isBlank(taskRelation.getFatherTaskName())) {
                continue;
            }
            LinkSankey linkSankey = new LinkSankey();
            linkSankey.setSource(taskRelation.getFatherTaskName());
            linkSankey.setTarget(taskRelation.getTaskName());
            linkSankey.setValue(1);
            result.add(linkSankey);
        }
        return result;
    }


}
