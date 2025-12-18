package com.ecommerce.apigateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Value;

import com.ecommerce.apigateway.config.TestConfig;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({TestConfig.class, ApiGatewayTest.TestSecurityConfig.class})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "test.services.port=${wiremock.server.port}",
    "spring.main.allow-bean-definition-overriding=true",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://fake-issuer-url.com",
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://fake-jwk-uri.com",
    "spring.cloud.gateway.routes[0].id=product-service",
    "spring.cloud.gateway.routes[0].uri=http://localhost:${wiremock.server.port}",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/products/**",
    "spring.cloud.gateway.routes[1].id=order-service",
    "spring.cloud.gateway.routes[1].uri=http://localhost:${wiremock.server.port}",
    "spring.cloud.gateway.routes[1].predicates[0]=Path=/api/orders/**",
    "spring.cloud.gateway.routes[2].id=user-service",
    "spring.cloud.gateway.routes[2].uri=http://localhost:${wiremock.server.port}",
    "spring.cloud.gateway.routes[2].predicates[0]=Path=/api/users/**",
    "spring.cloud.gateway.routes[3].id=cart-service",
    "spring.cloud.gateway.routes[3].uri=http://localhost:${wiremock.server.port}",
    "spring.cloud.gateway.routes[3].predicates[0]=Path=/api/carts/**",
    "spring.cloud.gateway.routes[4].id=payment-service",
    "spring.cloud.gateway.routes[4].uri=http://localhost:${wiremock.server.port}",
    "spring.cloud.gateway.routes[4].predicates[0]=Path=/api/payments/**",
    "spring.cloud.gateway.routes[5].id=notification-service",
    "spring.cloud.gateway.routes[5].uri=http://localhost:${wiremock.server.port}",
    "spring.cloud.gateway.routes[5].predicates[0]=Path=/api/notifications/**",
    "spring.cloud.gateway.routes[6].id=inventory-service",
    "spring.cloud.gateway.routes[6].uri=http://localhost:${wiremock.server.port}",
    "spring.cloud.gateway.routes[6].predicates[0]=Path=/api/inventory/**"
})
public class ApiGatewayTest {

    @LocalServerPort
    private int port;

    @Value("${wiremock.server.port}")
    private int wireMockPort;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // Reset WireMock stubs before each test
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
        
        // Configure WireMock to use the dynamic port
        WireMock.configureFor("localhost", wireMockPort);
        
        // Stub common responses for all services
        stubFor(get(anyUrl())
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"OK\"}")));
    }
    
    @TestConfiguration
    @EnableWebFluxSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
            return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
        }
    }

    @Test
    void productServiceRouting_ShouldRouteToProductService() {
        // Stub the product service response
        String responseBody = "{\"content\":[{\"id\":\"1\",\"name\":\"Test Product\"}]}";
        
        wireMockServer.stubFor(
            get(urlPathMatching("/api/products"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody))
        );

        // Test the API Gateway route using WebTestClient
        webTestClient.get()
            .uri("/api/products")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$.content[0].name").isEqualTo("Test Product");
    }

    @Test
    void orderServiceRouting_ShouldRouteToOrderService() {
        // Stub the order service response
        String responseBody = "{\"content\":[{\"id\":\"1\",\"status\":\"PENDING\"}]}";
        
        wireMockServer.stubFor(
            get(urlPathMatching("/api/orders"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody))
        );

        // Test the API Gateway route using WebTestClient
        webTestClient.get()
            .uri("/api/orders")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$.content[0].status").isEqualTo("PENDING");
    }

    @Test
    void userServiceRouting_ShouldRouteToUserService() {
        // Stub the user service response for unauthorized
        String responseBody = "{\"error\":\"Unauthorized\"}";
        
        wireMockServer.stubFor(
            get(urlPathMatching("/api/users/me"))
                .willReturn(aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody))
        );

        // Test the API Gateway route using WebTestClient
        webTestClient.get()
            .uri("/api/users/me")
            .exchange()
            .expectStatus().isUnauthorized()
            .expectHeader().contentType("application/json");
    }

    @Test
    void cartServiceRouting_ShouldRouteToCartService() {
        String customerId = "test-customer-" + System.currentTimeMillis();
        String responseBody = "{\"items\":[]}";
        
        // Stub the cart service response
        wireMockServer.stubFor(
            get(urlPathMatching("/api/carts"))
                .withHeader("X-Customer-Id", matching(".*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody))
        );
        
        webTestClient.get()
            .uri("/api/carts")
            .header("X-Customer-Id", customerId)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$.items").exists();
    }

    @Test
    void inventoryServiceRouting_ShouldRouteToInventoryService() {
        // Stub the inventory service response for non-existent product
        wireMockServer.stubFor(
            get(urlPathMatching("/api/inventory/123"))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json"))
        );
        
        webTestClient.get()
            .uri("/api/inventory/123")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void paymentServiceRouting_ShouldRouteToPaymentService() {
        String responseBody = "[{\"id\":\"1\",\"name\":\"Credit Card\"},{\"id\":\"2\",\"name\":\"PayPal\"}]";
        
        // Stub the payment service response
        wireMockServer.stubFor(
            get(urlPathMatching("/api/payments/methods"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody))
        );
        
        webTestClient.get()
            .uri("/api/payments/methods")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void notificationServiceRouting_ShouldRouteToNotificationService() {
        String responseBody = "{\"content\":[]}";
        
        // Stub the notification service response
        wireMockServer.stubFor(
            get(urlPathMatching("/api/notifications/history"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody))
        );
        
        webTestClient.get()
            .uri("/api/notifications/history")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$.content").exists();
    }

    @Test
    void nonExistentEndpoint_ShouldReturnNotFound() {
        // No need to stub for non-existent endpoints as they should be handled by the gateway
        webTestClient.get()
            .uri("/api/non-existent")
            .exchange()
            .expectStatus().isNotFound();
    }
}
