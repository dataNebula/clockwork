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

package com.creditease.adx.clockwork.common.entity;

import java.util.HashMap;
import java.util.Map;

import com.creditease.adx.clockwork.common.constant.Constant;

public class Response {

    /**
     * Response.success
     *
     * @param data ret data
     * @return
     */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constant.CODE, Constant.SUCCESS_CODE);
        result.put(Constant.MSG, null);
        result.put(Constant.DATA, data);
        return result;
    }


    /**
     * Response.fail
     *
     * @param errorMsg 异常信息
     * @return
     */
    public static Map<String, Object> fail(Object errorMsg) {
        return fail(null, errorMsg);
    }

    /**
     * Response.fail
     *
     * @param data     ret data
     * @param errorMsg 异常信息
     * @return
     */
    public static Map<String, Object> fail(Object data, Object errorMsg) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constant.CODE, Constant.FAILURE_CODE);
        result.put(Constant.MSG, errorMsg);
        result.put(Constant.DATA, data);
        return result;
    }


}
