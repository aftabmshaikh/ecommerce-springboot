package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.ProductStockUpdateRequest;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.InvalidOrderException;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.OrderStatusHistory;
import com.ecommerce.order.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public OrderResponse createOrder(OrderRequest request) {
        if (request == null || request.getCustomerId() == null) {
            throw new InvalidOrderException("Order request and customer ID cannot be null");
        }
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }
        
        checkProductAvailability(request);
        
        Order order = orderMapper.toEntity(request);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Add initial status history
        order.addStatusHistory(OrderStatus.PENDING, "Order created");
        
        // Calculate total
        BigDecimal total = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);
        
        Order savedOrder = orderRepository.save(order);
        
        updateProductInventory(savedOrder);
        publishOrderEvent(savedOrder, "ORDER_CREATED");
        
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        log.info("Fetching order with id: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        log.info("Fetching order with number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable) {
        log.info("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        log.info("Updating order {} status to {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Add status history
        order.addStatusHistory(status, String.format("Status changed from %s to %s", oldStatus, status));
        
        Order updatedOrder = orderRepository.save(order);
        
        // Publish event
        publishOrderEvent(updatedOrder, "ORDER_STATUS_UPDATED");
        
        return orderMapper.toResponse(updatedOrder);
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public OrderResponse cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (!order.getStatus().isCancellable()) {
            throw new InvalidOrderException("Order cannot be cancelled in its current state: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Add status history
        order.addStatusHistory(OrderStatus.CANCELLED, "Order cancelled by customer");
        
        Order cancelledOrder = orderRepository.save(order);
        
        // Update inventory (add back the stock)
        updateProductInventory(cancelledOrder, true);
        
        // Publish event
        publishOrderEvent(cancelledOrder, "ORDER_CANCELLED");
        
        return orderMapper.toResponse(cancelledOrder);
    }

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    protected void checkProductAvailability(OrderRequest request) {
        log.info("Checking product availability for order");
        try {
            List<Map<String, Object>> itemsToCheck = request.getItems().stream()
                    .map(item -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("productId", item.getProductId().toString());
                        map.put("quantity", item.getQuantity());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            productServiceClient.checkStockAvailability(itemsToCheck);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("One or more products not found", e);
        } catch (FeignException e) {
            log.error("Error checking product availability: {}", e.getMessage());
            throw new RuntimeException("Error checking product availability", e);
        }
    }

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    protected void updateProductInventory(Order order) {
        updateProductInventory(order, false);
    }

    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    protected void updateProductInventory(Order order, boolean isCancellation) {
        log.info("Updating product inventory for order: {}", order.getId());
        try {
            List<Map<String, Object>> items = order.getItems().stream()
                    .map(item -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("productId", item.getProductId().toString());
                        map.put("quantity", isCancellation ? item.getQuantity() : -item.getQuantity());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            productServiceClient.updateInventory(items);
        } catch (FeignException e) {
            log.error("Error updating product inventory: {}", e.getMessage());
            throw new RuntimeException("Error updating product inventory", e);
        }
    }

    protected void publishOrderEvent(Order order, String eventType) {
        log.info("Publishing {} event for order: {}", eventType, order.getId());
        try {
            Map<String, Object> event = Map.of(
                    "eventType", eventType,
                    "orderId", order.getId().toString(),
                    "orderNumber", order.getOrderNumber(),
                    "customerId", order.getCustomerId().toString(),
                    "status", order.getStatus().name(),
                    "timestamp", LocalDateTime.now().toString()
            );
            
            kafkaTemplate.send(ORDER_EVENTS_TOPIC, order.getOrderNumber(), event);
        } catch (Exception e) {
            log.error("Error publishing order event: {}", e.getMessage(), e);
            // Don't throw exception as it's not critical for the main order flow
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
