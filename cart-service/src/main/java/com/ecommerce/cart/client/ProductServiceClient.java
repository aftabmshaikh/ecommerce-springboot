package com.ecommerce.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    Optional<ProductDto> getProductById(@PathVariable("id") UUID productId);
    
    @GetMapping("/api/products/{id}/stock")
    boolean isInStock(
            @PathVariable("id") UUID productId,
            @RequestParam("quantity") int quantity
    );
    
    record ProductDto(
            UUID id,
            String name,
            String description,
            String imageUrl,
            double price,
            int stock
    ) {}
}
