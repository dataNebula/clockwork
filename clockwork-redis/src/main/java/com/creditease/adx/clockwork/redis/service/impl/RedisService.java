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

package com.creditease.adx.clockwork.redis.service.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import com.creditease.adx.clockwork.common.enums.RedisLockKey;
import com.creditease.adx.clockwork.redis.service.IRedisService;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-06-19
 */

@Service(value = "redisService")
public class RedisService implements IRedisService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JedisSentinelPool jedisSentinelPool = null;

    // nxxx NX|XX, NX -- Only set the key if it does not already exist. XX -- Only set the key if it already exist.
    private final static String NXXX = "NX";
    // expx EX|PX, expire time units: EX = seconds; PX = milliseconds
    private final static String EXPX = "EX";
    // expire time
    private final static long EXPIRE_TIME = 30;
    // set ok flag
    private final static String REDIS_RESULT = "OK";

    @Override
    public boolean getLock(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("param key is null.");
        }
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return jedis.exists(key);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
        return false;
    }

    @Override
    public boolean releaseLock(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("param key is null.");
        }
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.del(key);
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
        return false;
    }

    @Override
    public boolean tryLockForSubmitTask(long tryLockTimeout, TimeUnit tryLockTimeoutUnit) {
        try {
            return tryLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue(), tryLockTimeout, tryLockTimeoutUnit);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean tryLockForLaunchSubTask(long tryLockTimeout, TimeUnit tryLockTimeoutUnit) {
        try {
            return tryLock(RedisLockKey.LAUNCH_SUB_TASK_TRANSACTION.getValue(), tryLockTimeout, tryLockTimeoutUnit);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean tryLockForLaunchSubTask(int subTask, long tryLockTimeout, TimeUnit tryLockTimeoutUnit) {
        try {
            return tryLock(RedisLockKey.LAUNCH_SUB_TASK_TRANSACTION.getValue()+subTask, tryLockTimeout, tryLockTimeoutUnit);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean tryLockForRunTask(long tryLockTimeout, TimeUnit tryLockTimeoutUnit) {
        try {
            return tryLock(RedisLockKey.SUBMIT_TASK_TRANSACTION.getValue(), tryLockTimeout, tryLockTimeoutUnit);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean tryLockForUpdateTaskGroupStatusForTaskStart(long tryLockTimeout, TimeUnit tryLockTimeoutUnit) {
        try {
            return tryLock(
                    RedisLockKey.UPDATE_TASK_GROUP_STATUS_WHEN_TASK_START.getValue(),
                    tryLockTimeout,
                    tryLockTimeoutUnit);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean releaseLockForUpdateTaskGroupStatusForTaskStart() {
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.del(RedisLockKey.UPDATE_TASK_GROUP_STATUS_WHEN_TASK_START.getValue());
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
        return false;
    }

    @Override
    public boolean tryLockForUpdateTaskGroupStatusForTaskEnd(long tryLockTimeout, TimeUnit tryLockTimeoutUnit) {
        try {
            return tryLock(
                    RedisLockKey.UPDATE_TASK_GROUP_STATUS_WHEN_TASK_END.getValue(),
                    tryLockTimeout,
                    tryLockTimeoutUnit);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean releaseLockForUpdateTaskGroupStatusForTaskEnd() {
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            jedis.del(RedisLockKey.UPDATE_TASK_GROUP_STATUS_WHEN_TASK_END.getValue());
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
        return false;
    }

    /**
     * 获取redis lock
     *
     * @param key
     * @param tryLockTimeout
     * @param tryLockTimeoutUnit
     * @return
     */
    private boolean tryLock(String key, long tryLockTimeout, TimeUnit tryLockTimeoutUnit) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("param key is null.");
        }
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            long nano = System.nanoTime();
            do {
                String result = jedis.set(key, key, NXXX, EXPX, EXPIRE_TIME);
                if (result != null && result.equals(REDIS_RESULT)) {
                    return true;
                } else {
                    // lock had exist.
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("key: " + key + " locked by another business：" + key);
                    }
                }
                if (tryLockTimeout == 0) {
                    break;
                }
                // sleep,request redis buffer
                Thread.sleep(100);
                // loop check whether lock can be obtained within time out.
            } while ((System.nanoTime() - nano) < tryLockTimeoutUnit.toNanos(tryLockTimeout));
            return false;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
        return false;
    }

    @Override
    public Long incrementAndGet(String key) {
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("param key is null.");
        }
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            return jedis.incr(key);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            jedis.close();
        }
        return -1L;
    }

}
