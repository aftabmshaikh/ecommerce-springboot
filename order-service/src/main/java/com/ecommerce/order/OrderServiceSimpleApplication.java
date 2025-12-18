package com.ecommerce.order;

import org.springframework.boot.SpringApplication;

/**
 * Optional simplified entry point used for the "simple" profile.
 * Removed SpringBootApplication annotations to avoid multiple
 * @SpringBootConfiguration classes during test scanning.
 */
public class OrderServiceSimpleApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(OrderServiceSimpleApplication.class);
        // Use our simplified properties file
        application.setAdditionalProfiles("simple");
        application.run(args);
    }
}
