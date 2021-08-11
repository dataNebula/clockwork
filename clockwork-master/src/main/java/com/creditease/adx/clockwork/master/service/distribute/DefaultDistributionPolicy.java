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

package com.creditease.adx.clockwork.master.service.distribute;

import com.creditease.adx.clockwork.common.entity.gen.TbClockworkNode;
import com.creditease.adx.clockwork.common.pojo.TbClockworkTaskPojo;
import com.creditease.adx.clockwork.common.util.DataUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认任务分发策略
 *
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 1:37 下午 2020/5/15
 * @ Description：默认分发策略
 * @ Modified By：
 */
public class DefaultDistributionPolicy implements DistributePolicyInterface {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDistributionPolicy.class);

    /**
     * 分发策略
     *
     * @param taskPojoList     task list
     * @param tbClockworkNodes nodes
     * @return
     */
    @Override
    public Map<TbClockworkNode, List<TbClockworkTaskPojo>> distributePolicy(List<TbClockworkTaskPojo> taskPojoList, List<TbClockworkNode> tbClockworkNodes) {

        Map<TbClockworkNode, List<TbClockworkTaskPojo>> nodeTaskListMap = new HashMap<>();

        //切分任务-目前基于平均分配规则
        List<List<TbClockworkTaskPojo>> lists = DataUtil.averageAssign(taskPojoList, tbClockworkNodes.size());
        if (LOG.isDebugEnabled()) {
            LOG.debug("[DefaultDistributionPolicy-distribute]submit info. tasks.total.size = {}, split.size = {}, info = {}",
                    taskPojoList.size(), lists.size(), taskPojoList);
        } else {
            LOG.info("[DefaultDistributionPolicy-distribute]submit info. tasks.total.size = {}, split.size = {}",
                    taskPojoList.size(), lists.size());
        }

        for (int i = 0, size = lists.size(); i < size; i++) {
            TbClockworkNode tbClockworkNode = tbClockworkNodes.get(i);
            //分发任务到具体的节点
            List<TbClockworkTaskPojo> splitTaskIds = lists.get(i);
            if (tbClockworkNode == null || tbClockworkNode.getIp() == null || tbClockworkNode.getPort() == null) {
                throw new RuntimeException("[DefaultDistributionPolicy-distribute] node info is null.");
            }
            if (CollectionUtils.isEmpty(splitTaskIds)) {
                continue;
            }
            nodeTaskListMap.put(tbClockworkNode, splitTaskIds);
        }
        return nodeTaskListMap;
    }

}
