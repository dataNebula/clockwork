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

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

@SpringBootConfiguration
@ComponentScan("com.creditease.adx.clockwork.redis")
public class TssRedisClusterConfiguration {

	@Autowired
	private TssRedisClusterPropertiesConfiguration tssRedisClusterPropertiesConfiguration;

	@Autowired
	private TssRedisSentinelPropertiesConfiguration tssRedisSentinelPropertiesConfiguration;

	@Autowired
	private JedisPoolConfig jedisPoolConfig;

	@Bean
	public JedisPoolConfig getJedisPoolConfig() {
		if (jedisPoolConfig == null) {

			jedisPoolConfig = new JedisPoolConfig();

			jedisPoolConfig.setMaxIdle(tssRedisClusterPropertiesConfiguration.getRedisMaxIdle());

			jedisPoolConfig.setMinIdle(tssRedisClusterPropertiesConfiguration.getRedisMinIdle());

			jedisPoolConfig.setMaxTotal(tssRedisClusterPropertiesConfiguration.getRedisMaxTotal());

			jedisPoolConfig.setMaxWaitMillis(tssRedisClusterPropertiesConfiguration.getRedisMaxWaitMillis());

			jedisPoolConfig.setTestOnBorrow(tssRedisClusterPropertiesConfiguration.getRedisTestOnBorrow());

			jedisPoolConfig.setTestOnReturn(tssRedisClusterPropertiesConfiguration.getRedisTestOnReturn());

		}
		return jedisPoolConfig;
	}

	@Bean(name="jedisSentinelPool")
	public JedisSentinelPool getJedisSentinelPool() {
		Set<String> sentinels = new HashSet<String>();
		if (tssRedisSentinelPropertiesConfiguration.getNodes() != null
				&& !tssRedisSentinelPropertiesConfiguration.getNodes().isEmpty()) {
			for (String node : tssRedisSentinelPropertiesConfiguration.getNodes()) {
				sentinels.add(node);
			}
		}
		try {
			JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(
					tssRedisSentinelPropertiesConfiguration.getMaster(),
					sentinels,
					jedisPoolConfig,
					tssRedisClusterPropertiesConfiguration.getRedisSoTimeout(),
					tssRedisClusterPropertiesConfiguration.getPassword());

			return jedisSentinelPool;
		}catch (Exception e){
			e.printStackTrace();
			return null ;
		}
	}

}
