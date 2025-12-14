package com.ecommerce.cart.config;

import com.ecommerce.cart.client.ProductServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                return new Jwt(
                    "test-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600),
                    Map.of("alg", "none"),
                    Map.of(
                        "sub", "test-user",
                        "email", "test@example.com",
                        "scope", "read write",
                        "client_id", "test-client"
                    )
                );
            }
        };
    }
    
    @Bean
    @Primary
    public ProductServiceClient productServiceClient() {
        ProductServiceClient mockClient = Mockito.mock(ProductServiceClient.class);
        
        // Mock product response
        ProductServiceClient.ProductDto productDto = new ProductServiceClient.ProductDto(
            UUID.randomUUID(),
            "Test Product",
            "Test Description",
            "https://example.com/test.jpg",
            19.99,
            10
        );
        
        // Mock the getProductById call
        Mockito.when(mockClient.getProductById(Mockito.any(UUID.class)))
            .thenReturn(Optional.of(productDto));
            
        // Mock the isInStock call
        Mockito.when(mockClient.isInStock(Mockito.any(UUID.class), Mockito.anyInt()))
            .thenReturn(true);
            
        return mockClient;
    }
}
