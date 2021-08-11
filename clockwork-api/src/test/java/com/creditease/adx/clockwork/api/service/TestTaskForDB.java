package com.creditease.adx.clockwork.api.service;

import com.creditease.adx.clockwork.common.entity.TaskRunCell;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskExample;
import com.creditease.adx.clockwork.common.enums.TaskSource;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.dao.mapper.clockwork.TbClockworkTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 10:07 上午 2020/12/4
 * @ Description：
 * @ Modified By：
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestTaskForDB {

    protected static final Logger LOG = LoggerFactory.getLogger(TestTaskForDB.class);

    @Autowired
    private TbClockworkTaskMapper tbClockworkTaskMapper;

    @Test
    public void buildCommandAndGetRunShellFileForDDS2Test() throws IOException {
        File file = new File("/Users/xuandongtang/Documents/cmd.txt");
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        TbClockworkTaskExample example = new TbClockworkTaskExample();
        example.createCriteria().andSourceEqualTo(TaskSource.DDS_2.getValue());
        example.setOrderByClause("id");

        List<TbClockworkTask> tbClockworkTasks = tbClockworkTaskMapper.selectByExample(example);
        for (TbClockworkTask tbClockworkTask : tbClockworkTasks) {
            String cmd = buildCommandAndGetRunShellFileForDDS2(tbClockworkTask);
            String content = tbClockworkTask.getId() + ": " + cmd +"\n";
            bw.write(content);
        }

        bw.close();

    }


    private String buildCommandAndGetRunShellFileForDDS2(TbClockworkTask runTask ) {
        try {
            // 运行脚本: Location/ScriptName
            String runShellFilePath = runTask.getLocation().endsWith(File.separator) ?
                    runTask.getLocation() + runTask.getScriptName() : runTask.getLocation() + File.separator + runTask.getScriptName();

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
//            LOG.info("[B-DDS]command = {}", command);

            // 构建cmd
            String cmdStr = null ;
            if (StringUtils.isNotBlank(runTask.getProxyUser())) {
                String[] cmd = {"/bin/bash", "-c", "sudo su " + runTask.getProxyUser() + " -l -c \'" + command + "\'"};
                cmdStr = Arrays.toString(cmd);
            } else {
                String[] cmd = {"/bin/bash", "-c", command};
                cmdStr = Arrays.toString(cmd);
            }
            return cmdStr;
        } catch (Exception e) {
            LOG.error("[B-DDS] Error {}.", e.getMessage(), e);
            return null;
        }
    }


}
