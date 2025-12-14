package com.ecommerce.order.model;

/**
 * Represents the payment status of an order.
 */
public enum PaymentStatus {
    /**
     * Payment has not been processed yet.
     */
    PENDING,
    
    /**
     * Payment has been authorized but not captured.
     */
    AUTHORIZED,
    
    /**
     * Payment has been captured and funds have been transferred.
     */
    CAPTURED,
    
    /**
     * Payment has been successfully processed.
     */
    SUCCEEDED,
    
    /**
     * Payment has been partially refunded.
     */
    PARTIALLY_REFUNDED,
    
    /**
     * Payment has been fully refunded.
     */
    REFUNDED,
    
    /**
     * Payment has been voided/cancelled.
     */
    VOIDED,
    
    /**
     * Payment has failed.
     */
    FAILED,
    
    /**
     * Payment is being processed.
     */
    PROCESSING,
    
    /**
     * Payment requires additional action (e.g., 3D Secure).
     */
    REQUIRES_ACTION,
    
    /**
     * Payment requires a capture.
     */
    REQUIRES_CAPTURE,
    
    /**
     * Payment requires confirmation.
     */
    REQUIRES_CONFIRMATION,
    
    /**
     * Payment requires a payment method.
     */
    REQUIRES_PAYMENT_METHOD,
    
    /**
     * Payment has been canceled by the customer or the merchant.
     */
    CANCELED,
    
    /**
     * Payment has expired.
     */
    EXPIRED,
    
    /**
     * Payment is disputed by the customer.
     */
    DISPUTED
}
