package com.ecommerce.notification.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NotificationApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void sendEmail_WithValidRequest_ShouldReturnAccepted() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "to": "test@example.com",
                    "subject": "Test Email",
                    "templateName": "order-confirmation",
                    "data": {
                        "orderId": "12345",
                        "customerName": "Test User"
                    }
                }""")
        .when()
            .post("/api/notifications/email")
        .then()
            .statusCode(202)
            .body("message", equalTo("Email notification sent"));
    }

    @Test
    void sendSms_WithValidRequest_ShouldReturnAccepted() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "to": "+1234567890",
                    "message": "Test SMS message"
                }""")
        .when()
            .post("/api/notifications/sms")
        .then()
            .statusCode(202)
            .body("message", equalTo("SMS notification sent"));
    }

    @Test
    void getNotificationHistory_ShouldReturnPaginatedResults() {
        given()
            .param("page", 0)
            .param("size", 10)
        .when()
            .get("/api/notifications/history")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalPages", greaterThanOrEqualTo(0));
    }

    @Test
    void getNotificationStatus_WithValidId_ShouldReturnStatus() {
        // First send a notification to get an ID
        String notificationId = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "to": "test@example.com",
                    "subject": "Test Status Check",
                    "message": "Test message"
                }""")
        .when()
            .post("/api/notifications/email")
        .then()
            .statusCode(202)
            .extract()
            .path("notificationId");

        // Then check its status
        given()
            .pathParam("id", notificationId)
        .when()
            .get("/api/notifications/{id}/status")
        .then()
            .statusCode(200)
            .body("status", not(emptyString()));
    }
}
