package br.com.danilodps.receiver.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${coordinator.url}")
    private String coordinatorUrl;

    @Bean
    public RestClient coordinatorRestClient() {
        return RestClient.builder()
                .baseUrl(coordinatorUrl)
                .build();
    }

}