package com.creditease.adx.clockwork.common.enums;

public enum FillDataType {
	 HOUR("hour"),
     DAY("day"),
     WEEK("week"),
     MONTH("month");

     private String type;

     FillDataType(String type) {
         this.type = type;
     }

     public String getType() {
         return type;
     }

     public void setType(String type) {
         this.type = type;
     }

}
