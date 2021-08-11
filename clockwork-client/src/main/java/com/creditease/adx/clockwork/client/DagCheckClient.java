package com.creditease.adx.clockwork.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(value = "${api.service.name}")
public interface DagCheckClient {
    @GetMapping(value = "/clockwork/api/dag/check/checkDagById")
    Map<String, Object> checkDagById(@RequestParam(value = "dagId") Integer dagId,
                                     @RequestParam(value = "userName") String userName);

    @GetMapping(value = "/clockwork/api/dag/check/checkAllDags")
    Map<String, Object> checkAllDags(@RequestParam(value = "userName") String userName);
}
