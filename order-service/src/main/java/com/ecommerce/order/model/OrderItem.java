package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
@EqualsAndHashCode(exclude = "order")
public class OrderItem {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false)
    private UUID productId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private String productSku;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus status = OrderItemStatus.CREATED;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version = 0L;
    
    @Column(name = "reviewed", nullable = false)
    private boolean reviewed = false;
    
    @Column(name = "returned_quantity", nullable = false)
    private int returnedQuantity = 0;
    
    @Transient
    private BigDecimal totalPrice;
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be null or negative");
        }
        this.quantity = quantity; 
        calculateTotalPrice();
    }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be null or negative");
        }
        this.unitPrice = unitPrice; 
        calculateTotalPrice();
    }
    
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { 
        this.discount = discount != null && discount.compareTo(BigDecimal.ZERO) >= 0 ? 
            discount : BigDecimal.ZERO;
        calculateTotalPrice();
    }
    
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { 
        this.tax = tax != null && tax.compareTo(BigDecimal.ZERO) >= 0 ? 
            tax : BigDecimal.ZERO;
        calculateTotalPrice();
    }
    
    public BigDecimal getTotalPrice() { 
        if (totalPrice == null) {
            calculateTotalPrice();
        }
        return totalPrice; 
    }
    
    public void setTotalPrice(BigDecimal totalPrice) { 
        this.totalPrice = totalPrice; 
    }
    
    public OrderItemStatus getStatus() { return status; }
    public void setStatus(OrderItemStatus status) { 
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
    
    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { 
        if (returnedQuantity < 0) {
            throw new IllegalArgumentException("Returned quantity cannot be negative");
        }
        this.returnedQuantity = returnedQuantity; 
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    /**
     * Calculates and updates the total price based on unit price, quantity, discount, and tax.
     */
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            this.totalPrice = subtotal.subtract(discount).add(tax);
            if (this.totalPrice.compareTo(BigDecimal.ZERO) < 0) {
                this.totalPrice = BigDecimal.ZERO;
            }
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
    
    /**
     * Updates the unit price and recalculates the total price.
     * @param unitPrice the new unit price
     * @throws IllegalArgumentException if unitPrice is null or negative
     */
    public void updateUnitPrice(BigDecimal unitPrice) {
        setUnitPrice(unitPrice);
    }
    
    /**
     * Updates the quantity and recalculates the total price.
     * @param quantity the new quantity
     * @throws IllegalArgumentException if quantity is null or negative
     */
    public void updateQuantity(Integer quantity) {
        setQuantity(quantity);
    }
    
    /**
     * Builder pattern for creating OrderItem instances.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final OrderItem item = new OrderItem();
        
        public Builder id(UUID id) { item.setId(id); return this; }
        public Builder order(Order order) { item.setOrder(order); return this; }
        public Builder productId(UUID productId) { item.setProductId(productId); return this; }
        public Builder productName(String productName) { item.setProductName(productName); return this; }
        public Builder productSku(String productSku) { item.setProductSku(productSku); return this; }
        public Builder quantity(int quantity) { item.setQuantity(quantity); return this; }
        public Builder unitPrice(BigDecimal unitPrice) { item.setUnitPrice(unitPrice); return this; }
        public Builder discount(BigDecimal discount) { item.setDiscount(discount); return this; }
        public Builder tax(BigDecimal tax) { item.setTax(tax); return this; }
        public Builder status(OrderItemStatus status) { item.setStatus(status); return this; }
        public Builder notes(String notes) { item.setNotes(notes); return this; }
        public Builder reviewed(boolean reviewed) { item.setReviewed(reviewed); return this; }
        public Builder returnedQuantity(int returnedQuantity) { item.setReturnedQuantity(returnedQuantity); return this; }
        
        public OrderItem build() {
            item.calculateTotalPrice();
            return item;
        }
    }
    
    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
}
