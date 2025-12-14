package com.ecommerce.order.service;

import com.ecommerce.order.dto.tracking.OrderTrackingResponse;
import com.ecommerce.order.dto.tracking.OrderStatusUpdate;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.OrderStatusHistory;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderTrackingService {
    private static final Logger log = LoggerFactory.getLogger(OrderTrackingService.class);

    private final OrderRepository orderRepository;

    public OrderTrackingResponse getOrderTracking(UUID orderId) {
        log.debug("Fetching tracking information for order ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new OrderNotFoundException("Order not found with ID: " + orderId);
                });

        return OrderTrackingResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .trackingNumber(order.getTrackingNumber())
                .carrier(order.getCarrier())
                .carrierUrl(generateTrackingUrl(order.getCarrier(), order.getTrackingNumber()))
                .estimatedDelivery(order.getEstimatedDeliveryDate())
                .actualDelivery(order.getDeliveredDate())
                .statusUpdates(generateStatusUpdates(order))
                .build();
    }

    public List<OrderStatusUpdate> getOrderTimeline(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        return generateStatusUpdates(order);
    }

    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus, String notes) {
        log.info("Updating status for order ID: {} to status: {}", orderId, newStatus);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new OrderNotFoundException("Order not found with ID: " + orderId);
                });

        // Initialize status history if null
        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new ArrayList<>());
        }

        // Create and add status history entry
        String statusMessage = notes != null ? notes : "Status updated to " + newStatus;
        order.getStatusHistory().add(OrderStatusHistory.create(newStatus, statusMessage));

        // Update order status and timestamps based on the new status
        order.setStatus(newStatus);
        LocalDateTime now = LocalDateTime.now();
        order.setUpdatedAt(now);

        // Update relevant timestamps based on status
        if (newStatus == OrderStatus.PAID) {
            order.setPaidAt(now);
        } else if (newStatus == OrderStatus.PROCESSING) {
            order.setProcessingAt(now);
        } else if (newStatus == OrderStatus.SHIPPED) {
            order.setShippedAt(now);
        } else if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
            order.setOutForDeliveryAt(now);
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(now);
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelledAt(now);
        } else if (newStatus == OrderStatus.RETURN_REQUESTED) {
            order.setReturnRequestedAt(now);
        } else if (newStatus == OrderStatus.RETURNED) {
            order.setReturnedAt(now);
        }
        
        // Set estimated delivery date if not set and status is DELIVERED
        if (newStatus == OrderStatus.DELIVERED && order.getEstimatedDeliveryDate() == null) {
            order.setEstimatedDeliveryDate(now.plusDays(2)); // Default 2 days for delivery
        }

        orderRepository.save(order);
        log.info("Successfully updated status for order ID: {} to status: {}", orderId, newStatus);
    }

    private List<OrderStatusUpdate> generateStatusUpdates(Order order) {
        List<OrderStatusUpdate> updates = new ArrayList<>();
        
        // Add order created status
        updates.add(createStatusUpdate("PLACED", "Order was placed", order.getCreatedAt(), 
            OrderStatus.PLACED == order.getStatus()));
                
        // Add other status updates based on order state
        if (order.getPaidAt() != null) {
            updates.add(createStatusUpdate("PAID", "Payment received", order.getPaidAt(), 
                OrderStatus.PAID == order.getStatus()));
        }
        
        if (order.getProcessingAt() != null) {
            updates.add(createStatusUpdate("PROCESSING", "Order is being processed", order.getProcessingAt(), 
                OrderStatus.PROCESSING.name().equals(order.getStatus())));
        }
        
        if (order.getShippedAt() != null) {
            updates.add(createStatusUpdate("SHIPPED", "Order has been shipped", order.getShippedAt(), 
                OrderStatus.SHIPPED.name().equals(order.getStatus())));
        }
        
        if (order.getOutForDeliveryAt() != null) {
            updates.add(createStatusUpdate(
                "OUT_FOR_DELIVERY", 
                "Order is out for delivery", 
                order.getOutForDeliveryAt(),
                OrderStatus.OUT_FOR_DELIVERY.name().equals(order.getStatus())
            ));
        }
        
        if (order.getDeliveredDate() != null) {
            updates.add(createStatusUpdate(
                "DELIVERED", 
                "Order has been delivered", 
                order.getDeliveredDate(),
                OrderStatus.DELIVERED.name().equals(order.getStatus())
            ));
        }

        // Add delivered status
        if (order.getDeliveredAt() != null) {
            updates.add(createStatusUpdate(
                    "DELIVERED",
                    "Delivered",
                    order.getDeliveredAt(),
                    OrderStatus.DELIVERED.name().equals(order.getStatus())
            ));
        }

        // Add cancelled status if applicable
        if (order.getCancelledAt() != null) {
            updates.add(createStatusUpdate(
                "CANCELLED", 
                "Order was cancelled", 
                order.getCancelledAt(),
                OrderStatus.CANCELLED.name().equals(order.getStatus())
            ));
        }

        // Add return requested status if applicable
        if (order.getReturnRequestedAt() != null) {
            updates.add(createStatusUpdate(
                "RETURN_REQUESTED", 
                "Return has been requested", 
                order.getReturnRequestedAt(),
                OrderStatus.RETURN_REQUESTED.name().equals(order.getStatus())
            ));
        }

        // Add returned status if applicable
        if (OrderStatus.RETURNED.name().equals(order.getStatus()) && order.getReturnedAt() != null) {
            updates.add(createStatusUpdate(
                    "RETURNED",
                    "Order returned",
                    order.getReturnedAt(),
                    true
            ));
        }

        // Sort updates by timestamp
        updates.sort((u1, u2) -> u2.getTimestamp().compareTo(u1.getTimestamp()));

        return updates;
    }

    private OrderStatusUpdate createStatusUpdate(String status, String description, 
                                               LocalDateTime timestamp, boolean isCurrent) {
        return createStatusUpdate(status, description, timestamp, isCurrent, null);
    }

    private OrderStatusUpdate createStatusUpdate(String status, String description, 
                                               LocalDateTime timestamp, boolean isCurrent, 
                                               String details) {
        return new OrderStatusUpdate(status, description, details, timestamp, isCurrent);
    }

    private String generateTrackingUrl(String carrier, String trackingNumber) {
        if (carrier == null || trackingNumber == null) {
            return null;
        }
        
        return switch (carrier.toLowerCase()) {
            case "ups" -> String.format("https://www.ups.com/track?tracknum=%s", trackingNumber);
            case "fedex" -> String.format("https://www.fedex.com/fedextrack/?tracknumbers=%s", trackingNumber);
            case "usps" -> String.format("https://tools.usps.com/go/TrackConfirmAction?tLabels=%s", trackingNumber);
            case "dhl" -> String.format("https://www.dhl.com/us-en/home/tracking/tracking-express.html?submit=1&tracking-id=%s", trackingNumber);
            default -> null;
        };
    }
}
