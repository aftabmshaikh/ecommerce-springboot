package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotBlank(message = "SKU code is required")
    private String skuCode;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;
    
    @Min(value = 0, message = "Reserved quantity must be greater than or equal to 0")
    private Integer reservedQuantity;
    
    @Min(value = 0, message = "Low stock threshold must be greater than or equal to 0")
    private Integer lowStockThreshold;
    
    @Min(value = 0, message = "Restock threshold must be greater than or equal to 0")
    private Integer restockThreshold;
    
    @DecimalMin(value = "0.0", message = "Unit cost must be greater than or equal to 0")
    private BigDecimal unitCost;
    
    private String locationCode;
    private String binLocation;
    
    private Boolean isActive;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockAdjustment {
        @NotBlank(message = "SKU code is required")
        private String skuCode;
        
        @NotNull(message = "Adjustment quantity is required")
        private Integer adjustment;
        
        private String reason;
        private String referenceId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationRequest {
        @NotBlank(message = "SKU code is required")
        private String skuCode;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        private Integer quantity;
        
        @NotBlank(message = "Reservation ID is required")
        private String reservationId;
        
        private String notes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReleaseRequest {
        @NotBlank(message = "SKU code is required")
        private String skuCode;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        private Integer quantity;
        
        @NotBlank(message = "Reservation ID is required")
        private String reservationId;
        
        private String reason;
    }
}
