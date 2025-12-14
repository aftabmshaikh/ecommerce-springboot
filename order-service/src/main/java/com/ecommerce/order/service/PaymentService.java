package com.ecommerce.order.service;

import com.ecommerce.order.dto.payment.PaymentRequest;
import com.ecommerce.order.dto.payment.PaymentResponse;
import com.ecommerce.order.exception.PaymentProcessingException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.model.Transaction;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentService.class);

    @Value("${app.currency:USD}")
    private String defaultCurrency;
    
    private final Random random = new Random();
    private final OrderRepository orderRepository;
    
    // Helper method to determine card brand from card number
    private String determineCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        // Remove any non-digit characters
        String digits = cardNumber.replaceAll("\\D", "");
        
        // Check for Visa (starts with 4)
        if (digits.matches("^4[0-9]{12}(?:[0-9]{3})?$")) {
            return "VISA";
        }
        // Check for Mastercard (starts with 51-55)
        else if (digits.matches("^5[1-5][0-9]{14}$")) {
            return "MASTERCARD";
        }
        // Check for American Express (starts with 34 or 37)
        else if (digits.matches("^3[47][0-9]{13}$")) {
            return "AMEX";
        }
        // Check for Discover (starts with 6011, 644-649, or 65)
        else if (digits.matches("^6(?:011|5[0-9]{2}|4[4-9][0-9]|22[0-9]{2})[0-9]{12}$")) {
            return "DISCOVER";
        }
        
        return "UNKNOWN";
    }
    private final TransactionRepository transactionRepository;
    private final OrderStatusService orderStatusService;

    // Simulate payment processing with random success/failure
    private boolean processMockPayment() {
        // 90% success rate for testing
        return random.nextDouble() < 0.9;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        
        // Convert orderId from String to UUID
        UUID orderId;
        try {
            orderId = UUID.fromString(request.getOrderId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order ID format: " + request.getOrderId(), e);
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        validatePaymentRequest(order, request);

        // Simulate payment processing
        boolean paymentSuccess = false;
        String transactionId = "mock_tx_" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            paymentSuccess = processMockPayment();
        } catch (Exception e) {
            log.error("Error processing payment for order {}", orderId, e);
            throw new PaymentProcessingException("Error processing payment", e);
        }
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setOrder(order);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(paymentSuccess ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED);
        transaction.setPaymentMethod(request.getPaymentMethodId() != null ? request.getPaymentMethodId() : "MOCK_PAYMENT");
        transaction.setTransactionId(transactionId);
        transaction.setPaymentGateway("MOCK_GATEWAY");
        transaction.setGatewayTransactionId(transactionId);
        transaction.setGatewayResponse("200", 
            paymentSuccess ? "Payment processed successfully" : "Payment failed",
            null);
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : defaultCurrency);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setProcessedAt(LocalDateTime.now());
        transaction.setRefunded(false);
                
        // Set card details if available
        if (request.getCard() != null && request.getCard().getNumber() != null && !request.getCard().getNumber().trim().isEmpty()) {
            String cardNumber = request.getCard().getNumber().trim();
            transaction.setCardLastFour(cardNumber.substring(Math.max(0, cardNumber.length() - 4)));
            transaction.setCardBrand(determineCardBrand(cardNumber));
        }
        
        transaction = transactionRepository.save(transaction);
        
        // Update order status based on payment result
        if (paymentSuccess) {
            orderStatusService.processSuccessfulPayment(order);
        } else {
            orderStatusService.processFailedPayment(order);
        }
        
        orderRepository.save(order);

        return buildPaymentResponse(transaction);
    }

    // Mock payment processing methods
    public PaymentResponse getPayment(UUID paymentId) {
        return transactionRepository.findById(paymentId)
                .map(this::buildPaymentResponse)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }
    
    public PaymentResponse capturePayment(UUID paymentId) {
        Transaction transaction = transactionRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));
        
        if (transaction.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new IllegalStateException("Only authorized payments can be captured");
        }
        
        transaction.setStatus(PaymentStatus.CAPTURED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        
        return buildPaymentResponse(transaction);
    }
    
    public PaymentResponse refundPayment(UUID paymentId, BigDecimal amount) {
        Transaction originalTx = transactionRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));
                
        if (originalTx.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Only captured payments can be refunded");
        }
        
        // Update the original transaction with refund details
        originalTx.setStatus(PaymentStatus.REFUNDED);
        originalTx.setRefundAmount(amount != null ? amount : originalTx.getAmount());
        originalTx.setRefundedAt(LocalDateTime.now());
        originalTx.setUpdatedAt(LocalDateTime.now());
        originalTx.setRefunded(true);
        
        Transaction updatedTx = transactionRepository.save(originalTx);
        
        return buildPaymentResponse(updatedTx);
    }

    private PaymentResponse buildPaymentResponse(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        PaymentResponse response = new PaymentResponse();
        response.setId(transaction.getId());
        response.setOrderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null);
        response.setAmount(transaction.getAmount());
        response.setStatus(transaction.getStatus());
        response.setPaymentMethod(transaction.getPaymentMethod());
        response.setTransactionId(transaction.getTransactionId());
        response.setPaymentGateway(transaction.getPaymentGateway());
        response.setGatewayTransactionId(transaction.getGatewayTransactionId());
        response.setGatewayResponseCode(transaction.getGatewayResponseCode());
        response.setGatewayResponseMessage(transaction.getGatewayResponseMessage());
        response.setRefunded(transaction.isRefunded() ? transaction.isRefunded() : false);
        response.setRefundAmount(transaction.getRefundAmount());
        response.setRefundReason(transaction.getRefundReason());
        response.setRefundedAt(transaction.getRefundedAt());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        response.setCardLastFour(transaction.getCardLastFour());
        response.setCardBrand(transaction.getCardBrand());
        
        // Set additional fields if order is available
        if (transaction.getOrder() != null) {
            response.setOrderNumber(transaction.getOrder().getOrderNumber());
            response.setCustomerId(transaction.getOrder().getCustomerId());
            // You might want to set customer name and email if available
        }
        
        return response;
    }
    
    public PaymentResponse cancelPayment(UUID paymentId) {
        Transaction transaction = transactionRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with ID: " + paymentId));
        
        log.info("Cancelling payment with ID: {}", paymentId);
                
        if (transaction.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be cancelled");
        }
        
        transaction.setStatus(PaymentStatus.CANCELED);
        transaction = transactionRepository.save(transaction);
        
        return buildPaymentResponse(transaction);
    }
    
    public Object getAvailablePaymentMethods() {
        return Map.of(
            "paymentMethods", new String[]{"credit_card", "paypal", "bank_transfer"},
            "defaultCurrency", defaultCurrency
        );
    }
    
    public void handleStripeWebhook(String payload, String sigHeader) {
        // No-op in mock implementation
        log.info("Received mock webhook: {}", payload);
    }

    private void validatePaymentRequest(Order order, PaymentRequest request) {
        if (!order.getTotalAmount().equals(request.getAmount())) {
            throw new IllegalArgumentException("Payment amount does not match order total");
        }
        
        if (order.getStatus().isPaid()) {
            throw new IllegalStateException("Order is already paid");
        }
    }

    // No external payment processor integration needed
}
