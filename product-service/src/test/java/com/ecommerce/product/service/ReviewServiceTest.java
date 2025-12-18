package com.ecommerce.product.service;

import com.ecommerce.product.dto.review.ReviewRequest;
import com.ecommerce.product.dto.review.ReviewResponse;
import com.ecommerce.product.exception.ReviewAlreadyExistsException;
import com.ecommerce.product.exception.ReviewNotFoundException;
import com.ecommerce.product.exception.UnauthorizedAccessException;
import com.ecommerce.product.mapper.ReviewMapper;
import com.ecommerce.product.model.Review;
import com.ecommerce.product.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UUID customerId;
    private UUID productId;
    private UUID reviewId;
    private UUID orderId;
    private Review review;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        review = new Review();
        review.setId(reviewId);
        review.setCustomerId(customerId);
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(5);
        review.setComment("Great product!");
        review.setHelpfulCount(0);
        review.setNotHelpfulCount(0);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        reviewRequest = new ReviewRequest();
        reviewRequest.setProductId(productId);
        reviewRequest.setOrderId(orderId);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great product!");
    }

    @Test
    void createReview_WithValidRequest_ShouldReturnReviewResponse() {
        // Arrange
        when(reviewRepository.existsByOrderIdAndProductId(orderId, productId)).thenReturn(false);
        when(reviewMapper.toEntity(any(ReviewRequest.class))).thenReturn(review);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(createReviewResponse());
        doNothing().when(productService).updateProductRating(productId);

        // Act
        ReviewResponse result = reviewService.createReview(customerId, reviewRequest);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(productService, times(1)).updateProductRating(productId);
    }

    @Test
    void createReview_WithExistingReview_ShouldThrowException() {
        // Arrange
        when(reviewRepository.existsByOrderIdAndProductId(orderId, productId)).thenReturn(true);

        // Act & Assert
        assertThrows(ReviewAlreadyExistsException.class, 
                () -> reviewService.createReview(customerId, reviewRequest));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewsByProductId_WithValidProductId_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(review));
        when(reviewRepository.findByProductId(productId, pageable)).thenReturn(reviewPage);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(createReviewResponse());

        // Act
        Page<ReviewResponse> result = reviewService.getReviewsByProductId(productId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(reviewRepository, times(1)).findByProductId(productId, pageable);
    }

    @Test
    void getReviewById_WithValidId_ShouldReturnReviewResponse() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewMapper.toDto(any(Review.class))).thenReturn(createReviewResponse());

        // Act
        ReviewResponse result = reviewService.getReviewById(reviewId);

        // Assert
        assertNotNull(result);
        assertEquals(reviewId, result.getId());
        verify(reviewRepository, times(1)).findById(reviewId);
    }

    @Test
    void getReviewById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReviewNotFoundException.class, 
                () -> reviewService.getReviewById(reviewId));
    }

    @Test
    void markHelpful_WithValidId_ShouldIncrementHelpfulCount() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        reviewService.markHelpful(reviewId);

        // Assert
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void markHelpful_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReviewNotFoundException.class, 
                () -> reviewService.markHelpful(reviewId));
    }

    @Test
    void markNotHelpful_WithValidId_ShouldIncrementNotHelpfulCount() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Act
        reviewService.markNotHelpful(reviewId);

        // Assert
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void markNotHelpful_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReviewNotFoundException.class, 
                () -> reviewService.markNotHelpful(reviewId));
    }

    @Test
    void getAverageRating_WithValidProductId_ShouldReturnAverage() {
        // Arrange
        when(reviewRepository.calculateAverageRating(productId)).thenReturn(4.5);

        // Act
        double result = reviewService.getAverageRating(productId);

        // Assert
        assertEquals(4.5, result);
        verify(reviewRepository, times(1)).calculateAverageRating(productId);
    }

    @Test
    void getAverageRating_WithNoReviews_ShouldReturnZero() {
        // Arrange
        when(reviewRepository.calculateAverageRating(productId)).thenReturn(null);

        // Act
        double result = reviewService.getAverageRating(productId);

        // Assert
        assertEquals(0.0, result);
    }

    @Test
    void getReviewCount_WithValidProductId_ShouldReturnCount() {
        // Arrange
        when(reviewRepository.countByProductId(productId)).thenReturn(10L);

        // Act
        long result = reviewService.getReviewCount(productId);

        // Assert
        assertEquals(10L, result);
        verify(reviewRepository, times(1)).countByProductId(productId);
    }

    @Test
    void deleteReview_WithValidIdAndOwner_ShouldDeleteReview() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(any(Review.class));
        doNothing().when(productService).updateProductRating(productId);

        // Act
        reviewService.deleteReview(reviewId, customerId);

        // Assert
        verify(reviewRepository, times(1)).delete(any(Review.class));
        verify(productService, times(1)).updateProductRating(productId);
    }

    @Test
    void deleteReview_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ReviewNotFoundException.class, 
                () -> reviewService.deleteReview(reviewId, customerId));
    }

    @Test
    void deleteReview_WithUnauthorizedUser_ShouldThrowException() {
        // Arrange
        UUID differentCustomerId = UUID.randomUUID();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, 
                () -> reviewService.deleteReview(reviewId, differentCustomerId));
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    private ReviewResponse createReviewResponse() {
        return ReviewResponse.builder()
                .id(reviewId)
                .productId(productId)
                .userId(customerId)
                .userName("Test User")
                .rating(5)
                .comment("Great product!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}


