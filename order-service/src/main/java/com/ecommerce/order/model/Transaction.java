package com.ecommerce.order.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    public Transaction(){}
    
    // All-args constructor
    public Transaction(UUID id, Order order, BigDecimal amount, PaymentStatus status, String paymentMethod, 
                      String transactionId, String paymentGateway, String gatewayTransactionId, 
                      String gatewayResponseCode, String gatewayResponseMessage, 
                      String currency, String description, String metadata, String failureReason, 
                      String failureCode, LocalDateTime processedAt, BigDecimal refundedAmount, 
                      Boolean isRefunded, String refundReason, LocalDateTime refundedAt, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.order = order;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentGateway = paymentGateway;
        this.gatewayTransactionId = gatewayTransactionId;
        this.gatewayResponseCode = gatewayResponseCode;
        this.gatewayResponseMessage = gatewayResponseMessage;
        this.currency = currency;
        this.description = description;
        this.metadata = metadata;
        this.failureReason = failureReason;
        this.failureCode = failureCode;
        this.processedAt = processedAt;
        this.refundedAmount = refundedAmount;
        this.isRefunded = isRefunded;
        this.refundReason = refundReason;
        this.refundedAt = refundedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;
    
    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;
    
    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;
    
    @Column(name = "gateway_response_code", length = 50)
    private String gatewayResponseCode;
    
    @Column(name = "gateway_response_message", columnDefinition = "TEXT")
    private String gatewayResponseMessage;
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    /**
     * Sets the gateway response details for this transaction.
     * 
     * @param responseCode The response code from the payment gateway
     * @param responseMessage The response message from the payment gateway
     * @param additionalInfo Additional information about the response (optional)
     */
    public void setGatewayResponse(String responseCode, String responseMessage, String additionalInfo) {
        this.gatewayResponseCode = responseCode;
        this.gatewayResponseMessage = responseMessage;
        
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            this.gatewayResponseMessage = gatewayResponseMessage != null 
                ? gatewayResponseMessage + " | " + additionalInfo 
                : additionalInfo;
        }
    }
    
    @Column(name = "is_refunded", nullable = false)
    private boolean refunded = false;
    
    @Column(name = "refund_amount", precision = 19, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(name = "refund_reason", length = 255)
    private String refundReason;
    
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Additional fields can be added as needed
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;
    
    @Column(name = "card_brand", length = 50)
    private String cardBrand;
    
    // Explicit getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getPaymentGateway() {
        return paymentGateway;
    }
    
    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
    
    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
    
    public void setGatewayTransactionId(String gatewayTransactionId) {
        this.gatewayTransactionId = gatewayTransactionId;
    }
    
    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }
    
    public String getGatewayResponseMessage() {
        return gatewayResponseMessage;
    }
    
    public boolean isRefunded() {
        return refunded;
    }
    
    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getRefundReason() {
        return refundReason;
    }
    
    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
    
    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }
    
    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCardLastFour() {
        return cardLastFour;
    }
    
    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }
    
    public String getCardBrand() {
        return cardBrand;
    }
    
    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "failure_code", length = 50)
    private String failureCode;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "refunded_amount", precision = 19, scale = 2)
    private BigDecimal refundedAmount;
    
    @Column(name = "is_refunded")
    private Boolean isRefunded;

    // Helper method to get the order ID
    public UUID getOrderId() {
        return order != null ? order.getId() : null;
    }

    public void setOrderId(UUID orderId) {
        if (this.order == null) {
            this.order = new Order();
        }
        this.order.setId(orderId);
    }
    private UUID customerId;
}
