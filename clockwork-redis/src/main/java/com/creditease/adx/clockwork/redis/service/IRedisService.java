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

package com.creditease.adx.clockwork.redis.service;

import java.util.concurrent.TimeUnit;

/**
 * @author Muyuan Sun
 * @email sunmuyuans@163.com
 * @date 2019-06-19
 */
public interface IRedisService {

    boolean getLock(String key);

    boolean releaseLock(String key);

    boolean tryLockForSubmitTask(long tryLockTimeout, TimeUnit tryLockTimeoutUnit);

    boolean tryLockForLaunchSubTask(long tryLockTimeout, TimeUnit tryLockTimeoutUnit);

    boolean tryLockForLaunchSubTask(int subTask, long tryLockTimeout, TimeUnit tryLockTimeoutUnit);

    boolean tryLockForRunTask(long tryLockTimeout, TimeUnit tryLockTimeoutUnit);

    boolean tryLockForUpdateTaskGroupStatusForTaskStart(long tryLockTimeout, TimeUnit tryLockTimeoutUnit);

    boolean releaseLockForUpdateTaskGroupStatusForTaskStart();

    boolean tryLockForUpdateTaskGroupStatusForTaskEnd(long tryLockTimeout, TimeUnit tryLockTimeoutUnit);

    boolean releaseLockForUpdateTaskGroupStatusForTaskEnd();

    Long incrementAndGet(String key);

}
