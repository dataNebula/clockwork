package com.creditease.adx.clockwork.api.service.impl;

import com.creditease.adx.clockwork.api.service.IDagCheckService;
import com.creditease.adx.clockwork.api.service.ITaskService;
import com.creditease.adx.clockwork.common.entity.DagCheckRangeInfo;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagRangeCheck;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.entity.graph.LinkPosition;
import com.creditease.adx.clockwork.common.entity.graph.NodePosition;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.graphutils.DagGraphUtils;
import com.creditease.adx.clockwork.common.util.graphutils.GraphPositionBuilder;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkDagMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkDagRangeCheckMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName: DagCheckService
 * @Author: ltb
 * @Date: 2021/3/11:10:38 上午
 * @Description:
 */
@Service(value = "dagCheckService")
public class DagCheckService implements IDagCheckService, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DagCheckService.class);

    // 任务放到这个队列中进行成环检测
    private final BlockingQueue<HashMap<Integer, String>> dagCheckQueue = new LinkedBlockingQueue<>();
    @Autowired
    private DagService dagService;
    @Autowired
    private ITaskService taskService;
    @Autowired
    private GraphService graphService;
    @Autowired
    private TbClockworkDagRangeCheckMapper dagRangeCheckMapper;
    @Autowired
    private TbClockworkDagMapper dagMapper;

    @PostConstruct
    public void setup() {
        new Thread(this).start();
        LOG.info("[DagCheckService-setup]The thread that process need be executed check dag circle is started");
    }

    @Override
    public void run() {
        while (true) {
            long start = System.currentTimeMillis();
            try {
                HashMap<Integer, String> poll = null;
//                Thread.sleep(10000);
                // 获取 等待队列中的任务
                poll = dagCheckQueue.poll(5, TimeUnit.SECONDS);
                if (poll == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("[DagCheckService-run] There were no dag need to be executed,skip current loop!");
                    }
                    continue;
                }
                poll.forEach((k, v) -> {
                            LOG.info("DagCheckService-run, dag={}, userName={}，cost= {}ms.",
                                    k, v, System.currentTimeMillis() - start);
                            checkDagById(k, v);
                        }
                );
            } catch (Exception e) {
                LOG.error("DagCheckService-run, distribute Error {}.", e.getMessage(), e);
                // 下发不成功，重新放入队列等待下发
            }
        }


    }

    @Override
    public DagCheckRangeInfo checkDagById(Integer dagId, String userName) {
        DagCheckRangeInfo dagCheckRangeInfo = new DagCheckRangeInfo(false, null);
        // 获取节点以及关系
        List<TbClockworkTaskPojo> taskPojoList = taskService.getTasksByDagIdWhereTaskIsOnline(dagId);
        if (CollectionUtils.isEmpty(taskPojoList)){
            throw new RuntimeException("该dagID没有任务，可能已经重新构建了新的dag, 请重新选择！");
        }
        if (taskPojoList.size() == 1) {
            return dagCheckRangeInfo;
        }
        List<TbClockworkTaskRelation> relations
                = graphService.getGraphAllRelationByTaskId(taskPojoList.get(0).getId());
        Set<LinkPosition> links = GraphPositionBuilder.buildLinks(relations);
        Set<NodePosition> nodes = GraphPositionBuilder.buildNodes(taskPojoList);

        DagGraphUtils dagGraph = new DagGraphUtils(new ArrayList<NodePosition>(nodes), new ArrayList<LinkPosition>(links));
        List<String> circle = dagGraph.dfsToFindCircle();
        if (CollectionUtils.isEmpty(circle)) {
            LOG.info("[DagCheckService-checkDagById] this dag not has circle . dag id = {}", dagId);
        } else {
            LOG.error("[DagCheckService-checkDagById] this dag has circle . dag id = {}, infos = {}",
                    dagId, circle);
//          这条数据插入数据库中记录
            dagCheckRangeInfo.setIsrange(true);
            dagCheckRangeInfo.setTaskInfos(circle.toString());
            TbClockworkDagRangeCheck rangeCheck = new TbClockworkDagRangeCheck();
            rangeCheck.setDagId(dagId);
            rangeCheck.setOperator(userName);
            rangeCheck.setIsRange(true);
            rangeCheck.setTaskInfo(circle.toString());
            rangeCheck.setCreateTime(new Date());
            rangeCheck.setUpdateTime(new Date());
            int i = dagRangeCheckMapper.insertSelective(rangeCheck);
            if (i < 1) {
                LOG.error("[DagCheckService-checkDagById]insert error. dag id = {}, userName = {}",
                        dagId, userName);
            }
        }
        return dagCheckRangeInfo;
    }


    //  手动重新刷新说有成环检测任务
    @Override
    public boolean checkAllDags(String userName) {
//        如果刷新dag检测任务的队列中不是空的，就不需要重新全部检测
        if (dagCheckQueue.size() != 0) {
            LOG.info("[DagCheckService-checkAllDags] dagCheckQueue is not empty, dont need check all dag");
            return false;
        }
//      获得所有的不为0的dag任务，将他们组成一个map，传入队列中
        dagService.cleanEmptyDagInfo();
        TbClockworkDagExample dagExample = new TbClockworkDagExample();
        dagExample.createCriteria().andTaskCountGreaterThan(1);
        List<TbClockworkDag> tbClockworkDags = dagMapper.selectByExample(dagExample);
        if (CollectionUtils.isEmpty(tbClockworkDags)) {
            LOG.info("[DagCheckService-checkAllDags] no dag need check.");
            return true;
        }

        tbClockworkDags.stream().forEach(dag -> {
            HashMap<Integer, String> dagsMap = new HashMap<>();
            dagsMap.putIfAbsent(dag.getId(), userName);
            dagCheckQueue.offer(dagsMap);
        });

        LOG.info("[DagCheckService-checkAllDags] add dags to dagCheckQueue,dags= {}, size={}, userName = {}",
                tbClockworkDags.stream().map(TbClockworkDag::getId).collect(Collectors.toList()), tbClockworkDags.size(), userName);

        return true;
    }
}
