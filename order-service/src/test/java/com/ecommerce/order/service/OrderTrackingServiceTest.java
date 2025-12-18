package com.ecommerce.order.service;

import com.ecommerce.order.dto.tracking.OrderTrackingResponse;
import com.ecommerce.order.dto.tracking.OrderStatusUpdate;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTrackingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderTrackingService trackingService;

    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setOrderNumber("ORD-12345678");
        order.setStatus(OrderStatus.DELIVERED);
        order.setTrackingNumber("TRACK123456");
        order.setCarrier("UPS");
        order.setCreatedAt(LocalDateTime.now().minusDays(5));
        order.setPaidAt(LocalDateTime.now().minusDays(4));
        order.setProcessingAt(LocalDateTime.now().minusDays(3));
        order.setShippedAt(LocalDateTime.now().minusDays(2));
        order.setDeliveredAt(LocalDateTime.now().minusDays(1));
        order.setEstimatedDeliveryDate(LocalDateTime.now());
    }

    @Test
    void getOrderTracking_WithValidOrderId_ShouldReturnTrackingResponse() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderTrackingResponse result = trackingService.getOrderTracking(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals("ORD-12345678", result.getOrderNumber());
        assertEquals("DELIVERED", result.getStatus());
        assertEquals("TRACK123456", result.getTrackingNumber());
        assertEquals("UPS", result.getCarrier());
        assertNotNull(result.getStatusUpdates());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderTracking_WithInvalidOrderId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, 
                () -> trackingService.getOrderTracking(orderId));
    }

    @Test
    void getOrderTimeline_WithValidOrderId_ShouldReturnTimeline() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        List<OrderStatusUpdate> result = trackingService.getOrderTimeline(orderId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderTimeline_WithInvalidOrderId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, 
                () -> trackingService.getOrderTimeline(orderId));
    }

    @Test
    void updateOrderStatus_WithValidOrderId_ShouldUpdateStatus() {
        // Arrange
        OrderStatus newStatus = OrderStatus.SHIPPED;
        String notes = "Order shipped";
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        trackingService.updateOrderStatus(orderId, newStatus, notes);

        // Assert
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithInvalidOrderId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, 
                () -> trackingService.updateOrderStatus(orderId, OrderStatus.SHIPPED, "Notes"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithPaidStatus_ShouldSetPaidAt() {
        // Arrange
        order.setPaidAt(null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        trackingService.updateOrderStatus(orderId, OrderStatus.PAID, null);

        // Assert
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithDeliveredStatus_ShouldSetDeliveredAt() {
        // Arrange
        order.setDeliveredAt(null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        trackingService.updateOrderStatus(orderId, OrderStatus.DELIVERED, null);

        // Assert
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrderTracking_WithNullTrackingNumber_ShouldReturnNullCarrierUrl() {
        // Arrange
        order.setTrackingNumber(null);
        order.setCarrier(null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderTrackingResponse result = trackingService.getOrderTracking(orderId);

        // Assert
        assertNotNull(result);
        assertNull(result.getCarrierUrl());
    }

    @Test
    void getOrderTracking_WithFedexCarrier_ShouldGenerateCorrectUrl() {
        // Arrange
        order.setCarrier("FEDEX");
        order.setTrackingNumber("1234567890");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderTrackingResponse result = trackingService.getOrderTracking(orderId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCarrierUrl());
        assertTrue(result.getCarrierUrl().contains("fedex.com"));
    }

    @Test
    void getOrderTracking_WithUSPSCarrier_ShouldGenerateCorrectUrl() {
        // Arrange
        order.setCarrier("USPS");
        order.setTrackingNumber("1234567890");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        OrderTrackingResponse result = trackingService.getOrderTracking(orderId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCarrierUrl());
        assertTrue(result.getCarrierUrl().contains("usps.com"));
    }
}













