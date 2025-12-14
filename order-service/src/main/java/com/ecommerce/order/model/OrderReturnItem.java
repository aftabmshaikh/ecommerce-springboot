package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_return_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "orderReturn")
@EqualsAndHashCode(exclude = "orderReturn")
public class OrderReturnItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_return_id", nullable = false)
    private OrderReturn orderReturn;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "product_sku", nullable = false)
    private String productSku;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(nullable = false)
    private String reason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status = ReturnStatus.REQUESTED;
    
    // Getters and setters for status
    public ReturnStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReturnStatus status) {
        this.status = status;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getOrderItemId() { return orderItemId; }
    public void setOrderItemId(UUID orderItemId) { this.orderItemId = orderItemId; }
    
    public OrderReturn getOrderReturn() { return orderReturn; }
    public void setOrderReturn(OrderReturn orderReturn) { this.orderReturn = orderReturn; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final OrderReturnItem item = new OrderReturnItem();
        
        public Builder id(UUID id) { item.setId(id); return this; }
        public Builder orderItemId(UUID orderItemId) { item.setOrderItemId(orderItemId); return this; }
        public Builder orderReturn(OrderReturn orderReturn) { item.setOrderReturn(orderReturn); return this; }
        public Builder productId(UUID productId) { item.setProductId(productId); return this; }
        public Builder productName(String productName) { item.setProductName(productName); return this; }
        public Builder productSku(String productSku) { item.setProductSku(productSku); return this; }
        public Builder quantity(Integer quantity) { item.setQuantity(quantity); return this; }
        public Builder unitPrice(BigDecimal unitPrice) { item.setUnitPrice(unitPrice); return this; }
        public Builder refundAmount(BigDecimal refundAmount) { item.setRefundAmount(refundAmount); return this; }
        public Builder reason(String reason) { item.setReason(reason); return this; }
        public Builder createdAt(LocalDateTime createdAt) { item.setCreatedAt(createdAt); return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { item.setUpdatedAt(updatedAt); return this; }
        
        public OrderReturnItem build() {
            return item;
        }
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderReturnItem that = (OrderReturnItem) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    // toString
    @Override
    public String toString() {
        return "OrderReturnItem{" +
               "id=" + id +
               ", orderItemId=" + orderItemId +
               ", productId=" + productId +
               ", productName='" + productName + '\'' +
               ", quantity=" + quantity +
               ", unitPrice=" + unitPrice +
               ", refundAmount=" + refundAmount +
               ", reason='" + reason + '\'' +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}
