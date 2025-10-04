package com.servease.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Configuration
public class TossRestTemplateConfig {
    @Value("${toss.secret-key}")
    String secretKey;

    @Bean
    public RestTemplate tossRt(RestTemplateBuilder restTemplateBuilder) {
        String basic = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .additionalInterceptors((req, body, ex) -> {
                    req.getHeaders().set(HttpHeaders.AUTHORIZATION, "Basic " + basic);
                    req.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return ex.execute(req, body);
                })
                .build();
    }
}

