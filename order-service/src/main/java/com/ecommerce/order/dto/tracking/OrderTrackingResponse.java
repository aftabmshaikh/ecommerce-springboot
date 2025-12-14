package com.ecommerce.order.dto.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Objects;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private UUID orderId;
    private String orderNumber;
    private String status;
    private String trackingNumber;
    private String carrier;
    private String carrierUrl;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private List<OrderStatusUpdate> statusUpdates;

    // Removed manual getters, setters, and builder as they are now handled by Lombok
}
