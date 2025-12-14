package com.ecommerce.order.controller;

import com.ecommerce.order.dto.tracking.OrderStatusUpdate;
import com.ecommerce.order.dto.tracking.OrderTrackingResponse;
import com.ecommerce.order.service.OrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Tracking API", description = "APIs for order tracking")
public class OrderTrackingController {

    private final OrderTrackingService trackingService;

    @GetMapping("/{orderId}/tracking")
    @Operation(summary = "Get order tracking information")
    public OrderTrackingResponse getOrderTracking(@PathVariable UUID orderId) {
        return trackingService.getOrderTracking(orderId);
    }

    @GetMapping("/{orderId}/timeline")
    @Operation(summary = "Get order status timeline")
    public List<OrderStatusUpdate> getOrderTimeline(@PathVariable UUID orderId) {
        return trackingService.getOrderTimeline(orderId);
    }
}
