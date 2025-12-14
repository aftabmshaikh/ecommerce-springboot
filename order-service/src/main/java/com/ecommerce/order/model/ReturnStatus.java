package com.ecommerce.order.model;

public enum ReturnStatus {
    REQUESTED,       // Return has been requested by the customer
    APPROVED,        // Return has been approved by the admin
    REJECTED,        // Return has been rejected by the admin
    PICKUP_SCHEDULED, // Pickup has been scheduled for the return
    IN_TRANSIT,      // Return items are in transit back to the warehouse
    RECEIVED,        // Return items have been received at the warehouse
    INSPECTING,      // Return items are being inspected
    REFUND_PROCESSING, // Refund is being processed
    REFUNDED,        // Refund has been processed
    COMPLETED,       // Return process is complete
    CANCELLED        // Return was cancelled
}
