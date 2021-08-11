package com.creditease.adx.clockwork.common.util.graphutils;

import com.creditease.adx.clockwork.common.entity.graph.LinkPosition;
import com.creditease.adx.clockwork.common.entity.graph.NodePosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:43 下午 2020/8/10
 * @ Description：
 * @ Modified By：
 */
public class GraphSearchTaskAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GraphSearchTaskAdapter.class);

    private Set<NodePosition> nodesSet = new HashSet<NodePosition>(2000);
    private Set<LinkPosition> linksSet = new HashSet<LinkPosition>(2000);
    private Set<NodePosition> returnNodesSet = new HashSet<NodePosition>(2000);
    private Set<LinkPosition> returnLinksSet = new HashSet<LinkPosition>(2000);
    private Set<LinkPosition> firstLevelLinksSet = new HashSet<LinkPosition>(2000);
    private NodePosition currentNode;
    private LinkPosition currentLink;
    private NodePosition targetNode;
    private int maxDeepLevel = 4;
    private int currentDeepLevel = 0;
    private boolean isChildSearching = false;
    /**
     * The Visited.
     */
    List<NodePosition> visited = new ArrayList<NodePosition>();

    /**
     * default is parenting searching and deep level is 4
     *
     * @param nodesSet         the nodes set
     * @param linksSet         the links set
     * @param maxDeepLevel     the max deep level
     * @param isChildSearching the is child searching
     */
    public GraphSearchTaskAdapter(Set<NodePosition> nodesSet, Set<LinkPosition> linksSet, int maxDeepLevel, boolean isChildSearching) {
        this.nodesSet = nodesSet;
        this.linksSet = linksSet;
        this.maxDeepLevel = maxDeepLevel;
        this.isChildSearching = isChildSearching;
    }

    /**
     * 广度优先带深度级别图搜索
     *
     * @param startNodeName the start node name
     */
    public void widthFirstSeachWithDeepLevel(String startNodeName) {
        NodePosition node = getNodesByName(startNodeName);
        if (null == node) {
            return;
        }
        returnNodesSet.add(node);
        assumeDistanceLoop(this.maxDeepLevel, node);


    }

    /**
     * get all the next Level Nodes when doing @methods asumeDistanceLoop()
     *
     * @param node
     * @return
     */
    private Set<NodePosition> getNextLevelNodes(NodePosition node, boolean isStoreNode) {
        Set<NodePosition> currentLevelNodeSet = new HashSet<>();

        if (null == node) {
            LOG.error("getNextLevelNodes is found error the input node is null.");
        } else {

            if (isStoreNode) {
                returnNodesSet.add(node);
            }
            for (LinkPosition link : linksSet) {

                if (isChildSearching) {
                    if (node.getName().equalsIgnoreCase(link.getSource())) {
                        currentLevelNodeSet.add(getNodesByName(link.getTarget()));
                        returnLinksSet.add(link);
                    }

                } else {
                    if (node.getName().equalsIgnoreCase(link.getTarget())) {
                        currentLevelNodeSet.add(getNodesByName(link.getSource()));
                        returnLinksSet.add(link);
                    }
                }
            }
        }
        return currentLevelNodeSet;
    }

    /**
     * Here is a trick
     * 1: when finding the shortest distance between nodes in the non-Direction graph you can using {matrix:floyd} things but...you know..
     * 2: here we can assume that we can already know the distance .then -> loop -> find
     *
     * @param assumeDistance the assume distance
     * @param startNode      the start node
     * @return
     */
    public void assumeDistanceLoop(int assumeDistance, NodePosition startNode/*, Nodes endNode*/) {//>2
        /**
         * storage all the nodes by different level
         * in order
         */
        ArrayList<Set<NodePosition>> levelNodesStore = new ArrayList<>(assumeDistance);

        for (int i = 0; i < assumeDistance; i++) {
            /**
             * to store current node set
             */
            Set<NodePosition> currentLevelNodeSet = new HashSet<>();

            /**
             * first level do startNode things
             */
            if (i == 0) {
                currentLevelNodeSet.addAll(getNextLevelNodes(startNode, false));
            }
            /**
             * here is not first level then loop last level nodes
             * finding next nodes of last level nodes
             */
            else {
                Set<NodePosition> lastLevelNodeStore = levelNodesStore.get(i - 1);
                for (NodePosition tmpNode : lastLevelNodeStore) {
                    currentLevelNodeSet.addAll(getNextLevelNodes(tmpNode, true));
                }
            }
            /**
             * adding to levelNodesStore
             */
            levelNodesStore.add(currentLevelNodeSet);
            returnNodesSet.addAll(currentLevelNodeSet);
        }
    }

    /**
     * Gets nodes by name.
     *
     * @param name the name
     * @return the nodes by name
     */
    public NodePosition getNodesByName(String name) {
        NodePosition node = null;
        for (NodePosition tmp : nodesSet) {
            if (tmp.getName().equalsIgnoreCase(name)) {
                node = tmp;
                break;
            }
        }
        return node;
    }

    /**
     * Gets nodes set.
     *
     * @return the nodes set
     */
    public Set<NodePosition> getNodesSet() {
        return nodesSet;
    }

    /**
     * Sets nodes set.
     *
     * @param nodesSet the nodes set
     */
    public void setNodesSet(Set<NodePosition> nodesSet) {
        this.nodesSet = nodesSet;
    }

    /**
     * Gets links set.
     *
     * @return the links set
     */
    public Set<LinkPosition> getLinksSet() {
        return linksSet;
    }

    /**
     * Sets links set.
     *
     * @param linksSet the links set
     */
    public void setLinksSet(Set<LinkPosition> linksSet) {
        this.linksSet = linksSet;
    }

    /**
     * Gets return nodes set.
     *
     * @return the return nodes set
     */
    public Set<NodePosition> getReturnNodesSet() {
        return returnNodesSet;
    }

    /**
     * Sets return nodes set.
     *
     * @param returnNodesSet the return nodes set
     */
    public void setReturnNodesSet(Set<NodePosition> returnNodesSet) {
        this.returnNodesSet = returnNodesSet;
    }

    /**
     * Gets return links set.
     *
     * @return the return links set
     */
    public Set<LinkPosition> getReturnLinksSet() {
        return returnLinksSet;
    }

    /**
     * Sets return links set.
     *
     * @param returnLinksSet the return links set
     */
    public void setReturnLinksSet(Set<LinkPosition> returnLinksSet) {
        this.returnLinksSet = returnLinksSet;
    }

    /**
     * Gets current node.
     *
     * @return the current node
     */
    public NodePosition getCurrentNode() {
        return currentNode;
    }

    /**
     * Sets current node.
     *
     * @param currentNode the current node
     */
    public void setCurrentNode(NodePosition currentNode) {
        this.currentNode = currentNode;
    }

    /**
     * Gets target node.
     *
     * @return the target node
     */
    public NodePosition getTargetNode() {
        return targetNode;
    }

    /**
     * Sets target node.
     *
     * @param targetNode the target node
     */
    public void setTargetNode(NodePosition targetNode) {
        this.targetNode = targetNode;
    }

    /**
     * Gets max deep level.
     *
     * @return the max deep level
     */
    public int getMaxDeepLevel() {
        return maxDeepLevel;
    }

    /**
     * Sets max deep level.
     *
     * @param maxDeepLevel the max deep level
     */
    public void setMaxDeepLevel(int maxDeepLevel) {
        this.maxDeepLevel = maxDeepLevel;
    }

    /**
     * Gets current link.
     *
     * @return the current link
     */
    public LinkPosition getCurrentLink() {
        return currentLink;
    }

    /**
     * Sets current link.
     *
     * @param currentLink the current link
     */
    public void setCurrentLink(LinkPosition currentLink) {
        this.currentLink = currentLink;
    }

    /**
     * Gets current deep level.
     *
     * @return the current deep level
     */
    public int getCurrentDeepLevel() {
        return currentDeepLevel;
    }

    /**
     * Sets current deep level.
     *
     * @param currentDeepLevel the current deep level
     */
    public void setCurrentDeepLevel(int currentDeepLevel) {
        this.currentDeepLevel = currentDeepLevel;
    }

    /**
     * Is child searching boolean.
     *
     * @return the boolean
     */
    public boolean isChildSearching() {
        return isChildSearching;
    }

    /**
     * Sets child searching.
     *
     * @param childSearching the child searching
     */
    public void setChildSearching(boolean childSearching) {
        isChildSearching = childSearching;
    }

    /**
     * Gets first level links set.
     *
     * @return the first level links set
     */
    public Set<LinkPosition> getFirstLevelLinksSet() {
        return firstLevelLinksSet;
    }

    /**
     * Sets first level links set.
     *
     * @param firstLevelLinksSet the first level links set
     */
    public void setFirstLevelLinksSet(Set<LinkPosition> firstLevelLinksSet) {
        this.firstLevelLinksSet = firstLevelLinksSet;
    }
}

