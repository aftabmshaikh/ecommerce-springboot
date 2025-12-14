package com.ecommerce.order.dto.returns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReturnResponse {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ReturnResponse response = new ReturnResponse();
        
        public Builder id(UUID id) { response.setId(id); return this; }
        public Builder returnNumber(String returnNumber) { response.setReturnNumber(returnNumber); return this; }
        public Builder orderId(UUID orderId) { response.setOrderId(orderId); return this; }
        public Builder orderNumber(String orderNumber) { response.setOrderNumber(orderNumber); return this; }
        public Builder status(String status) { response.setStatus(status); return this; }
        public Builder returnReason(String returnReason) { response.setReturnReason(returnReason); return this; }
        public Builder comments(String comments) { response.setComments(comments); return this; }
        public Builder refundAmount(BigDecimal refundAmount) { response.setRefundAmount(refundAmount); return this; }
        public Builder refundMethod(String refundMethod) { response.setRefundMethod(refundMethod); return this; }
        public Builder requestedDate(LocalDateTime requestedDate) { response.setRequestedDate(requestedDate); return this; }
        public Builder processedDate(LocalDateTime processedDate) { response.setProcessedDate(processedDate); return this; }
        public Builder items(List<ReturnItemResponse> items) { response.setItems(items); return this; }
        
        public ReturnResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getRefundMethod() { return refundMethod; }
    public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
    
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
    
    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }
    
    public List<ReturnItemResponse> getItems() { return items; }
    public void setItems(List<ReturnItemResponse> items) { this.items = items; }
    
    // Fields
    private UUID id;
    private String returnNumber;
    private UUID orderId;
    private String orderNumber;
    private String status;
    private String returnReason;
    private String comments;
    private BigDecimal refundAmount;
    private String refundMethod;
    private LocalDateTime requestedDate;
    private LocalDateTime processedDate;
    private List<ReturnItemResponse> items;
    
    // Inner class for ReturnItemResponse
    public static class ReturnItemResponse {
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final ReturnItemResponse item = new ReturnItemResponse();
            
            public Builder id(UUID id) { item.setId(id); return this; }
            public Builder orderItemId(UUID orderItemId) { item.setOrderItemId(orderItemId); return this; }
            public Builder productId(UUID productId) { item.setProductId(productId); return this; }
            public Builder productName(String productName) { item.setProductName(productName); return this; }
            public Builder productSku(String productSku) { item.setProductSku(productSku); return this; }
            public Builder quantity(Integer quantity) { item.setQuantity(quantity); return this; }
            public Builder unitPrice(BigDecimal unitPrice) { item.setUnitPrice(unitPrice); return this; }
            public Builder refundAmount(BigDecimal refundAmount) { item.setRefundAmount(refundAmount); return this; }
            public Builder reason(String reason) { item.setReason(reason); return this; }
            public Builder status(String status) { item.setStatus(status); return this; }
            
            public ReturnItemResponse build() {
                return item;
            }
        }
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public UUID getOrderItemId() { return orderItemId; }
        public void setOrderItemId(UUID orderItemId) { this.orderItemId = orderItemId; }
        
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
        
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        // Fields
        private UUID id;
        private UUID orderItemId;
        private UUID productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal refundAmount;
        private String reason;
        private String status;
    }
}
