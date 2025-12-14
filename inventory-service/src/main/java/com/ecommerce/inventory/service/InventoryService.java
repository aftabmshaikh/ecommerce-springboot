package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.InventoryItemNotFoundException;
import com.ecommerce.inventory.exception.InvalidInventoryOperationException;
import com.ecommerce.inventory.model.InventoryItem;
import com.ecommerce.inventory.repository.InventoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    @Transactional
    public InventoryResponse createInventoryItem(InventoryRequest request) {
        log.info("Creating inventory item for product: {}", request.getProductId());
        
        // Check if inventory item already exists for this product
        if (inventoryRepository.existsBySkuCode(request.getSkuCode())) {
            throw new InvalidInventoryOperationException("Inventory item already exists for SKU: " + request.getSkuCode());
        }

        InventoryItem item = InventoryItem.builder()
                .productId(request.getProductId())
                .skuCode(request.getSkuCode())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .reservedQuantity(request.getReservedQuantity() != null ? request.getReservedQuantity() : 0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .restockThreshold(request.getRestockThreshold() != null ? request.getRestockThreshold() : 20)
                .unitCost(request.getUnitCost())
                .locationCode(request.getLocationCode())
                .binLocation(request.getBinLocation())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        item.calculateAvailableQuantity();
        InventoryItem savedItem = inventoryRepository.save(item);
        
        publishInventoryEvent("inventory-created", savedItem);
        
        return InventoryResponse.fromEntity(savedItem);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryBySkuCode(String skuCode) {
        log.debug("Fetching inventory for SKU: {}", skuCode);
        InventoryItem item = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
        return InventoryResponse.fromEntity(item);
    }

    @Transactional
    @Retry(name = "inventoryService", fallbackMethod = "adjustStockFallback")
    public InventoryResponse adjustStock(InventoryRequest.StockAdjustment adjustment) {
        log.info("Adjusting stock for SKU: {} by {}", adjustment.getSkuCode(), adjustment.getAdjustment());
        
        InventoryItem item = inventoryRepository.findBySkuCode(adjustment.getSkuCode())
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + adjustment.getSkuCode()));
        
        int newQuantity = item.getQuantity() + adjustment.getAdjustment();
        if (newQuantity < 0) {
            throw new InvalidInventoryOperationException("Insufficient quantity for adjustment");
        }
        
        int rowsUpdated = inventoryRepository.adjustInventory(adjustment.getSkuCode(), adjustment.getAdjustment());
        if (rowsUpdated == 0) {
            throw new InvalidInventoryOperationException("Failed to adjust inventory for SKU: " + adjustment.getSkuCode());
        }
        
        // Refresh the item to get the latest state
        InventoryItem updatedItem = inventoryRepository.findBySkuCode(adjustment.getSkuCode())
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found after update for SKU: " + adjustment.getSkuCode()));
        
        publishInventoryEvent("stock-adjusted", updatedItem, Map.of(
                "adjustment", adjustment.getAdjustment(),
                "reason", adjustment.getReason(),
                "referenceId", adjustment.getReferenceId()
        ));
        
        return InventoryResponse.fromEntity(updatedItem);
    }

    @Transactional
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "reserveStockFallback")
    public InventoryResponse reserveStock(InventoryRequest.ReservationRequest request) {
        log.info("Reserving {} units of SKU: {} for reservation ID: {}", 
                request.getQuantity(), request.getSkuCode(), request.getReservationId());
        
        int rowsUpdated = inventoryRepository.reserveStock(request.getSkuCode(), request.getQuantity());
        if (rowsUpdated == 0) {
            throw new InsufficientStockException("Insufficient stock available for SKU: " + request.getSkuCode());
        }
        
        InventoryItem item = inventoryRepository.findBySkuCode(request.getSkuCode())
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + request.getSkuCode()));
        
        publishInventoryEvent("stock-reserved", item, Map.of(
                "reservationId", request.getReservationId(),
                "quantityReserved", request.getQuantity(),
                "notes", request.getNotes()
        ));
        
        return InventoryResponse.fromEntity(item);
    }

    @Transactional
    public InventoryResponse releaseStock(InventoryRequest.ReleaseRequest request) {
        log.info("Releasing {} units of SKU: {} for reservation ID: {}", 
                request.getQuantity(), request.getSkuCode(), request.getReservationId());
        
        int rowsUpdated = inventoryRepository.releaseStock(request.getSkuCode(), request.getQuantity());
        if (rowsUpdated == 0) {
            throw new InvalidInventoryOperationException("Failed to release stock for SKU: " + request.getSkuCode());
        }
        
        InventoryItem item = inventoryRepository.findBySkuCode(request.getSkuCode())
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + request.getSkuCode()));
        
        publishInventoryEvent("stock-released", item, Map.of(
                "reservationId", request.getReservationId(),
                "quantityReleased", request.getQuantity(),
                "reason", request.getReason()
        ));
        
        return InventoryResponse.fromEntity(item);
    }

    @Transactional
    public InventoryResponse consumeReservedStock(String skuCode, int quantity, String reservationId) {
        log.info("Consuming {} reserved units of SKU: {} for reservation ID: {}", 
                quantity, skuCode, reservationId);
        
        int rowsUpdated = inventoryRepository.consumeReservedStock(skuCode, quantity);
        if (rowsUpdated == 0) {
            throw new InvalidInventoryOperationException("Failed to consume reserved stock for SKU: " + skuCode);
        }
        
        InventoryItem item = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
        
        publishInventoryEvent("reserved-stock-consumed", item, Map.of(
                "reservationId", reservationId,
                "quantityConsumed", quantity
        ));
        
        return InventoryResponse.fromEntity(item);
    }

    @Transactional(readOnly = true)
    public InventoryResponse.InventoryStatus checkInventoryStatus(String skuCode) {
        return inventoryRepository.getInventoryStatus(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse.StockLevel> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(item -> InventoryResponse.StockLevel.builder()
                        .skuCode(item.getSkuCode())
                        .currentLevel(item.getAvailableQuantity())
                        .lowStockThreshold(item.getLowStockThreshold())
                        .restockThreshold(item.getRestockThreshold())
                        .status(item.isLowStock() ? "LOW_STOCK" : "IN_STOCK")
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void processRestock(String skuCode, int quantity) {
        log.info("Processing restock of {} units for SKU: {}", quantity, skuCode);
        
        InventoryItem item = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
        
        item.adjustInventory(quantity);
        item.setLastRestockedDate(LocalDateTime.now());
        item.setNextRestockDate(LocalDateTime.now().plusWeeks(2)); // Default to 2 weeks for next restock
        
        inventoryRepository.save(item);
        
        publishInventoryEvent("inventory-restocked", item, Map.of(
                "quantityAdded", quantity,
                "newQuantity", item.getQuantity()
        ));
    }

    // Fallback methods
    private InventoryResponse adjustStockFallback(InventoryRequest.StockAdjustment adjustment, Exception e) {
        log.error("Failed to adjust stock for SKU: {}. Error: {}", adjustment.getSkuCode(), e.getMessage());
        throw new InvalidInventoryOperationException("Failed to adjust stock. Please try again later.");
    }
    
    private InventoryResponse reserveStockFallback(InventoryRequest.ReservationRequest request, Exception e) {
        log.error("Failed to reserve stock for SKU: {}. Error: {}", request.getSkuCode(), e.getMessage());
        throw new InsufficientStockException("Failed to reserve stock. Please try again later.");
    }

    // Helper methods
    private void publishInventoryEvent(String eventType, InventoryItem item) {
        publishInventoryEvent(eventType, item, null);
    }
    
    private void publishInventoryEvent(String eventType, InventoryItem item, Map<String, Object> additionalData) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("timestamp", LocalDateTime.now().toString());
            event.put("inventoryId", item.getId());
            event.put("productId", item.getProductId());
            event.put("skuCode", item.getSkuCode());
            event.put("quantity", item.getQuantity());
            event.put("availableQuantity", item.getAvailableQuantity());
            event.put("reservedQuantity", item.getReservedQuantity());
            
            if (additionalData != null) {
                event.putAll(additionalData);
            }
            
            kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, eventType, event);
            log.debug("Published {} event for SKU: {}", eventType, item.getSkuCode());
        } catch (Exception e) {
            log.error("Failed to publish inventory event: {}", e.getMessage(), e);
            // We don't want to fail the main operation if event publishing fails
        }
    }
}
