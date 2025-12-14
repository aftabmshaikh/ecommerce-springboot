package com.ecommerce.cart.config;

import com.ecommerce.cart.client.ProductServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Optional;
import java.util.UUID;

@TestConfiguration
@Profile("test")
@EnableAutoConfiguration(exclude = {FeignClient.class})
@ComponentScan(
    basePackages = {"com.ecommerce.cart"},
    excludeFilters = {@ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ANNOTATION, classes = FeignClient.class)}
)
public class TestConfig {
    
    @Bean
    @Primary
    public ProductServiceClient productServiceClient() {
        ProductServiceClient mockClient = Mockito.mock(ProductServiceClient.class);
        
        // Mock product response
        ProductServiceClient.ProductDto productDto = new ProductServiceClient.ProductDto(
            UUID.fromString("123e4567-e89b-42d3-a456-556642440000"),
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
