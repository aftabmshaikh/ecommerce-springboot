package com.ecommerce.product.service;

import com.ecommerce.product.dto.review.ReviewRequest;
import com.ecommerce.product.dto.review.ReviewResponse;
import com.ecommerce.product.exception.ReviewAlreadyExistsException;
import com.ecommerce.product.exception.ReviewNotFoundException;
import com.ecommerce.product.exception.UnauthorizedAccessException;
import com.ecommerce.product.mapper.ReviewMapper;
import com.ecommerce.product.model.Review;
import com.ecommerce.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(UUID customerId, ReviewRequest request) {
        // Check if review already exists for this order and product
        if (request.getOrderId() != null && 
            reviewRepository.existsByOrderIdAndProductId(request.getOrderId(), request.getProductId())) {
            throw new ReviewAlreadyExistsException(
                "A review already exists for this product in the specified order"
            );
        }

        // In a real app, you would verify the customer has purchased the product
        // For now, we'll skip this check to simplify the implementation
        
        // Create and save the review
        Review review = reviewMapper.toEntity(request);
        review.setCustomerId(customerId);
        // In a real app, you would fetch customer details from the user service
        review.setCustomerName("Anonymous");
        
        Review savedReview = reviewRepository.save(review);
        
        // Update product rating stats
        productService.updateProductRating(request.getProductId());
        
        return reviewMapper.toDto(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByProductId(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(reviewMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
    }

    @Transactional
    public void markHelpful(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Transactional
    public void markNotHelpful(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(UUID productId) {
        Double avgRating = reviewRepository.calculateAverageRating(productId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Transactional(readOnly = true)
    public long getReviewCount(UUID productId) {
        return reviewRepository.countByProductId(productId);
    }
    
    @Transactional(readOnly = true)
    public Map<Integer, Long> getRatingDistribution(UUID productId) {
        // In a real app, you would implement this to return the count of reviews for each rating (1-5)
        // For now, we'll return a map with all ratings set to 0
        return IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Collectors.toMap(
                        rating -> rating,
                        rating -> 0L
                ));
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID customerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        
        if (!review.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this review");
        }
        
        reviewRepository.delete(review);
        // Update product rating stats
        productService.updateProductRating(review.getProductId());
    }
}
