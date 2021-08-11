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

package com.creditease.adx.clockwork.worker.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 14:28 2019-11-28
 * @ Description：LinuxService
 * @ Modified By：
 */
@Service
public class LinuxService {

    private static final Logger LOG = LoggerFactory.getLogger(LinuxService.class);

    /**
     * 获取任务的进程PID信息
     */
    public Set<String> getPidsByCommand(String command1) {
        Set<String> result = new HashSet<>();
        Queue<String> fatherPidQueue = new LinkedList<>();
        Process p = null;
        try {

            String command2 = command1.replaceAll("\\[", "\\\\[").replaceAll("]", "\\\\]");
            String run_command = String.format("ps -ef | grep '%s' | grep -v grep | awk {'print $2'}", command2);
            LOG.info("[getPidsByCommand]run_command = {}", run_command);
            // 这里不加这行代码直接执行命令会报错，因为Linux不认java版本的管道
            String[] cmdArray = {"/bin/sh", "-c", run_command};
            p = Runtime.getRuntime().exec(cmdArray);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if (StringUtils.isEmpty(line)) {
                    LOG.warn("[getPidsByCommand]find process info, but pid is null, run_command = {}", run_command);
                    continue;
                }
                LOG.info("[getPidsByCommand]find process info, pid = {}, run_command = {}", line.trim(), run_command);
                result.add(line.trim());
                fatherPidQueue.offer(line.trim());
            }

            if (CollectionUtils.isEmpty(result)) {
                return result;
            }

            // 获得当前进程的子进程PID信息
            String parentPid;
            Set<String> skip = new HashSet<>();
            while ((parentPid = fatherPidQueue.poll()) != null) {
                if (skip.contains(parentPid)) continue;
                skip.add(parentPid);
                String subPidsInfo = getSubProcessTreeInfo(parentPid);
                if (StringUtils.isBlank(subPidsInfo)) {
                    LOG.info("[getPidsByCommand]No found sub process info, parentPid = {}, run_command = {}",
                            parentPid, run_command);
                    continue;
                }
                LOG.info("[getPidsByCommand]Found sub process info, parentPid = {}, run_command = {}, subPidsInfo = {}",
                        parentPid, run_command, subPidsInfo);
                String[] subProcessPids = StringUtils.substringsBetween(subPidsInfo, "(", ")");
                for (String subProcessPid : subProcessPids) {
                    result.add(subProcessPid.trim());
                    fatherPidQueue.offer(subProcessPid.trim());
                    LOG.info("[getPidsByCommand]add sub process pid to result, parentPid = {}, run_command = {}, "
                            + "sub process pid = {}", parentPid, run_command, subProcessPid);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (p != null) {
                    p.destroyForcibly();
                }
            } catch (Exception e) {
                //ignore
            }
        }
        return result;
    }

    /**
     * 获取进程的pid
     *
     * @param p
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public int getPid(Process p) throws NoSuchFieldException, IllegalAccessException {
        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            return (Integer) f.get(p);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return -1;
    }

    /**
     * kill -15 杀进程
     *
     * @param pid pid
     */
    public void kill15TaskByPid(int pid) {
        Process p = null;
        try {
            // 先尝试杀死Linux本地驱动进程
            p = Runtime.getRuntime().exec("kill -15 " + pid);
            int code = p.waitFor();
            // 只有code为0是成功
            if (code == 0) {
                LOG.info("[LinuxService-kill15TaskByPid]kill process success！pid = {}, return code = {}", pid, code);
            } else {
                LOG.info("[LinuxService-kill15TaskByPid]kill process unknown！pid = {}, return code = {}", pid, code);
                // 使用超级权限kill
                rootCommand("kill -15 " + pid);
            }
        } catch (Exception e) {
            LOG.error("[LinuxService-kill15TaskByPid] Error {}.", e.getMessage(), e);
        } finally {
            try {
                if (p != null) {
                    p.destroyForcibly();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * kill -9 杀进程
     *
     * @param pid pid
     */
    public void kill9TaskByPid(int pid) {
        Process p = null;
        try {
            // 先尝试杀死Linux本地驱动进程
            p = Runtime.getRuntime().exec("kill -9 " + pid);
            int code = p.waitFor();
            // 只有code为0是成功
            if (code == 0) {
                LOG.info("[LinuxService-kill9TaskByPid]kill process success！pid = {}, return code = {}", pid, code);
            } else {
                LOG.info("[LinuxService-kill9TaskByPid]kill process unknown！pid = {}, return code = {}", pid, code);
                rootCommand("kill -9 " + pid);
            }
        } catch (Exception e) {
            LOG.error("[LinuxService-kill9TaskByPid] Error {}.", e.getMessage(), e);
        } finally {
            try {
                if (p != null) {
                    p.destroyForcibly();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * 使用root权限执行
     *
     * @param command command
     */
    public void rootCommand(String command) {
        Process su = null;
        DataOutputStream dos = null;
        try {
            // 使用超级权限执行命令
            su = Runtime.getRuntime().exec("sudo su -"); // root
            dos = new DataOutputStream(su.getOutputStream());
            dos.writeBytes(command + "\n");
            dos.writeBytes("exit\n");
            dos.flush();
            int code = su.waitFor();
            // 只有code为0是成功
            if (code == 0) {
                LOG.info("[LinuxService-rootCommand] success！command = {}, return code = {}", command, code);
            } else {
                LOG.info("[LinuxService-rootCommand] unknown！command = {}, return code = {}", command, code);
            }
        } catch (Exception e) {
            LOG.error("[LinuxService-rootCommand] Error {}.", e.getMessage(), e);
        } finally {
            try {
                if (su != null) {
                    su.destroyForcibly();
                }
                if (dos != null) {
                    dos.close();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }


    // 根据pid获得子进程pid信息
    private String getSubProcessTreeInfo(String parentPid) {
        if (StringUtils.isBlank(parentPid)) {
            return null;
        }
        Process p = null;
        try {
            String command = String.format("pstree -p %s", parentPid);
            String[] cmdArray = {"/bin/sh", "-c", command};
            p = Runtime.getRuntime().exec(cmdArray);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if (StringUtils.isEmpty(line)) {
                    LOG.warn("[getSubProcessTreeInfo]find process info is null,command = {}", command);
                    continue;
                }
                LOG.warn("[getSubProcessTreeInfo]find process info {},command = {}", line, command);
                return line;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (p != null) {
                    p.destroyForcibly();
                }
            } catch (Exception e) {
                //ignore
            }
        }
        return null;
    }

}
