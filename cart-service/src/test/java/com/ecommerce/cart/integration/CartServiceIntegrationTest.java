package com.ecommerce.cart.integration;

import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.dto.CartItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private ProductServiceClient productServiceClient;

    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        // Mock product service responses
        ProductServiceClient.ProductDto productDto = new ProductServiceClient.ProductDto(
                productId, "Test Product", "Test Description", "http://example.com/image.jpg", 29.99, 100
        );
        when(productServiceClient.getProductById(any(UUID.class))).thenReturn(Optional.of(productDto));
        when(productServiceClient.isInStock(any(UUID.class), anyInt())).thenReturn(true);
    }

    @Test
    void getOrCreateCart_WithNewUser_ShouldCreateCart() {
        // Act
        CartResponse result = cartService.getOrCreateCart(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(0, result.getTotalItems());

        // Note: getOrCreateCart doesn't persist the cart if it doesn't exist,
        // it only returns a new CartResponse. The cart is only persisted when items are added.
        Optional<Cart> savedCart = cartRepository.findByUserId(userId);
        assertFalse(savedCart.isPresent()); // Cart is not persisted until items are added
    }

    @Test
    void addItemToCart_WithNewItem_ShouldAddItem() {
        // Arrange
        CartItemRequest itemRequest = new CartItemRequest(productId, 2);

        // Act
        CartResponse result = cartService.addItemToCart(userId, itemRequest);

        // Assert
        assertNotNull(result);
        // totalItems is the sum of all quantities, not the count of distinct items
        assertEquals(2, result.getTotalItems()); // quantity = 2
        assertFalse(result.getItems().isEmpty());
        assertEquals(1, result.getItems().size()); // 1 distinct item
        assertEquals(productId, result.getItems().get(0).getProductId());
        assertEquals(2, result.getItems().get(0).getQuantity());

        // Verify cart is persisted
        Cart savedCart = cartRepository.findByUserId(userId).orElse(null);
        assertNotNull(savedCart);
        assertEquals(1, savedCart.getItems().size());
    }

    @Test
    void addItemToCart_WithExistingItem_ShouldUpdateQuantity() {
        // Arrange
        CartItemRequest itemRequest = new CartItemRequest(productId, 2);
        cartService.addItemToCart(userId, itemRequest);

        // Act - Add same item again
        CartResponse result = cartService.addItemToCart(userId, itemRequest);

        // Assert
        assertNotNull(result);
        // totalItems is the sum of all quantities (2 + 2 = 4)
        assertEquals(4, result.getTotalItems());
        assertEquals(1, result.getItems().size()); // Still 1 distinct item
        assertEquals(4, result.getItems().get(0).getQuantity()); // 2 + 2 = 4
    }

    @Test
    void updateCartItem_WithValidQuantity_ShouldUpdateItem() {
        // Arrange
        CartItemRequest itemRequest = new CartItemRequest(productId, 2);
        CartResponse cart = cartService.addItemToCart(userId, itemRequest);
        String itemId = cart.getItems().get(0).getId();

        // Act
        CartResponse result = cartService.updateCartItem(userId, itemId, 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    @Test
    void removeItemFromCart_WithValidItem_ShouldRemoveItem() {
        // Arrange
        CartItemRequest itemRequest = new CartItemRequest(productId, 2);
        CartResponse cart = cartService.addItemToCart(userId, itemRequest);
        String itemId = cart.getItems().get(0).getId();

        // Act
        CartResponse result = cartService.removeItemFromCart(userId, itemId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalItems());
    }

    @Test
    void clearCart_WithExistingCart_ShouldDeleteCart() {
        // Arrange
        CartItemRequest itemRequest = new CartItemRequest(productId, 2);
        cartService.addItemToCart(userId, itemRequest);
        
        // Verify cart exists before clearing
        Optional<Cart> cartBefore = cartRepository.findByUserId(userId);
        assertTrue(cartBefore.isPresent());
        assertFalse(cartBefore.get().getItems().isEmpty());

        // Act
        cartService.clearCart(userId);

        // Assert
        Optional<Cart> cartAfter = cartRepository.findByUserId(userId);
        assertTrue(cartAfter.isEmpty());
    }
}

