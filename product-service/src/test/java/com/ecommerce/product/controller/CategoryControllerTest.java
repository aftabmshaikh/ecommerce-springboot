package com.ecommerce.product.controller;

import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category testCategory;
    private final UUID testId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testCategory = new Category();
        testCategory.setId(testId);
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Arrange
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(testCategory));
        
        // Act
        ResponseEntity<List<Category>> response = categoryController.getAllCategories();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        assertEquals(1, response.getBody().size());
        assertEquals("Electronics", response.getBody().get(0).getName());
    }

    @Test
    void getCategoryById_WithValidId_ShouldReturnCategory() {
        // Arrange
        when(categoryService.getCategoryById(testId)).thenReturn(testCategory);
        
        // Act
        ResponseEntity<Category> response = categoryController.getCategoryById(testId);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Electronics", response.getBody().getName());
    }

    @Test
    void getCategoryById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(categoryService.getCategoryById(any(UUID.class)))
            .thenThrow(new ResourceNotFoundException("Category not found with id: " + testId));
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryController.getCategoryById(testId);
        });
    }

    @Test
    void createCategory_WithValidData_ShouldReturnCreated() {
        // Arrange
        when(categoryService.createCategory(any(Category.class))).thenReturn(testCategory);
        
        // Act
        ResponseEntity<Category> response = categoryController.createCategory(testCategory);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Electronics", response.getBody().getName());
    }

    @Test
    void updateCategory_WithValidData_ShouldReturnUpdatedCategory() {
        // Arrange
        Category updatedCategory = new Category();
        updatedCategory.setName("Updated Electronics");
        updatedCategory.setSlug("updated-electronics");
        
        when(categoryService.updateCategory(eq(testId), any(Category.class))).thenReturn(updatedCategory);
        
        // Act
        ResponseEntity<Category> response = categoryController.updateCategory(testId, updatedCategory);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Electronics", response.getBody().getName());
    }

    @Test
    void deleteCategory_WithValidId_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(categoryService).deleteCategory(testId);
        
        // Act
        ResponseEntity<Void> response = categoryController.deleteCategory(testId);
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(categoryService, times(1)).deleteCategory(testId);
    }
    
    @Test
    void getCategoryBySlug_WithValidSlug_ShouldReturnCategory() {
        // Arrange
        String slug = "electronics";
        when(categoryService.getCategoryBySlug(slug)).thenReturn(testCategory);
        
        // Act
        ResponseEntity<Category> response = categoryController.getCategoryBySlug(slug);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Electronics", response.getBody().getName());
    }
    
    @Test
    void getRootCategories_ShouldReturnRootCategories() {
        // Arrange
        when(categoryService.getRootCategories()).thenReturn(Arrays.asList(testCategory));
        
        // Act
        ResponseEntity<List<Category>> response = categoryController.getRootCategories();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        assertEquals(1, response.getBody().size());
        assertEquals("Electronics", response.getBody().get(0).getName());
    }
    
    @Test
    void getSubCategories_WithValidParentId_ShouldReturnSubCategories() {
        // Arrange
        UUID parentId = UUID.randomUUID();
        when(categoryService.getSubCategories(parentId)).thenReturn(Arrays.asList(testCategory));
        
        // Act
        ResponseEntity<List<Category>> response = categoryController.getSubCategories(parentId);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isEmpty());
        assertEquals(1, response.getBody().size());
        assertEquals("Electronics", response.getBody().get(0).getName());
    }
    
    @Test
    void toggleCategoryStatus_ShouldReturnUpdatedCategory() {
        // Arrange
        boolean active = false;
        testCategory.setActive(active);
        when(categoryService.toggleCategoryStatus(testId, active)).thenReturn(testCategory);
        
        // Act
        ResponseEntity<Category> response = categoryController.toggleCategoryStatus(testId, active);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(active, response.getBody().isActive());
    }
}
