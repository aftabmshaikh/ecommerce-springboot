package com.ecommerce.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RemoveCorsFilterFactory extends AbstractGatewayFilterFactory<RemoveCorsFilterFactory.Config> {
    
    public RemoveCorsFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Remove CORS-related headers from the request
            ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("Origin");
                    headers.remove("Access-Control-Request-Method");
                    headers.remove("Access-Control-Request-Headers");
                })
                .build();
            
            // Continue with the filter chain
            return chain.filter(exchange.mutate().request(request).build())
                .then(Mono.fromRunnable(() -> {
                    // Remove CORS-related headers from the response
                    exchange.getResponse().getHeaders().remove("Access-Control-Allow-Origin");
                    exchange.getResponse().getHeaders().remove("Access-Control-Allow-Credentials");
                    exchange.getResponse().getHeaders().remove("Access-Control-Allow-Methods");
                    exchange.getResponse().getHeaders().remove("Access-Control-Allow-Headers");
                }));
        };
    }

    public static class Config {
        // Configuration properties if needed
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
