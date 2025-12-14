package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "APIs for managing inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new inventory item")
    public InventoryResponse createInventoryItem(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.createInventoryItem(request);
    }

    @GetMapping("/{skuCode}")
    @Operation(summary = "Get inventory by SKU code")
    public InventoryResponse getInventoryBySkuCode(@PathVariable String skuCode) {
        return inventoryService.getInventoryBySkuCode(skuCode);
    }

    @PostMapping("/adjust")
    @Operation(summary = "Adjust inventory stock level")
    public InventoryResponse adjustStock(@Valid @RequestBody InventoryRequest.StockAdjustment adjustment) {
        return inventoryService.adjustStock(adjustment);
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for an order")
    public InventoryResponse reserveStock(@Valid @RequestBody InventoryRequest.ReservationRequest request) {
        return inventoryService.reserveStock(request);
    }

    @PostMapping("/release")
    @Operation(summary = "Release reserved stock")
    public InventoryResponse releaseStock(@Valid @RequestBody InventoryRequest.ReleaseRequest request) {
        return inventoryService.releaseStock(request);
    }

    @GetMapping("/status/{skuCode}")
    @Operation(summary = "Check inventory status by SKU code")
    public InventoryResponse.InventoryStatus checkInventoryStatus(@PathVariable String skuCode) {
        return inventoryService.checkInventoryStatus(skuCode);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get all low stock items")
    public List<InventoryResponse.StockLevel> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }

    @PostMapping("/{skuCode}/restock")
    @Operation(summary = "Process restock for an item")
    public void processRestock(
            @PathVariable String skuCode,
            @RequestParam int quantity) {
        inventoryService.processRestock(skuCode, quantity);
    }

    @PostMapping("/{skuCode}/consume")
    @Operation(summary = "Consume reserved stock")
    public InventoryResponse consumeReservedStock(
            @PathVariable String skuCode,
            @RequestParam int quantity,
            @RequestParam String reservationId) {
        return inventoryService.consumeReservedStock(skuCode, quantity, reservationId);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public String health() {
        return "Inventory Service is healthy";
    }
}
