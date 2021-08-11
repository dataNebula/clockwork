package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.DagCheckClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.DagCheckRangeInfo;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkDag;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ClassName: DagCheckClientService
 * @Author: ltb
 * @Date: 2021/3/10:6:48 下午
 * @Description:
 */
@Service(value = "dagCheckClientService")
public class DagCheckClientService {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private DagCheckClient dagCheckClient;

    /**
     * 对当前对dag进行成环检测
     *
     * @param dagId dag id, userName 用户名
     * @return Map<String, Object>
     */
    public DagCheckRangeInfo checkDagById(Integer dagId,String userName) {
        Map<String, Object> interfaceResult = dagCheckClient.checkDagById(dagId,userName);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)
                || interfaceResult.get(Constant.DATA) == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(interfaceResult.get(Constant.DATA), new TypeReference<DagCheckRangeInfo>() {
        });
    }

    /**
     * @return Map<String, Object>
     * @Description 手动对所有满足条件的dag对象进行成环检测
     * @Param userName 用户名
     */
    public boolean checkAllDags(String userName) {
        Map<String, Object> interfaceResult = dagCheckClient.checkAllDags(userName);
        // 接口CODE 代码判断
        if (!HttpUtil.checkInterfaceCodeSuccess(interfaceResult)
                || interfaceResult.get(Constant.DATA) == null) {
            return false;
        }
        return (Boolean) interfaceResult.get(Constant.DATA);
    }

}
