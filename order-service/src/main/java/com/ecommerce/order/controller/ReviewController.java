package com.ecommerce.order.controller;

import com.ecommerce.order.dto.reviews.ReviewRequest;
import com.ecommerce.order.dto.reviews.ReviewResponse;
import com.ecommerce.order.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "APIs for product and order reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/orders/{orderId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a review for an order item")
    public ReviewResponse submitReview(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReviewRequest request) {
        return reviewService.submitReview(orderId, request);
    }

    @GetMapping("/orders/{orderId}/reviews")
    @Operation(summary = "Get reviews for an order")
    public List<ReviewResponse> getOrderReviews(@PathVariable UUID orderId) {
        return reviewService.getOrderReviews(orderId);
    }

    @GetMapping("/products/{productId}/reviews")
    @Operation(summary = "Get reviews for a product")
    public Page<ReviewResponse> getProductReviews(
            @PathVariable UUID productId,
            Pageable pageable) {
        return reviewService.getProductReviews(productId, pageable);
    }
}
