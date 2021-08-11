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

package com.creditease.adx.clockwork.master.service.impl;

import com.creditease.adx.clockwork.client.RestTemplateClient;
import com.creditease.adx.clockwork.client.service.*;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.*;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.enums.NodeType;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.exception.TaskDistributeException;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DataUtil;
import com.creditease.adx.clockwork.master.service.ITaskDistributeService;
import com.creditease.adx.clockwork.master.service.distribute.DefaultDistributionPolicy;
import com.creditease.adx.clockwork.master.service.distribute.DistributePolicyInterface;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 任务分发器
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 5:00 下午 2020/4/1
 * @ Description：任务分发逻辑
 * @ Modified By：
 */
@Service
public class TaskDistributeService implements ITaskDistributeService, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TaskDistributeService.class);

    // 任务提交信息等待下发队列
    private final BlockingQueue<TaskSubmitInfo> submitInfoWaitForDistributeQueue = new LinkedBlockingQueue<>();

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Autowired
    private NodeClientService nodeClientService;

    @Autowired
    private TaskStateClientService taskStateClientService;

    @Resource(name = "taskClientService")
    private TaskClientService taskClientService;

    @Resource(name = "taskLogClientService")
    private TaskLogClientService taskLogClientService;

    @Resource(name = "taskOperationClientService")
    private TaskOperationClientService taskOperationClientService;

    @Resource(name = "loopClockClientService")
    private LoopClockClientService loopClockClientService;

    @Value("${task.batch.send.to.worker.num}")
    protected int taskBatchSendToWorkerNum;

    @PostConstruct
    public void setup() {
        new Thread(this).start();
        LOG.info("[TaskDistributeService-setup]The thread that process need be executed task is started");
    }

    @Override
    public void run() {
        while (true) {
            TaskSubmitInfo taskSubmitInfo = null;
            long start = System.currentTimeMillis();
            try {
                // 获取 等待队列中的任务
                taskSubmitInfo = submitInfoWaitForDistributeQueue.poll(2, TimeUnit.SECONDS);
                if (taskSubmitInfo == null) {
                    LOG.debug("[TaskDistributeService-run] There were no tasks need to be executed,skip current loop!");
                    continue;
                }
                LOG.info("[TaskDistributeService-run]process task begin, task size = {}, executeType = {}",
                        taskSubmitInfo.getTaskIds().size(), taskSubmitInfo.getExecuteType());

                // 下发成功，输出日志信息
                if (taskDistributeToWorker(taskSubmitInfo, new DefaultDistributionPolicy())) {
                    LOG.info("[TaskDistributeService-run]process task success, ask size = {}, executeType = {}, cost time = {} ms.",
                            taskSubmitInfo.getTaskIds().size(), taskSubmitInfo.getExecuteType(), System.currentTimeMillis() - start);
                    continue;
                }
            } catch (Exception e) {
                LOG.error("TaskDistributeService-run, distribute Error {}.", e.getMessage(), e);
                // 下发不成功，重新放入队列等待下发
                if (taskSubmitInfo != null && CollectionUtils.isNotEmpty(taskSubmitInfo.getTaskIds())) {
                    // 下发失败，停止已经下发的作业
                    taskOperationClientService.stopTaskList(taskSubmitInfo.getTaskIds());

                    // 然后重新入队列
//                addSubmitInfoToWaitForDistributeQueue(taskSubmitInfo);
                    LOG.error("[TaskDistributeService-run]process task exception, task size = {}, executeType = {}",
                            taskSubmitInfo.getTaskIds().size(), taskSubmitInfo.getExecuteType());
                }
            }


        }
    }

    /**
     * 分发任务到Worker 节点
     *
     * @param taskSubmitInfo 基本提交信息
     * @return
     */
    @Override
    public boolean taskDistributeToWorker(TaskSubmitInfo taskSubmitInfo, DistributePolicyInterface policy) throws Exception {
        long start = System.currentTimeMillis();

        // 获取到节点组和任务的对应关系
        Map<Integer, List<TbClockworkTaskPojo>> nodeGroupIdAndTaskList = tasksGroupByNodeGroupId(taskSubmitInfo);
        int executeType = taskSubmitInfo.getExecuteType();

        // 分发任务
        for (Map.Entry<Integer, List<TbClockworkTaskPojo>> nodeGroupAndTaskIdsEntry : nodeGroupIdAndTaskList.entrySet()) {
            Integer nodeGroupId = nodeGroupAndTaskIdsEntry.getKey();
            List<TbClockworkTaskPojo> taskIdList = nodeGroupAndTaskIdsEntry.getValue();

            // 获取有效的worker节点，然后开始分发任务
            List<TbClockworkNode> tbClockworkNodes = nodeClientService
                    .getAllEnableNodeByRoleAndGroupId(NodeType.WORKER.getValue(), nodeGroupId);
            if (CollectionUtils.isEmpty(tbClockworkNodes)) {
                List<Integer> taskIds = taskIdList.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList());
                LOG.warn("DefaultDistributionPolicy No valid worker node! nodeGroupId = {}, taskIds = {}", nodeGroupId, taskIds);
                throw new RuntimeException("DefaultDistributionPolicy No valid worker node! nodeGroupId = " + nodeGroupId);
            }

            // 使用默认的分发策略（获取到节点和任务的对应关系）
            Map<TbClockworkNode, List<TbClockworkTaskPojo>> nodeTaskListMap = policy.distributePolicy(taskIdList, tbClockworkNodes);

            // 依次分发相应的节点的任务
            for (Map.Entry<TbClockworkNode, List<TbClockworkTaskPojo>> entry : nodeTaskListMap.entrySet()) {
                TbClockworkNode node = entry.getKey();
                List<TbClockworkTaskPojo> splitTask = entry.getValue();
                if (CollectionUtils.isEmpty(splitTask)) {
                    LOG.info("[TaskDistributeService-taskDistributeToWorker]skip the node that need to launch size of tasks is 0.");
                    continue;
                }

                // 多个线程并发下发任务到对应的服务节点, 每批次最多分发200个任务
                if (splitTask.size() > taskBatchSendToWorkerNum) {
                    List<List<TbClockworkTaskPojo>> fixedGrouping = DataUtil.fixedGrouping(splitTask, taskBatchSendToWorkerNum);
                    for (List<TbClockworkTaskPojo> taskPojoList : fixedGrouping) {
                        new Thread(new TaskDistributeService.SendTasksToWorkerThread(
                                node, taskPojoList, taskSubmitInfo, executeType)).start();
                    }
                } else {
                    new Thread(new TaskDistributeService.SendTasksToWorkerThread(
                            node, splitTask, taskSubmitInfo, executeType)).start();
                }
            }
        }

        LOG.info("[TaskDistributeService-taskDistributeToWorker]send tasks to worker finished, " +
                        "nodeGroupIdAndTaskList = {}, task size = {}, total cost time = {} ms.",
                nodeGroupIdAndTaskList.size(), taskSubmitInfo.getTaskIds().size(), System.currentTimeMillis() - start);
        return true;
    }

    /**
     * 获取执行机组和任务的关系
     *
     * @param taskSubmitInfo
     * @return
     */
    private Map<Integer, List<TbClockworkTaskPojo>> tasksGroupByNodeGroupId(TaskSubmitInfo taskSubmitInfo) {
        Map<Integer, List<TbClockworkTaskPojo>> result = new HashMap<>();
        if (taskSubmitInfo == null || CollectionUtils.isEmpty(taskSubmitInfo.getTaskIds())) {
            return result;
        }

        // taskMap: <taskId, taskPojo>
        List<TbClockworkTaskPojo> taskPojoList = taskSubmitInfo.getTaskPojoList();
        if (CollectionUtils.isEmpty(taskPojoList)) {
            taskPojoList = taskClientService.getTaskByTaskIds(taskSubmitInfo.getTaskIds());
        }

        // 拆分taskMap（分组）
        if (taskPojoList != null) for (TbClockworkTaskPojo taskPojo : taskPojoList) {
            Integer nodeGroupId = taskPojo.getNodeGid();
            List<TbClockworkTaskPojo> resultValue = result.get(nodeGroupId);
            if (resultValue == null) {
                resultValue = new ArrayList<>();
            }
            resultValue.add(taskPojo);
            result.remove(nodeGroupId);
            result.put(nodeGroupId, resultValue);
        }
        return result;
    }

    private class SendTasksToWorkerThread implements Runnable {
        private TbClockworkNode node;
        private List<TbClockworkTaskPojo> tasks;
        private TaskSubmitInfo taskSubmitInfo;
        private int executeType;

        private SendTasksToWorkerThread(TbClockworkNode node, List<TbClockworkTaskPojo> splitTasks,
                                        TaskSubmitInfo taskSubmitInfo, int executeType) {
            this.node = node;
            this.tasks = splitTasks;
            this.taskSubmitInfo = taskSubmitInfo;
            this.executeType = executeType;
        }

        @Override
        public void run() {
            try {
                /*
                 * 分发任务到worker
                 */
                sendTasksToNode(node, tasks, taskSubmitInfo);
            } catch (Exception e) {
                // 分发任务到worker失败，相关处理逻辑
                sendTasksToNodeExceptionHandler(tasks, executeType);
            }
        }

        /**
         * 发送相应的任务到节点
         *
         * @param node           节点
         * @param splitTasks     任务
         * @param taskSubmitInfo submit info
         * @throws TaskDistributeException ex
         */
        private void sendTasksToNode(TbClockworkNode node, List<TbClockworkTaskPojo> splitTasks,
                                     TaskSubmitInfo taskSubmitInfo) throws TaskDistributeException {
            long start = System.currentTimeMillis();
            // 构建: tuple
            TaskDistributeTuple taskDistributeTuple;
            if (taskSubmitInfo instanceof TaskSubmitInfoRouTine) {
                taskDistributeTuple = distributeTaskRouTine(node, splitTasks, (TaskSubmitInfoRouTine) taskSubmitInfo);
            } else if (taskSubmitInfo instanceof TaskSubmitInfoFillData) {
                taskDistributeTuple = distributeTaskFillData(node, splitTasks, (TaskSubmitInfoFillData) taskSubmitInfo);
            } else if (taskSubmitInfo instanceof TaskSubmitInfoRerun) {
                taskDistributeTuple = distributeTaskReRun(node, splitTasks, (TaskSubmitInfoRerun) taskSubmitInfo);
            } else if (taskSubmitInfo instanceof TaskSubmitInfoSignal) {
                taskDistributeTuple = distributeTaskSignal(node, splitTasks, (TaskSubmitInfoSignal) taskSubmitInfo);
            } else {
                throw new TaskDistributeException("[TaskDistributeService-sendTasksToNode] taskSubmitInfo type is Error.");
            }

            // 分发任务tuple到worker
            String URL = String.format(taskDistributeTuple.getDistributeURL(), node.getIp(), node.getPort());
            LOG.debug("[SendTasksToWorkerThread-sendTasksToNode]Launch run tasks to worker! URL = {}, task.size = {}, splitTasks = {}",
                    URL, taskDistributeTuple.getTaskIds().size(), taskDistributeTuple.getTaskIds());

            //分发任务
            try {
                Map<String, Object> interfaceResult = restTemplateClient.getResult(URL, taskDistributeTuple);
                if (splitTasks.size() == (Integer) interfaceResult.get(Constant.DATA)) {
                    LOG.info("[SendTasksToWorkerThread-SendTasksToNode]Launch success! ip = {}, splitTaskIds.size = {}, "
                            + "cost time = {} ms.", node.getIp(), taskDistributeTuple.getTaskIds().size(), System.currentTimeMillis() - start);
                    return;
                }
            } catch (Exception e) {
                LOG.error("SendTasksToWorkerThread-sendTasksToNode launch task failure! url = {}, splitTaskIds = {}, "
                                + "splitTaskIds.size = {}, cost time = {} ms, Error {}.",
                        URL, taskDistributeTuple.getTaskIds(), taskDistributeTuple.getTaskIds().size(),
                        System.currentTimeMillis() - start, e.getMessage(), e);
            }
            throw new TaskDistributeException("launch task failure! cost time = " + (System.currentTimeMillis() - start) + " ms.");
        }

        /**
         * 分发信号触发的任务
         *
         * @param node       节点
         * @param splitTasks 任务
         */
        private TaskDistributeTuple distributeTaskRouTine(
                TbClockworkNode node, List<TbClockworkTaskPojo> splitTasks, TaskSubmitInfoRouTine taskSubmitInfo) {
            LOG.info("[SendTasksToWorkerThread-distributeTaskRouTine] info. nodeIp= {}, task.size = {}", node.getIp(), splitTasks.size());

            // 构建TaskDistributeTuple，然后分发到WORKER
            return new TaskDistributeTupleRoutine(splitTasks, node.getId());
        }

        /**
         * 分发重启任务
         *
         * @param node           节点
         * @param splitTasks     任务
         * @param taskSubmitInfo 提交信息
         */
        private TaskDistributeTuple distributeTaskReRun(
                TbClockworkNode node, List<TbClockworkTaskPojo> splitTasks, TaskSubmitInfoRerun taskSubmitInfo) {
            LOG.info("[SendTasksToWorkerThread-distributeTaskReRun]info. nodeIp={}, task.size = {}, rerunBatchNumber = {}",
                    node.getIp(), splitTasks.size(), taskSubmitInfo.getRerunBatchNumber());

            // 构建TaskDistributeTuple，然后分发到WORKER
            return new TaskDistributeTupleReRun(splitTasks, taskSubmitInfo.getRerunBatchNumber(), node.getId());
        }

        /**
         * 分发补数任务
         *
         * @param node           节点
         * @param splitTasks     任务
         * @param taskSubmitInfo 提交信息
         */
        private TaskDistributeTuple distributeTaskFillData(
                TbClockworkNode node, List<TbClockworkTaskPojo> splitTasks, TaskSubmitInfoFillData taskSubmitInfo) {
            String fillDataTime = taskSubmitInfo.getFillDataTime();
            LOG.info("[SendTasksToWorkerThread-distributeTaskFillData]info. nodeIp={}, task.size = {}, rerunBatchNumber = {}, "
                    + "fillDataTime = {}", node.getIp(), splitTasks.size(), taskSubmitInfo.getRerunBatchNumber(), fillDataTime);

            // 构建TaskDistributeTuple，然后分发到WORKER
            return new TaskDistributeTupleFillData(splitTasks, taskSubmitInfo.getRerunBatchNumber(), fillDataTime, node.getId());
        }

        /**
         * 分发信号触发的任务
         *
         * @param node       节点
         * @param splitTasks 任务
         */
        private TaskDistributeTuple distributeTaskSignal(
                TbClockworkNode node, List<TbClockworkTaskPojo> splitTasks, TaskSubmitInfoSignal taskSubmitInfo) {
            LOG.info("[SendTasksToWorkerThread-distributeTaskSignal]info. nodeIp={}, task.size = {}", node.getIp(), splitTasks.size());

            // 构建TaskDistributeTuple，然后分发到WORKER
            return new TaskDistributeTupleSignal(splitTasks, node.getId());
        }

        /**
         * 分发任务失败相关处理逻辑
         *
         * @param splitTasks  任务
         * @param executeType 执行类型
         */
        private void sendTasksToNodeExceptionHandler(List<TbClockworkTaskPojo> splitTasks, int executeType) {
            /*
             * 分发任务失败，批量更新可以提交的任务的状态为EXCEPTION
             */
            String status = TaskStatus.EXCEPTION.getValue();
            if (CollectionUtils.isEmpty(splitTasks)) return;

            // 获取数据
            List<Integer> taskIds = splitTasks.stream().map(TbClockworkTaskPojo::getId).collect(Collectors.toList());
            List<Integer> taskLogIds = splitTasks.stream().map(TbClockworkTaskPojo::getTaskLogId).collect(Collectors.toList());

            // 修改状态
            if (CollectionUtils.isNotEmpty(taskIds))
                taskOperationClientService.updateTaskStatusEndBatch(new BatchUpdateTaskStatusEnd(taskIds, status, true));
            if (CollectionUtils.isNotEmpty(taskLogIds))
                taskLogClientService.updateBatchTaskLogEnd(taskLogIds, status, -1);

            // 构建环形时钟
            if (TaskExecuteType.ROUTINE.getCode() == executeType) {
                loopClockClientService.addTaskToLoopClockSlotByBatch(splitTasks);
            }
            LOG.error("SendTasksToWorkerThread Error taskIds = {}, update status = {}", taskIds, status);
        }
    }

    /**
     * 移除 NeedBeExecutedTaskQueue by ID（排除补数任务）
     *
     * @param taskId
     * @return
     */
    @Override
    public int removeTaskFromWaitForDistributeQueue(int taskId) {
        int count = 0;
        Iterator<TaskSubmitInfo> executedTaskIterator = submitInfoWaitForDistributeQueue.iterator();
        while (executedTaskIterator.hasNext()) {
            TaskSubmitInfo next = executedTaskIterator.next();
            Iterator<TbClockworkTaskPojo> tasksIterator = next.getTaskPojoList().iterator();
            while (tasksIterator.hasNext()) {
                TbClockworkTaskPojo taskPojo = tasksIterator.next();
                if (taskPojo.getId() == taskId) {
                    LOG.info("[removeNeedBeExecutedTaskQueue] existence needBeExecutedTaskQueue, taskId id = {}", taskId);
                    tasksIterator.remove();
                    count++;
                    break;
                }
            }
        }
        if (count == 0) {
            LOG.info("[removeNeedBeExecutedTaskQueue] notExistence needBeExecutedTaskQueue, taskId id = {}", taskId);
        }
        return count;
    }

    /**
     * 添加到队列，并修改状态为：MASTER_HAS_RECEIVE
     *
     * @param taskSubmitInfo taskSubmitInfo
     * @return boolean
     */
    @Override
    public boolean addSubmitInfoToWaitForDistributeQueue(TaskSubmitInfo taskSubmitInfo) {
        // MASTER_HAS_RECEIVE
        taskStateClientService.taskStateMasterHasReceive(taskSubmitInfo);
        return submitInfoWaitForDistributeQueue.add(taskSubmitInfo);
    }
}
