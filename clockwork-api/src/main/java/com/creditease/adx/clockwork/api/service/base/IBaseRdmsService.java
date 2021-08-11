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

package com.creditease.adx.clockwork.api.service.base;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.creditease.adx.clockwork.common.framework.entity.Pagination;
import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;
/**
 * postgres 基础服务接口
 * @author smy
 * @date 2016年1月16日
 * @param <Entity>
 * @param <EntityPojo>
 * @param <EntityExample>
 * @param <EntityMapper>
 */
public interface IBaseRdmsService<Entity,EntityPojo,EntityExample,EntityMapper> {
	/**
	 * 获得mybatis生成的mapper
	 * @return
	 */
    EntityMapper getMapper();
	/**
	 * 分页查询
	 * @param queryPojo
	 * @return
	 */
    Pagination<EntityPojo> queryPagination(QueryCriteria<EntityPojo> queryPojo);
	/**
	 * pojo转换成mybatis的example对象
	 * @param entityPojo
	 * @return
	 */
    EntityExample convertPojoToExample(EntityPojo entityPojo)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException ;
	/**
	 * 转换pojo到对应数据库实体对象
	 * @param entityPojo
	 * @return
	 */
    Entity convertPojoToEntity(EntityPojo entityPojo) throws IllegalAccessException, InvocationTargetException ;
	/**
	 * 转换数据库对象到pojo
	 * @param entity
	 * @return
	 */
    EntityPojo convertEntityToPojo(Entity entity) throws IllegalAccessException, InvocationTargetException ;
	
	/**
	 * 将数据库实体列表转换成Pojo列表
	 * @param entities
	 * @return
	 */
    List<EntityPojo> convertEntitiesToPojos(List<Entity> entities)
			throws IllegalAccessException, InvocationTargetException;
	/**
	 * 按主键更新
	 * @param entity
	 * @return
	 */
    int updateByPrimaryKey(Entity entity) throws Exception;
	/**
	 * 按条件查询
	 * @param entity
	 * @return
	 * @throws Exception
	 */
    List<Entity> select(Entity entity) throws Exception;
	/**
	 * 按条件查询(包含大字段,类似 task_run_log 表中的log字段，类型是longtext)
	 * @param entity
	 * @return
	 * @throws Exception
	 */
    List<Entity> selectWithBLOBs(Entity entity) throws Exception;
	/**
	 * 插入数据并返回实体对象
	 * @param entity
	 * @return
	 * @throws Exception
	 */
    Entity insert(Entity entity) throws Exception;
}
