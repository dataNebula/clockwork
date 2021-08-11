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

package com.creditease.adx.clockwork.common.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.creditease.adx.clockwork.common.util.PojoUtil;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;




import com.creditease.adx.clockwork.common.constant.Constant;
import com.creditease.adx.clockwork.common.framework.entity.Pagination;
import com.creditease.adx.clockwork.common.framework.annotation.QueryASC;
import com.creditease.adx.clockwork.common.framework.annotation.QueryBetweenEnd;
import com.creditease.adx.clockwork.common.framework.annotation.QueryBetweenStart;
import com.creditease.adx.clockwork.common.framework.annotation.QueryDESC;
import com.creditease.adx.clockwork.common.framework.annotation.QueryIn;
import com.creditease.adx.clockwork.common.framework.annotation.QueryLike;

public class CommonService {
	
	public Logger LOG = LoggerFactory.getLogger(this.getClass());

	/**
	 * 设置分页返回状态
	 * @param resultStatus
	 * @param errorMsg
	 * @param pagination
	 * @param <T>
	 * @return
	 */
	public <T> Pagination<T> setPaginationResultStatus(String resultStatus,String errorMsg,Pagination<T> pagination){
		if (pagination == null) {
			pagination = new Pagination<T>();
		}
		pagination.setResultStatus(resultStatus);
		pagination.setErrorMsg(errorMsg);
		return pagination;
	}

	/**
	 * 组装mybatis的criteria对象
	 * @param entity
	 * @param exampleCriteria
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public <Entity,EntityPojo,ExampleCriteria> void setEntityExampleCriteria(
			Entity entity,EntityPojo entityPojo,ExampleCriteria exampleCriteria)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String [] fields = PojoUtil.getFieldsNames(entity.getClass());
		if(fields == null || fields.length < 1){
			throw new RuntimeException("no obtained fields info is null");
		}
		Map<String,String> entityPojoFileds = null;
		if(entityPojo != null){
			entityPojoFileds = PojoUtil.getFieldsNamesInMap(entityPojo.getClass());
		}
		
		if(entityPojoFileds != null){
			//判断是否存在需要通过区间判断的条件
			List<QueryBetweenValue> containsQueryBetween = this.containsQueryBetween(entityPojo, entityPojoFileds);
			
			if(CollectionUtils.isNotEmpty(containsQueryBetween)) {
				for(QueryBetweenValue qbv:containsQueryBetween) {
					String methodName = this.generateAndFieldBetweenMethodName(qbv.getOriginalFieldName());
					Object[] fieldValues = new Object[]{qbv.getStartValue(), qbv.getEndValue()};
					//由于数据库字段类型的原因，暂时不正式启用该方法
					//MethodUtils.invokeExactMethod(exampleCriteria, methodName, fieldValues);
				}
			}
		}
		
		if(entityPojoFileds != null) {
			for(Map.Entry<String, String> entry:entityPojoFileds.entrySet()){
				if(PojoUtil.checkFieldHasAnnotation(entityPojo.getClass(), entry.getValue(), QueryIn.class)){
					String methodName = this.generateAndFieldInMethodName(entry.getValue());
					Object fieldValue = PropertyUtils.getProperty(entityPojo,entry.getValue());
					if(!StringUtils.isEmpty(methodName) && fieldValue!=null) {
						LOG.info("invoke criteria method:{},field :{},fieldValue:{}",methodName,entry.getValue(),fieldValue);
						MethodUtils.invokeMethod(exampleCriteria, methodName, fieldValue);
					}
				}
			}
		}
	
		for(String field:fields){
			Object fieldValue = PropertyUtils.getProperty(entity,field);
			if(field.getClass().getName().equals(Long.class.getName())) {
				if((Long)fieldValue == 0L) {
					fieldValue = null;
				}
			}
			if(field.getClass().getName().equals(Integer.class.getName())) {
				if((Integer)fieldValue == 0) {
					fieldValue = null;
				}
			}
			if(fieldValue != null){
				String methodName = null;
				if(entityPojoFileds != null && entityPojoFileds.containsKey(field)){
					if(PojoUtil.checkFieldHasAnnotation(entityPojo.getClass(), field, QueryLike.class)){
						methodName = generateAndFieldLikeMethodName(field);
						fieldValue = "%" + fieldValue + "%";
					}
				}else{
					methodName = generateAndFieldEqualToMethodName(field);
				}
				//如果等于NULL，默认按equals处理
				if(StringUtils.isEmpty(methodName)){
					methodName = generateAndFieldEqualToMethodName(field);
				}
				LOG.info("invoke criteria method:{},field :{},fieldValue:{}",methodName,field,fieldValue);
				MethodUtils.invokeExactMethod(exampleCriteria, methodName, fieldValue);
			}
		}
	}
	
	public <Entity, EntityPojo, EntityExample> void setEntityExample(
			Entity entity,EntityPojo entityPojo, EntityExample entityExample)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String [] fields = PojoUtil.getFieldsNames(entity.getClass());
		if(fields == null || fields.length < 1){
			throw new RuntimeException("no obtained fields info is null");
		}
		Map<String,String> entityPojoFileds = null;
		if(entityPojo != null){
			entityPojoFileds = PojoUtil.getFieldsNamesInMap(entityPojo.getClass());
		}
		
		String methodName = null;
		StringBuilder sb = new StringBuilder();
		String arg = null;
		if(entityPojoFileds != null) {
			for(Map.Entry<String, String> entry:entityPojoFileds.entrySet()){
				if(PojoUtil.checkFieldHasAnnotation(entityPojo.getClass(), entry.getValue(), QueryASC.class)){
					methodName = generateFiledOrderMethodName();
					sb.append(entry.getValue());
					sb.append(" asc,");
					LOG.info("invoke criteria method:{},field :{},fieldValue:{}",methodName,entry.getValue(),arg);
				}
				if(PojoUtil.checkFieldHasAnnotation(entityPojo.getClass(), entry.getValue(), QueryDESC.class)){
					methodName = generateFiledOrderMethodName();
					sb.append(entry.getValue());
					sb.append(" desc,");
					LOG.info("invoke criteria method:{},field :{},fieldValue:{}",methodName,entry.getValue(),arg);
				}
			}
		}
		if(!StringUtils.isEmpty(sb.toString())) {
			sb.deleteCharAt(sb.length() - 1);
			arg = sb.toString();
			if(!StringUtils.isEmpty(methodName)) {
				MethodUtils.invokeExactMethod(entityExample, methodName, arg);
			}
		}
		
	}
	
	public String generateAndFieldEqualToMethodName(String field){
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isBlank(field)){
			throw new RuntimeException("field is null.");
		}
		sb.append("and");
		sb.append(StringUtils.substring(field ,0 , 1).toUpperCase());
		sb.append(StringUtils.substring(field ,1 , field.length()));
		sb.append("EqualTo");
		return sb.toString();
	}
	
	public String generateAndFieldLikeMethodName(String field){
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isBlank(field)){
			throw new RuntimeException("field is null.");
		}
		sb.append("and");
		sb.append(StringUtils.substring(field ,0 , 1).toUpperCase());
		sb.append(StringUtils.substring(field ,1 , field.length()));
		sb.append("Like");
		return sb.toString();
	}
	
	public String generateAndFieldInMethodName(String field) {
		if(!field.contains("_")){
			return null;
		}
		String[] fieldparts = field.split("_");
		StringBuilder sb = new StringBuilder();
		sb.append("and");
		sb.append(StringUtils.substring(fieldparts[0] ,0 , 1).toUpperCase());
		sb.append(StringUtils.substring(fieldparts[0] ,1 , field.length()));
		sb.append("In");
		return sb.toString();
	}
	
	/**
	 * 构造区间条件方法名
	 * @param
	 * @return
	 */
	public String generateAndFieldBetweenMethodName(String field) {
		StringBuilder sb = new StringBuilder();
		if(StringUtils.isBlank(field)) {
			throw new RuntimeException("field is null.");
		}
		sb.append("and");
		sb.append(StringUtils.substring(field ,0 , 1).toUpperCase());
		sb.append(StringUtils.substring(field ,1 , field.length()));
		sb.append("Between");
		return sb.toString();
	}
	
