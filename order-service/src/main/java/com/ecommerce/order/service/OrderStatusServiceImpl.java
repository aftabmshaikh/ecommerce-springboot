package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Minimal implementation of {@link OrderStatusService} used by the current
 * payment and order flows. The methods are intentionally lightweight and can
 * be extended with full domain behavior as needed.
 */
@Service
@Slf4j
public class OrderStatusServiceImpl implements OrderStatusService {

    @Override
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus, String notes) {
        log.debug("updateOrderStatus called for orderId={}, status={}, notes={}", orderId, newStatus, notes);
    }

    @Override
    public void transitionToPaymentPending(Order order) {
        log.debug("transitionToPaymentPending called for orderId={}", safeId(order));
    }

    @Override
    public void transitionToProcessing(Order order) {
        log.debug("transitionToProcessing called for orderId={}", safeId(order));
    }

    @Override
    public void transitionToShipped(Order order) {
        log.debug("transitionToShipped called for orderId={}", safeId(order));
    }

    @Override
    public void transitionToDelivered(Order order) {
        log.debug("transitionToDelivered called for orderId={}", safeId(order));
    }

    @Override
    public void transitionToCompleted(Order order) {
        log.debug("transitionToCompleted called for orderId={}", safeId(order));
    }

    @Override
    public void transitionToCancelled(Order order, String reason) {
        log.debug("transitionToCancelled called for orderId={}, reason={}", safeId(order), reason);
    }

    @Override
    public void transitionToRefunded(Order order, double refundAmount) {
        log.debug("transitionToRefunded called for orderId={}, amount={}", safeId(order), refundAmount);
    }

    @Override
    public void transitionToReturnRequested(Order order, String reason) {
        log.debug("transitionToReturnRequested called for orderId={}, reason={}", safeId(order), reason);
    }

    @Override
    public void transitionToReturnApproved(Order order) {
        log.debug("transitionToReturnApproved called for orderId={}", safeId(order));
    }

    @Override
    public void transitionToReturnRejected(Order order, String reason) {
        log.debug("transitionToReturnRejected called for orderId={}, reason={}", safeId(order), reason);
    }

    @Override
    public void transitionToReturnReceived(Order order) {
        log.debug("transitionToReturnReceived called for orderId={}", safeId(order));
    }

    @Override
    public void transitionToReturnRefunded(Order order, double refundAmount) {
        log.debug("transitionToReturnRefunded called for orderId={}, amount={}", safeId(order), refundAmount);
    }

    @Override
    public void addOrderStatusHistory(Order order, OrderStatus status, String notes) {
        log.debug("addOrderStatusHistory called for orderId={}, status={}, notes={}", safeId(order), status, notes);
    }

    @Override
    public void processSuccessfulPayment(Order order) {
        log.debug("processSuccessfulPayment called for orderId={}", safeId(order));
    }

    @Override
    public void processFailedPayment(Order order) {
        log.debug("processFailedPayment called for orderId={}", safeId(order));
    }

    private UUID safeId(Order order) {
        return order != null ? order.getId() : null;
    }
}


