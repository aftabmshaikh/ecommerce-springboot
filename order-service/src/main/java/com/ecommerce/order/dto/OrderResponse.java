package com.ecommerce.order.dto;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatusHistory;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for {@link com.ecommerce.order.model.Order}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String customerEmail;
    private String customerPhone;
    private String status;
    private String shippingAddress;
    private String billingAddress;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    
    public List<OrderStatusHistoryResponse> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<OrderStatusHistoryResponse> statusHistory) { this.statusHistory = statusHistory; }
    
    public static OrderResponseBuilder builder() {
        return new OrderResponseBuilder();
    }
    
    public static class OrderResponseBuilder {
        private final OrderResponse response = new OrderResponse();
        
        public OrderResponseBuilder id(UUID id) { response.id = id; return this; }
        public OrderResponseBuilder orderNumber(String orderNumber) { response.orderNumber = orderNumber; return this; }
        public OrderResponseBuilder customerId(UUID customerId) { response.customerId = customerId; return this; }
        public OrderResponseBuilder customerEmail(String customerEmail) { response.customerEmail = customerEmail; return this; }
        public OrderResponseBuilder customerPhone(String customerPhone) { response.customerPhone = customerPhone; return this; }
        public OrderResponseBuilder status(String status) { response.status = status; return this; }
        public OrderResponseBuilder shippingAddress(String shippingAddress) { response.shippingAddress = shippingAddress; return this; }
        public OrderResponseBuilder billingAddress(String billingAddress) { response.billingAddress = billingAddress; return this; }
        public OrderResponseBuilder subtotal(BigDecimal subtotal) { response.subtotal = subtotal; return this; }
        public OrderResponseBuilder tax(BigDecimal tax) { response.tax = tax; return this; }
        public OrderResponseBuilder shippingFee(BigDecimal shippingFee) { response.shippingFee = shippingFee; return this; }
        public OrderResponseBuilder total(BigDecimal total) { response.total = total; return this; }
        public OrderResponseBuilder notes(String notes) { response.notes = notes; return this; }
        public OrderResponseBuilder createdAt(LocalDateTime createdAt) { response.createdAt = createdAt; return this; }
        public OrderResponseBuilder updatedAt(LocalDateTime updatedAt) { response.updatedAt = updatedAt; return this; }
        public OrderResponseBuilder items(List<OrderItemResponse> items) { response.items = items; return this; }
        public OrderResponseBuilder statusHistory(List<OrderStatusHistoryResponse> statusHistory) { response.statusHistory = statusHistory; return this; }
        
        public OrderResponse build() {
            return response;
        }
    }

    public static class OrderItemResponse {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        
        public static class Builder {
            private final OrderItemResponse item = new OrderItemResponse();
            
            public Builder id(UUID id) { item.id = id; return this; }
            public Builder productId(UUID productId) { item.productId = productId; return this; }
            public Builder productName(String productName) { item.productName = productName; return this; }
            public Builder productSku(String productSku) { item.productSku = productSku; return this; }
            public Builder quantity(Integer quantity) { item.quantity = quantity; return this; }
            public Builder unitPrice(BigDecimal unitPrice) { item.unitPrice = unitPrice; return this; }
            public Builder totalPrice(BigDecimal totalPrice) { item.totalPrice = totalPrice; return this; }
            public Builder notes(String notes) { item.notes = notes; return this; }
            public Builder createdAt(LocalDateTime createdAt) { item.createdAt = createdAt; return this; }
            public Builder updatedAt(LocalDateTime updatedAt) { item.updatedAt = updatedAt; return this; }
            
            public OrderItemResponse build() {
                return item;
            }
        }
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static OrderItemResponse fromEntity(OrderItem item) {
            if (item == null) {
                return null;
            }
            
            return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .notes(item.getNotes())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
        }
    }

    public static class OrderStatusHistoryResponse {
        private String status;
        private String message;
        private LocalDateTime statusDate;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getStatusDate() { return statusDate; }
        public void setStatusDate(LocalDateTime statusDate) { this.statusDate = statusDate; }

        public static OrderStatusHistoryResponseBuilder builder() {
            return new OrderStatusHistoryResponseBuilder();
        }

        public static class OrderStatusHistoryResponseBuilder {
            private final OrderStatusHistoryResponse response = new OrderStatusHistoryResponse();
            
            public OrderStatusHistoryResponseBuilder status(String status) { response.status = status; return this; }
            public OrderStatusHistoryResponseBuilder message(String message) { response.message = message; return this; }
            public OrderStatusHistoryResponseBuilder statusDate(LocalDateTime statusDate) { response.statusDate = statusDate; return this; }
            
            public OrderStatusHistoryResponse build() {
                return response;
            }
        }

        public static OrderStatusHistoryResponse fromEntity(OrderStatusHistory history) {
            if (history == null) {
                return null;
            }
            return OrderStatusHistoryResponse.builder()
                .status(history.getStatus() != null ? history.getStatus().name() : null)
                .message(history.getMessage())
                .statusDate(history.getStatusDate())
                .build();
        }
    }

    // Factory method using builder pattern
    public static OrderResponse fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomerId())
            .customerEmail(order.getCustomerEmail())
            .customerPhone(order.getCustomerPhone())
            .status(order.getStatus() != null ? order.getStatus().name() : null)
            .shippingAddress(order.getShippingAddress())
            .billingAddress(order.getBillingAddress())
            .subtotal(order.getSubtotal())
            .tax(order.getTax())
            .shippingFee(order.getShippingFee())
            .total(order.getTotal())
            .notes(order.getNotes())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(order.getItems() != null ? 
                order.getItems().stream()
                    .map(OrderItemResponse::fromEntity)
                    .collect(Collectors.toList()) : null)
            .statusHistory(order.getStatusHistory() != null && !order.getStatusHistory().isEmpty() ?
                order.getStatusHistory().stream()
                    .map(history -> OrderStatusHistoryResponse.builder()
                        .status(history.getStatus() != null ? history.getStatus().name() : null)
                        .message(history.getMessage())
                        .statusDate(history.getStatusDate())
                        .build())
                    .collect(Collectors.toList()) : null)
            .build();
    }

}
