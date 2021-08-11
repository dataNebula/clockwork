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

package com.creditease.adx.clockwork.worker.service.task.execute;

import com.creditease.adx.clockwork.client.service.*;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.TaskRunCell;
import com.creditease.adx.clockwork.common.entity.TaskRunCellFillData;
import com.creditease.adx.clockwork.common.entity.TaskRunCellReRun;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskDependencyScript;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;
import com.creditease.adx.clockwork.common.enums.BuildCommandType;
import com.creditease.adx.clockwork.common.enums.TaskExecuteType;
import com.creditease.adx.clockwork.common.enums.TaskStatus;
import com.creditease.adx.clockwork.common.exception.ShellFailureException;
import com.creditease.adx.clockwork.common.exception.TaskBeKilledException;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskLogPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.ReplaceParameterUtil;
import com.creditease.adx.clockwork.common.util.RetryUtil;
import com.creditease.adx.clockwork.common.util.TaskStatusUtil;
import com.creditease.adx.clockwork.worker.service.LinuxService;
import com.creditease.adx.clockwork.worker.service.TaskLogService;
import com.creditease.adx.clockwork.worker.service.TaskRunService;
import com.creditease.adx.clockwork.worker.service.TaskScriptService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 任务运行模版基础服务类
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:56 2020-04-26
 * @ Description：基础服务类
 * @ Modified By：
 */
public abstract class TaskExecuteService {

    protected static final Logger LOG = LoggerFactory.getLogger(TaskExecuteService.class);

    @Resource(name = "idClientService")
    protected IdClientService idClientService;

    @Resource(name = "taskClientService")
    protected TaskClientService taskClientService;

    @Resource(name = "taskOperationClientService")
    protected TaskOperationClientService taskOperationClientService;

    @Resource(name = "taskLogClientService")
    protected TaskLogClientService taskLogClientService;

    @Resource(name = "taskStateClientService")
    protected TaskStateClientService taskStateClientService;


    @Resource(name = "taskKeyWordClientService")
    protected TaskKeyWordClientService keyWordClientService;

    @Value("${spring.cloud.client.ipAddress}")
    protected String nodeIp;

    @Value("${node.build.command.type}")
    protected String buildCommandType;

    @Value("${task.run.log.dir}")
    protected String taskRunLogDir;

    protected int executeType;

    protected String executeName;

    @Autowired
    protected TaskRunService taskRunService;

    @Autowired
    private TaskLogService taskLogService;

    @Autowired
    protected TaskScriptService taskScriptService;

    @Autowired
    protected LinuxService linuxService;

    @PostConstruct
    public void init() {
        taskRunLogDir = taskRunLogDir.endsWith(File.separator) ? taskRunLogDir : taskRunLogDir + File.separator;
    }

    protected static long THREAD_POOL_KEEP_ALIVE_TIME = 5;

    protected static List<Class<?>> RETRY_EXCEPTION_CLASSES = new ArrayList<>();

    static {
        RETRY_EXCEPTION_CLASSES.add(ShellFailureException.class);
    }

    /**
     * execute task core process
     *
     * @param cell        run task basic cell
     * @param executeType execute type
     * @param executeName execute name
     * @return
     */
    public final boolean executeCore(TaskRunCell cell, int executeType, String executeName) {
        // Step 1 : Parameter detection and type detection
        boolean result = checkParameterHandle(cell, executeType, executeName);
        if (!result) {
            return false;
        }

        // Step 2 : Pre run tasks, file downloads, etc
        String logName = preTaskHandle(cell, executeType, executeName);
        if (logName == null) {
            return false;
        }

        // Step 3 : Process logic before task start
        result = beforeRunTaskHandle(cell, logName);
        if (!result) {
            return false;
        }

        // Step 4 : Processing task core logic
        result = runTaskHandle(cell, logName);
        if (!result) {
            return false;
        }

        //step 4.5: 根据生成的日志文件，去判断任务有没有绑定的关键词，扫描日志关键字，对任务的最终状态进行修改　
        result = checkTaskLog(cell, logName);
        if (!result) {
            return false;
        }
        // Step 5 : Process logic after task end
        result = afterRunTaskHandle(cell, logName, result);
        if (!result) {
            return false;
        }

        // Step 6 : Issue sub task
        return launchChildTasksHandle(cell, logName);
    }

