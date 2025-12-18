package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.InventoryItemNotFoundException;
import com.ecommerce.inventory.exception.InvalidInventoryOperationException;
import com.ecommerce.inventory.model.InventoryItem;
import com.ecommerce.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID productId;
    private String skuCode;
    private InventoryItem inventoryItem;
    private InventoryRequest inventoryRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        skuCode = "SKU-001";

        inventoryItem = InventoryItem.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .skuCode(skuCode)
                .quantity(100)
                .reservedQuantity(10)
                .lowStockThreshold(10)
                .restockThreshold(20)
                .unitCost(BigDecimal.valueOf(15.99))
                .locationCode("WH-001")
                .binLocation("A1-B2")
                .isActive(true)
                .build();
        inventoryItem.calculateAvailableQuantity();

        inventoryRequest = InventoryRequest.builder()
                .productId(productId)
                .skuCode(skuCode)
                .quantity(100)
                .lowStockThreshold(10)
                .restockThreshold(20)
                .unitCost(BigDecimal.valueOf(15.99))
                .locationCode("WH-001")
                .isActive(true)
                .build();
    }

    @Test
    void createInventoryItem_WithValidRequest_ShouldReturnInventoryResponse() {
        // Arrange
        when(inventoryRepository.existsBySkuCode(skuCode)).thenReturn(false);
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem);

        // Act
        InventoryResponse result = inventoryService.createInventoryItem(inventoryRequest);

        // Assert
        assertNotNull(result);
        assertEquals(skuCode, result.getSkuCode());
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void createInventoryItem_WithExistingSku_ShouldThrowException() {
        // Arrange
        when(inventoryRepository.existsBySkuCode(skuCode)).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidInventoryOperationException.class, 
                () -> inventoryService.createInventoryItem(inventoryRequest));
        verify(inventoryRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void getInventoryBySkuCode_WithValidSku_ShouldReturnInventoryResponse() {
        // Arrange
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));

        // Act
        InventoryResponse result = inventoryService.getInventoryBySkuCode(skuCode);

        // Assert
        assertNotNull(result);
        assertEquals(skuCode, result.getSkuCode());
        verify(inventoryRepository, times(1)).findBySkuCode(skuCode);
    }

    @Test
    void getInventoryBySkuCode_WithInvalidSku_ShouldThrowException() {
        // Arrange
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InventoryItemNotFoundException.class, 
                () -> inventoryService.getInventoryBySkuCode(skuCode));
    }

    @Test
    void adjustStock_WithValidAdjustment_ShouldUpdateStock() {
        // Arrange
        InventoryRequest.StockAdjustment adjustment = new InventoryRequest.StockAdjustment();
        adjustment.setSkuCode(skuCode);
        adjustment.setAdjustment(10);
        adjustment.setReason("Restock");
        adjustment.setReferenceId("REF-001");

        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));
        when(inventoryRepository.adjustInventory(skuCode, 10)).thenReturn(1);
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));

        // Act
        InventoryResponse result = inventoryService.adjustStock(adjustment);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).adjustInventory(skuCode, 10);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void adjustStock_WithInsufficientQuantity_ShouldThrowException() {
        // Arrange
        InventoryRequest.StockAdjustment adjustment = new InventoryRequest.StockAdjustment();
        adjustment.setSkuCode(skuCode);
        adjustment.setAdjustment(-200); // More than available

        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));

        // Act & Assert
        assertThrows(InvalidInventoryOperationException.class, 
                () -> inventoryService.adjustStock(adjustment));
        verify(inventoryRepository, never()).adjustInventory(anyString(), anyInt());
    }

    @Test
    void adjustStock_WithNonExistentSku_ShouldThrowException() {
        // Arrange
        InventoryRequest.StockAdjustment adjustment = new InventoryRequest.StockAdjustment();
        adjustment.setSkuCode(skuCode);
        adjustment.setAdjustment(10);

        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InventoryItemNotFoundException.class, 
                () -> inventoryService.adjustStock(adjustment));
    }

    @Test
    void reserveStock_WithValidRequest_ShouldReserveStock() {
        // Arrange
        InventoryRequest.ReservationRequest request = new InventoryRequest.ReservationRequest();
        request.setSkuCode(skuCode);
        request.setQuantity(5);
        request.setReservationId("RES-001");
        request.setNotes("Order reservation");

        when(inventoryRepository.reserveStock(skuCode, 5)).thenReturn(1);
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));

        // Act
        InventoryResponse result = inventoryService.reserveStock(request);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).reserveStock(skuCode, 5);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void reserveStock_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        InventoryRequest.ReservationRequest request = new InventoryRequest.ReservationRequest();
        request.setSkuCode(skuCode);
        request.setQuantity(200); // More than available

        when(inventoryRepository.reserveStock(skuCode, 200)).thenReturn(0);

        // Act & Assert
        assertThrows(InsufficientStockException.class, 
                () -> inventoryService.reserveStock(request));
    }

    @Test
    void releaseStock_WithValidRequest_ShouldReleaseStock() {
        // Arrange
        InventoryRequest.ReleaseRequest request = new InventoryRequest.ReleaseRequest();
        request.setSkuCode(skuCode);
        request.setQuantity(5);
        request.setReservationId("RES-001");
        request.setReason("Order cancelled");

        when(inventoryRepository.releaseStock(skuCode, 5)).thenReturn(1);
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));

        // Act
        InventoryResponse result = inventoryService.releaseStock(request);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).releaseStock(skuCode, 5);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void releaseStock_WithFailedRelease_ShouldThrowException() {
        // Arrange
        InventoryRequest.ReleaseRequest request = new InventoryRequest.ReleaseRequest();
        request.setSkuCode(skuCode);
        request.setQuantity(5);
        request.setReservationId("RES-001");

        when(inventoryRepository.releaseStock(skuCode, 5)).thenReturn(0);

        // Act & Assert
        assertThrows(InvalidInventoryOperationException.class, 
                () -> inventoryService.releaseStock(request));
    }

    @Test
    void consumeReservedStock_WithValidRequest_ShouldConsumeStock() {
        // Arrange
        String reservationId = "RES-001";
        int quantity = 5;

        when(inventoryRepository.consumeReservedStock(skuCode, quantity)).thenReturn(1);
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));

        // Act
        InventoryResponse result = inventoryService.consumeReservedStock(skuCode, quantity, reservationId);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).consumeReservedStock(skuCode, quantity);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void consumeReservedStock_WithFailedConsumption_ShouldThrowException() {
        // Arrange
        String reservationId = "RES-001";
        int quantity = 5;

        when(inventoryRepository.consumeReservedStock(skuCode, quantity)).thenReturn(0);

        // Act & Assert
        assertThrows(InvalidInventoryOperationException.class, 
                () -> inventoryService.consumeReservedStock(skuCode, quantity, reservationId));
    }

    @Test
    void checkInventoryStatus_WithValidSku_ShouldReturnStatus() {
        // Arrange
        InventoryResponse.InventoryStatus status = InventoryResponse.InventoryStatus.builder()
                .skuCode(skuCode)
                .availableQuantity(90)
                .inStock(true)
                .lowStock(false)
                .status("IN_STOCK")
                .build();

        when(inventoryRepository.getInventoryStatus(skuCode)).thenReturn(Optional.of(status));

        // Act
        InventoryResponse.InventoryStatus result = inventoryService.checkInventoryStatus(skuCode);

        // Assert
        assertNotNull(result);
        assertEquals(skuCode, result.getSkuCode());
        verify(inventoryRepository, times(1)).getInventoryStatus(skuCode);
    }

    @Test
    void checkInventoryStatus_WithInvalidSku_ShouldThrowException() {
        // Arrange
        when(inventoryRepository.getInventoryStatus(skuCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InventoryItemNotFoundException.class, 
                () -> inventoryService.checkInventoryStatus(skuCode));
    }

    @Test
    void processRestock_WithValidRequest_ShouldRestock() {
        // Arrange
        int quantity = 50;
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(inventoryItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(inventoryItem);

        // Act
        inventoryService.processRestock(skuCode, quantity);

        // Assert
        verify(inventoryRepository, times(1)).save(any(InventoryItem.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void processRestock_WithNonExistentSku_ShouldThrowException() {
        // Arrange
        when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InventoryItemNotFoundException.class, 
                () -> inventoryService.processRestock(skuCode, 50));
    }
}












