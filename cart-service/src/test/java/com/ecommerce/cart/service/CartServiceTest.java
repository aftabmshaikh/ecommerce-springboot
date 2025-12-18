package com.ecommerce.cart.service;

import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.dto.CartItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.exception.CartNotFoundException;
import com.ecommerce.cart.exception.ProductNotAvailableException;
import com.ecommerce.cart.mapper.CartMapper;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private UUID userId;
    private UUID productId;
    private Cart cart;
    private CartItem cartItem;
    private CartItemRequest cartItemRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        cartItem = new CartItem();
        cartItem.setId("item-1");
        cartItem.setProductId(productId);
        cartItem.setProductName("Test Product");
        cartItem.setUnitPrice(BigDecimal.valueOf(19.99));
        cartItem.setQuantity(2);
        cartItem.setItemTotal(BigDecimal.valueOf(39.98));

        cart = Cart.builder()
                .id("cart-1")
                .userId(userId)
                .items(new ArrayList<>())
                .build();
        cart.addItem(cartItem);

        cartItemRequest = new CartItemRequest(productId, 1);
    }

    @Test
    void getOrCreateCart_WithExistingCart_ShouldReturnCart() {
        // Arrange
        CartResponse cartResponse = createCartResponse();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartResponse);

        // Act
        CartResponse result = cartService.getOrCreateCart(userId);

        // Assert
        assertNotNull(result);
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getOrCreateCart_WithNonExistentCart_ShouldCreateNewCart() {
        // Arrange
        CartResponse cartResponse = createCartResponse();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartMapper.toDto(any(Cart.class))).thenReturn(cartResponse);

        // Act
        CartResponse result = cartService.getOrCreateCart(userId);

        // Assert
        assertNotNull(result);
        verify(cartRepository, times(1)).findByUserId(userId);
    }

    @Test
    void addItemToCart_WithNewItem_ShouldAddItem() {
        // Arrange
        UUID newProductId = UUID.randomUUID();
        CartItemRequest newItemRequest = new CartItemRequest(newProductId, 1);
        ProductServiceClient.ProductDto productDto = new ProductServiceClient.ProductDto(
                newProductId, "New Product", "Description", "http://example.com/image.jpg", 25.99, 100
        );

        when(productServiceClient.isInStock(newProductId, 1)).thenReturn(true);
        when(productServiceClient.getProductById(newProductId)).thenReturn(Optional.of(productDto));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(createCartResponse());

        // Act
        CartResponse result = cartService.addItemToCart(userId, newItemRequest);

        // Assert
        assertNotNull(result);
        verify(productServiceClient, times(1)).isInStock(newProductId, 1);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart_WithExistingItem_ShouldUpdateQuantity() {
        // Arrange
        when(productServiceClient.isInStock(productId, 1)).thenReturn(true);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(createCartResponse());

        // Act
        CartResponse result = cartService.addItemToCart(userId, cartItemRequest);

        // Assert
        assertNotNull(result);
        verify(productServiceClient, times(1)).isInStock(productId, 1);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart_WithUnavailableProduct_ShouldThrowException() {
        // Arrange
        when(productServiceClient.isInStock(productId, 1)).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotAvailableException.class, 
                () -> cartService.addItemToCart(userId, cartItemRequest));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItem_WithValidQuantity_ShouldUpdateItem() {
        // Arrange
        String itemId = "item-1";
        int newQuantity = 5;
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productServiceClient.isInStock(productId, newQuantity)).thenReturn(true);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(createCartResponse());

        // Act
        CartResponse result = cartService.updateCartItem(userId, itemId, newQuantity);

        // Assert
        assertNotNull(result);
        verify(productServiceClient, times(1)).isInStock(productId, newQuantity);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void updateCartItem_WithZeroQuantity_ShouldRemoveItem() {
        // Arrange
        String itemId = "item-1";
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        // When last item is removed, cart is deleted and new cart response is created
        doNothing().when(cartRepository).delete(any(Cart.class));
        when(cartMapper.toDto(any(Cart.class))).thenReturn(createCartResponse());

        // Act
        CartResponse result = cartService.updateCartItem(userId, itemId, 0);

        // Assert
        assertNotNull(result);
        // When last item is removed, cart is deleted (not saved)
        verify(cartRepository, times(1)).delete(any(Cart.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItem_WithNonExistentCart_ShouldThrowException() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartNotFoundException.class, 
                () -> cartService.updateCartItem(userId, "item-1", 5));
    }

    @Test
    void removeItemFromCart_WithValidItem_ShouldRemoveItem() {
        // Arrange - Add a second item so cart doesn't become empty
        CartItem secondItem = new CartItem();
        secondItem.setId("item-2");
        secondItem.setProductId(UUID.randomUUID());
        secondItem.setProductName("Second Product");
        secondItem.setUnitPrice(BigDecimal.valueOf(29.99));
        secondItem.setQuantity(1);
        cart.addItem(secondItem);
        
        String itemId = "item-1";
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(createCartResponse());

        // Act
        CartResponse result = cartService.removeItemFromCart(userId, itemId);

        // Assert
        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verify(cartRepository, never()).delete(any(Cart.class));
    }

    @Test
    void removeItemFromCart_WithLastItem_ShouldDeleteCart() {
        // Arrange
        String itemId = "item-1";
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        doNothing().when(cartRepository).delete(any(Cart.class));
        when(cartMapper.toDto(any(Cart.class))).thenReturn(createCartResponse());

        // Act
        CartResponse result = cartService.removeItemFromCart(userId, itemId);

        // Assert
        assertNotNull(result);
        verify(cartRepository, times(1)).delete(any(Cart.class));
    }

    @Test
    void removeItemFromCart_WithNonExistentCart_ShouldThrowException() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartNotFoundException.class, 
                () -> cartService.removeItemFromCart(userId, "item-1"));
    }

    @Test
    void clearCart_WithValidUser_ShouldDeleteCart() {
        // Arrange
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        doNothing().when(cartRepository).delete(any(Cart.class));

        // Act
        cartService.clearCart(userId);

        // Assert
        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).delete(cart);
        verify(cartRepository, never()).deleteByUserId(any(UUID.class));
    }

    private CartResponse createCartResponse() {
        return CartResponse.builder()
                .id("cart-1")
                .userId(userId)
                .items(Collections.emptyList())
                .totalItems(0)
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();
    }
}

