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

package com.creditease.adx.clockwork.worker;

import com.creditease.adx.clockwork.common.exception.TaskBeKilledException;
import com.creditease.adx.clockwork.common.exception.ShellFailureException;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DateUtil;
import com.creditease.adx.clockwork.common.util.RetryUtil;
import com.google.inject.internal.cglib.proxy.$Dispatcher;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-07-01
 */
public class TaskExecuteTest {



    @Test
    public void test1(){

        List<TbClockworkTaskPojo> tasks = new ArrayList<>();
        tasks.add(new TbClockworkTaskPojo());
        tasks.add(new TbClockworkTaskPojo());
        System.out.println(tasks.size());
        for (TbClockworkTaskPojo task : tasks) {
            for (int i = 0; i < 2; i++) {
                task.setTaskLogId(i);
                tasks.add(task);
            }
        }
        System.out.println(tasks.size());
    }

    @Test
    public void testFileCreate(){


        File file = new File("/tmp/abc/test.sh");
        try {
            if(!file.exists()){
                FileUtils.touch(file);
                System.out.println(file.exists());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    @Test
    public void testReExp(){

        String str = "aa[2323]bb";
        str = str.replaceAll("\\[", "\\\\[").replaceAll("]", "\\\\]");
        System.out.println(str);


    }


    private boolean runTask() throws ShellFailureException, TaskBeKilledException {


        boolean result = false;
        Process executeCommand = null;
        BufferedReader br = null;
        try {
            System.out.println("##########################################################################");
            String[] cmd = {"/bin/sh", "-c", "sh /tmp/abcd.sh"};
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            executeCommand = builder.start();

            //获取该task命令的Linux进程号
            //int pid = getPid(executeCommand);

//            br = new BufferedReader(new InputStreamReader(executeCommand.getInputStream()));
//            String line;
//
//            while (null != (line = br.readLine())) {
//                System.out.println(DateUtils.formatDate(new Date(), DateUtils.DATE_FULL_STR) + " == " + line);
//            }

            Thread t = new ReadLogThread(executeCommand);
            t.start();

//            while (executeCommand.isAlive()) {
//                System.out.println("wait------------>>>" + DateUtils.formatDate(new Date(), DateUtils.DATE_FULL_STR));
//                Thread.sleep(2000);
//            }
//
//            System.out.println("wait------------>>>11111111" + DateUtils.formatDate(new Date(), DateUtils.DATE_FULL_STR));

            int executeCommandCode = executeCommand.waitFor();

            System.out.println("wait------------>>>22222222" + DateUtil.formatDate(new Date(), DateUtil.DATE_FULL_STR));

            System.out.println(DateUtil.formatDate(new Date(), DateUtil.DATE_FULL_STR) + "============================>>>>>executeCommandCode = "
                    + executeCommandCode);

            if (executeCommandCode == 0) {
                System.out.println("成功，code = " + executeCommandCode);
                result = true;
            } else {
                if (executeCommandCode == 143 || executeCommandCode == 137) {
                    throw new TaskBeKilledException("task be killed");
                } else {
                    throw new ShellFailureException("shell failure");
                }
            }
            return result;
        } catch (Exception e) {
            if (e instanceof TaskBeKilledException) {
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                throw new TaskBeKilledException(e.getMessage());
            } else {
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                throw new ShellFailureException(e.getMessage());
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (executeCommand != null) {
                    executeCommand.destroyForcibly();
                }
            } catch (Exception e) {
                // ignore
            }
        }

    }



    @Test
    public void testTaskExecuteAndRetry() {

        List <Class <?>> RETRY_EXCEPTION_CLASSES = new ArrayList <>();
        RETRY_EXCEPTION_CLASSES.add(ShellFailureException.class);
        boolean result = false;
        try {

            result = RetryUtil.executeWithRetry(new Callable <Boolean>() {
                @Override
                public Boolean call() throws ShellFailureException, TaskBeKilledException {
                    return runTask();
                }
            }, 2, 5000, false, RETRY_EXCEPTION_CLASSES);

        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }

        System.out.println("&&&&&&&&&&&&&&>>>>>> result = " + result);

    }

    /**
     * 获取task进程的pid
     */
    protected int getPid(Process p) throws NoSuchFieldException, IllegalAccessException {
        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            return (Integer) f.get(p);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static class ReadLogThread extends Thread {

        private Process process = null;

        public ReadLogThread(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while (null != (line = in.readLine())) {
                    System.out.println(DateUtil.formatDate(new Date(), DateUtil.DATE_FULL_STR) + " == " + line);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }


    @Test
    public void test() throws Exception{

        File file = new File("/opt/tmp/a.sh");
        URL url = new URL("http://127.0.0.1:9007/clockwork/dfs/file/downloadFile?fileAbsolutePath=/tmp/id8f0a368d_date210919");
        FileUtils.copyURLToFile(url, file);

    }
}
