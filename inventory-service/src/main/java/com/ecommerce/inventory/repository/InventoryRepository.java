package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.inventory.dto.InventoryResponse;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    Optional<InventoryItem> findBySkuCode(String skuCode);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.skuCode = :skuCode")
    Optional<InventoryItem> findBySkuCodeForUpdate(@Param("skuCode") String skuCode);
    
    List<InventoryItem> findByProductIdIn(List<UUID> productIds);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.availableQuantity <= i.lowStockThreshold AND i.isActive = true")
    List<InventoryItem> findLowStockItems();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.availableQuantity <= i.restockThreshold AND i.isActive = true")
    List<InventoryItem> findItemsNeedingRestock();
    
    @Query("SELECT i FROM InventoryItem i WHERE i.nextRestockDate IS NOT NULL AND i.nextRestockDate <= CURRENT_TIMESTAMP")
    List<InventoryItem> findItemsDueForRestock();
    
    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity + :adjustment, " +
           "i.availableQuantity = (i.quantity + :adjustment) - i.reservedQuantity " +
           "WHERE i.skuCode = :skuCode AND (i.quantity + :adjustment) >= 0")
    int adjustInventory(@Param("skuCode") String skuCode, @Param("adjustment") int adjustment);
    
    @Modifying
    @Query("UPDATE InventoryItem i SET i.reservedQuantity = i.reservedQuantity + :quantity, " +
           "i.availableQuantity = i.quantity - (i.reservedQuantity + :quantity) " +
           "WHERE i.skuCode = :skuCode AND (i.quantity - (i.reservedQuantity + :quantity)) >= 0")
    int reserveStock(@Param("skuCode") String skuCode, @Param("quantity") int quantity);
    
    @Modifying
    @Query("UPDATE InventoryItem i SET " +
           "i.reservedQuantity = i.reservedQuantity - :quantity, " +
           "i.availableQuantity = i.availableQuantity + :quantity " +
           "WHERE i.skuCode = :skuCode AND i.reservedQuantity >= :quantity")
    int releaseStock(@Param("skuCode") String skuCode, @Param("quantity") int quantity);
    
    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity - :quantity, " +
           "i.reservedQuantity = i.reservedQuantity - :quantity " +
           "WHERE i.skuCode = :skuCode AND i.quantity >= :quantity AND i.reservedQuantity >= :quantity")
    int consumeReservedStock(@Param("skuCode") String skuCode, @Param("quantity") int quantity);
    
    @Query("SELECT NEW com.ecommerce.inventory.dto.InventoryResponse$InventoryStatus(" +
           "i.skuCode, i.availableQuantity > 0, i.availableQuantity, i.availableQuantity <= i.lowStockThreshold, " +
           "CASE " +
           "  WHEN i.availableQuantity <= 0 THEN 'OUT_OF_STOCK' " +
           "  WHEN i.availableQuantity <= i.lowStockThreshold THEN 'LOW_STOCK' " +
           "  ELSE 'IN_STOCK' " +
           "END) " +
           "FROM InventoryItem i WHERE i.skuCode = :skuCode")
    Optional<InventoryResponse.InventoryStatus> getInventoryStatus(@Param("skuCode") String skuCode);
    
    @Query("SELECT NEW com.ecommerce.inventory.dto.InventoryResponse$StockLevel(" +
           "i.skuCode, i.availableQuantity, i.lowStockThreshold, i.restockThreshold, " +
           "CASE " +
           "  WHEN i.availableQuantity <= 0 THEN 'OUT_OF_STOCK' " +
           "  WHEN i.availableQuantity <= i.lowStockThreshold THEN 'LOW_STOCK' " +
           "  WHEN i.availableQuantity <= i.restockThreshold THEN 'NEEDS_RESTOCK' " +
           "  ELSE 'IN_STOCK' " +
           "END) " +
           "FROM InventoryItem i WHERE i.skuCode = :skuCode")
    Optional<InventoryResponse.StockLevel> getStockLevel(@Param("skuCode") String skuCode);
    
    @Query("SELECT COUNT(i) > 0 FROM InventoryItem i WHERE i.skuCode = :skuCode AND i.availableQuantity >= :quantity")
    boolean isInStock(@Param("skuCode") String skuCode, @Param("quantity") int quantity);
    
    boolean existsBySkuCode(String skuCode);
}
