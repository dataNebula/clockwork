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

package com.creditease.adx.clockwork.dao.mybatis.plugin;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MybatisGeneratorMain {

    static {
        System.setProperty("spring.profiles.active", "dev");
    }

    public static void main(String[] args) throws Exception {
        File configFile = new File(MybatisGeneratorMain.class.getClassLoader().getResource("generator/generator_config.xml").getPath());
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        // 重新配置数据源
        // ConfigurableApplicationContext context = SpringApplication.run(MybatisGeneratorMain.class, args);
        setDataSource(config, null);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        ProgressCallback progressCallback = new VerboseProgressCallback();
        myBatisGenerator.generate(progressCallback);
    }

    /**
     * 替换generator中配置的数据库信息为applicationContext中的数据库信息
     */
    private static void setDataSource(Configuration config, ApplicationContext context) {
        // Environment environment = context.getEnvironment();
        JDBCConnectionConfiguration jdbcConfiguration = new JDBCConnectionConfiguration();
        // jdbcConfiguration.setConnectionURL(environment.getProperty("spring.datasource.url"));
        // jdbcConfiguration.setUserId(environment.getProperty("spring.datasource.username"));
        // jdbcConfiguration.setPassword(environment.getProperty("spring.datasource.password"));
        // jdbcConfiguration.setDriverClass(environment.getProperty("spring.datasource.driver-class-name"));
        jdbcConfiguration.setConnectionURL("jdbc:mysql://127.0.0.1:4322/adx_clockwork?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
        jdbcConfiguration.setUserId("adx_clockwork");
        jdbcConfiguration.setPassword("adx_clockwork");
        jdbcConfiguration.setDriverClass("com.mysql.jdbc.Driver");
        config.getContexts().get(0).setJdbcConnectionConfiguration(jdbcConfiguration);
    }
}
