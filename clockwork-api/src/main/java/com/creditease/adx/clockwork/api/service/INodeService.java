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


import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.pojo.TbClockworkNodePojo;

import java.util.List;

public interface INodeService {

    boolean nodeStartInit(final String ip, final String port, final String role);

    List<TbClockworkNode> getAllNode();

    TbClockworkNode getNodeById(int id);

    TbClockworkNode getNodeByIpAndPort(String ip, String port);

    List<TbClockworkNode> getAllEnableNodeByRole(String role);

    List<TbClockworkNode> getAllEnableNodeByRoleAndGroupId(String role, int nodeGroupId);

    int addNode(TbClockworkNodePojo tbClockworkNodePojo);

    int updateNode(TbClockworkNodePojo tbClockworkNodePojo);

    int disableNode(int id);

}
