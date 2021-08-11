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

import java.lang.reflect.Field;
import java.util.*;

import com.creditease.adx.clockwork.common.framework.entity.QueryCriteria;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.creditease.adx.clockwork.common.framework.entity.Pagination;


public class PojoUtil {

    public static String[] getDeclaredFieldsNames(@SuppressWarnings("rawtypes") Class c) {
        Field[] fields = c.getDeclaredFields();
        List<String> fieldList = new ArrayList<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("itemId") || field.equals("class") || fieldName.equals("belongCurrentThemeKeyword")) {
                continue;
            }
            fieldList.add(fieldName);
        }
        return fieldList.toArray(new String[fieldList.size()]);
    }

    public static List<Field> getDeclaredFields(@SuppressWarnings("rawtypes") Class c) {
        Field[] fields = c.getDeclaredFields();
        List<Field> fieldList = new ArrayList<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("itemId") || field.equals("class") || fieldName.equals("belongCurrentThemeKeyword")) {
                continue;
            }
            fieldList.add(field);
        }
        return fieldList;
    }

    public static List<Field> getAllFieldsIncludingParents(@SuppressWarnings("rawtypes") Class c) {
        Field[] fields = c.getDeclaredFields();
        List<Field> fieldList = new ArrayList<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("itemId") || field.equals("class") || fieldName.equals("belongCurrentThemeKeyword")) {
                continue;
            }
            fieldList.add(field);
        }
        Class<?> superclass = c.getSuperclass();
        if (superclass != null) {
            fieldList.addAll(getAllFieldsIncludingParents(superclass));
        }
        return fieldList;
    }

    @SuppressWarnings("unchecked")
    public static boolean checkFieldHasAnnotation(@SuppressWarnings("rawtypes") Class clazz,
            String fieldName, @SuppressWarnings("rawtypes") Class targetAnnotation) {
        Field[] fields = clazz.getDeclaredFields();
        if (StringUtils.isEmpty(fieldName)) {
            return false;
        }
        boolean hasField = false;
        Field targetField = null;
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                targetField = field;
                hasField = true;
                break;
            }
        }
        if (!hasField) {
            return false;
        }
        try {
            int hasAnnotationLength = targetField.getAnnotationsByType(targetAnnotation).length;
            return hasAnnotationLength > 0;
        } catch (NullPointerException e) {}
        return false;
    }

    public static void removeNullFromMap(Map<String, Object> conditions) {
        if (conditions == null) {
            return;
        }
        Iterator<Map.Entry<String, Object>> iterator  = conditions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() == null || entry.getKey().equals("class")) {
                iterator.remove();
            }
        }
    }

    public static <Bean> Map<String, Object> describe(Bean bean) {
        Map<String, Object> result = null;
        if (bean == null) {
            return result;
        }
        try {
            result = PropertyUtils.describe(bean);
            PojoUtil.removeNullFromMap(result);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return result;
    }

    public static String[] getThemeKeywordFieldsNames(Class<?> c) {
        Field[] fields = c.getDeclaredFields();
        List<String> fieldList = new ArrayList<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.equals("itemId") || field.equals("class")) {
                continue;
            }
            if (fieldName.equals("themeName")) {
                continue;
            }
            fieldList.add(fieldName);
        }
        return fieldList.toArray(new String[fieldList.size()]);
    }

    public static <T> T convert(Object o, Class<T> cls) {
        if( o == null ){
            return null ;
        }
        try {
            T instance = cls.newInstance();
            BeanUtils.copyProperties(instance, o);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> QueryCriteria<T> convertQueryCriteria(QueryCriteria o, Class<T> cls) {
        try {
            T instance = cls.newInstance();
            BeanUtils.copyProperties(instance, o.getEntityDto());
            o.setEntityDto(instance);
            return o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Pagination<T> convertPagination(Pagination pagination, Class<T> cls) {
        List data = pagination.getData();
        List<T> newData = new ArrayList<>(data.size());
        if (CollectionUtils.isNotEmpty(data)) {
            data.forEach(o -> {
                T instance;
                try {
                    instance = cls.newInstance();
                    BeanUtils.copyProperties(instance, o);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                newData.add(instance);
            });
        }
        pagination.setData(newData);
        return pagination;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> convertList(List list, Class<T> cls) {
        try {
            List<T> retList = new ArrayList<>(list.size());
            list.forEach(item -> {
                try {
                    T instance = cls.newInstance();
                    BeanUtils.copyProperties(instance, item);
                    retList.add(instance);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return retList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] getFieldsNames(@SuppressWarnings("rawtypes") Class c){
        Field[] fields = c.getDeclaredFields();
        List<String> fieldList = new ArrayList<>();
        for (Field field : fields){
            String fieldName = field.getName();
            if (fieldName.equals("itemId") || field.equals("class") || fieldName.equals("belongCurrentThemeKeyword")){
                continue;
            }
            fieldList.add(fieldName);
        }
        return fieldList.toArray(new String[fieldList.size()]);
    }

    public static Map<String,String> getFieldsNamesInMap(@SuppressWarnings("rawtypes") Class c){
        Field[] fields = c.getDeclaredFields();
        Map<String,String> fieldList = new HashMap<String,String>();
        for (Field field : fields){
            String fieldName = field.getName();
            if (fieldName.equals("itemId") || field.equals("class") || fieldName.equals("belongCurrentThemeKeyword")){
                continue;
            }
            fieldList.put(fieldName,fieldName);
        }
        return fieldList;
    }
}
