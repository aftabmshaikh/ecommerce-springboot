package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.base.BaseIntegrationTest;
import com.ecommerce.inventory.model.InventoryItem;
import com.ecommerce.inventory.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TODO: Optimize Testcontainers Docker client initialization to reduce test startup time
 * 
 * Issue: Testcontainers is taking a long time to initialize the Docker client during test execution.
 * The Docker client initialization happens before each test class that uses Testcontainers, causing
 * significant delays (especially on first run or when Docker Desktop is not running).
 * 
 * Symptoms:
 * - Long wait at "DockerClientProviderStrategy" initialization
 * - "ImageNameSubstitutor" loading messages
 * - Tests taking 30+ seconds just to start
 * 
 * Potential solutions:
 * 1. Ensure Docker Desktop is running before tests
 * 2. Create ~/.testcontainers.properties with optimized settings:
 *    - testcontainers.reuse.enable=true
 *    - testcontainers.ryuk.container.image=testcontainers/ryuk:0.5.1
 * 3. Consider using @DataJpaTest with H2 for simple repository tests (faster, no Docker)
 * 4. Use Testcontainers container reuse (already enabled in BaseIntegrationTest)
 * 5. Pre-pull Docker images: docker pull postgres:15-alpine
 * 6. Configure Docker environment variables to speed up client detection
 * 
 * Note: This test extends BaseIntegrationTest which uses PostgreSQL Testcontainer.
 * For faster execution, consider splitting into:
 * - Unit tests using @DataJpaTest with H2 (no Docker needed)
 * - Integration tests using Testcontainers (for complex queries/transactions)
 */
class InventoryRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private InventoryRepository inventoryRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveInventoryItem() {
        // Given
        InventoryItem item = TestUtils.createTestInventoryItem();
        
        // When
        InventoryItem savedItem = inventoryRepository.save(item);
        Optional<InventoryItem> foundItem = inventoryRepository.findById(savedItem.getId());
        
        // Then
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getSkuCode()).isEqualTo(item.getSkuCode());
        assertThat(foundItem.get().getProductId()).isEqualTo(item.getProductId());
    }

    @Test
    @Transactional
    void shouldNotSaveDuplicateSkuCode() {
        // Given
        InventoryItem item1 = TestUtils.createTestInventoryItem();
        InventoryItem savedItem1 = inventoryRepository.save(item1);
        entityManager.flush(); // Force immediate flush to database
        
        InventoryItem item2 = TestUtils.createTestInventoryItem();
        item2.setSkuCode(savedItem1.getSkuCode());
        item2.setProductId(UUID.randomUUID()); // Ensure productId is different to avoid unique constraint
        
        // When/Then
        assertThatThrownBy(() -> {
            inventoryRepository.saveAndFlush(item2); // Should fail due to duplicate skuCode
            entityManager.flush();
        }).isInstanceOf(Exception.class);
        
        // Verify the first item is still there
        assertThat(inventoryRepository.findById(savedItem1.getId())).isPresent();
    }

    @Test
    void shouldFindBySkuCode() {
        // Given
        InventoryItem item = TestUtils.createTestInventoryItem();
        inventoryRepository.save(item);
        
        // When
        Optional<InventoryItem> foundItem = inventoryRepository.findBySkuCode(item.getSkuCode());
        
        // Then
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getSkuCode()).isEqualTo(item.getSkuCode());
    }

    @Test
    void shouldFindLowStockItems() {
        // Given
        InventoryItem lowStockItem = TestUtils.createTestInventoryItem();
        lowStockItem.setQuantity(5); // Below low stock threshold (10)
        inventoryRepository.save(lowStockItem);
        
        InventoryItem inStockItem = TestUtils.createTestInventoryItem();
        inStockItem.setSkuCode("SKU-67890");
        inStockItem.setQuantity(50); // Above low stock threshold
        inventoryRepository.save(inStockItem);
        
        // When
        List<InventoryItem> lowStockItems = inventoryRepository.findLowStockItems();
        
        // Then
        assertThat(lowStockItems).hasSize(1);
        assertThat(lowStockItems.get(0).getSkuCode()).isEqualTo(lowStockItem.getSkuCode());
    }

    @Test
    @Transactional
    void shouldAdjustInventory() {
        // Given
        InventoryItem item = TestUtils.createTestInventoryItem();
        InventoryItem savedItem = inventoryRepository.save(item);
        
        // Flush to ensure the item is saved before the update
        inventoryRepository.flush();
        
        // When
        int updated = inventoryRepository.adjustInventory(savedItem.getSkuCode(), 10);
        
        // Then
        assertThat(updated).isEqualTo(1);
        
        // Clear the persistence context to ensure we get fresh data from the database
        entityManager.clear();
        
        Optional<InventoryItem> updatedItem = inventoryRepository.findById(savedItem.getId());
        assertThat(updatedItem).isPresent();
        assertThat(updatedItem.get().getQuantity()).isEqualTo(savedItem.getQuantity() + 10);
        assertThat(updatedItem.get().getAvailableQuantity())
                .isEqualTo(savedItem.getAvailableQuantity() + 10);
    }

    @Test
    @Transactional
    void shouldConsumeReservedStock() {
        // Given
        InventoryItem item = TestUtils.createTestInventoryItem();
        item.setReservedQuantity(30);
        item.setAvailableQuantity(item.getQuantity() - item.getReservedQuantity());
        InventoryItem savedItem = inventoryRepository.save(item);
        
        // Flush to ensure the item is saved before the update
        inventoryRepository.flush();
        
        // When
        int consumed = inventoryRepository.consumeReservedStock(savedItem.getSkuCode(), 10);
        
        // Then
        assertThat(consumed).isEqualTo(1);
        
        // Clear the persistence context to ensure we get fresh data from the database
        entityManager.clear();
        
        Optional<InventoryItem> updatedItem = inventoryRepository.findById(savedItem.getId());
        assertThat(updatedItem).isPresent();
        assertThat(updatedItem.get().getQuantity()).isEqualTo(savedItem.getQuantity() - 10);
        assertThat(updatedItem.get().getReservedQuantity()).isEqualTo(savedItem.getReservedQuantity() - 10);
        assertThat(updatedItem.get().getAvailableQuantity())
                .isEqualTo(updatedItem.get().getQuantity() - updatedItem.get().getReservedQuantity());
    }

    @Test
    @Transactional
    void shouldReleaseStock() {
        // Given
        InventoryItem item = TestUtils.createTestInventoryItem();
        item.setReservedQuantity(10); // Set initial reserved quantity
        item.setAvailableQuantity(item.getQuantity() - item.getReservedQuantity());
        InventoryItem savedItem = inventoryRepository.save(item);
        
        // Flush to ensure the item is saved before the update
        entityManager.flush();
        
        // When - release the reserved stock
        int rowsUpdated = inventoryRepository.releaseStock(savedItem.getSkuCode(), savedItem.getReservedQuantity());
        
        // Clear the persistence context to ensure we get fresh data from the database
        entityManager.clear();
        
        // Then
        assertThat(rowsUpdated).isEqualTo(1);
        
        // Verify the updated state
        Optional<InventoryItem> updatedItem = inventoryRepository.findBySkuCode(savedItem.getSkuCode());
        assertThat(updatedItem).isPresent();
        
        // After releasing all reserved quantity, reservedQuantity should be 0
        assertThat(updatedItem.get().getReservedQuantity()).isZero();
        
        // Available quantity should be equal to the total quantity after releasing all reserved items
        assertThat(updatedItem.get().getAvailableQuantity())
                .isEqualTo(updatedItem.get().getQuantity());
    }
}
