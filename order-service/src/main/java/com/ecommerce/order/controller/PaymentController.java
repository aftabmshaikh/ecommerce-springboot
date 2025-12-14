package com.ecommerce.order.controller;

import com.ecommerce.order.dto.payment.PaymentRequest;
import com.ecommerce.order.dto.payment.PaymentResponse;
import com.ecommerce.order.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "APIs for processing payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Process a payment for an order")
    public PaymentResponse processPayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment details by ID")
    public PaymentResponse getPayment(@PathVariable UUID paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @PostMapping("/{paymentId}/capture")
    @Operation(summary = "Capture an authorized payment")
    public PaymentResponse capturePayment(@PathVariable UUID paymentId) {
        return paymentService.capturePayment(paymentId);
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund a payment")
    public PaymentResponse refundPayment(
            @PathVariable UUID paymentId,
            @RequestParam(required = false) BigDecimal amount) {
        return paymentService.refundPayment(paymentId, amount);
    }

    @PostMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel a payment")
    public PaymentResponse cancelPayment(@PathVariable UUID paymentId) {
        return paymentService.cancelPayment(paymentId);
    }

    @GetMapping("/methods")
    @Operation(summary = "Get available payment methods")
    public Object getPaymentMethods() {
        return paymentService.getAvailablePaymentMethods();
    }

    @PostMapping("/webhook/stripe")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Stripe webhook for payment events")
    public void handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
    }
}
