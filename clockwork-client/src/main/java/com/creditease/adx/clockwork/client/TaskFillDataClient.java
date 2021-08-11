/*-
 *  
 * Clockwork
 *  
 * Copyright (C) 2019 - 2020 adx
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 */

package com.creditease.adx.clockwork.client;

import com.creditease.adx.clockwork.common.entity.PageParam;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskFillDataPojo;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 2:41 下午 2020/4/1
 * @ Description：
 * @ Modified By：
 */
@FeignClient(value = "${api.service.name}")
public interface TaskFillDataClient {

    /**
     * 获取对象
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/fillData/getTaskFillDataByRerunBatchNumber")
    Map<String, Object> getTaskFillDataByRerunBatchNumber(
            @RequestParam(value = "rerunBatchNumber") String rerunBatchNumber);

    /**
     * 更新成功的任务数
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/fillData/updateTaskFillDataSuccessCount")
    Map<String, Object> updateTaskFillDataSuccessCount(
            @RequestParam(value = "rerunBatchNumber") String rerunBatchNumber);

    /**
     * 通过批次号更新数据
     *
     * @param tbClockworkTaskFillDataPojo
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/fillData/updateTaskFillDataByRerunBatchNumber")
    Map<String, Object> updateTaskFillDataByRerunBatchNumber(
            @RequestBody TbClockworkTaskFillDataPojo tbClockworkTaskFillDataPojo);

    /**
     * 更新CurrFillDataTime
     *
     * @param rerunBatchNumber     批次号
     * @param CurrFillDataTime     当前周期补数时间
     * @param CurrFillDataTimeSort 当前补数时间序号
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/fillData/updateTaskFillDataCurrFillDataTime")
    Map<String, Object> updateTaskFillDataCurrFillDataTime(@RequestParam(value = "rerunBatchNumber") String rerunBatchNumber,
                                                           @RequestParam(value = "CurrFillDataTime") String CurrFillDataTime,
                                                           @RequestParam(value = "CurrFillDataTimeSort") Integer CurrFillDataTimeSort);

    /**
     * 更新补数 isRan
     *
     * @param rerunBatchNumber 批次号
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/fillData/updateTaskFillDataIsRan")
    Map<String, Object> updateTaskFillDataIsRan(
            @RequestParam(value = "rerunBatchNumber") String rerunBatchNumber);

    /**
     * 批量添加
     *
     * @param rerunBatchNumber 批次号
     * @param fillDataType     补数时间类型
     * @param fillDataTimes    补数时间
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/fillData/timeQueue/addTaskFillDataTimeQueueBatch")
    Map<String, Object> addTaskFillDataTimeQueueBatch(
            @RequestParam(value = "rerunBatchNumber") String rerunBatchNumber,
            @RequestParam(value = "fillDataType") String fillDataType,
            @RequestParam(value = "fillDataTimes") List<String> fillDataTimes);

    /**
     * 获取下一个补数时间周期
     *
     * @param rerunBatchNumber 批次号
     * @param fillDataTime     补数时间
     * @return
     */
    @GetMapping(value = "/clockwork/api/task/fillData/timeQueue/getNextTaskFillDataTimeQueue")
    Map<String, Object> getNextTaskFillDataTimeQueue(@RequestParam("rerunBatchNumber") String rerunBatchNumber,
                                                     @RequestParam("fillDataTime") String fillDataTime);

    /**
     * 分页
     *
     * @param pageParam page param
     * @return
     */
    @PostMapping(value = "/clockwork/api/task/fillData/searchPageList")
    Map<String, Object> searchPageList(
            @RequestBody @ApiParam(name = "分页参数", value = "传入json格式", required = true) PageParam pageParam);

}