    /**
     * @return boolean
     * @Description 检查任务日志，如果任务状态为success的，扫描它的关键词，根据关键词进行最终的任务状态控制
     * @Param [cell, logName]
     */
    private boolean checkTaskLog(TaskRunCell cell, String logName) {
        MDC.put("logFileName", logName);
        // 先判断任务状态是不是成功
        if (!(StringUtils.isNotBlank(cell.getTask().getStatus()) &&
                cell.getTask().getStatus().equals(TaskStatus.SUCCESS.getValue()))) {
            LOG.info("[checkTaskLog]task status not success dont need scan keywords, skip...");
            return true;
        }
        // 判断有没有绑定关键词
        if (StringUtils.isBlank(cell.getTask().getErrorKeywordIds())) {
            LOG.info("[checkTaskLog] keywords is null , skip...");
            return true;
        }
        // 找到这个日志所在位置
        File taskLogFile = null;
        BufferedReader reader = null;
        try {
            taskLogFile = new File(taskRunLogDir +
                    DateUtil.formatDate(new Date(), DateUtil.DATE_STD_STR) + File.separator + logName + ".log");
            reader = new BufferedReader(new FileReader(taskLogFile));
            // 读取文件内容
            String tempString = null;
            // 根据task中error_keyword, 返回一个关键字集合
            List<TbClockworkTaskErrorKeyword> errorKeywords =
                    keyWordClientService.getKeywordInfoBykeyIds(cell.getTask().getErrorKeywordIds());
            if (CollectionUtils.isEmpty(errorKeywords)) {
                LOG.info("[checkTaskLog]no scan keywords, skip...");
                return true;
            }
            int line = 1;
            boolean flag = false;
            StringBuilder contentBuilder = new StringBuilder();
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 在这处理具体逻辑,只读取最后一次
              if (tempString.contains("SHELL LOG BEGIN")) {
                flag = true;
                if (StringUtils.isNotBlank(contentBuilder.toString())){
                  contentBuilder.delete(0,contentBuilder.length());
                }
                continue;
              }
              if (tempString.contains("SHELL LOG END")) {
                    flag = false;
                    continue;
              }
              if (tempString.contains("[END!]")) {
                    break;
              }
              if (flag) {
                    contentBuilder.append(tempString);
              }
              line++;
            }
            boolean match = errorKeywords.stream().anyMatch(keyword -> contentBuilder.toString().contains(keyword.getErrorWord()));
            if (match) {
//                如果包含了关键字，说明任务运行其实是失败了，修改任务状态为失败
                LOG.error("[checkTaskLog]task contains keywords, task running failed, taskId = {}",
                        cell.getTask().getId());
                taskStateClientService.taskStateFinished(cell, cell.getTask().getTaskLogId(),
                        TaskStatus.FAILED.getValue(), true, -1);
                LOG.info("[END!]");
                return false;
            }
            LOG.error("[checkTaskLog]task has not keywords, task running success, taskId = {}, next step.",
                    cell.getTask().getId());
            reader.close();
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        } finally {
            MDC.remove("logFileName");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    LOG.error(e1.getMessage(), e1);
                }
            }
        }
    }

    /**
     * check parameters basic operation unit and task
     *
     * @param cell        cell
     * @param executeType execute type
     * @param executeName execute name
     * @return
     */
    private boolean checkParameterHandle(TaskRunCell cell, int executeType, String executeName) {
        TbClockworkTaskPojo task = cell.getTask();
        if (task == null) {
            LOG.warn("[checkParameterHandle][Don't have tasks need to rerun,task size = 0, skip current loop!");
            return false;
        }
        if (executeType != cell.getExecuteType() || task.getTaskLogId() == null) {
            LOG.warn("[checkParameterHandle][task executeType or task log id is error, skip current task " +
                            "taskId = {}, execute type is {} cell execute type is {}, task log id = {}",
                    task.getId(), executeType, cell.getExecuteType(), task.getTaskLogId());
            return false;
        }

        LOG.info("[checkParameterHandle]Get {} task to run, taskId = {}.", executeName, task.getId());

        LOG.info("[checkParameterHandle] execution {} task begin, taskId = {}, executeType = {}, script name = {}, "
                        + "script parameter = {}, location = {}, replace parameter = {}",
                executeName,
                task.getId(),
                executeType,
                task.getScriptName(),
                task.getScriptParameter(),
                task.getLocation(),
                task.getParameter());

        return true;
    }

    /**
     * Pre run tasks, file downloads, etc
     *
     * @param cell        cell
     * @param executeType execute type
     * @param executeName execute name
     * @return
     */
    private String preTaskHandle(TaskRunCell cell, int executeType, String executeName) {
        TbClockworkTaskPojo runTask = cell.getTask();
        String runtimeDirClientUrl = cell.getRuntimeDirClientUrl();

        int taskId = runTask.getId();
        Integer logId = runTask.getTaskLogId();
        String customBaseDate = null;
        String logName = null;

        try {
            // 创建一个空的日志文件, 记录相关日志
            logName = taskLogService.touchTaskExecuteLogFile(runTask, logId);
            if (StringUtils.isBlank(logName)) {
                taskStateClientService.taskStateFinished(cell, logId, TaskStatus.EXCEPTION.getValue(), false, -1);
                LOG.error("Touch task execute log file failure!");
                return null;
            }

            MDC.put("logFileName", logName);
            LOG.info("TbClockworkTaskPojo already queued, waiting to run......");
            LOG.info("taskId = {}", taskId);
            LOG.info("taskName = {}", runTask.getName());
            LOG.info("nodeIp = {}", nodeIp);
            LOG.info("executeType = {}", executeType);
            LOG.info("executeName = {}", executeName);
            LOG.info("createUser = {}", runTask.getCreateUser());

            // 处理相应的Cell类型的数据
            if (cell instanceof TaskRunCellFillData) {
                TaskRunCellFillData runTaskFillDataCell = (TaskRunCellFillData) cell;
                customBaseDate = runTaskFillDataCell.getFillDataTime();
                LOG.info("fillDataTime = {} \n reRunBatchNum = {}", customBaseDate, runTaskFillDataCell.getRerunBatchNumber());
            } else if (cell instanceof TaskRunCellReRun) {
                TaskRunCellReRun runTaskReRunCell = (TaskRunCellReRun) cell;
                LOG.info("reRunBatchNum = {}", runTaskReRunCell.getRerunBatchNumber());
            }
            LOG.info("LogName = {}", logName);

            // 参数校验：Status must be worker_has_receive to execute
            String status = taskClientService.getTaskStatusById(taskId);
            if (!TaskStatusUtil.canBeRunCurrentTaskStatus(status)) {
                taskStateClientService.taskStateFinished(cell, logId, TaskStatus.FAILED.getValue(), false, -1);
                LOG.error("The current task status doesn't WORKER_HAS_RECEIVE, " +
                        "can't be executed.task status = {}, task id = {}", status, taskId);
                return null;
            }

            // 获取依赖: 获取依赖脚本列表
            List<TbClockworkTaskDependencyScript> dependencyScriptFiles = taskClientService.getDependencyScriptFileByTaskId(taskId);
            if (CollectionUtils.isNotEmpty(dependencyScriptFiles)) {
                LOG.info("[Task dependency script file] Found script info. id = {}, taskGroupId = {}, files.size = {}",
                        runTask.getId(), runTask.getGroupId(), dependencyScriptFiles.size());
            } else {
                LOG.info("[Task dependency script file] Not found. id = {}, taskGroupId = {}", runTask.getId(), runTask.getGroupId());
            }

            // 下载依赖
            if (!downloadDependencyScriptFile(dependencyScriptFiles, runtimeDirClientUrl)) {
                taskStateClientService.taskStateFinished(cell, logId, TaskStatus.EXCEPTION.getValue(), false, -1);
                LOG.error("Task dependency script file download or replace parameter failure, taskId = {}", taskId);
                return null;
            }

            // 构建命令以及获取运行文件
            String runShellFilePath;
            if (BuildCommandType.LOAD_ENV.getValue().equals(buildCommandType)) {
                runShellFilePath = buildCommandAndGetRunShellFileForLoadEnv(cell);
            } else {
                runShellFilePath = buildCommandAndGetRunShellFile(cell, runTask.getLocation());
            }
            if (StringUtils.isBlank(runShellFilePath)) {
                taskStateClientService.taskStateFinished(cell, logId, TaskStatus.FAILED.getValue(), false, -1);
                LOG.error("[FillDataTaskExecuteService]runShellFilePath = {}", runShellFilePath);
                return null;
            }

            // 替换参数
            replaceParameterForExeScripts(runShellFilePath, runTask.getIsReplace(), runTask.getParameter(), customBaseDate);
            replaceParameterForDependencyScripts(dependencyScriptFiles, runTask.getParameter(), customBaseDate);

        } catch (Exception e) {
            LOG.error("[TaskExecuteService-preTaskHandle]taskId = {}, taskId = {}, logName = {}, Error {}.",
                    taskId, taskId, logName, e.getMessage(), e);
            taskStateClientService.taskStateFinished(cell, logId, TaskStatus.EXCEPTION.getValue(), false, -1);
            return null;
        } finally {
            MDC.remove("logFileName");
        }
        return logName;
    }

    public abstract <T extends TaskRunCell> boolean beforeRunTaskHandle(T runTaskCell, String logName);

    private boolean runTaskHandle(TaskRunCell cell, String logName) {

        // 获取重试次数
        int retryTimes = 0;
        Integer executeType = cell.getExecuteType();
        if (executeType != null && TaskExecuteType.ROUTINE.getCode().intValue() == executeType.intValue()) {
            TbClockworkTaskPojo task = cell.getTask();
            retryTimes = task.getFailedRetries();
            if (retryTimes < 0) retryTimes = 0;
            if (retryTimes > 2) retryTimes = 2;
        }

        // 本身运行一次，如果有尝试次数配置则相加，就是一共的运行次数
        retryTimes = retryTimes + 1;
        final int[] runNumber = {0};
        try {
            // 重试只针对ShellFailureException
            return RetryUtil.executeWithRetry(new Callable<Boolean>() {
                @Override
                public Boolean call() throws ShellFailureException, TaskBeKilledException {
                    return runTaskShell(cell, logName, ++runNumber[0]);
                }
            }, retryTimes, 1000, false, RETRY_EXCEPTION_CLASSES);
        } catch (Exception e) {
            // 异常状态处理
            if (e instanceof ShellFailureException) {
                taskStateClientService.taskStateFinished(
                        cell, cell.getTask().getTaskLogId(), TaskStatus.FAILED.getValue(), true, -1);
            } else {
                taskStateClientService.taskStateFinished(
                        cell, cell.getTask().getTaskLogId(), TaskStatus.EXCEPTION.getValue(), true, -1);
            }
            LOG.error("[TaskExecuteService-runTaskHandle] Error {}.", e.getMessage(), e);
            return false;
        } finally {
            MDC.remove("logFileName");
        }
    }

    public abstract <T extends TaskRunCell> boolean afterRunTaskHandle(T runTaskCell, String logName, boolean runTaskState);

    public abstract <T extends TaskRunCell> boolean launchChildTasksHandle(T runTaskCell, String logName);

    public abstract <T extends TaskRunCell> boolean checkParentsSuccess(T runTaskCell, TbClockworkTaskPojo childTask);

    /**
     * 下载依赖的文件
     *
     * @param dependencyScripts   依赖的脚本
     * @param runtimeDirClientUrl client
     * @return
     */
    private boolean downloadDependencyScriptFile(List<TbClockworkTaskDependencyScript> dependencyScripts, String runtimeDirClientUrl) {
        // 检查依赖是否有依赖的文件如果有则也需要替换参数
        try {
            if (CollectionUtils.isEmpty(dependencyScripts)) {
                LOG.info("[D]There are no dependent script files to download.");
                return true;
            }
            // 下载
            for (TbClockworkTaskDependencyScript dependencyScript : dependencyScripts) {
                String scriptFileAbsolutePath = dependencyScript.getScriptFileAbsolutePath();
                if (!dependencyScript.getIsDownload()) {
                    LOG.info("[D]dependency file not need to download, file path = {}", scriptFileAbsolutePath);
                    continue;
                }
                // 下载依赖的文件
                if (taskScriptService.downloadFile(scriptFileAbsolutePath, runtimeDirClientUrl)) {
                    LOG.info("[D]dependency file download to local success, file path = {}", scriptFileAbsolutePath);
                }
                // 下载到本地失败
                else {
                    LOG.info("[D]dependency file download to local failure, file path = {}", scriptFileAbsolutePath);
                    return false;
                }
            }

            // 检测依赖的文件本地是否已经存在了
            for (TbClockworkTaskDependencyScript dependencyScript : dependencyScripts) {
                try {
                    File file = new File(dependencyScript.getScriptFileAbsolutePath());
                    if (file.exists()) {
                        LOG.info("[D]Dependency file exist, dependencyScript = {}", dependencyScript);
                    } else {
                        LOG.warn("[D]Dependency file not exist, dependencyScript = {}", dependencyScript);
                    }
                } catch (Exception e) {
                    LOG.error("[D]Check dependency file Error {}.", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            LOG.info("[D]downloadAndReplaceDependencyScriptFile, Error {}.", e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 替换脚本参数（主脚本）
     *
     * @param exeScripts     执行文件
     * @param isReplace      是否可替换
     * @param parameter      自定义参数
     * @param customBaseDate 基准时间
     * @throws Exception
     */
    private void replaceParameterForExeScripts(String exeScripts, Boolean isReplace,
                                               String parameter, String customBaseDate) throws Exception {
        // 文件必须以[sh|py|mql|sql|hql]结尾
        if (!exeScripts.endsWith(".sh")
                && !exeScripts.endsWith(".py")
                && !exeScripts.endsWith(".mql")
                && !exeScripts.endsWith(".sql")
                && !exeScripts.endsWith(".hql")) {
            LOG.info("[R]skip main file replace  parameter. The file must end with " +
                    "[sh|py|mql|sql|hql], file = {}", exeScripts);
            return;
        }

        // 是否可替换
        if (isReplace != null && !isReplace) {
            try {
                LOG.info("[R]Do not have main scripts need to replace parameter. File: \n" +
                                "0.exeScripts = {},\n " +
                                "1.Parameter = {},\n " +
                                "2.File content : \n {}",
                        exeScripts, parameter, FileUtils.readFileToString(new File(exeScripts)));
            } catch (Exception e) {
                LOG.info("[R]Do not have main scripts need to replace parameter. File2: \n" +
                        "0.exeScripts = {},\n " +
                        "1.Parameter = {}", exeScripts, parameter);
            }
            return;
        }

        // 替换参数
        String content = ReplaceParameterUtil.replaceVariableReturnContent(exeScripts, parameter, customBaseDate);
        FileUtils.writeStringToFile(new File(exeScripts), content);
        LOG.info("[R]replace main file parameter success! ReplaceFile: \n" +
                        "0.exeScripts = {},\n " +
                        "1.Parameter = {},\n " +
                        "2.File content : \n {}",
                exeScripts, parameter, content);
    }

    /**
     * 替换脚本参数（依赖文件）
     *
     * @param dependencyScripts 依赖文件
     * @param parameter         自定义参数
     * @param customBaseDate    基准时间
     * @throws Exception
     */
    private void replaceParameterForDependencyScripts(List<TbClockworkTaskDependencyScript> dependencyScripts,
                                                      String parameter, String customBaseDate) throws Exception {
        if (CollectionUtils.isEmpty(dependencyScripts)) {
            LOG.info("[R]Do not have dependencyScripts need to replace parameter.");
            return;
        }
        for (TbClockworkTaskDependencyScript dependencyScript : dependencyScripts) {
            String scriptFileAbsolutePath = dependencyScript.getScriptFileAbsolutePath();
            // 文件必须以[sh|py|mql|sql|hql]结尾
            if (!scriptFileAbsolutePath.endsWith(".sh")
                    && !scriptFileAbsolutePath.endsWith(".py")
                    && !scriptFileAbsolutePath.endsWith(".mql")
                    && !scriptFileAbsolutePath.endsWith(".sql")
                    && !scriptFileAbsolutePath.endsWith(".hql")) {
                LOG.info("[R]skip replace dependency file parameter. The file must end with " +
                        "[sh|py|mql|sql|hql], file = {}", scriptFileAbsolutePath);
                continue;
            }
            if (!dependencyScript.getIsReplace()) {
                LOG.info("[R]dependency file not been replace parameter. file = {}",
                        scriptFileAbsolutePath);
                continue;
            }
            String content = ReplaceParameterUtil.replaceVariableReturnContent(scriptFileAbsolutePath, parameter, customBaseDate);
            FileUtils.writeStringToFile(new File(scriptFileAbsolutePath), content);
            LOG.info("[R]replace dependency file parameter success, " +
                            "file = {}, content : \n {}",
                    scriptFileAbsolutePath, content);
        }
    }

    /**
     * 构建命令以及获取运行文件
     *
     * @param cell
     * @param newFileDir
     * @return
     */
    private String buildCommandAndGetRunShellFile(TaskRunCell cell, String newFileDir) {
        try {
            TbClockworkTaskPojo runTask = cell.getTask();
            // 运行脚本: Location/ScriptName
            String runShellFilePath = runTask.getLocation().endsWith(File.separator) ?
                    runTask.getLocation() + runTask.getScriptName() : runTask.getLocation() + File.separator + runTask.getScriptName();

            // 构建新的脚本目录
            String dir = StringUtils.endsWith(newFileDir, File.separator) ? newFileDir : newFileDir + File.separator;

            // 获取脚本名称
            String fileName = runShellFilePath.substring(runShellFilePath.lastIndexOf(File.separator) + 1);
            if (fileName.lastIndexOf(".") != -1) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }

            // 目录下新生成脚本文件（唯一文件名：dir/DFS_20200101_fileName_uuid.sh）
            String filePath = dir + Constant.RUN_SCRIBE_PREFIX +
                    DateUtil.formatDate(new Date(), DateUtil.DATE_STD_STR_SIMPLE) + "_" +
                    fileName + "_" + idClientService.getUuid() + ".sh";
            File runShellFile = new File(filePath);
            if (!runShellFile.exists()) {
                FileUtils.touch(runShellFile);
                FileUtils.copyFile(new File(runShellFilePath), runShellFile);
            } else {
                throw new RuntimeException("[B]File already exist, file name = [" + filePath + "]");
            }
            runShellFilePath = runShellFile.getAbsolutePath();
            LOG.info("[B]runShellFilePath = {}", runShellFilePath);


            // 构建真正运行的Command, 把执行类型加载命令前面参数加到脚本后面
            String command = null;
            if (StringUtils.isEmpty(runTask.getScriptType())) {
                command = "sh " + runShellFilePath;
            } else {
                command = runTask.getScriptType() + " " + runShellFilePath;
            }

            if (StringUtils.isNotEmpty(runTask.getScriptParameter())) {
                command += " " + runTask.getScriptParameter();
            }

            // 修改真实runShellFilePath到日志
            cell.getTask().setCommand(command);
            taskLogClientService.updateTaskLogRealCommand(runTask.getTaskLogId(), command);
            LOG.info("[B]command = {}, logId = {}", command, runTask.getTaskLogId());

            // 构建cmd
            if (StringUtils.isNotBlank(runTask.getProxyUser())) {
                String[] cmd = {"/bin/sh", "-c", "sudo su " + runTask.getProxyUser() + " -l -c \'" + command + "\'"};
                cell.setCmd(cmd);
                LOG.info("[B]Script = {}, proxyUser = {}, proxyUserLength = {}",
                        Arrays.toString(cmd), runTask.getProxyUser(), runTask.getProxyUser().length());
            } else {
                String[] cmd = {"/bin/sh", "-c", command};
                cell.setCmd(cmd);
            }
            return runShellFilePath;
        } catch (Exception e) {
            LOG.error("[B][buildCommandAndGetRunShellFile] Error {}.", e.getMessage(), e);
            return null;
        }
    }

    private String buildCommandAndGetRunShellFileForLoadEnv(TaskRunCell cell) {
        try {
            // 获取运行脚本的相关参数
            TbClockworkTaskPojo runTask = cell.getTask();
            String scriptType = runTask.getScriptType();
            String scriptName = runTask.getScriptName();
            String location = runTask.getLocation();
            LOG.info("[B-LoadEnv]scriptType {}, scriptName = {}, location = {}", scriptType, scriptName, location);

            // scriptType scriptName
            StringBuilder command = new StringBuilder();
            command.append(StringUtils.isEmpty(scriptType) ? "sh" : scriptType);
            command.append(" ");
            command.append(scriptName);

            // 构建scriptParameter, 添加脚本参数scriptParameter
            if (StringUtils.isNotEmpty(runTask.getScriptParameter())) {
                command.append(" ").append(runTask.getScriptParameter());
            }

            // Command命令写入到日志
            cell.getTask().setCommand(command.toString());
            taskLogClientService.updateTaskLogRealCommand(runTask.getTaskLogId(), command.toString());
            LOG.info("[B-LoadEnv]command  = {}, logId = {}", command, runTask.getTaskLogId());

            // builder
            StringBuilder builder = new StringBuilder();
            if (StringUtils.isNotBlank(runTask.getProxyUser()) && !"default".equals(runTask.getProxyUser())) {
                builder.append("sudo -iu ").append(runTask.getProxyUser()).append(" -- bash -c \"");
                builder.append("cd ");
                builder.append(location);
                builder.append(";");
                builder.append(command);
                builder.append("\"");
            } else {
                builder.append("cd ");
                builder.append(location);
                builder.append(";");
                builder.append(command);
            }
            LOG.info("[B-LoadEnv]builder = {}, proxyUser = {}", builder, runTask.getProxyUser());
            String[] cmd = {"/bin/bash", "-c", builder.toString()};
            LOG.info("[B-LoadEnv]CMD = {}", Arrays.toString(cmd));
            cell.setCmd(cmd);
            return location.endsWith(File.separator) ? location + scriptName : location + File.separator + scriptName;
        } catch (Exception e) {
            LOG.error("[B-LoadEnv] Error {}.", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 运行单个任务
     *
     * @param cell    运行基本单元
     * @param logName 日志文件名
     * @return
     * @throws TaskBeKilledException
     * @throws ShellFailureException
     */
    private boolean runTaskShell(TaskRunCell cell, String logName, int runNumber) throws TaskBeKilledException, ShellFailureException {
        boolean result = false;
        Process process = null;
        ProcessBuilder builder = null;
        AtomicBoolean stop = new AtomicBoolean(false);
        TbClockworkTaskPojo task = cell.getTask();
        String[] cmd = cell.getCmd();
        Integer taskId = task.getId();
        Integer logId = task.getTaskLogId();

        MDC.put("logFileName", logName);
        LOG.info("Task starts running......");
        LOG.info("TaskId = {}", taskId);
        LOG.info("runNumber = {}", runNumber);
        try {
            LOG.info("Command = {}", task.getCommand());
            LOG.info("Script cmd = {}", Arrays.toString(cmd));
            builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            process = builder.start();

            // 获取该task命令的Linux进程号
            int pid = linuxService.getPid(process);
            if (runNumber == 1) {
                // 开始运行作业，更新作业开始状态
                taskStateClientService.taskStateRunning(cell, logId, pid, logName);
            } else {
                // 更新pid
                taskLogClientService.updateTaskLogPid(logId, pid);
            }
            LOG.info("task is now running, taskId = {}, logId = {}, PID = {}, status = {}.",
                    taskId, logId, pid, task.getStatus());

            MDC.remove("logFileName");

            // 获取任务执行日志线程
            new ReadShellScriptOutputLogThread(process, logName, stop).start();

            // 等待Unix操作系统进程是否终止，0 代表成功，其它都是失败或被手动杀死
            MDC.put("logFileName", logName);
            int executeCommandCode = process.waitFor();

            LOG.info("End of task operation, returnCode = {}", executeCommandCode);

            TbClockworkTaskPojo currentTask = taskClientService.getTaskById(taskId);
            String currentTaskStatus = currentTask.getStatus();

            if (executeCommandCode == 0) {
                // 任务执行成功后的任务状态
                // 任务成功的时候也要判断是不是杀死任务事务正在处理
                if (currentTaskStatus != null && currentTaskStatus.equals(TaskStatus.KILLING.getValue())) {
                    LOG.info("task is in killing transaction,which task status is success, " +
                                    "taskId = {}, logId = {}, PID = {}, status = {}, Command = {}.",
                            taskId, logId, pid, currentTaskStatus, task.getCommand());
                }
                // 【成功】任务time out killing
                else if (currentTaskStatus != null && currentTaskStatus.equals(TaskStatus.RUN_TIMEOUT_KILLING.getValue())) {
                    LOG.info("task is in time out killing transaction,which task status is success, " +
                                    "taskId = {}, logId = {}, PID = {}, status = {}, Command = {}.",
                            taskId, logId, pid, currentTaskStatus, task.getCommand());
                }
                //【成功】Task运行成功
                else {
                    currentTaskStatus = TaskStatus.SUCCESS.getValue();
                    LOG.info("task execute success, taskId = {}, logId = {}, PID = {}, status = {}, Command = {}, " +
                                    "Next, check the keywords to determine whether the operation is successful",
                            taskId, logId, pid, currentTaskStatus, task.getCommand());
                }
                taskStateClientService.taskStateFinished(cell, logId, currentTaskStatus, true, executeCommandCode);
                cell.getTask().setStatus(currentTaskStatus);
                result = true;
            } else {
                //【失败】检查状态是不是killing
                if (currentTaskStatus != null && currentTaskStatus.equals(TaskStatus.KILLING.getValue())) {
                    LOG.info("task is in killing transaction, taskId = {}, logId = {}, PID = {}, status = {}, Command = {}.",
                            taskId, logId, pid, currentTaskStatus, task.getCommand());
                    boolean killing = taskStateClientService.taskStateKilling(cell, logId, TaskStatus.KILLING.getValue(), executeCommandCode);
                    LOG.warn("task be killing! update task id = {} status = {}, result = {}.", task.getId(), TaskStatus.KILLING.getValue(), killing);
                    LOG.info("[END!]");
                    result = false;
                }
                // 【失败】检查状态是不是time out killing
                else if (currentTaskStatus != null && currentTaskStatus.equals(TaskStatus.RUN_TIMEOUT_KILLING.getValue())) {
                    // 内存更新为killing而不是killed原因，调用此函数的外部函数要使用此值判定失败的原因
                    LOG.info("task is in time out killing transaction, taskId = {}, logId = {}, PID = {}, status = {}, Command = {}.",
                            taskId, logId, pid, currentTaskStatus, task.getCommand());
                    boolean killing = taskStateClientService.taskStateKilling(cell, logId, TaskStatus.RUN_TIMEOUT_KILLING.getValue(), executeCommandCode);
                    LOG.warn("task be run_timeout_killing! update task id = {} status = {}, result = {}.", task.getId(), TaskStatus.RUN_TIMEOUT_KILLING.getValue(), killing);
                    LOG.info("[END!]");
                    result = false;
                }
                //【失败】任务脚本执行失败
                else {
                    currentTaskStatus = TaskStatus.FAILED.getValue();
                    LOG.info("task execute failure, taskId = {}, logId = {}, PID = {}, status = {}, Command = {}.",
                            taskId, logId, pid, currentTaskStatus, task.getCommand());
                    throw new ShellFailureException("shell failure");
                }
            }
        } catch (Exception e) {
            if (e instanceof ShellFailureException) {
                throw new ShellFailureException(e.getMessage());
            }
            // 其他异常
            LOG.error("RunTaskShell ERROR! Error: {}.", e.getMessage(), e);
            taskStateClientService.taskStateFinished(cell, logId, TaskStatus.FAILED.getValue(), true, -1);
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            // 任何情况结束之后都将停止标志位设置成true，以便打印日志线程和监控作业是否alive线程完成
            stop.set(true);
            MDC.remove("logFileName");
        }
        return result;
    }

    /**
     * remove task from queue
     *
     * @param queue  queue
     * @param taskId task id
     * @param <T>
     * @return
     */
    public <T extends TaskRunCell> int removeTaskExecuteServiceQueue(BlockingQueue<T> queue, int taskId) {
        int count = 0;
        Iterator<T> taskIterator = queue.iterator();
        while (taskIterator.hasNext()) {
            T next = taskIterator.next();
            TbClockworkTaskPojo task = next.getTask();
            if (task.getId() == taskId) {
                LOG.info("[removeTaskExecuteServiceQueue] existence taskId = {}.", taskId);
                queue.remove(next);
                count++;
                break;
            }
        }
        if (count == 0) {
            LOG.info("[removeTaskExecuteServiceQueue] not Existence taskId = {}.", taskId);
            return 0;
        }

        // 需要修改状态
        TbClockworkTaskLogPojo taskLog = taskLogClientService.getTaskLogByTaskId(taskId);
        if (taskLog != null && TaskStatus.WORKER_HAS_RECEIVE.getValue().equals(taskLog.getStatus())) {
            LOG.info("[removeTaskExecuteServiceQueue] Existence taskId = {}, taskLogId = {}", taskId, taskLog.getId());
            taskLogClientService.updateTaskLogEnd(taskLog.getId(), TaskStatus.KILLED.getValue(), -1);
        }
        return count;
    }

    /**
     * 读取Shell脚本运行时产生的日志类
     */
    private static class ReadShellScriptOutputLogThread extends Thread {
        private Process process;
        private String logName;
        private AtomicBoolean stop;

        private ReadShellScriptOutputLogThread(Process process, String logName, AtomicBoolean stop) {
            this.process = process;
            this.logName = logName;
            this.stop = stop;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                MDC.put("logFileName", logName);
                LOG.info("\n");
                LOG.info("##################### SHELL LOG BEGIN #######################");
                String line;
                while (!stop.get() && null != (line = in.readLine())) {
                    LOG.info(line);
                }
                LOG.info("###################### SHELL LOG END ########################");
                LOG.info("\n");
            } catch (Exception e) {
                LOG.error("Run shell Error. msg: {}", e.getMessage(), e);
            } finally {
                MDC.remove("logFileName");
            }
        }
    }
}
