package com.creditease.adx.clockwork.common.enums;
/**
 * Task生命周期操作类型
 */
public enum TaskLifeCycleOpType {
	 BASE(1),
     TIMER(2);

     private Integer value;

     TaskLifeCycleOpType(Integer value) {
         this.value = value;
     }

     public Integer getValue() {
         return value;
     }

}
