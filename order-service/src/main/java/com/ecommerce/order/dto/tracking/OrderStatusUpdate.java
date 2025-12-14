package com.ecommerce.order.dto.tracking;

import java.time.LocalDateTime;

public class OrderStatusUpdate {
    private String status;
    private String description;
    private String location;
    private LocalDateTime timestamp;
    private boolean isCurrent;
    
    // Default constructor
    public OrderStatusUpdate() {}
    
    // All-args constructor
    public OrderStatusUpdate(String status, String description, String location, 
                            LocalDateTime timestamp, boolean isCurrent) {
        this.status = status;
        this.description = description;
        this.location = location;
        this.timestamp = timestamp;
        this.isCurrent = isCurrent;
    }
    
    // Factory method for builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }
    
    // Builder class
    public static class Builder {
        private final OrderStatusUpdate update = new OrderStatusUpdate();
        
        public Builder status(String status) { update.setStatus(status); return this; }
        public Builder description(String description) { update.setDescription(description); return this; }
        public Builder location(String location) { update.setLocation(location); return this; }
        public Builder timestamp(LocalDateTime timestamp) { update.setTimestamp(timestamp); return this; }
        public Builder isCurrent(boolean isCurrent) { update.setCurrent(isCurrent); return this; }
        
        public OrderStatusUpdate build() {
            return update;
        }
    }
}
