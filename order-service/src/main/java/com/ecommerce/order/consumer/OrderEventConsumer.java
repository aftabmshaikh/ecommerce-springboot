package com.ecommerce.order.consumer;

import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderService orderService;

    @KafkaListener(
            topics = "${kafka.topics.payment-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(@Payload Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            UUID orderId = UUID.fromString((String) event.get("orderId"));
            
            log.info("Received payment event: {} for order: {}", eventType, orderId);
            
            switch (eventType) {
                case "payment-received":
                    orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.PAID
                    );
                    break;
                case "payment-failed":
                    orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CANCELLED
                    );
                    break;
                case "payment-refunded":
                    orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.REFUNDED
                    );
                    break;
                default:
                    log.warn("Unknown payment event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing payment event: {}", event, e);
            // In a production environment, consider implementing a dead-letter queue here
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.inventory-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryEvent(@Payload Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            String orderId = (String) event.get("orderId");
            
            log.info("Received inventory event: {} for order: {}", eventType, orderId);
            
            switch (eventType) {
                case "inventory-reserved":
                    orderService.updateOrderStatus(
                        UUID.fromString(orderId),
                        OrderStatus.PROCESSING
                    );
                    break;
                case "inventory-updated":
                    // Handle inventory update (e.g., update order items if needed)
                    break;
                case "inventory-out-of-stock":
                    orderService.updateOrderStatus(
                        UUID.fromString(orderId),
                        OrderStatus.CANCELLED
                    );
                    break;
                default:
                    log.warn("Unknown inventory event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing inventory event: {}", event, e);
            // In a production environment, consider implementing a dead-letter queue here
        }
    }
}
