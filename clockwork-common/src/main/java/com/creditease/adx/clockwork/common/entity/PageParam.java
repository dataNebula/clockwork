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

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Range;

/**
 * @desc 分页查询对象
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParam {

	@Builder.Default
    @ApiModelProperty("第几页")
    @Range(min = 1, max = Integer.MAX_VALUE)
    public int pageNum = 1;

	@Builder.Default
    @ApiModelProperty("每页多少条数据")
    @Range(min = 1, max = Integer.MAX_VALUE)
    public int pageSize = 10;

    @ApiModelProperty("用户")
    public String userName;

    @ApiModelProperty("角色")
    public String role;

    @ApiModelProperty("查询条件，单个条件的话直接是一个string，多个查询条件为json类型的string")
    public String condition;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "PageParam{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", userName=" + userName +
                ", role=" + role +
                ", condition=" + condition +
                "}";
    }


}
