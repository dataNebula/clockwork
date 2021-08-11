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

package com.creditease.adx.clockwork.common.util;

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;

import java.util.*;

public class TaskLogUtil {
    public static List<List<TbClockworkTaskLogPojo>> getBatchTaskLogByTriggerTime(List<TbClockworkTaskLogPojo> taskLogs) {
        //找出每个批次的第一个时间触发的任务
        List<TbClockworkTaskLogPojo> firstTaskLog = new ArrayList<>();
        for (TbClockworkTaskLogPojo taskLog : taskLogs) {
            if (taskLog.getTriggerTime() != null) {
                firstTaskLog.add(taskLog);
            }
        }

        //封装成时间批次对象
        List<BetweenTime> betweenTimes = new ArrayList<>();
        for (int i = 0; i < firstTaskLog.size(); i++) {
            if (i == (firstTaskLog.size() - 1)) {
                betweenTimes.add(new BetweenTime(firstTaskLog.get(firstTaskLog.size() - 1).getTriggerTime(), new Date()));
                break;
            }
            betweenTimes.add(new BetweenTime(firstTaskLog.get(i).getTriggerTime(), firstTaskLog.get(i + 1).getTriggerTime()));
        }

        //一个时间批次对象对应一个批次的任务
        List<List<TbClockworkTaskLogPojo>> batchTaskLog = new ArrayList<>();
        for (BetweenTime betweenTime : betweenTimes) {
            List<TbClockworkTaskLogPojo> oneBath = new ArrayList<>();
            for (TbClockworkTaskLogPojo taskLog : taskLogs) {
                if (betweenTime.isIn(taskLog.getStartTime())) {
                    oneBath.add(taskLog);
                }
            }
            batchTaskLog.add(oneBath);
        }
        return batchTaskLog;
    }

    public static List<List<TbClockworkTaskLogPojo>> getBatchTaskLogByBatchNum(List<TbClockworkTaskLogPojo> taskLogs) {
        //批次号
        Set<Long> batchNums = new HashSet<>();
        for (TbClockworkTaskLogPojo taskLog : taskLogs) {
            if (taskLog.getBatchNumber() != null) {
                batchNums.add(taskLog.getBatchNumber());
            }
        }

        //一个批次号对应一个批次的任务
        List<List<TbClockworkTaskLogPojo>> batchTaskLog = new ArrayList<>();
        for (Long batchNum : batchNums) {
            List<TbClockworkTaskLogPojo> oneBath = new ArrayList<>();
            for (TbClockworkTaskLogPojo taskLog : taskLogs) {
                if (taskLog.getBatchNumber() != null && taskLog.getBatchNumber().equals(batchNum)) {
                    oneBath.add(taskLog);
                }
            }
            batchTaskLog.add(oneBath);
        }
        return batchTaskLog;
    }
}

class BetweenTime {
    private Date startTime;
    private Date endTime;

    public BetweenTime(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isIn(Date timeValue) {
        return timeValue.getTime() >= startTime.getTime() && timeValue.getTime() < endTime.getTime();
    }
}
