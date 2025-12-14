package com.ecommerce.order.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private String message;
    
    @Column(name = "status_date")
    private LocalDateTime statusDate;
    
    public OrderStatusHistory() {
    }
    
    public OrderStatusHistory(OrderStatus status, String message, LocalDateTime statusDate) {
        this.status = status;
        this.message = message;
        this.statusDate = statusDate;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getStatusDate() {
        return statusDate;
    }
    
    public void setStatusDate(LocalDateTime statusDate) {
        this.statusDate = statusDate;
    }
    
    public static OrderStatusHistory create(OrderStatus status, String message) {
        return new OrderStatusHistory(status, message, LocalDateTime.now());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public static class Builder {
        private OrderStatus status;
        private String message;
        private LocalDateTime statusDate;
        
        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder statusDate(LocalDateTime statusDate) {
            this.statusDate = statusDate;
            return this;
        }
        
        public OrderStatusHistory build() {
            return new OrderStatusHistory(status, message, statusDate);
        }
    }
}
