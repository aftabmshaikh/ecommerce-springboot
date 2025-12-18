package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.orderitem.OrderItemRequest;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.InvalidOrderException;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private UUID customerId;
    private UUID orderId;
    private UUID productId;
    private Order order;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(productId.toString());
        itemRequest.setQuantity(2);
        itemRequest.setUnitPrice(BigDecimal.valueOf(29.99));
        itemRequest.setTotalPrice(BigDecimal.valueOf(59.98));
        itemRequest.setProductName("Test Product");

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

        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setProductId(productId);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(29.99));

        order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setOrderNumber("ORD-12345678");
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(BigDecimal.valueOf(59.98));
        order.setItems(List.of(orderItem));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createOrder_WithValidRequest_ShouldReturnOrderResponse() {
        // Arrange
        when(orderMapper.toEntity(any(OrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(createOrderResponse());
        when(productServiceClient.checkStockAvailability(anyList())).thenReturn(ResponseEntity.ok(new HashMap<>()));
        when(productServiceClient.updateInventory(anyList())).thenReturn(ResponseEntity.ok().build());
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        // Act
        OrderResponse result = orderService.createOrder(orderRequest);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productServiceClient, times(1)).checkStockAvailability(anyList());
        verify(productServiceClient, times(1)).updateInventory(anyList());
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void createOrder_WithNullRequest_ShouldThrowException() {
        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(null));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithNullCustomerId_ShouldThrowException() {
        // Arrange
        orderRequest.setCustomerId(null);

        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(orderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithEmptyItems_ShouldThrowException() {
        // Arrange
        orderRequest.setItems(Collections.emptyList());

        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(orderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        FeignException notFound = mock(FeignException.NotFound.class);
        doThrow(notFound).when(productServiceClient).checkStockAvailability(anyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_WithValidId_ShouldReturnOrderResponse() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(createOrderResponse());

        // Act
        OrderResponse result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void getOrderByNumber_WithValidNumber_ShouldReturnOrderResponse() {
        // Arrange
        String orderNumber = "ORD-12345678";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(createOrderResponse());

        // Act
        OrderResponse result = orderService.getOrderByNumber(orderNumber);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrderByNumber_WithInvalidNumber_ShouldThrowException() {
        // Arrange
        String orderNumber = "ORD-INVALID";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderByNumber(orderNumber));
    }

    @Test
    void getCustomerOrders_WithValidCustomerId_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findByCustomerId(customerId, pageable)).thenReturn(orderPage);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(createOrderResponse());

        // Act
        Page<OrderResponse> result = orderService.getCustomerOrders(customerId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(orderRepository, times(1)).findByCustomerId(customerId, pageable);
    }

    @Test
    void updateOrderStatus_WithValidId_ShouldUpdateStatus() {
        // Arrange
        OrderStatus newStatus = OrderStatus.PROCESSING;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(createOrderResponse());
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        // Act
        OrderResponse result = orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void updateOrderStatus_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> orderService.updateOrderStatus(orderId, OrderStatus.PROCESSING));
    }

    @Test
    void cancelOrder_WithValidId_ShouldCancelOrder() {
        // Arrange
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(createOrderResponse());
        when(productServiceClient.updateInventory(anyList())).thenReturn(ResponseEntity.ok().build());
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        // Act
        OrderResponse result = orderService.cancelOrder(orderId);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productServiceClient, times(1)).updateInventory(anyList());
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void cancelOrder_WithNonCancellableStatus_ShouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> orderService.cancelOrder(orderId));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrder_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(orderId));
    }

    private OrderResponse createOrderResponse() {
        OrderResponse response = new OrderResponse();
        response.setId(orderId);
        response.setOrderNumber("ORD-12345678");
        response.setCustomerId(customerId);
        response.setStatus(OrderStatus.PENDING.name());
        response.setTotal(BigDecimal.valueOf(59.98));
        return response;
    }
}

