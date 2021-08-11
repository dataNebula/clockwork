package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRerunRelation;
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
public class TestTaskRerunRelationService {

    @Autowired
    private ITaskRerunRelationService taskRerunRelationService;

    @Test
    public void findTaskAllChildrenNotIncludeSelfTest() {

        Integer taskId = 12191;
        Long rerunBatchNumber = 202770492359180486L;
        List<TbClockworkTaskRerunRelation> notIncludeSelf
                = taskRerunRelationService.findTaskAllChildrenNotIncludeSelf(taskId, rerunBatchNumber);
        for (TbClockworkTaskRerunRelation tbClockworkTaskRerunRelation : notIncludeSelf) {
            System.out.println(tbClockworkTaskRerunRelation.getTaskId());
        }

    }


}
