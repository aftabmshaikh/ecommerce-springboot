package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductStockUpdateRequest;
import com.ecommerce.order.exception.ProductServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Slf4j
@Component
public class ProductServiceClientFallback implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {
            private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductServiceClientFallback.class);
            
            @Override
            public ResponseEntity<Map<UUID, Boolean>> checkStockAvailability(List<Map<String, Object>> items) {
                log.error("Fallback: Error checking stock availability: {}", cause.getMessage());
                // Return a map with all items as out of stock
                Map<UUID, Boolean> result = new HashMap<>();
                for (Map<String, Object> item : items) {
                    try {
                        UUID productId = UUID.fromString(item.get("productId").toString());
                        result.put(productId, false);
                    } catch (Exception e) {
                        log.error("Error processing product ID: {}", item.get("productId"), e);
                    }
                }
                return ResponseEntity.ok(result);
            }
            
            @Override
            public ResponseEntity<Void> updateInventory(List<Map<String, Object>> inventoryUpdates) {
                log.error("Fallback: Error updating inventory: {}", cause.getMessage());
                // Log the failed inventory updates for manual processing
                log.warn("Failed inventory updates: {}", inventoryUpdates);
                return ResponseEntity.internalServerError().build();
            }
            @Override
            public ResponseEntity<Boolean> checkProductStock(UUID productId, int quantity) {
                log.error("Fallback: Error checking product stock for product {}: {}", 
                        productId, cause.getMessage());
                // In a real scenario, you might want to return false to fail fast
                // or true to continue with the order (at your own risk)
                return ResponseEntity.ok(false);
            }

            @Override
            public ResponseEntity<Void> updateProductStocks(Map<UUID, Integer> productStocks) {
                log.error("Fallback: Error updating product stocks: {}", cause.getMessage());
                // In a real scenario, you might want to throw an exception
                // or handle this differently based on your requirements
                return ResponseEntity.internalServerError().build();
            }

            @Override
            public ResponseEntity<Boolean> isProductAvailable(UUID productId) {
                log.error("Fallback: Error checking product availability for product {}: {}", 
                        productId, cause.getMessage());
                return ResponseEntity.ok(false);
            }

            @Override
            public ResponseEntity<Void> updateProductStock(ProductStockUpdateRequest request) {
                log.error("Fallback: Error updating product stock for product {}: {}", 
                        request.getProductId(), cause.getMessage());
                return ResponseEntity.internalServerError().build();
            }
        };
    }
}
