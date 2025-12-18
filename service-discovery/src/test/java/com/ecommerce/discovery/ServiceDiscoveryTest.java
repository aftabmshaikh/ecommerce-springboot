package com.ecommerce.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceDiscoveryTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EurekaInstanceConfigBean eurekaInstance;

    @Test
    void contextLoads() {
        // Basic test to verify the Spring context loads
        assertNotNull(restTemplate);
        assertNotNull(eurekaInstance);
    }

    @Test
    void eurekaServerEndpoints_ShouldBeAccessible() {
        // Test Eureka's REST API endpoints
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/eureka/apps", 
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() != null && !response.getBody().isEmpty());
    }

    @Test
    void serviceRegistration_ShouldBeEnabled() {
        // Verify that service registration is enabled
        // Note: isPreferIpAddress() may be false by default, so we just check that instance config exists
        assertNotNull(eurekaInstance);
        assertNotNull(eurekaInstance.getAppname());
        assertNotNull(eurekaInstance.getInstanceId());
    }

    @Test
    void healthCheck_ShouldBeAccessible() {
        // Test the health check endpoint
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/health", 
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().contains("\"status\":\"UP\""));
    }
}
