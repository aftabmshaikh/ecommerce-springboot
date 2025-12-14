package com.ecommerce.order.client;

import com.ecommerce.order.config.FeignClientConfig;
import com.ecommerce.order.dto.ProductStockUpdateRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(
    name = "product-service",
    url = "${product-service.url}",
    configuration = FeignClientConfig.class,
    fallback = ProductServiceClientFallback.class
)
public interface ProductServiceClient {

    @GetMapping("/api/products/{productId}/stock")
    @CircuitBreaker(name = "productService", fallbackMethod = "checkProductStockFallback")
    ResponseEntity<Boolean> checkProductStock(
            @PathVariable UUID productId,
            @RequestParam int quantity);

    @PostMapping("/api/products/batch/stock")
    @CircuitBreaker(name = "productService", fallbackMethod = "updateProductStocksFallback")
    ResponseEntity<Void> updateProductStocks(
            @RequestBody Map<UUID, Integer> productStocks);
            
    @GetMapping("/api/products/{productId}/available")
    @CircuitBreaker(name = "productService", fallbackMethod = "isProductAvailableFallback")
    ResponseEntity<Boolean> isProductAvailable(@PathVariable UUID productId);
    
    @PutMapping("/api/products/stock")
    @CircuitBreaker(name = "productService", fallbackMethod = "updateProductStockFallback")
    ResponseEntity<Void> updateProductStock(@RequestBody ProductStockUpdateRequest request);
    
    @PostMapping("/api/products/check-stock")
    @CircuitBreaker(name = "productService", fallbackMethod = "checkStockAvailabilityFallback")
    ResponseEntity<Map<UUID, Boolean>> checkStockAvailability(@RequestBody List<Map<String, Object>> items);
    
    @PostMapping("/api/products/update-inventory")
    @CircuitBreaker(name = "productService", fallbackMethod = "updateInventoryFallback")
    ResponseEntity<Void> updateInventory(@RequestBody List<Map<String, Object>> inventoryUpdates);

    default ResponseEntity<Boolean> checkProductStockFallback(UUID productId, int quantity, Throwable t) {
        // Fallback logic: Assume product is out of stock in case of failure
        return ResponseEntity.ok(false);
    }

    default ResponseEntity<Void> updateProductStocksFallback(Map<UUID, Integer> productStocks, Throwable t) {
        // Fallback logic: Log the error and return success to continue the order flow
        // In a real scenario, you might want to implement a retry mechanism or notify admins
        return ResponseEntity.ok().build();
    }
    
    default ResponseEntity<Boolean> isProductAvailableFallback(UUID productId, Throwable t) {
        // Fallback logic: Assume product is not available in case of failure
        return ResponseEntity.ok(false);
    }
    
    default ResponseEntity<Void> updateProductStockFallback(ProductStockUpdateRequest request, Throwable t) {
        // Fallback logic: Log the error and return success to continue the order flow
        // In a real scenario, you might want to implement a retry mechanism or notify admins
        return ResponseEntity.ok().build();
    }
    
    default ResponseEntity<Map<UUID, Boolean>> checkStockAvailabilityFallback(List<Map<String, Object>> items, Throwable t) {
        // Fallback logic: Assume all items are in stock to continue the order flow
        // In a real scenario, you might want to implement a different strategy
        Map<UUID, Boolean> result = new HashMap<>();
        items.forEach(item -> {
            UUID productId = UUID.fromString(item.get("productId").toString());
            result.put(productId, true);
        });
        return ResponseEntity.ok(result);
    }
    
    default ResponseEntity<Void> updateInventoryFallback(List<Map<String, Object>> inventoryUpdates, Throwable t) {
        // Fallback logic: Log the error and return success to continue the order flow
        // In a real scenario, you might want to implement a retry mechanism or notify admins
        return ResponseEntity.ok().build();
    }
}
