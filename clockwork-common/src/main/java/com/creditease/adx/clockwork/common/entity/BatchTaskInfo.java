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

package com.creditease.adx.clockwork.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by xuandongtang on 2019/4/13.
 */
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchTaskInfo {

  private String tasksJson;
  private String nodeId;
  private String isAuto;

  public String getTasksJson() {
    return tasksJson;
  }

  public void setTasksJson(String tasksJson) {
    this.tasksJson = tasksJson;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getIsAuto() {
    return isAuto;
  }

  public void setIsAuto(String isAuto) {
    this.isAuto = isAuto;
  }
}
