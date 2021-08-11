package com.creditease.adx.clockwork.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "${api.service.name}")
public interface TaskKeyWordClient {

    @GetMapping(value = "/clockwork/api/task/keyword/getKeywordInfoByKeywordIds")
    Map<String, Object> getKeywordInfoBykeyIds(@RequestParam("keyIds") String taskId);
}
