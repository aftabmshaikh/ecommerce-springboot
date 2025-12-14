package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_returns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order", "items"})
@EqualsAndHashCode(exclude = {"order", "items"})
public class OrderReturn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "return_number", nullable = false, unique = true)
    private String returnNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReturnStatus status;
    
    @Column(name = "return_reason", nullable = false, columnDefinition = "TEXT")
    private String returnReason;
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_method")
    private String refundMethod;
    
    @Column(name = "requested_date", nullable = false)
    private LocalDateTime requestedDate;
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "orderReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderReturnItem> items = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public ReturnStatus getStatus() { return status; }
    public void setStatus(ReturnStatus status) { this.status = status; }
    
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
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public List<OrderReturnItem> getItems() { return items; }
    public void setItems(List<OrderReturnItem> items) { this.items = items; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper method to set order ID
    public void setOrderId(UUID orderId) {
        if (this.order == null) {
            this.order = new Order();
        }
        this.order.setId(orderId);
    }

    public UUID getOrderId() {
        return order != null ? order.getId() : null;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final OrderReturn orderReturn = new OrderReturn();
        
        public Builder id(UUID id) { orderReturn.setId(id); return this; }
        public Builder returnNumber(String returnNumber) { orderReturn.setReturnNumber(returnNumber); return this; }
        public Builder order(Order order) { orderReturn.setOrder(order); return this; }
        public Builder customerId(UUID customerId) { orderReturn.setCustomerId(customerId); return this; }
        public Builder status(ReturnStatus status) { orderReturn.setStatus(status); return this; }
        public Builder returnReason(String returnReason) { orderReturn.setReturnReason(returnReason); return this; }
        public Builder comments(String comments) { orderReturn.setComments(comments); return this; }
        public Builder refundAmount(BigDecimal refundAmount) { orderReturn.setRefundAmount(refundAmount); return this; }
        public Builder refundMethod(String refundMethod) { orderReturn.setRefundMethod(refundMethod); return this; }
        public Builder requestedDate(LocalDateTime requestedDate) { orderReturn.setRequestedDate(requestedDate); return this; }
        public Builder processedDate(LocalDateTime processedDate) { orderReturn.setProcessedDate(processedDate); return this; }
        public Builder completedAt(LocalDateTime completedAt) { orderReturn.setCompletedAt(completedAt); return this; }
        public Builder items(List<OrderReturnItem> items) { orderReturn.setItems(items); return this; }
        public Builder createdAt(LocalDateTime createdAt) { orderReturn.setCreatedAt(createdAt); return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { orderReturn.setUpdatedAt(updatedAt); return this; }
        
        public OrderReturn build() {
            return orderReturn;
        }
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        
        if (this.returnNumber == null) {
            this.returnNumber = "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "OrderReturn{" +
               "id=" + id +
               ", returnNumber='" + returnNumber + '\'' +
               ", orderId=" + (order != null ? order.getId() : null) +
               ", customerId=" + customerId +
               ", status=" + status +
               ", requestedDate=" + requestedDate +
               ", processedDate=" + processedDate +
               ", completedAt=" + completedAt +
               ", refundAmount=" + refundAmount +
               '}';
    }
}
