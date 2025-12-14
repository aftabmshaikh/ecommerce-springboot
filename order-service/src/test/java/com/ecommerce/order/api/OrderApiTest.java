package com.ecommerce.order.api;

import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.orderitem.OrderItemRequest;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(OrderControllerTestConfig.class)
@WebAppConfiguration
@AutoConfigureMockMvc(addFilters = false)
public class OrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Mock the service response
        String orderId = UUID.randomUUID().toString();
        UUID customerId = UUID.randomUUID();
        OrderRequest request = createTestOrderRequest(customerId.toString());
        OrderResponse response = createTestOrderResponse(orderId, customerId);

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        // Execute the request
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrder_WithValidId_ShouldReturnOrder() throws Exception {
        // Mock the service response
        String orderId = UUID.randomUUID().toString();
        UUID orderUuid = UUID.fromString(orderId);
        
        OrderResponse mockResponse = OrderResponse.builder()
                .id(UUID.fromString(orderId))
                .orderNumber("ORD-123456")
                .customerId(UUID.randomUUID())
                .status(OrderStatus.COMPLETED.name())
                .shippingAddress("123 Test St, Test City")
                .billingAddress("123 Test St, Test City")
                .subtotal(BigDecimal.valueOf(99.99))
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .total(BigDecimal.valueOf(99.99))
                .build();
        
        when(orderService.getOrderById(orderUuid)).thenReturn(mockResponse);

        // Perform the request and verify the response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value(OrderStatus.COMPLETED.name()));
    }

    @Test
    void updateOrderStatus_WithValidStatus_ShouldUpdateOrder() throws Exception {
        // Create a mock response
        String orderId = UUID.randomUUID().toString();
        UUID orderUuid = UUID.fromString(orderId);
        
        OrderResponse mockResponse = OrderResponse.builder()
                .id(UUID.fromString(orderId))
                .orderNumber("ORD-123456")
                .customerId(UUID.randomUUID())
                .status(OrderStatus.SHIPPED.name())
                .shippingAddress("123 Test St, Test City")
                .billingAddress("123 Test St, Test City")
                .subtotal(BigDecimal.valueOf(99.99))
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .total(BigDecimal.valueOf(99.99))
                .build();
        
        // Mock the service response
        when(orderService.updateOrderStatus(any(UUID.class), eq(OrderStatus.SHIPPED))).thenReturn(mockResponse);

        // Perform the request with status as a request parameter
        mockMvc.perform(MockMvcRequestBuilders.put("/api/orders/" + orderId + "/status")
                .param("status", "SHIPPED")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value(OrderStatus.SHIPPED.name()));
    }

    @Test
    void getOrder_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Mock the service to throw a ResourceNotFoundException
        UUID invalidId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        when(orderService.getOrderById(invalidId))
            .thenThrow(new ResourceNotFoundException("Order not found with id: " + invalidId));

        // Perform the request and verify the response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/orders/550e8400-e29b-41d4-a716-446655440000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerOrders_ShouldReturnPaginatedResults() throws Exception {
        // Mock the service response
        String orderId = UUID.randomUUID().toString();
        UUID customerId = UUID.randomUUID();
        OrderResponse orderResponse = createTestOrderResponse(orderId, customerId);
        Page<OrderResponse> page = new PageImpl<>(List.of(orderResponse));

        when(orderService.getCustomerOrders(any(UUID.class), any(Pageable.class))).thenReturn(page);

        // Execute the request
        mockMvc.perform(get("/api/orders/customer/{customerId}", customerId)
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderId))
                .andExpect(jsonPath("$.content[0].customerId").value(customerId.toString()));
    }
    
    @Test
    void cancelOrder_ShouldReturnSuccess() throws Exception {
        // Create a mock response
        String orderId = UUID.randomUUID().toString();
        UUID orderUuid = UUID.fromString(orderId);
        
        OrderResponse mockResponse = OrderResponse.builder()
                .id(UUID.fromString(orderId))
                .orderNumber("ORD-123456")
                .customerId(UUID.randomUUID())
                .status(OrderStatus.CANCELLED.name())
                .shippingAddress("123 Test St, Test City")
                .billingAddress("123 Test St, Test City")
                .subtotal(BigDecimal.valueOf(99.99))
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .total(BigDecimal.valueOf(99.99))
                .build();
        
        // Mock the service response
        when(orderService.cancelOrder(orderUuid)).thenReturn(mockResponse);
        
        // Perform the request and verify the response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/" + orderId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.CANCELLED.name()));
    }
    
    private OrderRequest createTestOrderRequest(String customerId) {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(customerId);
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("1234567890");
        request.setShippingAddress("123 Test St, Test City");
        request.setBillingAddress("123 Test St, Test City");
        request.setSubtotal(BigDecimal.valueOf(99.99));
        request.setTax(BigDecimal.ZERO);
        request.setShippingFee(BigDecimal.ZERO);
        request.setTotal(BigDecimal.valueOf(99.99));
        
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(UUID.randomUUID().toString());
        item.setProductName("Test Product");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(99.99));
        item.setTotalPrice(BigDecimal.valueOf(99.99));
        
        request.setItems(List.of(item));
        return request;
    }
    
    private OrderResponse createTestOrderResponse(String orderId, UUID customerId) {
        return OrderResponse.builder()
                .id(UUID.fromString(orderId))
                .orderNumber("ORD-123456")
                .customerId(customerId)
                .customerEmail("test@example.com")
                .customerPhone("1234567890")
                .status(OrderStatus.PENDING.name())
                .shippingAddress("123 Test St, Test City")
                .billingAddress("123 Test St, Test City")
                .subtotal(BigDecimal.valueOf(99.99))
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .total(BigDecimal.valueOf(99.99))
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }
}
