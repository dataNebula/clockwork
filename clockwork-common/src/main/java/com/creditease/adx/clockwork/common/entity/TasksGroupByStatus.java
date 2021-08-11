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

import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-10-16
 */
public class TasksGroupByStatus {

    List<TbClockworkTaskPojo> canBeSubmitTasks = null;

    List<TbClockworkTaskPojo> fathersNoSuccessTasks = null;

    List<TbClockworkTaskPojo> notFinishedTasks = null;

    List<TbClockworkTaskPojo> triggerModelIncorrectTasks = null;

    public List <TbClockworkTaskPojo> getCanBeSubmitTasks() {
        return canBeSubmitTasks;
    }

    public void setCanBeSubmitTasks(List <TbClockworkTaskPojo> canBeSubmitTasks) {
        this.canBeSubmitTasks = canBeSubmitTasks;
    }

    public List <TbClockworkTaskPojo> getFathersNoSuccessTasks() {
        return fathersNoSuccessTasks;
    }

    public void setFathersNoSuccessTasks(List <TbClockworkTaskPojo> fathersNoSuccessTasks) {
        this.fathersNoSuccessTasks = fathersNoSuccessTasks;
    }

    public List <TbClockworkTaskPojo> getNotFinishedTasks() {
        return notFinishedTasks;
    }

    public void setNotFinishedTasks(List <TbClockworkTaskPojo> notFinishedTasks) {
        this.notFinishedTasks = notFinishedTasks;
    }

    public List <TbClockworkTaskPojo> getTriggerModelIncorrectTasks() {
        return triggerModelIncorrectTasks;
    }

    public void setTriggerModelIncorrectTasks(List <TbClockworkTaskPojo> triggerModelIncorrectTasks) {
        this.triggerModelIncorrectTasks = triggerModelIncorrectTasks;
    }

    public int getFathersNoSuccessTasksSize(){
        if(CollectionUtils.isEmpty(fathersNoSuccessTasks)){
            return 0;
        }
        return fathersNoSuccessTasks.size();
    }

    public int getNotFinishedTasksSize(){
        if(CollectionUtils.isEmpty(notFinishedTasks)){
            return 0;
        }
        return notFinishedTasks.size();
    }

    public int getTriggerModelIncorrectTasksSize(){
        if(CollectionUtils.isEmpty(triggerModelIncorrectTasks)){
            return 0;
        }
        return triggerModelIncorrectTasks.size();
    }

    public int getCanBeSubmitTasksSize(){
        if(CollectionUtils.isEmpty(canBeSubmitTasks)){
            return 0;
        }
        return canBeSubmitTasks.size();
    }

}
