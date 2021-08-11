package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.GraphClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTask;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskRelation;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午9:17 2020/12/6
 * @ Description：
 * @ Modified By：
 */
@Service(value = "graphClientService")
public class GraphClientService {

    private static final Logger LOG = LoggerFactory.getLogger(GraphClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected GraphClient graphClient;

    /**
     * 获取整个dag图的所有关系
     *
     * @param taskId 该图的某一个taskId
     * @return all task relations
     */
    public List<TbClockworkTaskRelation> getGraphAllRelationByTaskId(Integer taskId) {
        try {
            Map<String, Object> interfaceResult = graphClient.getGraphAllRelationByTaskId(taskId);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskRelation>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取整个dag图的所有TaskIds
     *
     * @param taskId 该图的某一个taskId
     * @return all task ids
     */
    public List<Integer> getGraphAllTaskIdsByTaskId(Integer taskId) {
        try {
            Map<String, Object> interfaceResult = graphClient.getGraphAllTaskIdsByTaskId(taskId);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<Integer>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取整个dag图的所有任务
     *
     * @param taskId 该图的某一个taskId
     * @return all task
     */
    public List<TbClockworkTask> getGraphAllTasksByTaskId(Integer taskId) {
        try {
            Map<String, Object> interfaceResult = graphClient.getGraphAllTasksByTaskId(taskId);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTask>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }


}
