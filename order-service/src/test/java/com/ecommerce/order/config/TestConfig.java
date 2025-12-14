package com.ecommerce.order.config;

import com.ecommerce.order.service.OrderService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public OrderService orderService() {
        return null; // Mocked by @MockBean in the test
    }
    
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
