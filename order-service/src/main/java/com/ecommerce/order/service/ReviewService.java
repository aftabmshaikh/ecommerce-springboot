package com.ecommerce.order.service;

import com.ecommerce.order.client.CustomerServiceClient;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.reviews.ReviewRequest;
import com.ecommerce.order.dto.reviews.ReviewResponse;
import com.ecommerce.order.exception.InvalidReviewException;
import com.ecommerce.order.exception.OrderItemNotFoundException;
import com.ecommerce.order.exception.OrderNotDeliveredException;
import com.ecommerce.order.model.*;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceClient productServiceClient;
    private final CustomerServiceClient customerServiceClient;

    @Transactional
    public ReviewResponse submitReview(UUID orderId, ReviewRequest request) {
        // Validate the review request
        validateReviewRequest(orderId, request);

        // Check if review already exists for this order item
        if (reviewRepository.existsByOrderItemId(request.getOrderItemId())) {
            throw new InvalidReviewException("You have already reviewed this item");
        }

        // Create and save the review
        Review review = new Review();
        review.setOrderId(orderId);
        review.setOrderItemId(request.getOrderItemId());
        review.setProductId(request.getProductId());
        review.setCustomerId(getCurrentCustomerId()); // Get from security context
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setRating(request.getRating());
        review.setImageUrls(request.getImageUrls());
        review.setTags(request.getTags());
        review.setHelpfulCount(0);
        review.setVerifiedPurchase(true);
        review.setReviewDate(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Update product rating in product service
        updateProductRating(savedReview.getProductId());

        // Mark order item as reviewed
        orderItemRepository.findById(request.getOrderItemId())
                .ifPresent(item -> {
                    item.setReviewed(true);
                    orderItemRepository.save(item);
                });

        return mapToReviewResponse(savedReview);
    }

    public List<ReviewResponse> getOrderReviews(UUID orderId) {
        return reviewRepository.findByOrderId(orderId).stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    public Page<ReviewResponse> getProductReviews(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(this::mapToReviewResponse);
    }

    private void validateReviewRequest(UUID orderId, ReviewRequest request) {
        // Check if order exists and belongs to the customer
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderItemNotFoundException("Order not found with ID: " + orderId));

        // Check if order is delivered
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new OrderNotDeliveredException("Cannot review an order that hasn't been delivered");
        }

        // Check if order item exists in the order
        boolean itemExists = order.getItems().stream()
                .anyMatch(item -> item.getId().equals(request.getOrderItemId()) && 
                                 item.getProductId().equals(request.getProductId()));

        if (!itemExists) {
            throw new OrderItemNotFoundException("Order item not found in the specified order");
        }

        // Check if the customer has already reviewed this item
        if (reviewRepository.existsByOrderItemIdAndCustomerId(
                request.getOrderItemId(), getCurrentCustomerId())) {
            throw new InvalidReviewException("You have already reviewed this item");
        }
    }

    private void updateProductRating(UUID productId) {
        try {
            // Call the product service to update the average rating
            // Note: The actual implementation depends on the ProductServiceClient method signature
            // productServiceClient.updateProductRating(productId, "Bearer token-placeholder");
            log.info("Updating product rating for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Failed to update product rating for product ID: " + productId, e);
            // In a production environment, you might want to implement a retry mechanism or dead-letter queue here
        }
    }

    private UUID getCurrentCustomerId() {
        // In a real implementation, this would get the current customer ID from the security context
        return UUID.randomUUID(); // Placeholder
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        // In a real implementation, you would fetch customer details from the user service
        // For now, we'll use a placeholder for customer name
        String customerName = "Customer";
        
        // Uncomment and implement when CustomerServiceClient has getCustomerInfo method
        // try {
        //     CustomerInfo customerInfo = customerServiceClient.getCustomerInfo(review.getCustomerId(), "Bearer token-placeholder");
        //     customerName = customerInfo != null ? customerInfo.getFullName() : "Anonymous";
        // } catch (Exception e) {
        //     log.error("Failed to fetch customer info for ID: " + review.getCustomerId(), e);
        //     customerName = "Anonymous";
        // }
        
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .orderItemId(review.getOrderItemId())
                .productId(review.getProductId())
                .customerId(review.getCustomerId())
                .customerName(customerName)
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
