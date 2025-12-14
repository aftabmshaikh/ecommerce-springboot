package com.ecommerce.product.api;

import com.ecommerce.product.controller.ProductController;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
@TestPropertySource(locations = "classpath:application-test.yml")
public class ProductApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private ProductResponse testProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = ProductResponse.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .build();

        // Mock the service responses
        when(productService.getAllProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testProduct)));
        when(productService.getProductById(productId))
                .thenReturn(testProduct);
        when(productService.searchProducts(anyString()))
                .thenReturn(List.of(testProduct));
        when(productService.updateProduct(any(UUID.class), any()))
                .thenReturn(testProduct);
    }

    @Test
    void getProducts_ShouldReturnPaginatedResults() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/{id}", productId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productId.toString())))
                .andExpect(jsonPath("$.name", is(testProduct.getName())))
                .andExpect(jsonPath("$.price", is(testProduct.getPrice().doubleValue())));
    }

    @Test
    void searchProducts_WithQuery_ShouldReturnMatchingProducts() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/search")
                .param("query", "test")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(testProduct.getName())));
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreated() throws Exception {
        String requestBody = """
            {
                "name": "Test Product",
                "description": "A test product",
                "price": 99.99,
                "stockQuantity": 100,
                "categoryId": "550e8400-e29b-41d4-a716-446655440000",
                "imageUrl": "http://test.com/image.jpg",
                "sku": "TEST-SKU-001",
                "active": true
            }
            """;

        when(productService.createProduct(any())).thenReturn(testProduct);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(testProduct.getName())))
                .andExpect(jsonPath("$.price", is(testProduct.getPrice().doubleValue())));
    }

    @Test
    void updateProduct_WithValidData_ShouldUpdateProduct() throws Exception {
        String requestBody = """
            {
                "name": "Updated Product",
                "description": "Updated description",
                "price": 59.99,
                "stockQuantity": 75,
                "categoryId": "550e8400-e29b-41d4-a716-446655440000",
                "sku": "UPDATED-SKU-001",
                "active": true,
                "imageUrl": "http://test.com/updated-image.jpg"
            }
            """;

        when(productService.updateProduct(eq(productId), any())).thenReturn(testProduct);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productId.toString())))
                .andExpect(jsonPath("$.name", is(testProduct.getName())));
    }

    @Test
    void deleteProduct_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());
    }
}
