package com.ecommerce.inventory.api;

import com.ecommerce.inventory.base.BaseIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * TODO: Fix KafkaController performance issues causing test execution to hang/stuck
 * 
 * Issue: EmbeddedKafka's KafkaController is generating periodic "Processing automatic 
 * preferred replica leader election" messages every 5 minutes, which is causing tests 
 * to hang and get stuck during execution. The KafkaController background maintenance 
 * tasks are interfering with test execution, and tests are not completing within 
 * reasonable time limits.
 * 
 * Symptoms:
 * - Tests hang during execution and do not complete
 * - KafkaController messages appear every 5 minutes in logs
 * - Testcontainers may also be contributing to the hang (PostgreSQL container startup)
 * - Tests timeout even with @Timeout annotations
 * 
 * Potential solutions to investigate:
 * 1. Configure EmbeddedKafka with optimized broker properties to reduce controller overhead
 * 2. Use Testcontainers Kafka instead of EmbeddedKafka for better isolation
 * 3. Mock KafkaTemplate completely in integration tests instead of using EmbeddedKafka
 * 4. Add explicit shutdown hooks to ensure EmbeddedKafka stops cleanly after tests
 * 5. Investigate if Kafka listener auto-startup configuration is causing delays
 * 6. Consider using @DirtiesContext to force context refresh between tests
 * 7. Investigate Testcontainers PostgreSQL container reuse and startup timeout settings
 * 8. Evaluate if Spring Boot test context caching is causing issues
 * 
 * Related configuration:
 * - BaseIntegrationTest: EmbeddedKafka configuration, PostgreSQL Testcontainer setup
 * - TestConfig: KafkaTemplate mocking
 * - application-test.yml: Kafka consumer/producer settings
 * - pom.xml: Maven Surefire plugin timeout settings
 */
@Disabled
public class InventoryApiTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = ""; // Ensure base path is empty since context-path is overridden to empty
    }

    @Test
    @Timeout(30) // 30 seconds timeout per test
    void getInventory_WithValidSku_ShouldReturnInventory() {
        String skuCode = createTestInventoryItem(10, 5);

        given()
            .pathParam("skuCode", skuCode)
        .when()
            .get("/api/inventory/{skuCode}")
        .then()
            .statusCode(200)
            .body("skuCode", equalTo(skuCode))
            .body("quantity", greaterThanOrEqualTo(0));
    }

    @Test
    @Timeout(30)
    void updateInventory_WithValidData_ShouldUpdateQuantity() {
        // Start with quantity 0 so adjustment sets it to 50
        String skuCode = createTestInventoryItem(0, 5);

        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "skuCode": "%s",
                    "adjustment": 50,
                    "reason": "Test adjustment",
                    "referenceId": "REF-123"
                }
                """.formatted(skuCode))
        .when()
            .post("/api/inventory/adjust")
        .then()
            .statusCode(200)
            .body("skuCode", equalTo(skuCode))
            .body("quantity", equalTo(50));
    }

    @Test
    @Timeout(30)
    void checkStock_WithValidRequest_ShouldReturnStockStatus() {
        String skuCode = createTestInventoryItem(10, 5);

        given()
        .when()
            .get("/api/inventory/status/{skuCode}", skuCode)
        .then()
            .statusCode(200)
            .body("skuCode", equalTo(skuCode))
            .body("inStock", equalTo(true));
    }

    @Test
    @Timeout(30)
    void getLowStockItems_ShouldReturnItems() {
        // Create one low-stock item (quantity below threshold)
        String lowStockSku = createTestInventoryItem(3, 5);

        given()
        .when()
            .get("/api/inventory/low-stock")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("skuCode", hasItem(lowStockSku));
    }

    private String createTestInventoryItem(int quantity, int lowStockThreshold) {
        String productId = UUID.randomUUID().toString();
        String skuCode = "SKU-" + UUID.randomUUID().toString().substring(0, 8);

        String body = """
            {
              "productId": "%s",
              "skuCode": "%s",
              "quantity": %d,
              "reservedQuantity": 0,
              "lowStockThreshold": %d,
              "restockThreshold": %d,
              "unitCost": 19.99,
              "locationCode": "WH-001",
              "binLocation": "A1-B2",
              "isActive": true
            }
            """.formatted(productId, skuCode, quantity, lowStockThreshold, lowStockThreshold + 5);

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/inventory")
        .then()
            .statusCode(201)
            .body("skuCode", equalTo(skuCode));

        return skuCode;
    }
}
