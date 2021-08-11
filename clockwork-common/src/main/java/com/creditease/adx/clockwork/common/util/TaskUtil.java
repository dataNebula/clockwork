package com.creditease.adx.clockwork.common.util;

import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 3:17 下午 2020/7/16
 * @ Description：
 * @ Modified By：
 */
public class TaskUtil {

    private static final Logger LOG = LoggerFactory.getLogger(TaskUtil.class);

    /**
     * 计算出该图中，当天高优先级的任务
     *
     * @param tasks 通过dagId扫描出的整个图
     * @return
     */
    public static int getHighPriorityTaskId(List<TbClockworkTaskPojo> tasks) {
        // list大小， size = 0 时，返回 -1
        int size = 0;
        if (tasks == null || (size = tasks.size()) == 0) {
            return -1;
        }

        int result = -1;
        try {
            if (size == 1) { // 特殊处理单任务
                return tasks.get(0).getId();
            }
            String resultExp = null;
            for (int i = 0; i < size; i++) {
                // 针对时间触发模式的任务
                if (TaskTriggerModel.TIME.getValue().intValue() == tasks.get(i).getTriggerMode().intValue()) {
                    String currCronExp = tasks.get(i).getCronExp();
                    // 初始化首个id 和 conExp
                    if (result == -1) {
                        resultExp = currCronExp;
                        result = tasks.get(i).getId();
                        continue;
                    }
                    if (resultExp.equals(currCronExp)) {
                        continue;
                    }
                    String highExp = CronExpression.highPriorityTime(resultExp, currCronExp);
                    if (currCronExp.equals(highExp)) {
                        resultExp = currCronExp;
                        result = tasks.get(i).getId();
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("getHighPriorityTaskId 严重Error {}.", e.getMessage(), e);
        }
        return result;
    }

}
