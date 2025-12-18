package com.ecommerce.order.config;

import com.ecommerce.order.client.ProductServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for integration tests.
 * This configuration is only active when the 'integration-test' profile is active.
 */
@TestConfiguration
@Profile("integration-test")
public class IntegrationTestConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }

    @Bean
    public ProductServiceClient productServiceClient() {
        return Mockito.mock(ProductServiceClient.class);
    }
}
