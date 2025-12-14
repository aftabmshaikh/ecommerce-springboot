package com.ecommerce.order.dto.payment;

import com.ecommerce.order.model.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentResponse {
    // Fields
    private boolean refunded = false;
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private String paymentGateway;
    private String gatewayTransactionId;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cardLastFour;
    private String cardBrand;
    private UUID billingAddressId;
    private UUID customerId;
    // Additional fields for response only
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    
    // Getters and Setters
    public boolean isRefunded() { return refunded; }
    public void setRefunded(boolean refunded) { this.refunded = refunded; }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getPaymentGateway() { return paymentGateway; }
    public void setPaymentGateway(String paymentGateway) { this.paymentGateway = paymentGateway; }
    
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public void setGatewayTransactionId(String gatewayTransactionId) { this.gatewayTransactionId = gatewayTransactionId; }
    
    public String getGatewayResponseCode() { return gatewayResponseCode; }
    public void setGatewayResponseCode(String gatewayResponseCode) { this.gatewayResponseCode = gatewayResponseCode; }
    
    public String getGatewayResponseMessage() { return gatewayResponseMessage; }
    public void setGatewayResponseMessage(String gatewayResponseMessage) { this.gatewayResponseMessage = gatewayResponseMessage; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
    
    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    
    public UUID getBillingAddressId() { return billingAddressId; }
    public void setBillingAddressId(UUID billingAddressId) { this.billingAddressId = billingAddressId; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    // Builder pattern implementation
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final PaymentResponse response = new PaymentResponse();
        
        public Builder id(UUID id) { response.setId(id); return this; }
        public Builder orderId(UUID orderId) { response.setOrderId(orderId); return this; }
        public Builder amount(BigDecimal amount) { response.setAmount(amount); return this; }
        public Builder status(PaymentStatus status) { response.setStatus(status); return this; }
        public Builder paymentMethod(String paymentMethod) { response.setPaymentMethod(paymentMethod); return this; }
        public Builder transactionId(String transactionId) { response.setTransactionId(transactionId); return this; }
        public Builder paymentGateway(String paymentGateway) { response.setPaymentGateway(paymentGateway); return this; }
        public Builder gatewayTransactionId(String gatewayTransactionId) { response.setGatewayTransactionId(gatewayTransactionId); return this; }
        public Builder gatewayResponseCode(String gatewayResponseCode) { response.setGatewayResponseCode(gatewayResponseCode); return this; }
        public Builder gatewayResponseMessage(String gatewayResponseMessage) { response.setGatewayResponseMessage(gatewayResponseMessage); return this; }
        public Builder refundAmount(BigDecimal refundAmount) { response.setRefundAmount(refundAmount); return this; }
        public Builder refundReason(String refundReason) { response.setRefundReason(refundReason); return this; }
        public Builder refundedAt(LocalDateTime refundedAt) { response.setRefundedAt(refundedAt); return this; }
        public Builder createdAt(LocalDateTime createdAt) { response.setCreatedAt(createdAt); return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { response.setUpdatedAt(updatedAt); return this; }
        public Builder cardLastFour(String cardLastFour) { response.setCardLastFour(cardLastFour); return this; }
        public Builder cardBrand(String cardBrand) { response.setCardBrand(cardBrand); return this; }
        public Builder billingAddressId(UUID billingAddressId) { response.setBillingAddressId(billingAddressId); return this; }
        public Builder customerId(UUID customerId) { response.setCustomerId(customerId); return this; }
        public Builder orderNumber(String orderNumber) { response.setOrderNumber(orderNumber); return this; }
        public Builder customerName(String customerName) { response.setCustomerName(customerName); return this; }
        public Builder customerEmail(String customerEmail) { response.setCustomerEmail(customerEmail); return this; }
        public Builder refunded(boolean refunded) { response.setRefunded(refunded); return this; }
        
        public PaymentResponse build() {
            return response;
        }
    }
    
    // Helper method to create a response with minimal fields
    public static PaymentResponse createMinimalResponse(
            UUID id, 
            UUID orderId, 
            BigDecimal amount, 
            PaymentStatus status, 
            String paymentMethod) {
        return builder()
                .id(id)
                .orderId(orderId)
                .amount(amount)
                .status(status)
                .paymentMethod(paymentMethod)
                .build();
    }
}
