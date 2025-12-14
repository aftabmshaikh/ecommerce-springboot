package com.ecommerce.inventory.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class InventoryApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void getInventory_WithValidProductId_ShouldReturnInventory() {
        String productId = createTestProduct();
        
        given()
            .pathParam("productId", productId)
        .when()
            .get("/api/inventory/{productId}")
        .then()
            .statusCode(200)
            .body("productId", equalTo(productId))
            .body("quantity", greaterThanOrEqualTo(0));
    }

    @Test
    void updateInventory_WithValidData_ShouldUpdateQuantity() {
        String productId = createTestProduct();
        
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "quantity": 50,
                    "operation": "SET"
                }""")
        .when()
            .put("/api/inventory/" + productId)
        .then()
            .statusCode(200)
            .body("quantity", equalTo(50));
    }

    @Test
    void checkStock_WithValidRequest_ShouldReturnStockStatus() {
        String productId = createTestProduct();
        
        given()
            .contentType(ContentType.JSON)
            .body("[" +
                "{\"productId\":\"" + productId + "\"," +
                "\"quantity\":5}" +
                "]")
        .when()
            .post("/api/inventory/check-stock")
        .then()
            .statusCode(200)
            .body("inStock", hasItem(true));
    }

    @Test
    void getInventoryHistory_ShouldReturnHistory() {
        String productId = createTestProduct();
        
        given()
            .param("productId", productId)
            .param("page", 0)
            .param("size", 10)
        .when()
            .get("/api/inventory/history")
        .then()
            .statusCode(200)
            .body("content", not(empty()));
    }

    private String createTestProduct() {
        // In a real scenario, this would create a test product in the database
        // For testing purposes, we'll use a random UUID
        return UUID.randomUUID().toString();
    }
}
