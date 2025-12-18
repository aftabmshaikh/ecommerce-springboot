package com.ecommerce.product.integration;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);
    }

    @Test
    void createProduct_WithValidRequest_ShouldPersistProduct() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .categoryId(categoryId)
                .imageUrl("http://example.com/image.jpg")
                .active(true)
                .build();

        // Act
        ProductResponse result = productService.createProduct(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("SKU-001", result.getSku());
        assertEquals("Test Product", result.getName());
        assertEquals(BigDecimal.valueOf(99.99), result.getPrice());
        assertEquals(100, result.getStockQuantity());

        // Verify product is persisted
        Product savedProduct = productRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedProduct);
        assertEquals("SKU-001", savedProduct.getSku());
    }

    @Test
    void getProductById_WithExistingProduct_ShouldReturnProduct() {
        // Arrange - Create a product first
        Product product = createTestProduct();
        Product savedProduct = productRepository.save(product);

        // Act
        ProductResponse result = productService.getProductById(savedProduct.getId());

        // Assert
        assertNotNull(result);
        assertEquals(savedProduct.getId(), result.getId());
        assertEquals(savedProduct.getName(), result.getName());
    }

    @Test
    void updateProduct_WithExistingProduct_ShouldUpdateProduct() {
        // Arrange
        Product product = createTestProduct();
        Product savedProduct = productRepository.save(product);

        ProductRequest updateRequest = ProductRequest.builder()
                .sku("SKU-001")
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(149.99))
                .stockQuantity(150)
                .categoryId(categoryId)
                .imageUrl("http://example.com/image.jpg")
                .active(true)
                .build();

        // Act
        ProductResponse result = productService.updateProduct(savedProduct.getId(), updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(BigDecimal.valueOf(149.99), result.getPrice());

        // Verify product is updated in database
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getName());
    }

    @Test
    void updateStock_WithValidQuantity_ShouldUpdateStock() {
        // Arrange
        Product product = createTestProduct();
        product.setStockQuantity(100);
        Product savedProduct = productRepository.save(product);

        // Act
        ProductResponse result = productService.updateStock(savedProduct.getId(), 50);

        // Assert
        assertNotNull(result);
        assertEquals(150, result.getStockQuantity()); // 100 + 50

        // Verify stock is updated in database
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(150, updatedProduct.getStockQuantity());
    }

    @Test
    void deleteProduct_WithExistingProduct_ShouldDeleteProduct() {
        // Arrange
        Product product = createTestProduct();
        Product savedProduct = productRepository.save(product);

        // Act
        productService.deleteProduct(savedProduct.getId());

        // Assert
        assertFalse(productRepository.existsById(savedProduct.getId()));
    }

    @Test
    void isInStock_WithSufficientStock_ShouldReturnTrue() {
        // Arrange
        Product product = createTestProduct();
        product.setStockQuantity(100);
        Product savedProduct = productRepository.save(product);

        // Act
        boolean result = productService.isInStock(savedProduct.getId(), 50);

        // Assert
        assertTrue(result);
    }

    @Test
    void isInStock_WithInsufficientStock_ShouldReturnFalse() {
        // Arrange
        Product product = createTestProduct();
        product.setStockQuantity(10);
        Product savedProduct = productRepository.save(product);

        // Act
        boolean result = productService.isInStock(savedProduct.getId(), 50);

        // Assert
        assertFalse(result);
    }

    private Product createTestProduct() {
        return Product.builder()
                .sku("SKU-" + UUID.randomUUID().toString().substring(0, 8))
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .categoryId(categoryId)
                .imageUrl("http://example.com/image.jpg")
                .active(true)
                .build();
    }
}


