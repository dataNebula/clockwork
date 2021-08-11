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

package com.creditease.adx.clockwork.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class RestTemplateClient {

    private static final Logger LOG = LoggerFactory.getLogger(RestTemplateClient.class);

    @Autowired
    private RestTemplate customRestTemplate;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @SuppressWarnings("unchecked")
	public Map<String, Object> getResult(String url, MultiValueMap<String, String> paramMap) {
        return customRestTemplate.postForObject(url, paramMap, Map.class);
    }

    @SuppressWarnings("unchecked")
	public Map<String, Object> getResult(String url, Object obj) {
        return customRestTemplate.postForObject(url, obj, Map.class);
    }


    @SuppressWarnings("rawtypes")
	public void getAsyncResult(String url, Object obj) {
        ListenableFuture<ResponseEntity<Map>> listenableFuture =
                asyncRestTemplate.postForEntity(url, new HttpEntity<Object>(obj), Map.class);

        listenableFuture.addCallback(
                new SuccessCallback<ResponseEntity<Map>>() {
                    @Override
                    public void onSuccess(ResponseEntity<Map> result) {
                        LOG.info("[RestTemplateClient]Response = {}", result.getBody());
                    }
                },
                new FailureCallback() {
                    @Override
                    public void onFailure(Throwable t) {
                        LOG.error(t.getMessage(), t);
                    }
                });

    }
}
