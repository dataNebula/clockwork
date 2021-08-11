package com.creditease.adx.clockwork.common.util.graphutils;

import com.creditease.adx.clockwork.common.entity.graph.LinkPosition;
import com.creditease.adx.clockwork.common.entity.graph.NodePosition;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 作用: 绘制有向无环图(DAG图)
 * <p>
 * 说明:
 * 配合Echarts的simple graph使用，地址见http://echarts.baidu.com/examples/editor.html?c=graph-simple
 * 用来进行点的坐标定位
 * <p>
 * 使用条件: 图必须是一个有向无环图(DAG图)
 * <p>
 * 调用方式: return new DagGraphUtil(points, links).drawDagGraph()
 * <p>
 * 参数:
 * points的格式为 ['点1','点2','点3','点4']
 * links的格式为 [{"source":"点1","target":"点2"},{"source":"点2","target":"点3"},{"source":"点3","target":"点4"}]
 * <p>
 * 返回结果:
 * {
 * "points": [{"name": "点1","x":100,"y":100},{"name":"点2","x":200,"y":200}, ...],
 * "links": [{"source":"点1","target":"点2"},{"source":"点2","target":"点3"}, ...]
 * }
 * <p>
 * <p>
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:43 下午 2020/8/10
 * @ Description：主要算法: 拓扑排序
 * @ Modified By：
 */
@Getter
public class DagGraphUtils {

    private final int MAX_LENGTH_X = 1000;
    private final int MAX_LENGTH_Y = 800;
    private int width;
    private Map<String, Integer> nodeDegreeMap;
    private Map<String, Integer> xPosition;
    private Map<String, Integer> yPosition;
    private List<Integer> height;
    private List<NodePosition> nodes;
    private List<LinkPosition> links;
    private List<String> circleInfoList = new LinkedList<>();

    public DagGraphUtils(List<NodePosition> nodes, List<LinkPosition> links) {
        this.nodes = nodes;
        this.links = links;
        nodeDegreeMap = new HashMap<>(nodes.size());
        xPosition = new HashMap<>(nodes.size());
        yPosition = new HashMap<>(nodes.size());
        width = 0;
        height = new ArrayList<>();

        // 初始化所有节点node, 起始值为0
        for (NodePosition node : nodes) {
            nodeDegreeMap.put(node.getName(), 0);
        }
        // 计算节点入度，有去往这个节点的，就把这个节点的入度+1
        for (LinkPosition link : links) {
            String target = link.getTarget();
            addMapValue(nodeDegreeMap, target, 1);
        }
    }

    public Map<String, Object> getDagGraphWithLevel() {
        Map<String, Object> result = new HashMap<>(2);
        dfs(0);
        for (NodePosition node : nodes) {
            node.setLevel(yPosition.get(node.getName()));
        }
        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }

    // 设置node的x坐标，y坐标
    public Map<String, Object> drawDagGraph() {
        Map<String, Object> result = new HashMap<>(2);
        dfs(0);
        for (NodePosition node : nodes) {
            // 计算y坐标单位， 图宽/该层要放多少个点，height是个list(每层的节点数）
            int yUnitLength = MAX_LENGTH_Y / height.get(xPosition.get(node.getName()));
            // 计算y坐标，每层位置*y单位，i从0开始，需要加半个单位，不能贴边
            int y = yPosition.get(node.getName()) * yUnitLength + yUnitLength / 2;
            // 计算x坐标单位，图高度/一共要放多少层
            int xUnitLength = MAX_LENGTH_X / width;
            // 计算x坐标, 第几层*x单位
            int x = xPosition.get(node.getName()) * xUnitLength + xUnitLength / 2;
            node.setX(x);
            node.setY(y);
        }
        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }

