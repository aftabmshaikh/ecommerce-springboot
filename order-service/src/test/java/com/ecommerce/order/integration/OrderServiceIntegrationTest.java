package com.ecommerce.order.integration;

import com.ecommerce.order.base.BaseIntegrationTest;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.orderitem.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.NotificationService;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderService orderService;

    @MockBean
    private ProductServiceClient productServiceClient;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    private UUID customerId;
    private UUID productId;
    private OrderRequest orderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Setup test data
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(productId.toString());
        itemRequest.setQuantity(2);
        itemRequest.setUnitPrice(BigDecimal.valueOf(29.99));
        itemRequest.setTotalPrice(BigDecimal.valueOf(59.98));
        itemRequest.setProductName("Test Product");
        itemRequest.setSku("TEST-SKU-001");

        orderRequest = new OrderRequest();
        orderRequest.setCustomerId(customerId.toString());
        orderRequest.setCustomerEmail("test@example.com");
        orderRequest.setCustomerPhone("1234567890");
        orderRequest.setShippingAddress("123 Test St");
        orderRequest.setBillingAddress("123 Test St");
        orderRequest.setSubtotal(BigDecimal.valueOf(59.98));
        orderRequest.setTax(BigDecimal.valueOf(5.99));
        orderRequest.setShippingFee(BigDecimal.valueOf(10.00));
        orderRequest.setTotal(BigDecimal.valueOf(75.97));
        orderRequest.setItems(List.of(itemRequest));

        // Mock other service calls
        when(productServiceClient.checkStockAvailability(anyList()))
            .thenReturn(ResponseEntity.ok(new HashMap<>()));
        when(productServiceClient.updateInventory(anyList()))
            .thenReturn(ResponseEntity.ok().build());
        doNothing().when(notificationService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void createOrder_WithValidRequest_ShouldPersistOrder() {
        // Act
        OrderResponse result = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(result, "Order response should not be null");
        assertNotNull(result.getId(), "Order ID should not be null");
        assertEquals(OrderStatus.PENDING.name(), result.getStatus(), "Order status should be PENDING");
        assertEquals(customerId, result.getCustomerId(), "Customer ID should match");

        // Verify interactions
        verify(productServiceClient).checkStockAvailability(anyList());
        
        // Verify order details
        assertEquals(OrderStatus.PENDING.name(), result.getStatus(), "Order status should be PENDING");
        assertTrue(result.getTotal().compareTo(BigDecimal.ZERO) > 0, "Order total should be greater than zero");
        assertEquals(customerId, result.getCustomerId(), "Customer ID should match");
        assertEquals(1, result.getItems().size(), "Should have one order item");
    }

    @Test
    void getOrderById_WithExistingOrder_ShouldReturnOrder() {
        // Arrange - Create an order first
        Order order = createTestOrder();
        Order savedOrder = orderRepository.save(order);

        // Act
        OrderResponse result = orderService.getOrderById(savedOrder.getId());

        // Assert
        assertNotNull(result);
        assertEquals(savedOrder.getId(), result.getId());
        assertEquals(savedOrder.getOrderNumber(), result.getOrderNumber());
    }

    @Test
    void updateOrderStatus_WithExistingOrder_ShouldUpdateStatus() {
        // Arrange
        Order order = createTestOrder();
        Order savedOrder = orderRepository.save(order);

        // Act
        OrderResponse result = orderService.updateOrderStatus(savedOrder.getId(), OrderStatus.PROCESSING);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING.name(), result.getStatus());

        // Verify status is updated in database
        Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.PROCESSING, updatedOrder.getStatus());
    }

    @Test
    void cancelOrder_WithPendingOrder_ShouldCancelOrder() {
        // Arrange
        Order order = createTestOrder();
        order.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);

        // Act
        OrderResponse result = orderService.cancelOrder(savedOrder.getId());

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED.name(), result.getStatus());

        // Verify order is cancelled in database
        Order cancelledOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        assertNotNull(cancelledOrder.getCancelledAt());
    }

    @Test
    void getCustomerOrders_WithExistingOrders_ShouldReturnOrders() {
        // Arrange
        Order order1 = createTestOrder();
        Order order2 = createTestOrder();
        orderRepository.save(order1);
        orderRepository.save(order2);

        // Act
        var result = orderService.getCustomerOrders(customerId, 
                org.springframework.data.domain.PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
    }

    private Order createTestOrder() {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("123 Test St");
        order.setBillingAddress("123 Test St");
        order.setCustomerEmail("test@example.com");
        order.setCustomerPhone("1234567890");
        order.setSubtotal(BigDecimal.valueOf(59.98));
        order.setTax(BigDecimal.valueOf(5.99));
        order.setShippingFee(BigDecimal.valueOf(10.00));
        order.setTotal(BigDecimal.valueOf(75.97));
        return order;
    }
}
