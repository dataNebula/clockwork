package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.RelationChildrenFather;
import com.creditease.adx.clockwork.dao.mapper.TaskRelationMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 上午11:59 2020/12/6
 * @ Description：Test
 * @ Modified By：
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestGraphService {

    @Autowired
    private IGraphService graphService;

    @Autowired
    private TaskRelationMapper taskRelationMapper;

    /**
     * 测试Method：getGraphAllTasksByTaskId()耗时
     */
    @Test
    public void findAllGraphTaskIdsByTaskIdTest() {

        long start = System.currentTimeMillis();
        Integer taskId = 24258;
        graphService.getGraphAllTasksByTaskId(taskId);
        System.out.println("耗时：" + (System.currentTimeMillis() - start));
    }


    @Test
    public void selectChildrenFatherTest() {

        long start = System.currentTimeMillis();
        HashMap<Integer, RelationChildrenFather> hashMap = taskRelationMapper.selectChildrenFather();
        for (Integer integer : hashMap.keySet()) {
            System.out.println(hashMap.get(integer).getTaskId());
            System.out.println(hashMap.get(integer).getFatherTaskIds());
        }
        System.out.println("耗时：" + (System.currentTimeMillis() - start));
    }
}
