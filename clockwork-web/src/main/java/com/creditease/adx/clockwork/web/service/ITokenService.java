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

package com.creditease.adx.clockwork.web.service;

import com.creditease.adx.clockwork.common.enums.TokenStatus;

/**
 * @ Author     ：XuanDongTang
 * @ Date       ：Created in 6:08 下午 2020/1/13
 * @ Description：ITokenService
 * @ Modified By：
 */
public interface ITokenService {

    String  getAccessTokenType();

    String  getRefreshTokenType();

    String createToken(String username, String password, String tokenType);

    TokenStatus checkToken(String token);

    String refreshToken(String refreshToken);


}
