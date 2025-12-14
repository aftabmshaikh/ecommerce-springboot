package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @CircuitBreaker(name = "product-service", fallbackMethod = "getProductFallback")
    @Retry(name = "product-service")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or description")
    public List<ProductResponse> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ProductResponse updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product")
    public void deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
    }

    @PostMapping("/{id}/stock")
    @Operation(summary = "Update product stock")
    public ProductResponse updateStock(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        return productService.updateStock(id, quantity);
    }

    @PostMapping("/batch")
    @Operation(summary = "Get multiple products by IDs")
    public List<ProductResponse> getProductsByIds(@RequestBody List<UUID> productIds) {
        return productService.getProductsByIds(productIds);
    }

    @GetMapping("/{id}/stock")
    @Operation(summary = "Check if product is in stock")
    public ResponseEntity<Boolean> isInStock(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int quantity) {
        boolean inStock = productService.isInStock(id, quantity);
        return ResponseEntity.ok(inStock);
    }

    // Fallback method for circuit breaker
    public ProductResponse getProductFallback(UUID id, Exception e) {
        // Return a default response or fetch from cache
        return ProductResponse.builder()
                .id(id)
                .name("Product information is not available at the moment")
                .description("Please try again later")
                .price(BigDecimal.ZERO)
                .stockQuantity(0)
                .active(false)
                .build();
    }
}
