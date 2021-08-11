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

package com.creditease.adx.clockwork.redis.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="tss.redis")
public class TssRedisClusterPropertiesConfiguration {

	private Integer redisMaxIdle;

	private Integer redisMaxTotal;

	private Integer redisMinIdle;

	private Long redisMaxWaitMillis;

	private Boolean redisTestOnBorrow;

	private Boolean redisTestOnReturn;

	private Integer redisExpiration;

	private Integer redisSoTimeout;

	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getRedisMaxIdle() {
		return redisMaxIdle;
	}
	public void setRedisMaxIdle(Integer redisMaxIdle) {
		this.redisMaxIdle = redisMaxIdle;
	}
	public Integer getRedisMaxTotal() {
		return redisMaxTotal;
	}
	public void setRedisMaxTotal(Integer redisMaxTotal) {
		this.redisMaxTotal = redisMaxTotal;
	}
	public Integer getRedisMinIdle() {
		return redisMinIdle;
	}
	public void setRedisMinIdle(Integer redisMinIdle) {
		this.redisMinIdle = redisMinIdle;
	}
	public Long getRedisMaxWaitMillis() {
		return redisMaxWaitMillis;
	}
	public void setRedisMaxWaitMillis(Long redisMaxWaitMillis) {
		this.redisMaxWaitMillis = redisMaxWaitMillis;
	}
	public Boolean getRedisTestOnBorrow() {
		return redisTestOnBorrow;
	}
	public void setRedisTestOnBorrow(Boolean redisTestOnBorrow) {
		this.redisTestOnBorrow = redisTestOnBorrow;
	}
	public Boolean getRedisTestOnReturn() {
		return redisTestOnReturn;
	}
	public void setRedisTestOnReturn(Boolean redisTestOnReturn) {
		this.redisTestOnReturn = redisTestOnReturn;
	}
	public Integer getRedisExpiration() {
		return redisExpiration;
	}
	public void setRedisExpiration(Integer redisExpiration) {
		this.redisExpiration = redisExpiration;
	}
	public Integer getRedisSoTimeout() {
		return redisSoTimeout;
	}
	public void setRedisSoTimeout(Integer redisSoTimeout) {
		this.redisSoTimeout = redisSoTimeout;
	}
}
