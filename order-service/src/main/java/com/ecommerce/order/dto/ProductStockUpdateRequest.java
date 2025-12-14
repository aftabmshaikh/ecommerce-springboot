package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class ProductStockUpdateRequest {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ProductStockUpdateRequest request = new ProductStockUpdateRequest();
        
        public Builder productId(UUID productId) { request.setProductId(productId); return this; }
        public Builder quantity(int quantity) { request.setQuantity(quantity); return this; }
        public Builder increment(boolean increment) { request.setIncrement(increment); return this; }
        
        public ProductStockUpdateRequest build() {
            return request;
        }
    }
    
    // Getters and Setters
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public boolean isIncrement() { return increment; }
    public void setIncrement(boolean increment) { this.increment = increment; }
    
    // Helper method to create a stock update request for order placement
    public static ProductStockUpdateRequest forOrder(UUID productId, int quantity) {
        return ProductStockUpdateRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .increment(false) // Decrease stock for order
                .build();
    }
    
    // Helper method to create a stock update request for order cancellation
    public static ProductStockUpdateRequest forCancellation(UUID productId, int quantity) {
        return ProductStockUpdateRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .increment(true) // Increase stock for cancellation
                .build();
    }
    
    // Fields
    private UUID productId;
    private int quantity;
    private boolean increment;
    
    // Constructors
    public ProductStockUpdateRequest() {}
    
    public ProductStockUpdateRequest(UUID productId, int quantity, boolean increment) {
        this.productId = productId;
        this.quantity = quantity;
        this.increment = increment;
    }
}
