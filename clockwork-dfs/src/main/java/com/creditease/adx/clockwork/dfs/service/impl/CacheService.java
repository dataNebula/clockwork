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

package com.creditease.adx.clockwork.dfs.service.impl;

import com.creditease.adx.clockwork.dfs.hdfs.HDfsUtils;
import com.creditease.adx.clockwork.dfs.service.ICacheService;
import com.google.common.cache.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.HAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 缓存（目前只针对hostname进行了缓存，后期有需求再扩展）
 */
@Service(value = "cacheService")
public class CacheService implements ICacheService {

    private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);

    @Value("${active.namenode.cache.timeout}")
    private Long timeout;

    @Value("${hadoop.conf.file.dir}")
    private String hadoopConfFileDir;

    private FileSystem HDFS_SYSTEM = null;
    private LoadingCache<String, String> cache;

    @PostConstruct
    public void init() {
        //CacheBuilder的构造函数是私有的，只能通过其静态方法newBuilder()来获得CacheBuilder的实例
        cache = CacheBuilder.newBuilder()
                // 设置并发级别
                .concurrencyLevel(5)
                // 设置写缓存后120秒钟过期
                .expireAfterWrite(120, TimeUnit.SECONDS)
                // 设置写缓存后30秒钟刷新
                .refreshAfterWrite(30, TimeUnit.SECONDS)
                // 设置缓存容器的初始容量为5
                .initialCapacity(5)
                // 设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
                .maximumSize(100)
                // 设置要统计缓存的命中率
                .recordStats()
                // 设置缓存的移除通知
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> notification) {
                        LOG.info(" {} 被移除了，原因：{}", notification.getKey(), notification.getCause());
                    }
                })
                // build方法中可以指定CacheLoader
                // 在缓存不存在时通过CacheLoader的实现自动加载缓存
                .build(
                        new CacheLoader<String, String>() {
                            @Override
                            public String load(String key) throws Exception {
                                String cacheValue = getCacheValue(key);
                                if (HDfsUtils.haCacheKey.equals(key)) {
                                    String hostname = getActiveNameNode();
                                    LOG.info("刷新重新加载，key = {}, hostname = {}", key, hostname);
                                    return hostname == null ? cacheValue : hostname;
                                }
                                LOG.error("异常的key，key = {}, cacheValue = {}", key, cacheValue);
                                return cacheValue;
                            }
                        }
                );
    }

    @Override
    public String getCacheValue(String key) {
        String cacheValue = null;
        try {
            cacheValue = cache.get(key);
        } catch (Exception e) {
            LOG.warn("[CacheService]getCacheValue " +
                    "msg:{} 缓存为空，重新获取hostname", e.getMessage());
        }
        if (StringUtils.isBlank(cacheValue)) {
            if (HDfsUtils.haCacheKey.equals(key)) {
                cacheValue = getActiveNameNode();
                setCache(key, cacheValue);
            }
        }
        return cacheValue;
    }

    @Override
    public String getActiveNameNode() {
        long startTime = System.currentTimeMillis();
        try {
            if (HDFS_SYSTEM == null) {
                Configuration conf = new Configuration();
                hadoopConfFileDir = hadoopConfFileDir.endsWith(File.separator) ?
                        hadoopConfFileDir : hadoopConfFileDir + File.separator;
                File coreSiteFile = new File(hadoopConfFileDir + "core-site.xml");
                File hdfsSiteFile = new File(hadoopConfFileDir + "hdfs-site.xml");
                File yarnSiteFile = new File(hadoopConfFileDir + "yarn-site.xml");
                conf.addResource(FileUtils.openInputStream(coreSiteFile));
                conf.addResource(FileUtils.openInputStream(hdfsSiteFile));
                conf.addResource(FileUtils.openInputStream(yarnSiteFile));
                HDFS_SYSTEM = FileSystem.get(conf);
                LOG.info("[CacheService-getActiveNameNodeIpAndPort]init hadoop conf files," +
                                "coreSiteFile = {},hdfsSiteFile = {},yarnSiteFile = {},,cost time = {} ms.",
                        coreSiteFile.getAbsolutePath(),
                        hdfsSiteFile.getAbsolutePath(),
                        yarnSiteFile.getAbsolutePath(), System.currentTimeMillis() - startTime);
            }
            InetSocketAddress active = HAUtil.getAddressOfActive(HDFS_SYSTEM);
            InetAddress address = active.getAddress();
            LOG.info("[CacheService-getActiveNameNodeIpAndPort]" +
                            "active host name = {}, host address = {}, cost time = {} ms.",
                    active.getHostName(), address.getHostAddress(), System.currentTimeMillis() - startTime);
            return address.getHostAddress();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void setCache(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public void refresh(String key) {
        cache.refresh(key);
    }


}
