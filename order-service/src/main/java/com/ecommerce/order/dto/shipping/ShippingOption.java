package com.ecommerce.order.dto.shipping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShippingOption {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ShippingOption shippingOption = new ShippingOption();
        
        public Builder id(String id) { shippingOption.setId(id); return this; }
        public Builder name(String name) { shippingOption.setName(name); return this; }
        public Builder description(String description) { shippingOption.setDescription(description); return this; }
        public Builder price(BigDecimal price) { shippingOption.setPrice(price); return this; }
        public Builder currency(String currency) { shippingOption.setCurrency(currency); return this; }
        public Builder estimatedDelivery(String estimatedDelivery) { shippingOption.setEstimatedDelivery(estimatedDelivery); return this; }
        public Builder isRecommended(boolean isRecommended) { shippingOption.setRecommended(isRecommended); return this; }
        public Builder carrier(String carrier) { shippingOption.setCarrier(carrier); return this; }
        public Builder serviceLevel(String serviceLevel) { shippingOption.setServiceLevel(serviceLevel); return this; }
        public Builder minDeliveryDate(LocalDateTime minDeliveryDate) { shippingOption.setMinDeliveryDate(minDeliveryDate); return this; }
        public Builder maxDeliveryDate(LocalDateTime maxDeliveryDate) { shippingOption.setMaxDeliveryDate(maxDeliveryDate); return this; }
        public Builder hasTracking(boolean hasTracking) { shippingOption.setHasTracking(hasTracking); return this; }
        public Builder hasInsurance(boolean hasInsurance) { shippingOption.setHasInsurance(hasInsurance); return this; }
        public Builder hasSignatureConfirmation(boolean hasSignatureConfirmation) { shippingOption.setHasSignatureConfirmation(hasSignatureConfirmation); return this; }
        public Builder supportsCod(boolean supportsCod) { shippingOption.setSupportsCod(supportsCod); return this; }
        public Builder maxWeight(Double maxWeight) { shippingOption.setMaxWeight(maxWeight); return this; }
        public Builder maxLength(Double maxLength) { shippingOption.setMaxLength(maxLength); return this; }
        public Builder maxWidth(Double maxWidth) { shippingOption.setMaxWidth(maxWidth); return this; }
        public Builder maxHeight(Double maxHeight) { shippingOption.setMaxHeight(maxHeight); return this; }
        
        public ShippingOption build() {
            return shippingOption;
        }
    }
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String estimatedDelivery;
    private boolean isRecommended;
    private String carrier;
    private String serviceLevel;
    private LocalDateTime minDeliveryDate;
    private LocalDateTime maxDeliveryDate;
    
    // Additional features
    private boolean hasTracking;
    private boolean hasInsurance;
    private boolean hasSignatureConfirmation;
    private boolean supportsCod;
    
    // Dimensions and weight limits
    private Double maxWeight; // in kg
    private Double maxLength; // in cm
    private Double maxWidth;  // in cm
    private Double maxHeight; // in cm
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    
    public boolean isRecommended() { return isRecommended; }
    public void setRecommended(boolean recommended) { isRecommended = recommended; }
    
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    
    public String getServiceLevel() { return serviceLevel; }
    public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
    
    public LocalDateTime getMinDeliveryDate() { return minDeliveryDate; }
    public void setMinDeliveryDate(LocalDateTime minDeliveryDate) { this.minDeliveryDate = minDeliveryDate; }
    
    public LocalDateTime getMaxDeliveryDate() { return maxDeliveryDate; }
    public void setMaxDeliveryDate(LocalDateTime maxDeliveryDate) { this.maxDeliveryDate = maxDeliveryDate; }
    
    public boolean isHasTracking() { return hasTracking; }
    public void setHasTracking(boolean hasTracking) { this.hasTracking = hasTracking; }
    
    public boolean isHasInsurance() { return hasInsurance; }
    public void setHasInsurance(boolean hasInsurance) { this.hasInsurance = hasInsurance; }
    
    public boolean isHasSignatureConfirmation() { return hasSignatureConfirmation; }
    public void setHasSignatureConfirmation(boolean hasSignatureConfirmation) { this.hasSignatureConfirmation = hasSignatureConfirmation; }
    
    public boolean isSupportsCod() { return supportsCod; }
    public void setSupportsCod(boolean supportsCod) { this.supportsCod = supportsCod; }
    
    public Double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(Double maxWeight) { this.maxWeight = maxWeight; }
    
    public Double getMaxLength() { return maxLength; }
    public void setMaxLength(Double maxLength) { this.maxLength = maxLength; }
    
    public Double getMaxWidth() { return maxWidth; }
    public void setMaxWidth(Double maxWidth) { this.maxWidth = maxWidth; }
    
    public Double getMaxHeight() { return maxHeight; }
    public void setMaxHeight(Double maxHeight) { this.maxHeight = maxHeight; }
}
