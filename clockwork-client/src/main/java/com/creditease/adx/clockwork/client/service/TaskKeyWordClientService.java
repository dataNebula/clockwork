package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.TaskKeyWordClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword;
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
 * @ClassName: TaskKeyWordClientService
 * @Author: ltb
 * @Date: 2021/3/17:5:03 下午
 * @Description:
 */
@Service(value = "taskKeyWordClientService")
public class TaskKeyWordClientService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskKeyWordClientService.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Autowired
    private TaskKeyWordClient keyWordClient;

    /**
     * @return java.util.List<com.creditease.adx.clockwork.common.entity.gen.TbClockworkTaskErrorKeyword>
     * @Description 根据keyIds，获得它绑定的关键词列表
     * @Param [keyIds]
     */
    public List<TbClockworkTaskErrorKeyword> getKeywordInfoBykeyIds(String keyIds) {
        try {
            Map<String, Object> interfaceResult = keyWordClient.getKeywordInfoBykeyIds(keyIds);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            return OBJECT_MAPPER.convertValue(
                    interfaceResult.get(Constant.DATA), new TypeReference<List<TbClockworkTaskErrorKeyword>>() {
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }


}
