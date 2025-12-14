package com.ecommerce.order.service.shipping;

import com.ecommerce.order.dto.shipping.ShippingOption;

import java.util.List;

public interface ShippingProvider {
    List<ShippingOption> getAvailableShippingOptions(String country, String postalCode);
    double calculateShippingCost(String country, String postalCode, double weight, double orderValue);
    Object trackShipment(String trackingNumber);
}
