package com.ecommerce.order.dto.reviews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReviewResponse {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ReviewResponse response = new ReviewResponse();
        
        public Builder id(UUID id) { response.setId(id); return this; }
        public Builder orderId(UUID orderId) { response.setOrderId(orderId); return this; }
        public Builder orderItemId(UUID orderItemId) { response.setOrderItemId(orderItemId); return this; }
        public Builder productId(UUID productId) { response.setProductId(productId); return this; }
        public Builder productName(String productName) { response.setProductName(productName); return this; }
        public Builder productImage(String productImage) { response.setProductImage(productImage); return this; }
        public Builder customerId(UUID customerId) { response.setCustomerId(customerId); return this; }
        public Builder customerName(String customerName) { response.setCustomerName(customerName); return this; }
        public Builder customerAvatar(String customerAvatar) { response.setCustomerAvatar(customerAvatar); return this; }
        public Builder title(String title) { response.setTitle(title); return this; }
        public Builder comment(String comment) { response.setComment(comment); return this; }
        public Builder rating(Integer rating) { response.setRating(rating); return this; }
        public Builder imageUrls(List<String> imageUrls) { response.setImageUrls(imageUrls); return this; }
        public Builder tags(List<String> tags) { response.setTags(tags); return this; }
        public Builder helpfulCount(Integer helpfulCount) { response.setHelpfulCount(helpfulCount); return this; }
        public Builder isVerifiedPurchase(Boolean isVerifiedPurchase) { response.setIsVerifiedPurchase(isVerifiedPurchase); return this; }
        public Builder reviewDate(LocalDateTime reviewDate) { response.setReviewDate(reviewDate); return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { response.setUpdatedAt(updatedAt); return this; }
        
        public ReviewResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public UUID getOrderItemId() { return orderItemId; }
    public void setOrderItemId(UUID orderItemId) { this.orderItemId = orderItemId; }
    
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerAvatar() { return customerAvatar; }
    public void setCustomerAvatar(String customerAvatar) { this.customerAvatar = customerAvatar; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }
    
    public Boolean getIsVerifiedPurchase() { return isVerifiedPurchase; }
    public void setIsVerifiedPurchase(Boolean verifiedPurchase) { isVerifiedPurchase = verifiedPurchase; }
    
    public LocalDateTime getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDateTime reviewDate) { this.reviewDate = reviewDate; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    private UUID id;
    private UUID orderId;
    private UUID orderItemId;
    private UUID productId;
    private String productName;
    private String productImage;
    private UUID customerId;
    private String customerName;
    private String customerAvatar;
    private String title;
    private String comment;
    private Integer rating;
    private List<String> imageUrls;
    private List<String> tags;
    private Integer helpfulCount;
    private Boolean isVerifiedPurchase;
    private LocalDateTime reviewDate;
    private LocalDateTime updatedAt;
}
