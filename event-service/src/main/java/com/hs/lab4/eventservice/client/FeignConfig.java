package com.hs.lab3.eventservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.client.ReactiveHttpRequestInterceptor;
import reactivefeign.client.ReactiveHttpRequestInterceptors;

@Slf4j
@Configuration
public class FeignConfig {

    @Value("${service.token}")
    private String token;

    @Bean
    public ReactiveHttpRequestInterceptor reactiveTokenInterceptor() {
        return ReactiveHttpRequestInterceptors.addHeader("Authorization", "Bearer " + token);
    }
}
