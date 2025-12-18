package com.ecommerce.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("product-service", r -> r.path("/api/products/**")
                .filters(f -> f
                    .removeRequestHeader("Origin")
                    .removeResponseHeader("Access-Control-Allow-Origin")
                    .removeResponseHeader("Access-Control-Allow-Credentials")
                    .removeResponseHeader("Access-Control-Allow-Methods")
                    .removeResponseHeader("Access-Control-Allow-Headers")
                )
                .uri("lb://product-service"))
            .route("order-service", r -> r.path("/api/orders/**")
                .filters(f -> f
                    .removeRequestHeader("Origin")
                    .removeResponseHeader("Access-Control-Allow-Origin")
                    .removeResponseHeader("Access-Control-Allow-Credentials")
                    .removeResponseHeader("Access-Control-Allow-Methods")
                    .removeResponseHeader("Access-Control-Allow-Headers")
                )
                .uri("lb://order-service"))
            .route("user-service", r -> r.path("/api/users/**")
                .filters(f -> f
                    .removeRequestHeader("Origin")
                    .removeResponseHeader("Access-Control-Allow-Origin")
                    .removeResponseHeader("Access-Control-Allow-Credentials")
                    .removeResponseHeader("Access-Control-Allow-Methods")
                    .removeResponseHeader("Access-Control-Allow-Headers")
                )
                .uri("lb://user-service"))
            .route("inventory-service", r -> r.path("/api/inventory/**")
                .filters(f -> f
                    .removeRequestHeader("Origin")
                    .removeResponseHeader("Access-Control-Allow-Origin")
                    .removeResponseHeader("Access-Control-Allow-Credentials")
                    .removeResponseHeader("Access-Control-Allow-Methods")
                    .removeResponseHeader("Access-Control-Allow-Headers")
                )
                .uri("lb://inventory-service"))
            .build();
    }
}
