package com.ecommerce.product;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest productRequest;
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Setup test product
        product = Product.builder()
                .id(productId)
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .active(true)
                .build();
                
        // Setup test product request
        productRequest = ProductRequest.builder()
                .sku("SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .active(true)
                .build();
        
        // Set additional required fields
        product.setCategoryId(UUID.randomUUID());
        product.setImageUrl("http://test.com/image.jpg");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(1L);
        
        // Setup basic mapper behavior (can be overridden in individual tests)
        lenient().when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        lenient().when(productMapper.toDto(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return ProductResponse.fromEntity(p);
        });
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(product);
        when(productMapper.toDto(any(Product.class))).thenReturn(ProductResponse.fromEntity(product));

        // Act
        ProductResponse result = productService.createProduct(productRequest);

        // Assert
        assertNotNull(result);
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getDescription(), result.getDescription());
        assertEquals(product.getPrice(), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(Product.class));
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(any(Product.class))).thenReturn(ProductResponse.fromEntity(product));

        // Act
        ProductResponse result = productService.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductNotFound() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void updateProduct_ShouldUpdateProduct_WhenProductExists() {
        // Arrange
        Product updatedProduct = Product.builder()
                .id(productId)
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199.99))
                .stockQuantity(50)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        lenient().when(productMapper.toEntity(any(ProductRequest.class))).thenReturn(updatedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(ProductResponse.fromEntity(updatedProduct));

        // Act
        ProductResponse result = productService.updateProduct(productId, productRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updatedProduct.getName(), result.getName());
        assertEquals(updatedProduct.getDescription(), result.getDescription());
        assertEquals(updatedProduct.getPrice(), result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(Product.class));
    }

    @Test
    void updateStock_ShouldUpdateStock_WhenSufficientStock() {
        // Arrange
        int quantityToAdd = 10;
        Product productWithUpdatedStock = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(110) // 100 (initial) + 10
                .active(true)
                .sku("SKU-001")
                .categoryId(UUID.randomUUID())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(productWithUpdatedStock);
        when(productMapper.toDto(any(Product.class))).thenReturn(ProductResponse.fromEntity(productWithUpdatedStock));

        // Act
        ProductResponse result = productService.updateStock(productId, quantityToAdd);

        // Assert
        assertNotNull(result);
        assertEquals(110, result.getStockQuantity()); // Verify the exact expected value
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        // Act & Assert
        assertDoesNotThrow(() -> productService.deleteProduct(productId));
        verify(productRepository, times(1)).delete(any(Product.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(Product.class));
    }
}
