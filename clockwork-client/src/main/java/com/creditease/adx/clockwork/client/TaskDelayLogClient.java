package com.creditease.adx.clockwork.client;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDelayLog;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:21 下午 2020/9/1
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface TaskDelayLogClient {

    @PostMapping(value = "/clockwork/api/task/delayLog/addTaskDelayLog")
    Map<String, Object> addTaskDelayLog(
            @RequestParam(value = "taskId") Integer taskId,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "delayStatus") Integer delayStatus);

    @PostMapping(value = "/clockwork/api/task/delayLog/addTaskDelayLogBatch")
    Map<String, Object> addTaskDelayLogBatch(@RequestBody List<TbClockworkTaskDelayLog> taskDelayLogList);
}
