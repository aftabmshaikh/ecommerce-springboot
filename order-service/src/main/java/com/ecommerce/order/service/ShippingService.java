package com.ecommerce.order.service;

import com.ecommerce.order.dto.shipping.ShippingOption;
import com.ecommerce.order.service.shipping.ShippingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingProvider shippingProvider;
    
    @Value("${app.currency:USD}")
    private String defaultCurrency;

    public List<ShippingOption> getAvailableShippingOptions(String country, String postalCode) {
        log.info("Fetching available shipping options for country: {}, postal code: {}", country, postalCode);
        List<ShippingOption> options = shippingProvider.getAvailableShippingOptions(country, postalCode);
        options.forEach(option -> option.setCurrency(defaultCurrency));
        return options;
    }

    public List<ShippingOption> calculateShippingOptions(String country, String postalCode, Double weight, Double value) {
        log.info("Calculating shipping options for country: {}, postal code: {}, weight: {}, value: {}", 
                country, postalCode, weight, value);
        
        // If value is over $100, return free shipping option
        if (value != null && value >= 100.0) {
            return List.of(ShippingOption.builder()
                .id("free")
                .name("Free Shipping")
                .estimatedDelivery("3-7 business days")
                .price(BigDecimal.ZERO)
                .currency(defaultCurrency)
                .build());
        }
        
        // Otherwise, use the shipping provider to calculate options
        List<ShippingOption> options = shippingProvider.getAvailableShippingOptions(country, postalCode);
        
        // If weight is provided, calculate the actual shipping cost
        if (weight != null) {
            for (ShippingOption option : options) {
                double cost = shippingProvider.calculateShippingCost(country, postalCode, weight, value != null ? value : 0.0);
                option.setPrice(BigDecimal.valueOf(cost));
                option.setCurrency(defaultCurrency);
            }
        }
        
        return options;
    }

    public Object trackShipment(String trackingNumber) {
        log.info("Tracking shipment with tracking number: {}", trackingNumber);
        return shippingProvider.trackShipment(trackingNumber);
    }
}
