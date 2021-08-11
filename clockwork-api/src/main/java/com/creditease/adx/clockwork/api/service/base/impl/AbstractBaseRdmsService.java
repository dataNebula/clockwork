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

package com.creditease.adx.clockwork.api.service.base.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.creditease.adx.clockwork.api.service.base.IBaseRdmsService;
import com.creditease.adx.clockwork.common.common.CommonService;
import com.creditease.adx.clockwork.common.enums.SystemStatus;
import com.creditease.adx.clockwork.common.framework.entity.Pagination;
import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;
import com.creditease.adx.clockwork.common.util.ReflectUtil;

public abstract class AbstractBaseRdmsService<Entity, EntityPojo, EntityExample, EntityMapper>
        extends CommonService
        implements IBaseRdmsService <Entity, EntityPojo, EntityExample, EntityMapper> {

    @SuppressWarnings("unchecked")
    @Override
    public Pagination<EntityPojo> queryPagination(QueryCriteria<EntityPojo> queryPojo) {
        Pagination<EntityPojo> result = null;
        EntityExample entityExample;
        try {
            if (queryPojo == null || queryPojo.getEntityDto() == null) {
                entityExample = convertPojoToExample(null);
            } else {
                entityExample = convertPojoToExample(queryPojo.getEntityDto());
            }
            long total = (long) invokeObjectMethod("countByExample", getMapper(), entityExample);
            if (total < 1) {
                return new Pagination<EntityPojo>().emptyInit();
            }
            result = new Pagination<EntityPojo>();
            //当前页的第一条，例如每页10条，第一页的开始为0，第二页的开始为10
            int pageStartIndex = (queryPojo.getPageIndex() - 1) * queryPojo.getPageSize();
            int pageSize = queryPojo.getPageSize();
            String orderByClause = queryPojo.getOrderByClause();

            invokeObjectMethod("setLimitStart", entityExample, pageStartIndex);
            invokeObjectMethod("setLimitEnd", entityExample, pageSize);
            if (orderByClause != null) {
                invokeObjectMethod("setOrderByClause", entityExample, orderByClause);
            }

            List<Entity> entities
                    = (List<Entity>) invokeObjectMethod("selectByExample", getMapper(), entityExample);

            // 查询出的数据
            result.setData(convertEntitiesToPojos(entities));
            // 总条数
            result.setTotalCount(total);
            // 每页多少条
            result.setPageSize(pageSize);
            // 总共多少页
            result.setTotalPageCount(total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1);
            // 当前页页号
            result.setCurrentPageIndex(queryPojo.getPageIndex()); // 页号从1开始
            // 下一页页号
            result.setNextPageIndex(queryPojo.getPageIndex() >= result.getTotalPageCount() ? -1 : queryPojo.getPageIndex() + 1);
            // 上一页页号
            result.setPreviousPageIndex(queryPojo.getPageIndex() <= 1 ? -1 : queryPojo.getPageIndex() - 1);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return setPaginationResultStatus(SystemStatus.FAILURE.getStatus(), e.getMessage(), result);
        }
        return result;

    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityExample convertPojoToExample(EntityPojo entityPojo)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        EntityExample result = (EntityExample) ReflectUtil
                .constructorNewInstance(ReflectUtil.getClassGenricType(this.getClass(), 2).getName());
        // 查询条件为空返回默认
        if (entityPojo == null) {
            return result;
        }
        Entity dest = (Entity) ReflectUtil
                .constructorNewInstance(ReflectUtil.getClassGenricType(this.getClass(), 0).getName());
        ConvertUtils.register(new DateConverter(null), Date.class);
        ConvertUtils.register(new IntegerConverter(null), Integer.class);
        ConvertUtils.register(new LongConverter(null), Long.class);
        BeanUtils.copyProperties(dest, entityPojo);
        setEntityExample(dest, entityPojo, result);
        setEntityExampleCriteria(dest, entityPojo, MethodUtils.invokeExactMethod(result, "createCriteria", null));
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Entity convertPojoToEntity(EntityPojo entityPojo) throws IllegalAccessException, InvocationTargetException {
        Entity dest = (Entity) ReflectUtil
                .constructorNewInstance(ReflectUtil.getClassGenricType(this.getClass(), 0).getName());
        ConvertUtils.register(new DateConverter(null), Date.class);
        BeanUtils.copyProperties(dest, entityPojo);
        return dest;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityPojo convertEntityToPojo(Entity entity) throws IllegalAccessException, InvocationTargetException {
        EntityPojo dest = (EntityPojo) ReflectUtil
                .constructorNewInstance(ReflectUtil.getClassGenricType(this.getClass(), 1).getName());
        ConvertUtils.register(new DateConverter(null), Date.class);
        BeanUtils.copyProperties(dest, entity);
        return dest;
    }

    @Override
    public List<EntityPojo> convertEntitiesToPojos(List<Entity> entities)
            throws IllegalAccessException, InvocationTargetException {
        List<EntityPojo> result = null;
        if (CollectionUtils.isEmpty(entities)) {
            return result;
        }
        result = new ArrayList<EntityPojo>();
        for (Entity taskRunLog : entities) {
            result.add(convertEntityToPojo(taskRunLog));
        }
        return result;
    }

    @Override
    public int updateByPrimaryKey(Entity entity) throws Exception {
        int result = -1;
        if (entity == null) {
            return result;
        }
        int nullNumber = 0;
        Map<String, String> beanInfo = BeanUtils.describe(entity);
        for (String value : beanInfo.values()) {
            if (StringUtils.isBlank(value)) {
                nullNumber++;
            }
        }
        //字段都为NULL返回
        if (nullNumber == beanInfo.size() - 1) {
            return result;
        }
        return (int) invokeObjectMethod("updateByPrimaryKeySelective", getMapper(), entity);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> select(Entity entity) throws Exception {
        EntityExample entityExample = (EntityExample) ReflectUtil
                .constructorNewInstance(ReflectUtil.getClassGenricType(this.getClass(), 2).getName());
        setEntityExampleCriteria(
                entity, null, MethodUtils.invokeExactMethod(entityExample, "createCriteria", null));
        return (List<Entity>) invokeObjectMethod("selectByExample", getMapper(), entityExample);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> selectWithBLOBs(Entity entity) throws Exception {
        EntityExample entityExample = (EntityExample) ReflectUtil
                .constructorNewInstance(ReflectUtil.getClassGenricType(this.getClass(), 2).getName());
        setEntityExampleCriteria(
                entity, null, MethodUtils.invokeExactMethod(entityExample, "createCriteria", null));
        return (List<Entity>) invokeObjectMethod("selectByExampleWithBLOBs", getMapper(), entityExample);
    }

    @Override
    public Entity insert(Entity entity) throws Exception {
        if (entity == null) {
            return entity;
        }
        int nullNumber = 0;
        Map<String, String> beanInfo = BeanUtils.describe(entity);
        for (String value : beanInfo.values()) {
            if (StringUtils.isBlank(value)) {
                nullNumber++;
            }
        }
        //字段都为NULL返回
        if (nullNumber == beanInfo.size() - 1) {
            return entity;
        }
        invokeObjectMethod("insertSelective", getMapper(), entity);
        return entity;
    }

}
