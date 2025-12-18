package com.ecommerce.inventory.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Test configuration that provides mocked beans for testing.
 * Note: EmbeddedKafka and PostgreSQL container are configured in BaseIntegrationTest
 * to avoid conflicts and duplicate initialization.
 */
@TestConfiguration
public class TestConfig {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;
}
