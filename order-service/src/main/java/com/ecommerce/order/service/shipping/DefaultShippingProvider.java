package com.ecommerce.order.service.shipping;

import com.ecommerce.order.dto.shipping.ShippingOption;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DefaultShippingProvider implements ShippingProvider {

    @Override
    public List<ShippingOption> getAvailableShippingOptions(String country, String postalCode) {
        // In a real implementation, this would call an external shipping service
        // For testing, we'll return some dummy data
        return Arrays.asList(
                ShippingOption.builder()
                        .id("standard")
                        .name("Standard Shipping")
                        .estimatedDelivery("3-5 business days")
                        .price(new BigDecimal("4.99"))
                        .build(),
                ShippingOption.builder()
                        .id("express")
                        .name("Express Shipping")
                        .estimatedDelivery("1-2 business days")
                        .price(new BigDecimal("9.99"))
                        .build(),
                ShippingOption.builder()
                        .id("overnight")
                        .name("Overnight Shipping")
                        .estimatedDelivery("Next business day")
                        .price(new BigDecimal("19.99"))
                        .build()
        );
    }

    @Override
    public double calculateShippingCost(String country, String postalCode, double weight, double orderValue) {
        // Free shipping for orders over $100
        if (orderValue >= 100.0) {
            return 0.0;
        }
        
        // Simple calculation based on weight and country
        double baseRate = "US".equals(country) ? 5.0 : 15.0;
        double weightSurcharge = Math.max(0, weight - 1.0) * 2.0; // $2 per kg over 1kg
        
        return baseRate + weightSurcharge;
    }

    @Override
    public Object trackShipment(String trackingNumber) {
        // In a real implementation, this would call an external tracking service
        // For testing, we'll return some dummy data
        Map<String, Object> trackingInfo = new HashMap<>();
        trackingInfo.put("trackingNumber", trackingNumber);
        trackingInfo.put("status", "In Transit");
        trackingInfo.put("estimatedDelivery", "2023-12-01");
        trackingInfo.put("carrier", "Test Carrier");
        return trackingInfo;
    }
}
