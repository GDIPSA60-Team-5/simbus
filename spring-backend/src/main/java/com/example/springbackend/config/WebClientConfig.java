package com.example.springbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient ltaWebClient(@Value("${api.lta.baseurl}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient nusWebClient(@Value("${api.nus.baseurl}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient oneMapWebClient(@Value("${onemap.base-url:https://www.onemap.gov.sg}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}