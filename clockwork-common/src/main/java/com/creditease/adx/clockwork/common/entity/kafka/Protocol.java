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

package com.creditease.adx.clockwork.common.entity.kafka;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 18:23 2019-09-03
 * @ Description：
 * @ Modified By：
 */
public class Protocol {

    private String type;
    private String version = "v1";
    private int msg_id = -1;
    private int msg_prev_id = -1;

    @SuppressWarnings("unused")
	private Protocol() {
    }

    public Protocol(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(int msg_id) {
        this.msg_id = msg_id;
    }

    public int getMsg_prev_id() {
        return msg_prev_id;
    }

    public void setMsg_prev_id(int msg_prev_id) {
        this.msg_prev_id = msg_prev_id;
    }
}
