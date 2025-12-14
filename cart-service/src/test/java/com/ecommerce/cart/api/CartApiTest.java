package com.ecommerce.cart.api;

import com.ecommerce.cart.config.TestSecurityConfig;
import com.ecommerce.cart.dto.CartItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.ecommerce.cart.config.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = TestSecurityConfig.class)
public class CartApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private CartRepository cartRepository;

    private String customerId;
    private Cart testCart;
    private CartItem testItem;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID().toString();
        
        // Setup test data
        testItem = new CartItem();
        testItem.setId("item-1");
        testItem.setProductId(UUID.randomUUID());
        testItem.setProductName("Test Product");
        testItem.setUnitPrice(BigDecimal.valueOf(19.99));
        testItem.setQuantity(2);
        testItem.setItemTotal(BigDecimal.valueOf(39.98));
        
        testCart = Cart.builder()
                .id("cart-1")
                .userId(UUID.fromString(customerId))
                .items(new ArrayList<>()) // Initialize the items list
                .build();
        testCart.addItem(testItem);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCart_WithValidUser_ShouldReturnCart() throws Exception {
        // Arrange
        when(cartService.getOrCreateCart(any(UUID.class))).thenReturn(createTestCartResponse());

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .header("X-User-Id", customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.getId()))
                .andExpect(jsonPath("$.userId").value(customerId))
                .andExpect(jsonPath("$.items[0].productId").value(testItem.getProductId().toString()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void addItemToCart_WithValidData_ShouldReturnUpdatedCart() throws Exception {
        // Arrange
        CartItemRequest request = new CartItemRequest(testItem.getProductId(), 1);
        when(cartService.addItemToCart(any(UUID.class), any(CartItemRequest.class)))
            .thenReturn(createTestCartResponse());

        // Act & Assert
        mockMvc.perform(post("/api/cart/items")
                .header("X-User-Id", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.getId()))
                .andExpect(jsonPath("$.items[0].productId").value(testItem.getProductId().toString()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateCartItem_WithValidData_ShouldReturnUpdatedCart() throws Exception {
        // Arrange
        int newQuantity = 3;
        when(cartService.updateCartItem(any(UUID.class), anyString(), anyInt()))
            .thenReturn(createTestCartResponse());

        // Act & Assert
        mockMvc.perform(put("/api/cart/items/{itemId}", testItem.getId())
                .header("X-User-Id", customerId)
                .param("quantity", String.valueOf(newQuantity))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void removeItemFromCart_WithValidItemId_ShouldReturnUpdatedCart() throws Exception {
        // Arrange
        when(cartService.removeItemFromCart(any(UUID.class), anyString()))
            .thenReturn(createTestCartResponse());

        // Act & Assert
        mockMvc.perform(delete("/api/cart/items/{itemId}", testItem.getId())
                .header("X-User-Id", customerId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.getId()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void clearCart_WithValidUser_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(cartService).clearCart(any(UUID.class));

        // Act & Assert
        mockMvc.perform(delete("/api/cart")
                .header("X-User-Id", customerId))
                .andExpect(status().isNoContent());
    }

    private CartResponse createTestCartResponse() {
        // Calculate values that would normally be calculated in the service
        BigDecimal subtotal = testItem.getItemTotal();
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax for testing
        BigDecimal shippingFee = BigDecimal.valueOf(9.99);
        BigDecimal total = subtotal.add(tax).add(shippingFee);
        
        return CartResponse.builder()
                .id(testCart.getId())
                .userId(testCart.getUserId())
                .items(Collections.singletonList(convertToDto(testItem)))
                .totalItems(1)
                .subtotal(subtotal)
                .tax(tax)
                .shippingFee(shippingFee)
                .total(total)
                .build();
    }

    private CartResponse.CartItemDto convertToDto(CartItem item) {
        return CartResponse.CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImage(item.getProductImage())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .itemTotal(item.getItemTotal())
                .inStock(true)
                .availableStock(10) // Assuming 10 items available for testing
                .build();
    }
}
