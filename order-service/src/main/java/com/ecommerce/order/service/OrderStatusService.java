package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;

import java.util.UUID;

public interface OrderStatusService {
    void updateOrderStatus(UUID orderId, OrderStatus newStatus, String notes);
    void transitionToPaymentPending(Order order);
    void transitionToProcessing(Order order);
    void transitionToShipped(Order order);
    void transitionToDelivered(Order order);
    void transitionToCompleted(Order order);
    void transitionToCancelled(Order order, String reason);
    void transitionToRefunded(Order order, double refundAmount);
    void transitionToReturnRequested(Order order, String reason);
    void transitionToReturnApproved(Order order);
    void transitionToReturnRejected(Order order, String reason);
    void transitionToReturnReceived(Order order);
    void transitionToReturnRefunded(Order order, double refundAmount);
    void addOrderStatusHistory(Order order, OrderStatus status, String notes);
    
    /**
     * Process a successful payment for an order.
     * @param order The order to process
     */
    void processSuccessfulPayment(Order order);
    
    /**
     * Process a failed payment for an order.
     * @param order The order to process
     */
    void processFailedPayment(Order order);
}
