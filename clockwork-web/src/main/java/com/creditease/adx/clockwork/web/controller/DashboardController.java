package com.creditease.adx.clockwork.web.controller;

import com.creditease.adx.clockwork.common.entity.Response;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.pojo.TbClockworkUserPojo;
import com.creditease.adx.clockwork.web.service.IDashboardService;
import com.creditease.adx.clockwork.web.service.IUserService;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:11 上午 2020/9/27
 * @ Description：DashboardController
 * @ Modified By：
 */

@Api("WEB任务获取相关接口")
@RestController
@RequestMapping("/clockwork/web/dashboard")
public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private IDashboardService dashboardService;

    @Resource(name = "userService")
    private IUserService userService;

    /**
     * 总任务运行情况
     *
     * @param userName userName
     * @return
     */
    @GetMapping(value = "/taskTotalRunStatus")
    public Map<String, Object> taskTotalRunStatus(@RequestParam(value = "userName") String userName) {

        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("DashboardController-taskTotalRunStatus, invalid userName");
                return Response.fail("invalid userName");
            }
            TbClockworkUserPojo userAndRole = userService.getUserAndRoleByUserName(userName);
            String createUser = null, roleName = null;
            if (userAndRole == null) {
                createUser = userName;
            } else if (!userAndRole.getIsAdmin()) {
                roleName = userAndRole.getRoleName();
            }
            return Response.success(dashboardService.getTaskTotalRunStatus(createUser, roleName));
        } catch (Exception e) {
            LOG.error("DashboardController-taskTotalRunStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 今日任务运行情况（当天）
     *
     * @param userName userName
     * @return
     */
    @GetMapping(value = "/taskTodayRunStatus")
    public Map<String, Object> taskTodayRunStatus(@RequestParam(value = "userName") String userName) {
        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("DashboardController-taskTodayRunStatus, invalid userName");
                return Response.fail("invalid userName");
            }
            TbClockworkUserPojo userAndRole = userService.getUserAndRoleByUserName(userName);
            String createUser = null, roleName = null;
            if (userAndRole == null) {
                createUser = userName;
            } else if (!userAndRole.getIsAdmin()) {
                roleName = userAndRole.getRoleName();
            }
            return Response.success(dashboardService.getTaskTodayRunStatus(createUser, roleName));
        } catch (Exception e) {
            LOG.error("DashboardController-taskTodayRunStatus Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 任务每小时成功数（当天）
     *
     * @param userName 用户名
     * @return
     */
    @GetMapping(value = "/getTaskHourSuccess")
    public Map<String, Object> getTaskHourSuccess(@RequestParam(value = "userName") String userName) {

        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("DashboardController-getTaskHourSuccess, invalid userName");
                return Response.fail("invalid userName");
            }

            TbClockworkUserPojo userAndRole = userService.getUserAndRoleByUserName(userName);
            String createUser = null, roleName = null;
            if (userAndRole == null) {
                createUser = userName;
            } else if (!userAndRole.getIsAdmin()) {
                roleName = userAndRole.getRoleName();
            }
            return Response.success(dashboardService.getTaskHourSuccess(createUser, roleName));
        } catch (Exception e) {
            LOG.error("DashboardController-getTaskHourSuccess Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 任务每小时失败数（当天）
     *
     * @param userName 用户名
     * @return
     */
    @GetMapping(value = "/getTaskHourFailed")
    public Map<String, Object> getTaskHourFailed(@RequestParam(value = "userName") String userName) {

        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("DashboardController-getTaskHourFailed, invalid userName");
                return Response.fail("invalid userName");
            }

            TbClockworkUserPojo userAndRole = userService.getUserAndRoleByUserName(userName);
            String createUser = null, roleName = null;
            if (userAndRole == null) {
                createUser = userName;
            } else if (!userAndRole.getIsAdmin()) {
                roleName = userAndRole.getRoleName();
            }
            return Response.success(dashboardService.getTaskHourFailed(createUser, roleName));
        } catch (Exception e) {
            LOG.error("DashboardController-getTaskHourFailed Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 查询任务节点运行情况（当天）
     *
     * @param userName 用户名
     * @return
     */
    @GetMapping(value = "/getTaskNodeRunCount")
    public Map<String, Object> getTaskNodeRunCount(@RequestParam(value = "userName") String userName) {

        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("DashboardController-getTaskNodeRunCount, invalid userName");
                return Response.fail("invalid userName");
            }

            TbClockworkUserPojo userAndRole = userService.getUserAndRoleByUserName(userName);
            String createUser = null, roleName = null;
            if (userAndRole == null) {
                createUser = userName;
            } else if (!userAndRole.getIsAdmin()) {
                roleName = userAndRole.getRoleName();
            }
            return Response.success(dashboardService.getTaskNodeRunCount(createUser, roleName));
        } catch (Exception e) {
            LOG.error("DashboardController-getTaskNodeRunCount Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }

    /**
     * 每小时运行任务数（当天）
     *
     * @param userName 用户名
     * @return
     */
    @GetMapping(value = "/getTaskNodeHourRun")
    public Map<String, Object> getTaskNodeHourRun(@RequestParam(value = "userName") String userName) {

        try {
            if (StringUtils.isBlank(userName)) {
                LOG.error("DashboardController-getTaskNodeHourRun, invalid userName");
                return Response.fail("invalid userName");
            }

            TbClockworkUserPojo userAndRole = userService.getUserAndRoleByUserName(userName);
            String createUser = null, roleName = null;
            if (userAndRole == null) {
                createUser = userName;
            } else if (!userAndRole.getIsAdmin()) {
                roleName = userAndRole.getRoleName();
            }
            return Response.success(dashboardService.getTaskNodeHourRun(createUser, roleName));
        } catch (Exception e) {
            LOG.error("DashboardController-getTaskNodeHourRun Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 获取等待运行的任务
     *
     * @return
     */
    @GetMapping(value = "/getWaitForRunTask")
    public Map<String, Object> getWaitForRunTask(@RequestParam(value = "size") Integer size,
                                                 @RequestParam(value = "userName") String userName) {
        try {
            List<TbClockworkTaskPojo> tasks = dashboardService.getWaitForRunTask(size);
            LOG.info("[TaskController-getWaitForRunTask] tasks.size = {}", tasks != null ? tasks.size() : 0);
            return Response.success(tasks);
        } catch (Exception e) {
            LOG.error("getWaitForRunTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 获取最近运行失败的任务
     *
     * @return
     */
    @GetMapping(value = "/getLatelyFailedTask")
    public Map<String, Object> getLatelyFailedTask(@RequestParam(value = "size") Integer size,
                                                   @RequestParam(value = "userName") String userName) {
        try {
            List<TbClockworkTaskPojo> tasks = dashboardService.getLatelyFailedTask(size);
            LOG.info("[TaskController-getLatelyFailedTask] tasks.size = {}", tasks != null ? tasks.size() : 0);
            return Response.success(tasks);
        } catch (Exception e) {
            LOG.error("getLatelyFailedTask Error {}.", e.getMessage(), e);
            return Response.fail(e.getMessage());
        }
    }


}
