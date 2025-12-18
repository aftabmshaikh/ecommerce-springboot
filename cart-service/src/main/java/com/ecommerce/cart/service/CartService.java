package com.ecommerce.cart.service;

import com.ecommerce.cart.client.ProductServiceClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.ecommerce.cart.dto.CartItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.exception.CartNotFoundException;
import com.ecommerce.cart.exception.ProductNotAvailableException;
import com.ecommerce.cart.mapper.CartMapper;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .map(cartMapper::toDto)
                .orElseGet(() -> createNewCart(userId));
    }

    @Transactional
    public CartResponse addItemToCart(UUID userId, CartItemRequest request) {
        // Check product availability
        validateProductAvailability(request.getProductId(), request.getQuantity());
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartEntity(userId));
        
        // Check if item already exists in cart
        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                        () -> addNewCartItem(cart, request)
                );
        
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateCartItem(UUID userId, String itemId, int quantity) {
        if (quantity <= 0) {
            return removeItemFromCart(userId, itemId);
        }
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    // Check product availability before updating quantity
                    validateProductAvailability(item.getProductId(), quantity);
                    item.setQuantity(quantity);
                });
        
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItemFromCart(UUID userId, String itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            return createNewCart(userId);
        }
        
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(UUID userId) {
        // Fetch the cart first to trigger cascade deletion of cart_items
        // Direct JPQL DELETE doesn't trigger cascade, so we need to delete the entity
        cartRepository.findByUserId(userId)
                .ifPresent(cartRepository::delete);
    }

    private Cart createNewCartEntity(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cart;
    }

    private CartResponse createNewCart(UUID userId) {
        Cart newCart = createNewCartEntity(userId);
        return cartMapper.toDto(newCart);
    }

    private void addNewCartItem(Cart cart, CartItemRequest request) {
        // In a real application, fetch product details from product service
        var product = productServiceClient.getProductById(request.getProductId())
                .orElseThrow(() -> new ProductNotAvailableException("Product not found"));
        
        CartItem item = new CartItem();
        item.setProductId(request.getProductId());
        item.setProductName(product.name());
        item.setProductImage(product.imageUrl());
        item.setUnitPrice(BigDecimal.valueOf(product.price()).setScale(2, RoundingMode.HALF_UP));
        item.setQuantity(request.getQuantity());
        
        cart.addItem(item);
    }

    private void validateProductAvailability(UUID productId, int quantity) {
        boolean isAvailable = productServiceClient.isInStock(productId, quantity);
        if (!isAvailable) {
            throw new ProductNotAvailableException("Product is not available in the requested quantity");
        }
    }
}
