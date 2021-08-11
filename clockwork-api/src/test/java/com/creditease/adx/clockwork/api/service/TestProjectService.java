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

package com.creditease.adx.clockwork.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkProjectUser;
import com.creditease.adx.clockwork.common.pojo.TbClockworkProjectPojo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestProjectService {

    @Autowired
    private IProjectService projectService;

    @Test
    public void testAddProject() {
        final TbClockworkProjectPojo projectPojo = new TbClockworkProjectPojo();
        projectPojo.setName("测试项目");
        projectPojo.setImgIdx(0);
        projectPojo.setCreateBy("xuedongcao2@clockwork.com");
        projectPojo.setVcores(1);
        projectPojo.setMemory(1);
        final int result = projectService.saveProject(projectPojo);
        System.out.println(String.format("result=%s", result));
    }

    @Test
    public void testInviteUser() {
        final TbClockworkProjectUser projectUser = new TbClockworkProjectUser();
        projectUser.setProjectId(1L);
        projectUser.setCreateBy("xuedongcao2@clockwork.com");
        projectUser.setUserName("xuandongtang@clockwork.com, muyuansun@clockwork.com ");
        final int result = projectService.inviteUsers(projectUser);
        System.out.println(String.format("result=%s", result));
    }

    @Test
    public void testGetMyProject() {
        String[] users = new String[]{"xuedongcao2@clockwork.com", "xuandongtang@clockwork.com"};
        for (String user : users) {
            System.out.println(String.format("Projects for %s:", user));
            projectService.getProjectsByUser(user).forEach(System.out::println);
            System.out.println();
        }
    }
}
