package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.api.service.impl.GraphService;
import com.creditease.adx.clockwork.api.service.impl.TaskRelationService;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskRelationPojo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 13:59 2019-12-09
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestTaskRelationService {

    @Autowired
    private GraphService graphService;

    @Autowired
    private TaskRelationService taskRelationService;

    @Test
    public void getGraphAllTasksByTaskId() {

        int[] ids = new int[]{1687, 1694, 1695, 1696, 1697, 1698, 1779, 1780, 1781, 1782};
//        int[] ids = new int[]{1697};
        for (int id : ids) {
            long start = System.currentTimeMillis();
            System.out.println("=========== " + id + " ===========");

            List<TbClockworkTask> getRelationGraphTasks = graphService.getGraphAllTasksByTaskId(id);
            if (getRelationGraphTasks == null) {
                System.out.println(0);

            } else {
                System.out.println(getRelationGraphTasks.size());
                for (TbClockworkTask task : getRelationGraphTasks) {
                    System.out.println(task.getId());
                }
            }
            System.out.println("======================");
            System.out.println(System.currentTimeMillis() - start);
            System.out.println("======================/n");
        }


    }


    @Test
    public void getTaskAllChildrenNotIncludeSelf() {
        long start = System.currentTimeMillis();

        int taskId = 1687;
        List<TbClockworkTaskPojo> taskAllChildrenNotIncludeSelf
                = taskRelationService.getTaskAllChildrenNotIncludeSelf(taskId);
        for (TbClockworkTaskPojo taskPojo : taskAllChildrenNotIncludeSelf) {
            System.out.println(taskPojo.getId() + " ->" + taskPojo.getName());
        }

        System.out.println("======================");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("======================/n");

    }


    @Test
    public void getTaskAllChildrenIncludeSelf() {
        long start = System.currentTimeMillis();
        int taskId = 1687;
        List<TbClockworkTaskPojo> taskAllChildrenIncludeSelf
                = taskRelationService.getTaskAllChildrenIncludeSelf(taskId);
        for (TbClockworkTaskPojo taskPojo : taskAllChildrenIncludeSelf) {
            System.out.println(taskPojo.getId() + " ->" + taskPojo.getName());
        }

        System.out.println("======================");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("======================/n");

    }

    @Test
    public void getTaskDirectlyChildrenNotIncludeSelf() {
        long start = System.currentTimeMillis();
        int taskId = 1687;
        List<TbClockworkTaskPojo> taskAllChildrenIncludeSelf
                = taskRelationService.getTaskDirectlyChildrenNotIncludeSelf(taskId);
        for (TbClockworkTaskPojo taskPojo : taskAllChildrenIncludeSelf) {
            System.out.println(taskPojo.getId() + " ->" + taskPojo.getName());
        }

        System.out.println("======================");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("======================/n");

    }

    @Test
    public void getTaskDirectlyFatherNotIncludeSelf() {
        long start = System.currentTimeMillis();
        int taskId = 1694;
        List<TbClockworkTaskPojo> taskAllChildrenIncludeSelf
                = taskRelationService.getTaskDirectlyFatherNotIncludeSelf(taskId);
        for (TbClockworkTaskPojo taskPojo : taskAllChildrenIncludeSelf) {
            System.out.println(taskPojo.getId() + " ->" + taskPojo.getName());
        }

        System.out.println("======================");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("======================/n");
    }

    @Test
    public void findDirectlyRelationTaskIdsIncludeSelf() {
        long start = System.currentTimeMillis();
        int taskId = 1687;
        List<Integer> includeSelf = taskRelationService.findDirectlyRelationTaskIdsIncludeSelf(taskId);
        System.out.println(includeSelf);

        System.out.println("======================");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("======================/n");

    }


    @Test
    public void findTaskDirectlyFather() {
        long start = System.currentTimeMillis();
        int taskId = 1694;
        List<TbClockworkTaskRelationPojo> taskAllChildrenIncludeSelf
                = taskRelationService.findTaskDirectlyFather(taskId);
        for (TbClockworkTaskRelationPojo taskPojo : taskAllChildrenIncludeSelf) {
            System.out.println(taskPojo.getId() + " ->" + taskPojo.getTaskName());
        }

        System.out.println("======================");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("======================/n");
    }

    @Test
    public void findTaskDirectlyChildren() {
        long start = System.currentTimeMillis();
        int taskId = 1687;
        List<TbClockworkTaskRelationPojo> taskDirectlyChildren = taskRelationService.findTaskDirectlyChildren(taskId);
        for (TbClockworkTaskRelationPojo taskPojo : taskDirectlyChildren) {
            System.out.println(taskPojo.getId() + " ->" + taskPojo.getTaskName());
        }

        System.out.println("======================");
        System.out.println(System.currentTimeMillis() - start);
        System.out.println("======================/n");
    }





}
