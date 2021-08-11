package com.creditease.adx.clockwork.master.monitor;

import com.creditease.adx.clockwork.client.service.DagCheckClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @ClassName: DagCheckMonitor
 * @Author: ltb
 * @Date: 2021/3/12:5:33 下午
 * @Description:
 */
@Service
public class DagCheckMonitor {

    private final Logger LOG = LoggerFactory.getLogger(DagCheckMonitor.class);

    @Autowired
    private DagCheckClientService dagCheckClient;

    @Scheduled(cron = "${monitor.dag.check.cron.exp}")
    public void taskLifeCycleMonitor() {
        try {
            boolean isSuccess = dagCheckClient.checkAllDags("system-master");
            if (isSuccess) {
                LOG.info("[DagCheckMonitor.taskLifeCycleMonitor] start.");
            } else {
                LOG.warn("[DagCheckMonitor.taskLifeCycleMonitor] dont add task , please check dagCheckQueue, " +
                        "if dagCheckQueue is not empty, this client will return false.");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
