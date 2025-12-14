package com.ecommerce.inventory.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class TestConfig {

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("inventory_test")
                .withUsername("test")
                .withPassword("test");
        container.start();
        return container;
    }

    @Bean
    public DataSource dataSource(PostgreSQLContainer<?> postgreSQLContainer) {
        Map<String, String> properties = new HashMap<>();
        properties.put("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        properties.put("spring.datasource.username", postgreSQLContainer.getUsername());
        properties.put("spring.datasource.password", postgreSQLContainer.getPassword());
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setUrl(properties.get("spring.datasource.url"));
        dataSource.setUsername(properties.get("spring.datasource.username"));
        dataSource.setPassword(properties.get("spring.datasource.password"));
        dataSource.setDriverClassName(properties.get("spring.datasource.driver-class-name"));
        
        return dataSource;
    }
}
