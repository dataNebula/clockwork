package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDagExample;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkDagMapper;
import it.sauronsoftware.cron4j.Predictor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 上午11:59 2020/12/6
 * @ Description：Test
 * @ Modified By：
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestDagService {

    private static final Logger LOG = LoggerFactory.getLogger(TestDagService.class);

    @Autowired
    private IDagService dagService;


    @Autowired
    private ITaskService taskService;

    @Autowired
    private TbClockworkDagMapper tbClockworkDagMapper;

    @Autowired
    private ITaskRelationService taskRelationService;

    @Test
    public void initTaskDagIdTest() {
        long start = System.currentTimeMillis();
        boolean result = dagService.initTaskDagId();
        LOG.info("result = {}, 耗时：{}", result, (System.currentTimeMillis() - start));
    }

    @Test
    public void cleanEmptyDagInfoTest() {
        long start = System.currentTimeMillis();
        List<Integer> taskIds = dagService.cleanEmptyDagInfo();
        LOG.info("result = {}, 耗时：{}", taskIds, (System.currentTimeMillis() - start));
    }


    @Test
    public void buildDagIdForTaskIdTest() {
        long start = System.currentTimeMillis();
        int taskId = 26802;
        int dagId = dagService.buildDagIdForTaskId(taskId);
        LOG.info("dagId = {}, 耗时：{}", dagId, (System.currentTimeMillis() - start));
    }


    @Test
    public void buildDagIdForTaskIdsTest() {
        long start = System.currentTimeMillis();
        List<Integer> taskIds = new ArrayList<>();
        taskIds.add(26801);
        taskIds.add(26802);
        dagService.buildDagIdForTaskIds(taskIds);
        LOG.info("耗时：{}", (System.currentTimeMillis() - start));
    }


    @Test
    public void buildDagIdForTaskUpdateTest() {
        long start = System.currentTimeMillis();
        int taskId = 26801;

        List<Integer> oldDirectlyRelatedTask = new ArrayList<>();
//        oldDirectlyRelatedTask.add(26801);
//        oldDirectlyRelatedTask.add(26802);

        oldDirectlyRelatedTask = taskRelationService.findDirectlyRelationTaskIdsNotIncludeSelf(taskId);
        boolean result = dagService.buildDagIdForTaskUpdate(taskId, oldDirectlyRelatedTask);

        LOG.info("result = {}, 耗时：{}", result, (System.currentTimeMillis() - start));
    }


    @Test
    public void refreshDagInfoByIdTest() {
        TbClockworkDagExample example = new TbClockworkDagExample();

        // refreshDagInfoById
        example.createCriteria().andTaskCountGreaterThan(1);
        example.setOrderByClause("task_count desc");
        List<TbClockworkDag> tbClockworkDags = tbClockworkDagMapper.selectByExample(example);
        for (TbClockworkDag tbClockworkDag : tbClockworkDags) {
            dagService.refreshDagInfoById(tbClockworkDag.getId());
        }

        // Get
        tbClockworkDags = tbClockworkDagMapper.selectByExample(example);
        for (TbClockworkDag tbClockworkDag : tbClockworkDags) {
            TbClockworkTaskPojo task = taskService.getTaskById(tbClockworkDag.getLeaderTaskId());
            if (task != null) {
                System.out.println(task.getId() + ": " + task.getCronExp());
            } else {
                System.out.println("==========" + tbClockworkDag.getLeaderTaskId());
            }
        }


    }


    @Test
    public void refreshDagInfoByIdTest2() {
        TbClockworkDagExample example = new TbClockworkDagExample();
        example.createCriteria().andTaskCountGreaterThan(1).andLeaderTaskIdIsNotNull();
        example.setOrderByClause("task_count desc");

        List<String> daDay = new ArrayList<>();
        List<String> day = new ArrayList<>();
        List<String> xiDay = new ArrayList<>();

        // Get
        List<TbClockworkDag> tbClockworkDags = tbClockworkDagMapper.selectByExample(example);
        List<Integer> leaderTaskIds = tbClockworkDags.stream().map(TbClockworkDag::getLeaderTaskId).collect(Collectors.toList());
        List<TbClockworkTask> TbClockworkTaskPojos = taskService.getTaskByTaskIds(leaderTaskIds);
        for (TbClockworkTask task : TbClockworkTaskPojos) {
            if (task != null) {
                String str = task.getId() + ": " + task.getCronExp();
                Predictor p = new Predictor(task.getCronExp());
                long first = p.nextMatchingTime();
                long second = p.nextMatchingTime();
                if (second - first > 86400000) {
                    daDay.add(str);
                } else if (second - first == 86400000) {
                    day.add(str);
                } else {
                    xiDay.add(str);
                }
            } else {
                System.out.println("==========" + task);
            }
        }

        System.out.println("==============");
        for (String s : day) {
            System.out.println("等：" + s);
        }
        for (String s : daDay) {
            System.out.println("大：" + s);
        }
        for (String s : xiDay) {
            System.out.println("小：" + s);
        }


    }
}
