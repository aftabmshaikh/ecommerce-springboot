package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderReturn;

import java.util.UUID;

public interface NotificationService {
    void sendOrderConfirmation(Order order);
    void sendOrderShippedNotification(Order order);
    void sendOrderDeliveredNotification(Order order);
    void sendOrderCancellationNotification(Order order);
    void sendReturnRequestReceivedNotification(OrderReturn orderReturn);
    void sendReturnRequestApprovedNotification(OrderReturn orderReturn);
    void sendReturnRequestRejectedNotification(OrderReturn orderReturn, String reason);
    void sendRefundProcessedNotification(Order order, double refundAmount);
    void sendLowInventoryNotification(UUID productId, int currentStock, int threshold);
}
