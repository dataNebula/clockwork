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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ipolaris on 7/13/15.
 */
public class ReflectUtil {
    /**
     * @param dir
     * @param pk
     * @return
     */
    private static List<Class<?>> getClasses(File dir, String pk) {
        List<Class<?>> classes = new ArrayList<>();
        if (dir == null || !dir.exists()){
            return classes;
        }
        for (File f : dir.listFiles()){
            if (f.isDirectory()){
                //classes.addAll(getClasses(f, pk));
                continue;
            }
            String fName = f.getName();
            if (fName.endsWith(".class")){
                try {
                    classes.add(Class.forName(pk+"."+fName.substring(0,fName.length()-6)));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

    /**
     * 获取指定类下同级及下级的所有类
     * @param targetClass 指定的类
     * @return
     */
    public static List<Class<?>> getClasses(@SuppressWarnings("rawtypes") Class targetClass){
        String packG = targetClass.getPackage().getName();
        String path = packG.replace(".", File.separator);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String filePath = classLoader.getResource("").getPath().concat(path);
        return getClasses(new File(filePath), packG);
    }

    /**
     * 获取指定类所在包下同级及下级子类
     * @param targetClass
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<Class<?>> getExtendsClassInSubPackage(@SuppressWarnings("rawtypes") Class targetClass){
        List<Class<?>> targetClasses = getClasses(targetClass);
        List<Class<?>> resultCollection = new ArrayList<>();
        if (null != targetClasses && !targetClasses.isEmpty()){
            for (Class<?> cls: targetClasses){
                if (targetClass.isAssignableFrom(cls) && !cls.equals(targetClass)){
                   resultCollection.add(cls);
                }
            }
        }
        return resultCollection;
    }
    
    /** 
     * 获得超类的参数类型，取第一个参数类型 
     * @param <T> 类型参数 
     * @param clazz 超类类型 
     */  
    @SuppressWarnings({ "rawtypes", "unchecked" })  
    public static <T> Class<T> getClassGenricType(final Class clazz) {  
        return getClassGenricType(clazz, 0);  
    }  
      
    /** 
     * 根据索引获得超类的参数类型 
     * @param clazz 超类类型 
     * @param index 索引 
     */  
    @SuppressWarnings("rawtypes")  
    public static Class getClassGenricType(final Class clazz, final int index) {  
        Type genType = clazz.getGenericSuperclass();  
        if (!(genType instanceof ParameterizedType)) {  
            return Object.class;  
        }  
        Type[] params = ((ParameterizedType)genType).getActualTypeArguments();  
        if (index >= params.length || index < 0) {  
            return Object.class;  
        }  
        if (!(params[index] instanceof Class)) {  
            return Object.class;  
        }  
        return (Class) params[index];  
    } 
    
    @SuppressWarnings("rawtypes")  
    public static Object constructorNewInstance(String className,Class [] parameterTypes,Object[] initargs) {   
        try {  
            Constructor<?> constructor = Class.forName(className).getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);  
            return constructor.newInstance(initargs);  
        } catch (Exception ex) {  
            throw new RuntimeException();  
        }  
    }  
    
    public static Object constructorNewInstance(String className) {   
        try {  
            return Class.forName(className).newInstance();
        } catch (Exception ex) {  
            throw new RuntimeException();  
        }  
    }  

}
