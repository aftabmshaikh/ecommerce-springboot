package com.ecommerce.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@Profile("minimal")
public class MinimalConfig {
    
    // CORS Configuration
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.addAllowedHeader("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
    
    // Security Configuration
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/actuator/info").permitAll()
                .pathMatchers("/api/users/register").permitAll()
                .pathMatchers("/api/users/login").permitAll()
                .pathMatchers("/api/products/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
            .build();
    }
    
    // Rate Limiting Key Resolver
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String user = exchange.getRequest().getHeaders().getFirst("User");
            return Mono.just(user != null ? user : "anonymous");
        };
    }
    
    // Route Configuration with Circuit Breaker and Retry
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("product-service", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .addRequestHeader("X-Service-Name", "product-service")
                    .circuitBreaker(config -> config
                        .setName("productServiceCB")
                        .setFallbackUri("forward:/fallback"))
                    .retry(3)
                )
                .uri("lb://product-service"))
                
            .route("order-service", r ->
                r.path("/api/orders/**")
                .filters(f ->
                    f.addRequestHeader("X-Service-Name", "order-service")
                     .circuitBreaker(config -> config
                         .setName("orderServiceCB")
                         .setFallbackUri("forward:/fallback"))
                     .retry(3)
                )
                .uri("lb://order-service"))
                
            .route("user-service", r ->
                r.path("/api/users/**")
                .filters(f ->
                    f.addRequestHeader("X-Service-Name", "user-service")
                     .circuitBreaker(config -> config
                         .setName("userServiceCB")
                         .setFallbackUri("forward:/fallback"))
                     .retry(3)
                )
                .uri("lb://user-service"))
                
            .route("inventory-service", r ->
                r.path("/api/inventory/**")
                .filters(f ->
                    f.addRequestHeader("X-Service-Name", "inventory-service")
                     .circuitBreaker(config -> config
                         .setName("inventoryServiceCB")
                         .setFallbackUri("forward:/fallback"))
                     .retry(3)
                )
                .uri("lb://inventory-service"))
                
            // Fallback route for circuit breaker
            .route("fallback-route", r ->
                r.path("/fallback")
                .uri("forward:/fallback"))
                
            .build();
    }
}