	public String generateFiledOrderMethodName() {
		StringBuilder sb = new StringBuilder();
		sb.append("setOrderByClause");
		return sb.toString();
	}
	
	public <EntityMapper,EntityExample> Object invokeObjectMethod(
			String methodName,EntityMapper entityMapper,EntityExample entityExample) throws Exception{
		return MethodUtils.invokeExactMethod(entityMapper, methodName, entityExample);
	}
	
	public boolean containsMethod(Object obj, String name) {
		Method[] methods = obj.getClass().getMethods();
		if(methods != null && methods.length > 0) {
			for(Method method:methods) {
				if(method.getName().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断entityPojo是否存在区间判断条件，并且返回所有区间判断条件的属性名，属性值，是否全部存在，对应的原属性名等一系列信息
	 * @param entityPojo
	 * @param entityPojoFields
	 * @param <EntityPojo>
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public <EntityPojo> List<QueryBetweenValue> containsQueryBetween(
			EntityPojo entityPojo, Map<String, String> entityPojoFields)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<QueryBetweenValue> queryBetweenValues = new ArrayList<QueryBetweenValue>();
		for(Map.Entry<String, String> entry:entityPojoFields.entrySet()) {
			//遍历entityPojo的属性名map，筛选出含有QueryBetweenStart注解的属性
			if(PojoUtil.checkFieldHasAnnotation(entityPojo.getClass(), entry.getKey(), QueryBetweenStart.class)) {
				QueryBetweenValue qbv = new QueryBetweenValue();
				//默认区间判断条件属性名起名方式为 “原始属性名（例如创建时间）_start/end”，通过截取获取原始属性名
				String[] fieldNames = entry.getKey().split("_");
				if(fieldNames == null || fieldNames.length < 1) {
					throw new RuntimeException("Illegal query between field name!");
				}
				String originalFieldName = fieldNames[0];
				//拼接区间另一个属性名
				String endFiledName = fieldNames[0] + "_end";
				Object endValue = null;
				//默认区间左右两边属性同时出现，判断另外一边的属性是否含有queryBetweenEnd注解
				if(PojoUtil.checkFieldHasAnnotation(entityPojo.getClass(), endFiledName, QueryBetweenEnd.class)) {
					endValue = PropertyUtils.getProperty(entityPojo, entry.getKey());
				}
				Object startValue = PropertyUtils.getProperty(entityPojo,entry.getKey());
				String contains = null;
				//判断区间属性是否同时有值
				if(startValue!=null && endValue == null) {
					contains = Constant.START;
				}else if(startValue == null && endValue != null) {
					contains = Constant.END;
				}else if(startValue != null && endValue != null) {
					contains = Constant.BOTH;
				}else if(startValue == null && endValue == null) {
					contains = Constant.BOTH_NULL;
				}
				qbv.setContains(contains);
				qbv.setEndFiledName(endFiledName);
				qbv.setEndValue(endValue);
				qbv.setOriginalFieldName(originalFieldName);
				qbv.setStartFiledName(entry.getKey());
				qbv.setStartValue(startValue);
				//如果含有多对区间判断条件，返回为list
				queryBetweenValues.add(qbv);
			}
		}
		return queryBetweenValues;
	}
	
}
