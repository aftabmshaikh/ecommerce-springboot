package com.ecommerce.order.service;

import com.ecommerce.order.dto.returns.ReturnRequest;
import com.ecommerce.order.dto.returns.ReturnResponse;
import com.ecommerce.order.exception.InvalidReturnRequestException;
import com.ecommerce.order.exception.OrderItemNotFoundException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.*;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OrderReturnRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderReturnServiceTest {

    @Mock
    private OrderReturnRepository returnRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderReturnService returnService;

    private UUID orderId;
    private UUID customerId;
    private UUID orderItemId;
    private Order order;
    private OrderItem orderItem;
    private ReturnRequest returnRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        orderItemId = UUID.randomUUID();

        orderItem = new OrderItem();
        orderItem.setId(orderItemId);
        orderItem.setProductId(UUID.randomUUID());
        orderItem.setProductName("Test Product");
        orderItem.setProductSku("SKU-001");
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.valueOf(29.99));
        orderItem.setReturnedQuantity(0);

        order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setOrderNumber("ORD-12345678");
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now().minusDays(5));
        order.setItems(List.of(orderItem));
        order.setShippingAddress("123 Test St");
        order.setBillingAddress("123 Test St");
        order.setCustomerEmail("test@example.com");
        order.setCustomerPhone("1234567890");
        order.setSubtotal(BigDecimal.valueOf(59.98));
        order.setTax(BigDecimal.valueOf(5.99));
        order.setShippingFee(BigDecimal.valueOf(10.00));
        order.setTotal(BigDecimal.valueOf(75.97));

        ReturnRequest.ReturnItemRequest itemRequest = new ReturnRequest.ReturnItemRequest();
        itemRequest.setOrderItemId(orderItemId);
        itemRequest.setQuantity(1);
        itemRequest.setReason("Defective product");

        returnRequest = new ReturnRequest();
        returnRequest.setReason("Product defect");
        returnRequest.setComments("Item arrived damaged");
        returnRequest.setItems(List.of(itemRequest));
    }

    @Test
    void initiateReturn_WithValidRequest_ShouldReturnReturnResponse() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItem));
        when(returnRepository.save(any(OrderReturn.class))).thenAnswer(invocation -> {
            OrderReturn returnOrder = invocation.getArgument(0);
            returnOrder.setId(UUID.randomUUID());
            return returnOrder;
        });
        doNothing().when(notificationService).sendReturnRequestReceivedNotification(any(OrderReturn.class));

        // Act
        ReturnResponse result = returnService.initiateReturn(orderId, returnRequest);

        // Assert
        assertNotNull(result);
        assertEquals(ReturnStatus.REQUESTED.name(), result.getStatus());
        verify(returnRepository, times(1)).save(any(OrderReturn.class));
        verify(notificationService, times(1)).sendReturnRequestReceivedNotification(any(OrderReturn.class));
    }

    @Test
    void initiateReturn_WithNonExistentOrder_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, 
                () -> returnService.initiateReturn(orderId, returnRequest));
        verify(returnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    void initiateReturn_WithCancelledOrder_ShouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidReturnRequestException.class, 
                () -> returnService.initiateReturn(orderId, returnRequest));
        verify(returnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    void initiateReturn_WithAlreadyReturnedOrder_ShouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.RETURNED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidReturnRequestException.class, 
                () -> returnService.initiateReturn(orderId, returnRequest));
        verify(returnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    void initiateReturn_WithExpiredReturnWindow_ShouldThrowException() {
        // Arrange
        order.setDeliveredAt(LocalDateTime.now().minusDays(31));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidReturnRequestException.class, 
                () -> returnService.initiateReturn(orderId, returnRequest));
        verify(returnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    void initiateReturn_WithInvalidOrderItem_ShouldThrowException() {
        // Arrange
        returnRequest.getItems().get(0).setOrderItemId(UUID.randomUUID());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidReturnRequestException.class, 
                () -> returnService.initiateReturn(orderId, returnRequest));
        verify(returnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    void initiateReturn_WithZeroQuantity_ShouldThrowException() {
        // Arrange
        returnRequest.getItems().get(0).setQuantity(0);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidReturnRequestException.class, 
                () -> returnService.initiateReturn(orderId, returnRequest));
        verify(returnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    void initiateReturn_WithNonExistentOrderItem_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, 
                () -> returnService.initiateReturn(orderId, returnRequest));
        verify(returnRepository, never()).save(any(OrderReturn.class));
    }

    @Test
    void getReturnDetails_WithValidId_ShouldReturnReturnResponse() {
        // Arrange
        UUID returnId = UUID.randomUUID();
        OrderReturn orderReturn = createOrderReturn(returnId);
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(orderReturn));

        // Act
        ReturnResponse result = returnService.getReturnDetails(returnId);

        // Assert
        assertNotNull(result);
        assertEquals(returnId, result.getId());
        verify(returnRepository, times(1)).findById(returnId);
    }

    @Test
    void getReturnDetails_WithInvalidId_ShouldThrowException() {
        // Arrange
        UUID returnId = UUID.randomUUID();
        when(returnRepository.findById(returnId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, 
                () -> returnService.getReturnDetails(returnId));
    }

    private OrderReturn createOrderReturn(UUID returnId) {
        OrderReturn orderReturn = new OrderReturn();
        orderReturn.setId(returnId);
        orderReturn.setReturnNumber("RTN-123456");
        orderReturn.setOrderId(orderId);
        orderReturn.setOrder(order);
        orderReturn.setCustomerId(customerId);
        orderReturn.setStatus(ReturnStatus.REQUESTED);
        orderReturn.setReturnReason("Product defect");
        orderReturn.setRefundAmount(BigDecimal.valueOf(29.99));
        orderReturn.setRequestedDate(LocalDateTime.now());

        OrderReturnItem returnItem = new OrderReturnItem();
        returnItem.setId(UUID.randomUUID());
        returnItem.setOrderItemId(orderItemId);
        returnItem.setProductId(orderItem.getProductId());
        returnItem.setQuantity(1);
        returnItem.setRefundAmount(BigDecimal.valueOf(29.99));
        returnItem.setOrderReturn(orderReturn);

        orderReturn.setItems(List.of(returnItem));
        return orderReturn;
    }
}

