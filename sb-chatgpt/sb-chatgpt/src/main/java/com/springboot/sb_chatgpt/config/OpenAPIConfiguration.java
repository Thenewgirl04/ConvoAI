package com.springboot.sb_chatgpt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAPIConfiguration {

    @Value("${openapi.api.url}")
    private String apiUrl;

    @Value("${openapi.api.key}")
    private String apiKey;

    @Bean
    public RestClient restClient()  {
        return RestClient.builder()
                .baseUrl("https://api.openai.com/v1")       // 👈 base URL here
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey) // 👈 auth here
                .build();
    }
}
