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

package com.creditease.adx.clockwork.common.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class MailUtil {

    public static String mailBuild(String desc, String filePath) {
        String content = "报警邮件！<br/>【%s】%s。<br/>文件：%s";
        return String.format(content, DateUtil.formatDateTime(new Date()), desc, filePath);
    }

    /**
     * 发送邮件
     * @param receivers
     * @param subject
     * @param content
     * @return
     */
    public static Map<String, Object> sendMail(String url, String receivers, String subject, String content) {
        return sendMail(url, receivers, subject, content, null, null);
    }

    /**
     * 发送邮件
     * @param receivers
     * @param subject
     * @param content
     * @param proxyIp
     * @param proxyPort
     * @return
     */
    public static Map<String, Object> sendMail(String url, String receivers, String subject, String content, String proxyIp, String proxyPort) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(receivers)) {
            result.put("code", -1);
            result.put("msg", "收件人不能为空");
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("from", "调度系统监控邮件");
        params.put("to", receivers);
        params.put("subject", subject);
        params.put("message", content);

        String response = HttpUtil.post(url, params);
        result.put("code", 0);
        result.put("msg", response);
        return result;
    }
}
