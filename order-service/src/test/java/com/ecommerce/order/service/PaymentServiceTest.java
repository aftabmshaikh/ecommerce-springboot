package com.ecommerce.order.service;

import com.ecommerce.order.dto.payment.PaymentRequest;
import com.ecommerce.order.dto.payment.PaymentResponse;
import com.ecommerce.order.exception.PaymentProcessingException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.PaymentStatus;
import com.ecommerce.order.model.Transaction;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private OrderStatusService orderStatusService;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID transactionId;
    private Order order;
    private Transaction transaction;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setOrderNumber("ORD-12345678");
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(BigDecimal.valueOf(99.99));
        order.setCustomerId(UUID.randomUUID());
        order.setShippingAddress("123 Test St");
        order.setBillingAddress("123 Test St");
        order.setCustomerEmail("test@example.com");
        order.setCustomerPhone("1234567890");
        order.setSubtotal(BigDecimal.valueOf(89.99));
        order.setTax(BigDecimal.valueOf(5.00));
        order.setShippingFee(BigDecimal.valueOf(5.00));

        transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setOrder(order);
        transaction.setAmount(BigDecimal.valueOf(99.99));
        transaction.setStatus(PaymentStatus.SUCCEEDED);
        transaction.setTransactionId("mock_tx_12345678");
        transaction.setCreatedAt(LocalDateTime.now());

        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderId.toString());
        paymentRequest.setAmount(BigDecimal.valueOf(99.99));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethodId("CREDIT_CARD");

        ReflectionTestUtils.setField(paymentService, "defaultCurrency", "USD");
        // Make payment processing deterministic for tests
        ReflectionTestUtils.setField(paymentService, "random", new Random(1));
    }

    @Test
    void processPayment_WithValidRequest_ShouldReturnPaymentResponse() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        doNothing().when(orderStatusService).processSuccessfulPayment(any(Order.class));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        PaymentResponse result = paymentService.processPayment(paymentRequest);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(orderStatusService, atLeastOnce()).processSuccessfulPayment(any(Order.class));
    }

    @Test
    void processPayment_WithNullRequest_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> paymentService.processPayment(null));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processPayment_WithInvalidOrderId_ShouldThrowException() {
        // Arrange
        paymentRequest.setOrderId("invalid-uuid");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> paymentService.processPayment(paymentRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processPayment_WithNonExistentOrder_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> paymentService.processPayment(paymentRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processPayment_WithAmountMismatch_ShouldThrowException() {
        // Arrange
        paymentRequest.setAmount(BigDecimal.valueOf(50.00));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> paymentService.processPayment(paymentRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processPayment_WithAlreadyPaidOrder_ShouldThrowException() {
        // Arrange
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> paymentService.processPayment(paymentRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getPayment_WithValidId_ShouldReturnPaymentResponse() {
        // Arrange
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        PaymentResponse result = paymentService.getPayment(transactionId);

        // Assert
        assertNotNull(result);
        assertEquals(transactionId, result.getId());
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    void getPayment_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> paymentService.getPayment(transactionId));
    }

    @Test
    void capturePayment_WithAuthorizedPayment_ShouldCapture() {
        // Arrange
        transaction.setStatus(PaymentStatus.AUTHORIZED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        PaymentResponse result = paymentService.capturePayment(transactionId);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void capturePayment_WithNonAuthorizedPayment_ShouldThrowException() {
        // Arrange
        transaction.setStatus(PaymentStatus.SUCCEEDED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> paymentService.capturePayment(transactionId));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void refundPayment_WithCapturedPayment_ShouldRefund() {
        // Arrange
        transaction.setStatus(PaymentStatus.CAPTURED);
        BigDecimal refundAmount = BigDecimal.valueOf(50.00);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        PaymentResponse result = paymentService.refundPayment(transactionId, refundAmount);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void refundPayment_WithNonCapturedPayment_ShouldThrowException() {
        // Arrange
        transaction.setStatus(PaymentStatus.SUCCEEDED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> paymentService.refundPayment(transactionId, BigDecimal.valueOf(50.00)));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void cancelPayment_WithPendingPayment_ShouldCancel() {
        // Arrange
        transaction.setStatus(PaymentStatus.PENDING);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        PaymentResponse result = paymentService.cancelPayment(transactionId);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void cancelPayment_WithNonPendingPayment_ShouldThrowException() {
        // Arrange
        transaction.setStatus(PaymentStatus.SUCCEEDED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> paymentService.cancelPayment(transactionId));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void getAvailablePaymentMethods_ShouldReturnMethods() {
        // Act
        Object result = paymentService.getAvailablePaymentMethods();

        // Assert
        assertNotNull(result);
    }
}

