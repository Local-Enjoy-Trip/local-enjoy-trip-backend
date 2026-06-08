package com.ssafy.enjoytrip.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
class ExternalClientConfig {

    @Bean
    HttpClient externalHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    ObjectMapper externalObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    RestClient openWeatherMapRestClient() {
        return RestClient.create();
    }
}
