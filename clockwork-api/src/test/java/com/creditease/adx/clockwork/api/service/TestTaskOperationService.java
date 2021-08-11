package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.api.service.impl.TaskOperationService;
import com.creditease.adx.clockwork.common.entity.TaskGroupAndTasks;
import com.creditease.adx.clockwork.common.enums.TaskSource;
import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskGroupPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 9:51 上午 2020/12/21
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TestTaskOperationService {

    @Autowired
    private TaskOperationService taskOperationService;


    @Test
    public void addTaskListTest() {

        TaskGroupAndTasks taskGroupAndTasks = new TaskGroupAndTasks();

        List<TbClockworkTaskPojo> tasks = new ArrayList<>();
        TbClockworkTaskGroupPojo taskGroup = new TbClockworkTaskGroupPojo();
        taskGroup.setName("g_04");
        taskGroup.setDescription("g_04");

        TbClockworkTaskPojo task = new TbClockworkTaskPojo();
        task.setExternalSystemTaskId("666");    // 外部系统id
        task.setAliasName("taskListTest_01");
        task.setName("taskListTest_01");
        task.setDescription("taskListTest_01");
        task.setLocation("/user/adx/clockwork/dfs/shell/");
        task.setScriptName("test.sh");
        task.setRunEngine("hive");
        task.setTriggerMode(TaskTriggerModel.DEPENDENCY.getValue());
        task.setTaskFatherIdsCrossTaskGroup("12199,12188"); // 跨组依赖
        task.setCreateUser("xuandongtang");
        task.setOperatorName("xuandongtang");
        task.setIsPrivate(true);
        task.setEmailList("xuandongtang@clockwork.com");
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setNodeGid(1);
        task.setSource(TaskSource.ADX_CLOCKWORK.getValue());
        tasks.add(task);

        TbClockworkTaskPojo task2 = new TbClockworkTaskPojo();
        task2.setExternalSystemTaskId("667");    // 外部系统id
        task2.setExternalSystemTaskDependencyId("666"); // 依赖的外部系统id，本组依赖
        task2.setAliasName("taskListTest_02");
        task2.setName("taskListTest_02");
        task2.setDescription("taskListTest_02");
        task2.setLocation("/user/adx/clockwork/dfs/shell/");
        task2.setScriptName("test.sh");
        task2.setRunEngine("hive");
        task2.setTriggerMode(TaskTriggerModel.DEPENDENCY.getValue());
        task2.setCreateUser("xuandongtang");
        task2.setOperatorName("xuandongtang");
        task2.setIsPrivate(true);
        task2.setEmailList("xuandongtang@clockwork.com");
        task2.setCreateTime(new Date());
        task2.setUpdateTime(new Date());
        task2.setNodeGid(1);
        task2.setSource(TaskSource.ADX_CLOCKWORK.getValue());
        tasks.add(task2);

        // taskGroupAndTasks
        taskGroupAndTasks.setOperator("xuandongtang@clockwork.com");
        taskGroupAndTasks.setTaskGroup(taskGroup);
        taskGroupAndTasks.setTasks(tasks);

        taskOperationService.addTaskList(taskGroupAndTasks);

    }




    @Test
    public void updateTaskListTest() {

        TaskGroupAndTasks taskGroupAndTasks = new TaskGroupAndTasks();

        List<TbClockworkTaskPojo> tasks = new ArrayList<>();
        TbClockworkTaskGroupPojo taskGroup = new TbClockworkTaskGroupPojo();
        taskGroup.setId(557);
        taskGroup.setName("g_04");
        taskGroup.setDescription("g_04");

        TbClockworkTaskPojo task = new TbClockworkTaskPojo();
        task.setId(12206);
        task.setGroupId(557);
        task.setExternalSystemTaskId("666");    // 外部系统id
        task.setAliasName("taskListTest_01");
        task.setName("taskListTest_01");
        task.setDescription("taskListTest_01");
        task.setLocation("/user/adx/clockwork/dfs/shell/");
        task.setScriptName("test.sh");
        task.setRunEngine("hive");
        task.setTriggerMode(TaskTriggerModel.DEPENDENCY.getValue());
        task.setTaskFatherIdsCrossTaskGroup("12201"); // 跨组依赖
        task.setCreateUser("xuandongtang");
        task.setOperatorName("xuandongtang");
        task.setIsPrivate(true);
        task.setEmailList("xuandongtang@clockwork.com");
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setNodeGid(1);
        task.setSource(TaskSource.ADX_CLOCKWORK.getValue());
        tasks.add(task);

        TbClockworkTaskPojo task2 = new TbClockworkTaskPojo();
        task2.setId(12207);
        task2.setGroupId(557);
        task2.setExternalSystemTaskId("667");    // 外部系统id
        task2.setExternalSystemTaskDependencyId("666"); // 依赖的外部系统id，本组依赖
        task2.setAliasName("taskListTest_02");
        task2.setName("taskListTest_02");
        task2.setDescription("taskListTest_02");
        task2.setLocation("/user/adx/clockwork/dfs/shell/");
        task2.setScriptName("test.sh");
        task2.setRunEngine("hive");
        task2.setTriggerMode(TaskTriggerModel.DEPENDENCY.getValue());
        task2.setCreateUser("xuandongtang");
        task2.setOperatorName("xuandongtang");
        task2.setIsPrivate(true);
        task2.setEmailList("xuandongtang@clockwork.com");
        task2.setCreateTime(new Date());
        task2.setUpdateTime(new Date());
        task2.setNodeGid(1);
        task2.setSource(TaskSource.ADX_CLOCKWORK.getValue());
        tasks.add(task2);

        // taskGroupAndTasks
        taskGroupAndTasks.setOperator("xuandongtang@clockwork.com");
        taskGroupAndTasks.setTaskGroup(taskGroup);
        taskGroupAndTasks.setTasks(tasks);

        taskOperationService.updateTaskList(taskGroupAndTasks);


    }


}
