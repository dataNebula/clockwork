package com.creditease.adx.clockwork.client;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskGroup;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:27 上午 2020/10/21
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface TaskGroupClient {


    @PostMapping(value = "/clockwork/api/task/group/addTaskGroup")
    Map<String, Object> addTaskGroup(@RequestBody TbClockworkTaskGroup taskGroup);

    /**
     * 修改taskGroup
     *
     * @param taskGroup json string
     */
    @PostMapping(value = "/clockwork/api/task/group/updateTaskGroup")
    Map<String, Object> updateTaskGroup(@RequestBody TbClockworkTaskGroup taskGroup);

    /**
     * 使taskGroup本身失效，并将其内部的所有作业任务也一并失效，涉及操作作业状态必须加锁
     * 作业组以及作业下线是，队列中的任务需要移除
     *
     * @param taskGroupId
     */
    @PostMapping(value = "/clockwork/api/task/group/disableTaskGroup")
    Map<String, Object> disableTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId);

    /**
     * 使taskGroup生效，并将其内部的所有作业任务也一并生效，涉及操作作业状态必须加锁
     *
     * @param taskGroupId
     */
    @PostMapping(value = "/clockwork/api/task/group/enableTaskGroup")
    Map<String, Object> enableTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId);


    /**
     * 删除
     *
     * @param taskGroupId
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/group/delete")
    Map<String, Object> deleteTaskGroup(@RequestParam(value = "taskGroupId") Integer taskGroupId);


    /**
     * 查询taskGroupName是否重复
     *
     * @param taskGroupName taskGroup name
     */
    @GetMapping(value = "/clockwork/api/task/group/taskGroupIsExists")
    Map<String, Object> checkTaskGroupName(@RequestParam(value = "taskGroupName") String taskGroupName);
}
