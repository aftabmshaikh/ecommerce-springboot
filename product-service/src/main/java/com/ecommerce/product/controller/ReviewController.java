package com.ecommerce.product.controller;

import com.ecommerce.product.dto.review.ReviewRequest;
import com.ecommerce.product.dto.review.ReviewResponse;
import com.ecommerce.product.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "APIs for managing product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a product review")
    public ReviewResponse submitReview(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID productId,
            @Valid @RequestBody ReviewRequest request) {
        request.setProductId(productId);
        return reviewService.createReview(userId, request);
    }

    @GetMapping
    @Operation(summary = "Get reviews for a product")
    public Page<ReviewResponse> getProductReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 10) Pageable pageable) {
        return reviewService.getReviewsByProductId(productId, pageable);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID")
    public ReviewResponse getReview(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @PostMapping("/{reviewId}/helpful")
    @Operation(summary = "Mark a review as helpful")
    public ResponseEntity<Void> markHelpful(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId) {
        reviewService.markHelpful(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reviewId}/not-helpful")
    @Operation(summary = "Mark a review as not helpful")
    public ResponseEntity<Void> markNotHelpful(
            @PathVariable UUID productId,
            @PathVariable UUID reviewId) {
        reviewService.markNotHelpful(reviewId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a review")
    public void deleteReview(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID productId,
            @PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId, userId);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get review statistics for a product")
    public ReviewStatsResponse getReviewStats(@PathVariable UUID productId) {
        double averageRating = reviewService.getAverageRating(productId);
        long reviewCount = reviewService.getReviewCount(productId);
        
        return new ReviewStatsResponse(
            productId,
            averageRating,
            reviewCount,
            reviewService.getRatingDistribution(productId)
        );
    }

    // DTO for review statistics
    private record ReviewStatsResponse(
        UUID productId,
        double averageRating,
        long totalReviews,
        // Map of rating (1-5) to count
        java.util.Map<Integer, Long> ratingDistribution
    ) {}
}
