package com.ecommerce.order.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
            // Database
            "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.datasource.initialization-mode=always",
            
            // JPA
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.show-sql=true",
            "spring.jpa.properties.hibernate.format_sql=true",
            "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
            
            // Disable Eureka
            "eureka.client.enabled=false",
            "spring.cloud.discovery.enabled=false",
            
            // Disable Kafka
            "spring.kafka.enabled=false",
            
            // Test config
            "spring.test.database.replace=any",
            "spring.main.allow-bean-definition-overriding=true"
        ).applyTo(applicationContext.getEnvironment());
    }
}
