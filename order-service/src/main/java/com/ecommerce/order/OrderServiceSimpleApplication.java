package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.ecommerce.order.controller",
    "com.ecommerce.order.service",
    "com.ecommerce.order.model"
})
@EntityScan("com.ecommerce.order.model")
@EnableJpaRepositories("com.ecommerce.order.repository")
public class OrderServiceSimpleApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(OrderServiceSimpleApplication.class);
        // Use our simplified properties file
        application.setAdditionalProfiles("simple");
        application.run(args);
    }
}
