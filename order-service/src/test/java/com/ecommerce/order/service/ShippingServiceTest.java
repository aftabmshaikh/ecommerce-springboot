package com.ecommerce.order.service;

import com.ecommerce.order.dto.shipping.ShippingOption;
import com.ecommerce.order.service.shipping.ShippingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ShippingServiceTest {

    @Mock
    private ShippingProvider shippingProvider;
    
    @InjectMocks
    private ShippingService shippingService;
    
    @BeforeEach
    void setUp() {
        // Set default currency for tests
        ReflectionTestUtils.setField(shippingService, "defaultCurrency", "USD");
    }

    @Test
    void getAvailableShippingOptions_ShouldReturnOptions() {
        // Given
        String country = "US";
        String postalCode = "12345";
        List<ShippingOption> mockOptions = List.of(
            ShippingOption.builder()
                .id("standard")
                .name("Standard Shipping")
                .estimatedDelivery("3-5 days")
                .price(new BigDecimal("4.99"))
                .currency("USD")
                .build(),
            ShippingOption.builder()
                .id("express")
                .name("Express Shipping")
                .estimatedDelivery("1-2 days")
                .price(new BigDecimal("9.99"))
                .currency("USD")
                .build()
        );
        
        when(shippingProvider.getAvailableShippingOptions(country, postalCode))
            .thenReturn(mockOptions);
        
        // When
        List<ShippingOption> options = shippingService.getAvailableShippingOptions(country, postalCode);
        
        // Then
        assertNotNull(options);
        assertEquals(2, options.size());
        assertEquals("USD", options.get(0).getCurrency());
        verify(shippingProvider).getAvailableShippingOptions(country, postalCode);
    }

    @Test
    void calculateShippingOptions_WithHighValue_ShouldIncludeFreeShipping() {
        // Given
        String country = "US";
        String postalCode = "12345";
        double weight = 2.5;
        double value = 100.0;
        
        // When
        List<ShippingOption> options = shippingService.calculateShippingOptions(country, postalCode, weight, value);
        
        // Then
        assertFalse(options.isEmpty());
        assertTrue(options.stream().anyMatch(o -> o.getPrice().compareTo(BigDecimal.ZERO) == 0));
        assertEquals("free", options.get(0).getId());
        assertEquals("Free Shipping", options.get(0).getName());
        assertEquals("3-7 business days", options.get(0).getEstimatedDelivery());
        assertEquals(BigDecimal.ZERO, options.get(0).getPrice());
    }
    
    @Test
    void calculateShippingOptions_WithWeight_ShouldCalculateCost() {
        // Given
        String country = "US";
        String postalCode = "12345";
        double weight = 2.5;
        double value = 50.0;
        
        // Mock the shipping provider to return some options
        when(shippingProvider.getAvailableShippingOptions(country, postalCode))
            .thenReturn(List.of(
                ShippingOption.builder()
                    .id("standard")
                    .name("Standard")
                    .estimatedDelivery("3-5 days")
                    .price(BigDecimal.ZERO)
                    .currency("USD")
                    .build(),
                ShippingOption.builder()
                    .id("express")
                    .name("Express")
                    .estimatedDelivery("1-2 days")
                    .price(BigDecimal.ZERO)
                    .currency("USD")
                    .build()
            ));
            
        // Mock the shipping cost calculation
        when(shippingProvider.calculateShippingCost(eq(country), eq(postalCode), eq(weight), eq(value)))
            .thenReturn(10.0)  // For standard
            .thenReturn(20.0); // For express
        
        // When
        List<ShippingOption> options = shippingService.calculateShippingOptions(country, postalCode, weight, value);
        
        // Then
        assertEquals(2, options.size());
        assertEquals(0, new BigDecimal("10.0").compareTo(options.get(0).getPrice()));
        assertEquals(0, new BigDecimal("20.0").compareTo(options.get(1).getPrice()));
        
        verify(shippingProvider).getAvailableShippingOptions(country, postalCode);
        verify(shippingProvider, times(2)).calculateShippingCost(eq(country), eq(postalCode), eq(weight), eq(value));
    }

    @Test
    void trackShipment_ShouldReturnTrackingInfo() {
        // Given
        String trackingNumber = "TEST123";
        Object expectedTrackingInfo = "Test tracking info";
        when(shippingProvider.trackShipment(trackingNumber)).thenReturn(expectedTrackingInfo);
        
        // When
        Object trackingInfo = shippingService.trackShipment(trackingNumber);
        
        // Then
        assertNotNull(trackingInfo);
        assertEquals(expectedTrackingInfo, trackingInfo);
        verify(shippingProvider).trackShipment(trackingNumber);
    }
}