    // 按照层数从上到下摆node，按照task名称从左往右摆node
    // 从入度为0的上层开始往下遍历，处理完一层，把当前层节点的入度变为-1
    private void dfs(int depth) {
        List<String> currentLevelPoints = new ArrayList<>();
        for (String key : nodeDegreeMap.keySet()) {
            // 查找入度为0的点加入当前层节点，并且把这些节点的入度变成-1,这样下次判断就不会这些入度为-1的点当成顶层
            if (nodeDegreeMap.get(key) == 0) {
                currentLevelPoints.add(key);
                addMapValue(nodeDegreeMap, key, -1);
            }
        }
        // 如果没有入度为0的点，直接返回，证明遍历到底了
        if (currentLevelPoints.isEmpty()) {
            return;
        }
        // Graph高度+1（左边开始计算，存放位置：第一个为1）
        width = depth + 1;
        // 每一层要放多少个点（从上往下放多少个节点）
        height.add(currentLevelPoints.size());
        // 相同层的点按照名称排序
        Collections.sort(currentLevelPoints, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        // 生成当前层node的x、y坐标
        for (int i = 0, len = currentLevelPoints.size(); i < len; i++) {
            String node = currentLevelPoints.get(i);
            // 每层中放在第几位
            yPosition.put(node, i);
            // 放在第几层中
            xPosition.put(node, depth);
            // 处理完父节点，子节点的入度-1
            for (LinkPosition link : links) {
                if (link.getSource().equals(node)) {
                    addMapValue(nodeDegreeMap, link.getTarget(), -1);
                }
            }
        }
        dfs(depth + 1);
    }

    // 如果map包含这个node，增加入度,否则加入map
    private void addMapValue(Map<String, Integer> map, String key, Integer value) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + value);
        } else {
            map.put(key, value);
        }
    }

    //  根据三色去深度遍历图，将所有成环的点输出到一个list中
    public List<String> dfsToFindCircle() {
        this.circleInfoList.clear();
        //    1.找到入度为0的点
        List<NodePosition> zeroDegressNodes = getZeroDegressNodes();
        //    2.遍历
        zeroDegressNodes.forEach(
                firstNode -> {
                    if (firstNode.getColor() == 1) {
                        dfsVisit(firstNode);
                    }
                }
        );
        return this.circleInfoList;
    }

    //  进行循环把所有节点变成黑色为止
    private void dfsVisit(NodePosition node) {
        node.setColor(2);
//        找到这个节点的所有子节点
        List<NodePosition> childs = getChildrenNodes(node);
        for (int i = 0; i < childs.size(); i++) {
            if (childs.get(i).getColor() == 1) {
                dfsVisit(childs.get(i));
            }
//            如果一个孩子节点的颜色已经变化了，说明这个节点已经成环了，成环节点是当前节点和这个灰色节点
            if (childs.get(i).getColor() == 2) {
                String cricleInfo = getCricleInfo(node, childs.get(i));
                this.circleInfoList.add(cricleInfo);
            }
        }
        node.setColor(3);
    }

    private List<NodePosition> getChildrenNodes(NodePosition node) {
        List<String> childLinksName =
                this.links.stream().
                        filter(linkPosition -> node.getName().equals(linkPosition.getSource()))
                        .map(linkPosition -> linkPosition.getTarget())
                        .collect(Collectors.toList());
        List<NodePosition> childNodes =
                this.nodes.stream().
                        filter(nodePosition -> childLinksName.contains(nodePosition.getName()))
                        .collect(Collectors.toList());
        return childNodes;

    }

    private List<NodePosition> getZeroDegressNodes() {
        List<String> zeroName = this.nodeDegreeMap.entrySet().stream().filter(e -> e.getValue() == 0).map(e -> e.getKey()).
                collect(Collectors.toList());
        if (null == zeroName) {
            throw new RuntimeException("error graph");
        }
        List<NodePosition> zeroNodes =
                this.nodes.stream().
                        filter(nodePosition -> zeroName.contains(nodePosition.getName()))
                        .collect(Collectors.toList());
        return zeroNodes;
    }

    private String getCricleInfo(NodePosition node, NodePosition node1) {
        StringBuilder taskInfo = new StringBuilder();
        taskInfo.append("task name: ").append(node.getName()).append(",")
                .append("task id: ").append(node.getTask().getId()).append("||")
                .append("task name: ").append(node1.getName()).append(",")
                .append("task id: ").append(node1.getTask().getId());
        return taskInfo.toString();
    }

}
