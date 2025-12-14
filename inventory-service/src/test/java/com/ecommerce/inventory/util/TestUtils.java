package com.ecommerce.inventory.util;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.model.InventoryItem;

import java.math.BigDecimal;
import java.util.UUID;

public class TestUtils {

    public static InventoryItem createTestInventoryItem() {
        return InventoryItem.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .skuCode("SKU-12345")
                .quantity(100)
                .reservedQuantity(20)
                .lowStockThreshold(10)
                .restockThreshold(20)
                .unitCost(new BigDecimal("9.99"))
                .locationCode("WH-01")
                .binLocation("A1-01-01")
                .isActive(true)
                .version(0L)  // Initialize version for new entities
                .build();
    }

    public static InventoryRequest createTestInventoryRequest() {
        return InventoryRequest.builder()
                .productId(UUID.randomUUID())
                .skuCode("SKU-12345")
                .quantity(100)
                .reservedQuantity(20)
                .lowStockThreshold(10)
                .restockThreshold(20)
                .unitCost(new BigDecimal("9.99"))
                .locationCode("WH-01")
                .binLocation("A1-01-01")
                .isActive(true)
                .build();
    }

    public static InventoryRequest.StockAdjustment createTestStockAdjustment(String skuCode, int adjustment) {
        return InventoryRequest.StockAdjustment.builder()
                .skuCode(skuCode)
                .adjustment(adjustment)
                .reason("Test adjustment")
                .referenceId("REF-123")
                .build();
    }

    public static InventoryRequest.ReservationRequest createTestReservationRequest(String skuCode, int quantity) {
        return InventoryRequest.ReservationRequest.builder()
                .skuCode(skuCode)
                .quantity(quantity)
                .reservationId("RES-" + UUID.randomUUID())
                .notes("Test reservation")
                .build();
    }

    public static InventoryRequest.ReleaseRequest createTestReleaseRequest(String skuCode, int quantity, String reservationId) {
        return InventoryRequest.ReleaseRequest.builder()
                .skuCode(skuCode)
                .quantity(quantity)
                .reservationId(reservationId)
                .reason("Test release")
                .build();
    }
}
