package com.creditease.adx.clockwork.common.enums;
/**
 * token 状态
 */
public enum TokenStatus {
	 SUCCESS("success"),
     TOKEN_EXPIRED("tokenExpired"),
     ACCESS_TOKEN_EXPIRED("accessTokenExpired"),
     REFRESH_TOKEN_EXPIRED("refreshTokenExpired"),
     ERROR("tokenError");

     private String value;

     TokenStatus(String value) {
         this.value = value;
     }

     public String getValue() {
         return value;
     }
}
