package com.ecommerce.apigateway.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.Map;

@TestConfiguration
public class TestConfig {
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                // Return a mock JWT with basic claims
                return new Jwt(
                    "mock-token",
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
}
