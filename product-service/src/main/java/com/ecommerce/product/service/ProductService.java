package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String PRODUCT_TOPIC = "product-events";
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        
        // Publish product created event
        kafkaTemplate.send(PRODUCT_TOPIC, "product-created", savedProduct);
        
        log.info("Created product with id: {}", savedProduct.getId());
        return productMapper.toDto(savedProduct);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(UUID id) {
        log.info("Fetching product with id: {}", id);
        Product product = findProductOrThrow(id);
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#root.methodName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Fetching all products");
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }
    
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void updateProductRating(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // In a real app, you would calculate the average rating from the reviews
        // For now, we'll just log that the method was called
        log.info("Updating rating for product with id: {}", productId);
        
        // Save the updated product
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query) {
        return productRepository.searchProducts(query).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product existingProduct = findProductOrThrow(id);
        
        // Update fields
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setCategoryId(request.getCategoryId());
        existingProduct.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            existingProduct.setActive(request.getActive());
        }
        
        Product updatedProduct = productRepository.save(existingProduct);
        
        // Publish product updated event
        kafkaTemplate.send(PRODUCT_TOPIC, "product-updated", updatedProduct);
        
        log.info("Updated product with id: {}", id);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(UUID id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
        
        // Publish product deleted event
        kafkaTemplate.send(PRODUCT_TOPIC, "product-deleted", product);
        
        log.info("Deleted product with id: {}", id);
    }

    @Transactional
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public ProductResponse updateStock(UUID productId, int quantity) {
        Product product = findProductOrThrow(productId);
        int newStock = product.getStockQuantity() + quantity;
        
        if (newStock < 0) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        
        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);
        
        // Publish stock updated event
        kafkaTemplate.send("inventory-updates", "stock-updated", 
            new StockUpdateEvent(productId, quantity, newStock));
        
        log.info("Updated stock for product: {}. New quantity: {}", productId, newStock);
        return productMapper.toDto(updatedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByIds(List<UUID> productIds) {
        return productRepository.findAllById(productIds).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isInStock(UUID productId, int quantity) {
        return productRepository.findById(productId)
                .map(p -> p.getStockQuantity() >= quantity)
                .orElse(false);
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // Event class for stock updates
    public record StockUpdateEvent(UUID productId, int quantityChange, int newStock) {}
}
