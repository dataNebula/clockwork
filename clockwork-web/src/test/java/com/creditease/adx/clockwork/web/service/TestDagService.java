package com.creditease.adx.clockwork.web.service;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.entity.graph.LinkRelPic;
import com.creditease.adx.clockwork.common.entity.graph.NodeRelPic;
import com.creditease.adx.clockwork.web.entity.SelectedParams;
import com.creditease.adx.clockwork.web.entity.*;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:36 下午 2020/12/3
 * @ Description：
 * @ Modified By：
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class TestDagService {

    private static final Logger LOG = LoggerFactory.getLogger(TestDagService.class);

    @Resource(name = "dagService")
    private IDagService dagService;

    @Test
    public void getTheChildTaskRelPicArrayTest() throws Exception {
        Integer taskId = 1687;

        Map<String, Object> theChildTaskRelPicArray = dagService.getTheChildTaskRelPicArray(taskId, new SelectedParams());
        System.out.println(JSONObject.toJSONString(theChildTaskRelPicArray));

    }


    @Test
    public void deleteNodesFromPicTest() throws Exception {

        String str = "{\"rootId\":\"9503\",\"selectedParams\":{\"selectedNodes\":[{\"name\":\"9503\"},{\"name\":9327},{\"name\":9198},{\"name\":9003},{\"name\":9167},{\"name\":8883}],\"selectedLinks\":[{\"source\":\"8883\",\"target\":\"9003\"},{\"source\":\"9327\",\"target\":\"9503\"},{\"source\":\"9198\",\"target\":\"9327\"},{\"source\":\"9003\",\"target\":\"9327\"},{\"source\":\"9167\",\"target\":\"9327\"}]},\"inactiveId\":\"9327\",\"direction\":\"1\"}";
        str = str.replaceAll("name", "id");
        DeleteNodesParams params = JSONObject.parseObject(str, DeleteNodesParams.class);

        Map<String, Object> interfaceResult = deleteNodesFromPic(params);
        System.out.println(JSONObject.toJSONString(interfaceResult));

    }


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
            } else if (direction == 1) { // 子节点关系关系
                // 直接链接的父节点直接设置
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

                // 往下继续查找
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


}
