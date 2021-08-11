package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 4:03 下午 2020/12/28
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestProblemTaskService {

    @Autowired
    private IProblemTaskService problemTaskService;

    @Test
    public void getTasksByInvalidDagIdTest() throws Exception {
        List<TbClockworkTaskPojo> invalidDagId = problemTaskService.getTasksByInvalidDagId();
        for (TbClockworkTaskPojo taskPojo : invalidDagId) {
            System.out.println(String.format(
                    "id:%s, name:%s, status:%s, lastStartTime:%s",
                    taskPojo.getId(), taskPojo.getName(), taskPojo.getStatus(), taskPojo.getLastStartTime()
            ));
        }

    }


}
