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

package com.creditease.adx.clockwork.common.framework.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ipolaris on 9/10/15.
 */
public class Pagination<T> {
    /**
     * 总页数
     */
    private long totalPageCount;
    /**
     * 当前页数
     */
    private long currentPageIndex;
    /**
     * 每页大小
     */
    private int pageSize;
    /**
     * 下一页页码
     */
    private long nextPageIndex;
    /**
     * 上一页页码
     */
    private long previousPageIndex;
    /**
     * 数据总条数
     */
    private long totalCount;
    /**
     * 返回的数据列表
     */
    private List<T> data;
    /**
     * 其它辅助资源
     */
    private Map<String,Object> other;
    /**
     * 错误信息
     */
    private String errorMsg;
	/**
     * 结果状态
     */
    private String resultStatus;
    
    
    public Pagination<T> emptyInit(){
    		data = new ArrayList<T>();
    		totalCount = 0;
    		return this;
    }

	public Pagination(){

    }
    public Pagination(long totalCount, long currentPageIndex, int pageSize, List<T> data){
        this.totalCount = totalCount;
        this.currentPageIndex = currentPageIndex;
        this.pageSize = pageSize;
        this.data = data;
        this.totalPageCount = (totalCount%pageSize == 0) ? totalCount/pageSize : (totalCount/pageSize +1);
        if (totalPageCount > currentPageIndex){
            this.nextPageIndex = currentPageIndex + 1;
        }else{
            nextPageIndex = totalPageCount;
        }
    }

    public long getTotalPageCount() {
        return totalPageCount;
    }

    public void setTotalPageCount(long totalPageCount) {
        this.totalPageCount = totalPageCount;
    }

    public long getCurrentPageIndex() {
        return currentPageIndex;
    }

    public void setCurrentPageIndex(long currentPageIndex) {
        this.currentPageIndex = currentPageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getNextPageIndex() {
        return nextPageIndex;
    }

    public void setNextPageIndex(long nextPageIndex) {
        this.nextPageIndex = nextPageIndex;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
    
    public Map<String, Object> getOther() {
		return other;
	}
	public void setOther(Map<String, Object> other) {
		this.other = other;
	}
    public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public String getResultStatus() {
		return resultStatus;
	}
	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}
    public long getPreviousPageIndex() {
        return previousPageIndex;
    }

    public void setPreviousPageIndex(long previousPageIndex) {
        this.previousPageIndex = previousPageIndex;
    }

    @Override
    public String toString() {
        return "Pagination{" +
                "totalPageCount=" + totalPageCount +
                ", currentPageIndex=" + currentPageIndex +
                ", pageSize=" + pageSize +
                ", totalCount=" + totalCount +
                ", data=" + data +
                '}';
    }
}
