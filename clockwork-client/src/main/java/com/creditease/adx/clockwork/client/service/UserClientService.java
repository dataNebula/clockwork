package com.creditease.adx.clockwork.client.service;

import com.creditease.adx.clockwork.client.UserClient;
import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 下午4:41 2020/12/12
 * @ Description：
 * @ Modified By：
 */
@Service(value = "userClientService")
public class UserClientService {

    private static final Logger LOG = LoggerFactory.getLogger(UserClientService.class);

    @Autowired
    protected UserClient userClient;

    /**
     * 根据用户名获取手机号
     *
     * @param userName userName
     */
    public String getMobileNumberUserName(String userName) {
        try {
            Map<String, Object> interfaceResult = userClient.getMobileNumberUserName(userName);
            if (!HttpUtil.checkInterfaceDataSuccess(interfaceResult)) {
                return null;
            }
            return (String) interfaceResult.get(Constant.DATA);
        } catch (Exception e) {
            LOG.error("UserClientService-getMobileNumberUserName Error {}.", e.getMessage(), e);
        }
        return null;
    }
}
