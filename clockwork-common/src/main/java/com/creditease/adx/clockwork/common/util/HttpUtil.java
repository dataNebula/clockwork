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

import com.creditease.adx.clockwork.common.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 16:38 2019-09-09
 * @ Description：工具类
 * @ Modified By：
 */
public class HttpUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);


    public static String get(String url, Map<String, Object> params, Map<String, String> header) throws IOException {
        // 解析 Url Params
        String paramString = mapToStr(params);
        return get(url, paramString, header);
    }

    public static String get(String url, String params, Map<String, String> header) throws IOException {

        // 返回结果
        String result = "";

        BufferedReader in = null;

        // 打开和URL之间的连接
        URLConnection connection = getURLConnection(url, params, header);

        // 获取所有响应头字段
        if (connection != null) {

/*            Map<String, List<String>> map = connection.getHeaderFields();

            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }*/

            // 定义 BufferedReader输入流来读取URL的响应，设置utf8防止中文乱码
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            if (in != null) {
                in.close();
            }
        }
        return result;
    }


    /**
     * Post请求
     *
     * @param url    URL
     * @param params params
     * @return
     */
    public static String post(String url, Map<String, Object> params) {
        BufferedReader in = null;
        String result = "";

        try {
            URLConnection conn = postURLConnection(url, params, null);
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            LOG.error("发送 POST 请求出现异常！", e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


    /**
     * post 请求URl带参数，body带参数
     *
     * @param url    URL
     * @param params Map
     * @param body   JsonString
     * @return
     * @throws IOException
     */
    public static String postByBody(String url, Map<String, Object> params, String body) throws IOException {
        String result = "";
        BufferedReader in = null;

        // 解析 Url Params
        String paramStr = mapToStr(params);
        if (paramStr != null && !"".equals(paramStr)) {
            // 最终URL
            url = url + "?" + paramStr;
        }
        LOG.info(url);

        // 打开和URL之间的连接
        URLConnection connection = postBodyURLConnection(url, body);
        if (connection != null) {
            // 定义 BufferedReader输入流来读取URL的响应，设置utf8防止中文乱码
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            if (in != null) {
                in.close();
            }
        }
        return result;
    }


    private static URLConnection getURLConnection(String url, String param, Map<String, String> header) {

        if (param != null && !"".equals(param)) {
            url = url + "?" + param;
        }
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            if (header != null) {
                Iterator<Map.Entry<String, String>> it = header.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = it.next();
                    if ("connectTimeout".equals(entry.getKey())) {
                        conn.setConnectTimeout(Integer.parseInt(entry.getValue()));
                    } else {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            conn.connect();
            return conn;
        } catch (Exception e) {
            LOG.error("发送 GET 请求URLConnection异常！", e);
        }
        return null;
    }


    public static URLConnection postURLConnection(String url, Map<String, Object> params, Map<String, String> header) {
        PrintWriter out = null;
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性 Header
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 参数
            String paramStr = mapToStr(params);

            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(paramStr);
            out.flush();
            return conn;
        } catch (Exception e) {
            LOG.error("发送 POST 请求URLConnection异常！", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return null;
    }


    private static URLConnection postBodyURLConnection(String url, String bodyParam) {
        PrintWriter out = null;
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/json");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(bodyParam);
            out.flush();
            return conn;
        } catch (Exception e) {
            LOG.error("发送 POST 请求URLConnection异常！", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return null;
    }

    /**
     * map格式转为key=value&key=value
     *
     * @param params
     * @return
     */
    private static String mapToStr(Map<String, Object> params) {
        String paramStr = null;
        if (params != null && params.size() != 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sb.append(String.format("%1$s=%2$s", entry.getKey(), entry.getValue())).append("&");
            }
            paramStr = sb.toString();
            if (paramStr.endsWith("&")) {
                paramStr = paramStr.substring(0, paramStr.length() - 1);
            }
        }
        return paramStr;
    }

    /**
     * 接口code检验码是否成功
     *
     * @param interfaceResult map
     * @return
     */
    public static boolean checkInterfaceCodeSuccess(Map<String, Object> interfaceResult) {
        if (interfaceResult == null || !interfaceResult.containsKey(Constant.CODE)) {
            return false;
        }
        return Constant.SUCCESS_CODE.equals(interfaceResult.get(Constant.CODE));
    }

    public static boolean checkInterfaceDataSuccess(Map<String, Object> interfaceResult) {
        if (checkInterfaceCodeSuccess(interfaceResult)) {
            return interfaceResult.get(Constant.DATA) != null;
        }
        return false;
    }
}
