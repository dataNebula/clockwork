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

package com.creditease.adx.clockwork.master.fix;

import com.alibaba.fastjson.JSONObject;
import com.creditease.adx.clockwork.common.entity.TaskSubmitInfoFillData;
import com.creditease.adx.clockwork.common.enums.TaskSource;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 20:32 2020-11-27
 * @ Description：
 * @ Modified By：
 */
public class FixFillData {


    /**
     * 修复已经中断的补数任务
     * - 上线导致补数任务中断，怎么修复？
     * 1. 通过批次号查看补数信息（成功数，当前被中断的补数日期）
     * 2. 获取批次号、任务id\taskLogId\nodeGid\name\等等必要参数
     * 3. 构建请求参数jsonData
     * 4. 在master节点发送请求
     * 5. 观察结果即可
     */
    @Test
    public void fixBreakFillData() {

        // 必要的一些参数
        List<TbClockworkTaskPojo> tasksPojo = new ArrayList<>();

        // task obj
        TbClockworkTaskPojo taskPojo = new TbClockworkTaskPojo();
        taskPojo.setId(20009);
        taskPojo.setTaskLogId(3623092);
        taskPojo.setGroupId(9007);
        taskPojo.setName("12332_93850_[sql-1]");
        taskPojo.setTriggerMode(4);
        taskPojo.setNodeGid(1);
        taskPojo.setDagId(42814);
        taskPojo.setLocation("/user/adx/triangle/runTimeDir/shell/moonbox");
        taskPojo.setScriptName("moonbox-submit.sh");
        taskPojo.setScriptParameter("/user/adx/triangle/runTimeDir/shell/moonbox/DFS_20210108_moonbox-submit_204057337575505928.sh /user/adx/triangle/runTimeDir/shell/moonbox -n12332_93850_[sql-1] -s -uadx@p94 -p -dc4p94_default -lmql -f/user/adx/triangle/runTimeDir/shell/dataworks/batch/94/12332/93850/93850.mql -Cspark.executor.memory=6g,spark.driver.memory=6g,spark.driver.cores=2,spark.executor.cores=2,spark.executor.instances=10,spark.sql.shuffle.partitions=32");
        taskPojo.setStatus(TaskStatus.SUBMIT.getValue());
        taskPojo.setSource(TaskSource.ADX_DATA_WORKS.getValue());
        taskPojo.setCreateUser("aiqiangzhang2@clockwork.com");

        tasksPojo.add(taskPojo);                        // 补数任务

        Long rerunBatchNumber = 204057109942239234L;    // 批次号
        String fillDataTime = "2020-09-02 10:00:00";    // 被中断的补数日期

        // 构建请求jsonData
        TaskSubmitInfoFillData taskSubmitInfoFillData = new TaskSubmitInfoFillData(tasksPojo,rerunBatchNumber,fillDataTime);
        System.out.println(JSONObject.toJSONString(taskSubmitInfoFillData));

        // 模拟请求，分发给master
//         curl -i -X POST -H 'Content-type':'application/json' -d '{jsonData}' http://127.0.0.1:9009/clockwork/master/task/distribute/filldata

//        curl -i -X POST -H 'Content-type':'application/json' -d '{"executeType":2,"fillDataTime":"2020-09-02 10:00:00","rerunBatchNumber":204057109942239234,"taskIds":[20009],"taskPojoList":[{"createUser":"aiqiangzhang2@clockwork.com","dagId":42814,"groupId":9007,"id":20009,"location":"/user/adx/triangle/runTimeDir/shell/moonbox","name":"12332_93850_[sql-1]","nodeGid":1,"scriptName":"moonbox-submit.sh","scriptParameter":"/user/adx/triangle/runTimeDir/shell/moonbox/DFS_20210108_moonbox-submit_204057337575505928.sh /user/adx/triangle/runTimeDir/shell/moonbox -n12332_93850_[sql-1] -s -uadx@p94 -p -dc4p94_default -lmql -f/user/adx/triangle/runTimeDir/shell/dataworks/batch/94/12332/93850/93850.mql -Cspark.executor.memory=6g,spark.driver.memory=6g,spark.driver.cores=2,spark.executor.cores=2,spark.executor.instances=10,spark.sql.shuffle.partitions=32","source":3,"status":"submit","taskLogId":3623092,"triggerMode":4}]}' http://127.0.0.1:9009/clockwork/master/task/distribute/filldata
//        HTTP/1.1 200 OK
//        Date: Fri, 27 Nov 2020 14:29:16 GMT
//        Content-Type: application/json;charset=utf-8
//        Transfer-Encoding: chunked
//
//        {"msg":null,"code":"OK","data":true}


        // 如果是当前周期头节点没下发，需要修改相关字段
        // update tb_clockwork_task_fill_data set curr_fill_data_time='2020-09-02 10:00:00',curr_fill_data_time_sort=2,task_count_success=1 where rerun_batch_number=204057109942239234;
    }


}
