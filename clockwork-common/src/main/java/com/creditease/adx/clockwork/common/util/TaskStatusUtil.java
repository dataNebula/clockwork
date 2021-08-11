package com.creditease.adx.clockwork.common.util;

import com.creditease.adx.clockwork.common.enums.TaskStatus;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TaskStatusUtil {

    /**
     * 获得完结的任务状态信息
     */
    private static Map<String, String> getFinishedTaskStatus() {
        Map<String, String> result = new HashMap<>();
        result.put(
                TaskStatus.ENABLE.getValue(),
                TaskStatus.ENABLE.getValue()
        );

        result.put(
                TaskStatus.SUCCESS.getValue(),
                TaskStatus.SUCCESS.getValue()
        );

        result.put(
                TaskStatus.FAILED.getValue(),
                TaskStatus.FAILED.getValue()
        );

        result.put(
                TaskStatus.KILLED.getValue(),
                TaskStatus.KILLED.getValue()
        );

        result.put(
                TaskStatus.RUN_TIMEOUT_KILLED.getValue(),
                TaskStatus.RUN_TIMEOUT_KILLED.getValue()
        );

        result.put(
                TaskStatus.FATHER_NOT_SUCCESS.getValue(),
                TaskStatus.FATHER_NOT_SUCCESS.getValue()
        );

        result.put(
                TaskStatus.EXCEPTION.getValue(),
                TaskStatus.EXCEPTION.getValue()
        );

        result.put(
                TaskStatus.LIFE_CYCLE_RESET.getValue(),
                TaskStatus.LIFE_CYCLE_RESET.getValue()
        );
        return result;
    }

    /**
     * 获得属于运行态的任务状态（未完结状态）
     */
    private static Map<String, String> getStartedTaskStatus() {
        Map<String, String> result = new HashMap<>();
        result.put(
                TaskStatus.SUBMIT.getValue(),
                TaskStatus.SUBMIT.getValue()
        );
        result.put(
                TaskStatus.MASTER_HAS_RECEIVE.getValue(),
                TaskStatus.MASTER_HAS_RECEIVE.getValue()
        );

        result.put(
                TaskStatus.WORKER_HAS_RECEIVE.getValue(),
                TaskStatus.WORKER_HAS_RECEIVE.getValue()
        );

        result.put(
                TaskStatus.RUNNING.getValue(),
                TaskStatus.RUNNING.getValue()
        );

        result.put(
                TaskStatus.KILLING.getValue(),
                TaskStatus.KILLING.getValue()
        );

        result.put(
                TaskStatus.RUN_TIMEOUT_KILLING.getValue(),
                TaskStatus.RUN_TIMEOUT_KILLING.getValue()
        );
        return result;
    }

    /**
     * 重启任务可以被下发状态（RERUN_SCHEDULE_PREP）
     */
    private static Map<String, String> getCanBeLaunchReRunTasksStatus() {
        Map<String, String> result = new HashMap<>();

        result.put(
                TaskStatus.ENABLE.getValue(),
                TaskStatus.ENABLE.getValue()
        );

        result.put(
                TaskStatus.RERUN_SCHEDULE_PREP.getValue(),
                TaskStatus.RERUN_SCHEDULE_PREP.getValue()
        );

        result.put(
                TaskStatus.SUCCESS.getValue(),
                TaskStatus.SUCCESS.getValue()
        );

        result.put(
                TaskStatus.FAILED.getValue(),
                TaskStatus.FAILED.getValue()
        );

        result.put(
                TaskStatus.KILLED.getValue(),
                TaskStatus.KILLED.getValue()
        );

        result.put(
                TaskStatus.RUN_TIMEOUT_KILLED.getValue(),
                TaskStatus.RUN_TIMEOUT_KILLED.getValue()
        );

        result.put(
                TaskStatus.FATHER_NOT_SUCCESS.getValue(),
                TaskStatus.FATHER_NOT_SUCCESS.getValue()
        );

        result.put(
                TaskStatus.EXCEPTION.getValue(),
                TaskStatus.EXCEPTION.getValue()
        );

        result.put(
                TaskStatus.LIFE_CYCLE_RESET.getValue(),
                TaskStatus.LIFE_CYCLE_RESET.getValue()
        );

        return result;
    }

    /**
     * 获得可以被运行的任务状态
     */
    private static Map<String, String> getCanBeRunTaskStatus() {
        Map<String, String> result = new HashMap<>();
        result.put(
                TaskStatus.WORKER_HAS_RECEIVE.getValue(),
                TaskStatus.WORKER_HAS_RECEIVE.getValue()
        );
        result.put(
                TaskStatus.LIFE_CYCLE_RESET.getValue(),
                TaskStatus.LIFE_CYCLE_RESET.getValue()
        );
        return result;
    }


    /**
     * 获得失败状态的任务状态信息（KILLED暂不考虑）
     */
    public static Map<String, String> getFailedTaskStatus() {
        Map<String, String> result = new HashMap<>();
        result.put(
                TaskStatus.FAILED.getValue(),
                TaskStatus.FAILED.getValue()
        );

        result.put(
                TaskStatus.RUN_TIMEOUT_KILLED.getValue(),
                TaskStatus.RUN_TIMEOUT_KILLED.getValue()
        );

        result.put(
                TaskStatus.EXCEPTION.getValue(),
                TaskStatus.EXCEPTION.getValue()
        );
        return result;
    }


    /**
     * 该状态是否是结束态
     *
     * @param status 状态
     * @return 结束态返回true
     */
    public static boolean isFinishedTaskStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        Map<String, String> isStartedTaskStatus = getFinishedTaskStatus();
        return isStartedTaskStatus.get(status) != null;
    }


    /**
     * 该状态是否是运行态
     *
     * @param status 状态
     * @return 运行态返回true
     */
    public static boolean isStartedTaskStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        Map<String, String> isStartedTaskStatus = getStartedTaskStatus();
        return isStartedTaskStatus.get(status) != null;
    }

    /**
     * 是否可以被提交（即完结状态可被提交）
     *
     * @param status 状态
     * @return 可以被提交返回true
     */
    public static boolean canBeSubmitCurrentTaskStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        Map<String, String> canBeSubmitTaskStatus = getFinishedTaskStatus();
        return canBeSubmitTaskStatus.get(status) != null;
    }

    /**
     * 是否可以下发（重启子任务下发）
     *
     * @param status 状态
     * @return 可以被下发返回true
     */
    public static boolean canBeLaunchReRunTasksStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        Map<String, String> canBeSubmitTaskStatus = getCanBeLaunchReRunTasksStatus();
        return canBeSubmitTaskStatus.get(status) != null;
    }


    /**
     * 是否可以被运行
     *
     * @param status 状态
     * @return 可以被运行返回true
     */
    public static boolean canBeRunCurrentTaskStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        Map<String, String> canBeReRunTaskStatus = getCanBeRunTaskStatus();
        return canBeReRunTaskStatus.get(status) != null;
    }


    /**
     * 否可以被kill
     *
     * @param status 状态
     * @return 可以被kill返回true
     */
    public static boolean canBeKilledCurrentTaskStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        return status.equals(TaskStatus.RUNNING.getValue());
    }


    /**
     * 是否可以被删除（即完结状态可被删除）
     *
     * @param status 状态
     * @return 可以被删除返回true
     */
    public static boolean canBeDeleteCurrentTaskStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        Map<String, String> canBeDeleteTaskStatus = getFinishedTaskStatus();
        return canBeDeleteTaskStatus.get(status) != null;
    }


    /**
     * 是否可以被修改（即完结状态可被修改）
     *
     * @param status 状态
     * @return 可以被修改返回true
     */
    public static boolean canBeUpdateCurrentTaskStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return false;
        }
        Map<String, String> canBeUpdateTaskStatus = getFinishedTaskStatus();
        return canBeUpdateTaskStatus.get(status) != null;
    }


}
