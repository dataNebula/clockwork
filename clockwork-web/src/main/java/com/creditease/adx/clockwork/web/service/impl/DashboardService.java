package com.creditease.adx.clockwork.web.service.impl;

import com.creditease.adx.clockwork.common.entity.dashboard.BarChartEntity;
import com.creditease.adx.clockwork.common.entity.dashboard.TaskOperationEntity;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.enums.TaskTriggerModel;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.PojoUtil;
import com.creditease.adx.clockwork.common.util.StringUtil;
import com.creditease.adx.clockwork.dao.mapper.DashboardMapper;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import com.creditease.adx.clockwork.web.service.IDashboardService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:18 上午 2020/9/27
 * @ Description：
 * @ Modified By：
 */
@Service
public class DashboardService implements IDashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    /**
     * 总任务运行情况
     *
     * @param userName 用户名
     * @param roleName 用户角色
     * @return
     */
    @Override
    public TaskOperationEntity getTaskTotalRunStatus(String userName, String roleName) {
        // 获取总任务运行情况
        Map<String, Object> param = new HashMap<>();
        param.put("userName", userName);
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(roleName));
        List<Map<String, Object>> taskOperation = dashboardMapper.selectTaskTotalRunStatus(param);
        return formatTaskOperation(taskOperation);
    }

    /**
     * 今日任务运行情况
     *
     * @param userName 用户名
     * @param roleName 用户角色
     * @return
     */
    @Override
    public TaskOperationEntity getTaskTodayRunStatus(String userName, String roleName) {
        Map<String, Object> param = new HashMap<>();
        param.put("userName", userName);
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(roleName));
        List<Map<String, Object>> taskOperation = dashboardMapper.selectTaskTodayRunStatus(param);
        return formatTaskOperation(taskOperation);
    }


    @Override
    public List<Map<String, Integer>> getTaskHourSuccess(String userName, String roleName) {
        Map<String, Object> param = new HashMap<>();
        param.put("userName", userName);
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(roleName));
        return dashboardMapper.selectTaskHourSuccess(param);
    }

    @Override
    public List<Map<String, Integer>> getTaskHourFailed(String userName, String roleName) {
        Map<String, Object> param = new HashMap<>();
        param.put("userName", userName);
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(roleName));
        return dashboardMapper.selectTaskHourFailed(param);
    }

    @Override
    public List<Map<String, Object>> getTaskNodeRunCount(String userName, String roleName) {
        Map<String, Object> param = new HashMap<>();
        param.put("userName", userName);
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(roleName));
        return dashboardMapper.selectTaskNodeRunCount(param);
    }

    @Override
    public BarChartEntity getTaskNodeHourRun(String userName, String roleName) {
        Map<String, Object> param = new HashMap<>();
        param.put("userName", userName);
        param.put("roleName", StringUtil.spiltAndAppendSingleCitation(roleName));

        BarChartEntity barChartEntity = new BarChartEntity();

        // 查询节点当天历史运行情况
        List<Map<String, Object>> maps = dashboardMapper.selectTaskNodeHourRun(param);

        // 分组（通过nodeId), 重新构建数据：xData、nodeAndTimeData<节点id, <时间，cnt>>
        List<String> xData = new ArrayList<>();
        Map<String, Map<String, Integer>> nodeAndTimeData = new HashMap<>();
        for (Map<String, Object> map : maps) {
            String id = String.valueOf(map.get("nodeId"));
            xData.add(String.valueOf(map.get("hours")));
            if (nodeAndTimeData.containsKey(id)) {
                nodeAndTimeData.get(id).put(String.valueOf(map.get("hours")), Integer.valueOf(String.valueOf(map.get("cnt"))));
            } else {
                Map<String, Integer> tmpMap = new HashMap<>();
                tmpMap.put(String.valueOf(map.get("hours")), Integer.valueOf(String.valueOf(map.get("cnt"))));
                nodeAndTimeData.put(id, tmpMap);
            }
        }

        // 去重
        LinkedHashSet<String> middleHashSet = new LinkedHashSet<String>(xData);
        xData = new ArrayList<String>(middleHashSet);

        // 构建图数据
        List<Map<String, Object>> chartDataList = new ArrayList<>();    // chartData
        Map<String, Object> chartData = null;
        // groups -> {节点id: [{hours:0点, cnt:123, nodeId:id}]}
        for (Map.Entry<String, Map<String, Integer>> entry : nodeAndTimeData.entrySet()) {

            // 该节点的所有数据
            chartData = new HashMap<>();  // 构建chartData
            String nodeId = entry.getKey(); // 节点id
            Map<String, Integer> timeData = entry.getValue(); // [{hours:0点, cnt:123, nodeId:id}]

            List<Integer> chartDataValue = new ArrayList<>(); // [100,200,300,...]
            for (String time : xData) {
                if (timeData.get(time) != null) {
                    chartDataValue.add(timeData.get(time));
                } else {
                    chartDataValue.add(0);
                }
            }
            chartData.put("name", "节点" + nodeId);
            chartData.put("data", chartDataValue.toArray());
            chartDataList.add(chartData);
        }

        barChartEntity.setxData(xData.stream().map(c -> c + "点").toArray(String[]::new));
        barChartEntity.setChartData(chartDataList);
        return barChartEntity;
    }

    /**
     * 获取最近要运行的任务
     *
     * @return
     */
    @Override
    public List<TbClockworkTaskPojo> getWaitForRunTask(int size) {
        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andOnlineEqualTo(true)
                .andNextTriggerTimeIsNotNull()
                .andTriggerModeNotEqualTo(TaskTriggerModel.DEPENDENCY.getValue());
        example.setOrderByClause("next_trigger_time ASC");
        example.setLimitStart(0);
        example.setLimitEnd(size);
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }


    /**
     * 获取最近失败的任务
     *
     * @param size size
     * @return
     */
    @Override
    public List<TbClockworkTaskPojo> getLatelyFailedTask(int size) {
        List<String> status = new ArrayList<>();
        status.add(TaskStatus.FAILED.getValue());
        status.add(TaskStatus.EXCEPTION.getValue());
        status.add(TaskStatus.KILLED.getValue());
        status.add(TaskStatus.RUN_TIMEOUT_KILLED.getValue());

        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andStatusIn(status).andOnlineEqualTo(true);
        example.setOrderByClause("update_time DESC");
        example.setLimitStart(0);
        example.setLimitEnd(size);
        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(tbClockworkTasks)) {
            LOG.info("getLatelyFailedTask task.size = {}", tbClockworkTasks.size());
            return PojoUtil.convertList(tbClockworkTasks, TbClockworkTaskPojo.class);
        }
        return null;
    }

    private TaskOperationEntity formatTaskOperation(List<Map<String, Object>> taskOperation) {
        TaskOperationEntity entity = new TaskOperationEntity();
        if (CollectionUtils.isEmpty(taskOperation)) {
            return entity;
        }
        // 解析
        for (Map<String, Object> interfaceResult : taskOperation) {
            String key = String.valueOf(interfaceResult.get("status"));
            int value = Integer.parseInt(String.valueOf(interfaceResult.get("cnt")));
            switch (key) {
                case "total":
                    entity.setTotal(value);
                    break;
                case "success":
                    entity.setSuccess(value);
                    break;
                case "failed":
                    entity.setFailed(value);
                    break;
                case "exception":
                    entity.setException(value);
                    break;
                case "life_cycle_reset":
                    entity.setLifeCycleReset(value);
                    break;
                case "enable":
                    entity.setEnable(value);
                    break;
                case "rerun_schedule_prep":
                    entity.setRerunSchedulePrep(value);
                    break;
                case "submit":
                    entity.setSubmit(value);
                    break;
                case "worker_has_received":
                    entity.setWorkerHasReceived(value);
                    break;
                case "master_has_received":
                    entity.setMasterHasReceived(value);
                    break;
                case "running":
                    entity.setRunning(value);
                    break;
                case "killing":
                    entity.setKilling(value);
                    break;
                case "killed":
                    entity.setKilled(value);
                    break;
                case "run_timeout_killing":
                    entity.setRunTimeoutKilling(value);
                    break;
                case "run_timeout_killed":
                    entity.setRunTimeoutKilled(value);
                    break;
                case "father_not_success":
                    entity.setFatherNotSuccess(value);
                    break;
                case "online":
                    entity.setOnline(value);
                    break;
            }
        }
        entity.setOffline(entity.getTotal() - entity.getOnline());
        return entity;
    }

}
