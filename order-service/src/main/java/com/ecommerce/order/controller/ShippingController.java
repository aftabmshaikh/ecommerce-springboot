package com.ecommerce.order.controller;

import com.ecommerce.order.dto.shipping.ShippingOption;
import com.ecommerce.order.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping API", description = "APIs for shipping operations")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/options")
    @Operation(summary = "Get available shipping options")
    public ResponseEntity<List<ShippingOption>> getShippingOptions(
            @RequestParam String country,
            @RequestParam(required = false) String postalCode) {
        List<ShippingOption> options = shippingService.getAvailableShippingOptions(country, postalCode);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate shipping costs")
    public ResponseEntity<List<ShippingOption>> calculateShipping(
            @RequestParam String country,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) Double weight,
            @RequestParam(required = false) Double value) {
        List<ShippingOption> options = shippingService.calculateShippingOptions(country, postalCode, weight, value);
        return ResponseEntity.ok(options);
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track a shipment")
    public ResponseEntity<?> trackShipment(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(shippingService.trackShipment(trackingNumber));
    }
}
