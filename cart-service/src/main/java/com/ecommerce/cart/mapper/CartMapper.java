package com.ecommerce.cart.mapper;

import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cart))")
    @Mapping(target = "tax", expression = "java(calculateTax(cart))")
    @Mapping(target = "shippingFee", expression = "java(calculateShippingFee(cart))")
    @Mapping(target = "total", expression = "java(calculateTotal(cart))")
    CartResponse toDto(Cart cart);

    default int calculateTotalItems(Cart cart) {
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal calculateTax(Cart cart) {
        // In a real application, you would calculate tax based on the user's location and tax rules
        BigDecimal subtotal = calculateSubtotal(cart);
        return subtotal.multiply(new BigDecimal("0.10")); // 10% tax for example
    }

    default BigDecimal calculateShippingFee(Cart cart) {
        // In a real application, you would calculate shipping based on the items and destination
        return new BigDecimal("9.99"); // Flat rate for example
    }

    default BigDecimal calculateTotal(Cart cart) {
        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal tax = calculateTax(cart);
        BigDecimal shippingFee = calculateShippingFee(cart);
        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        
        return subtotal.add(tax).add(shippingFee).subtract(discount);
    }
}
