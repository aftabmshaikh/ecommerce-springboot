package com.ecommerce.product.service;

import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private UUID testId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testId = UUID.randomUUID();
        testCategory = new Category();
        testCategory.setId(testId);
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
        testCategory.setDescription("Electronic devices and accessories");
        testCategory.setActive(true);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(testCategory));
        
        // Act
        List<Category> categories = categoryService.getAllCategories();
        
        // Assert
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }

    @Test
    void getCategoryById_WithValidId_ShouldReturnCategory() {
        // Arrange
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        
        // Act
        Category found = categoryService.getCategoryById(testId);
        
        // Assert
        assertNotNull(found);
        assertEquals("Electronics", found.getName());
    }

    @Test
    void getCategoryById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryById(testId);
        });
    }

    @Test
    void getCategoryBySlug_WithValidSlug_ShouldReturnCategory() {
        // Arrange
        String slug = "electronics";
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(testCategory));
        
        // Act
        Category found = categoryService.getCategoryBySlug(slug);
        
        // Assert
        assertNotNull(found);
        assertEquals("Electronics", found.getName());
    }

    @Test
    void getRootCategories_ShouldReturnRootCategories() {
        // Arrange
        when(categoryRepository.findRootCategories()).thenReturn(Collections.singletonList(testCategory));
        
        // Act
        List<Category> categories = categoryService.getRootCategories();
        
        // Assert
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }

    @Test
    void getSubCategories_WithValidParentId_ShouldReturnSubCategories() {
        // Arrange
        UUID parentId = UUID.randomUUID();
        when(categoryRepository.findSubCategories(parentId)).thenReturn(Collections.singletonList(testCategory));
        
        // Act
        List<Category> categories = categoryService.getSubCategories(parentId);
        
        // Assert
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertEquals(1, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
    }

    @Test
    void createCategory_WithValidData_ShouldReturnSavedCategory() {
        // Arrange
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        // Act
        Category saved = categoryService.createCategory(testCategory);
        
        // Assert
        assertNotNull(saved);
        assertEquals("Electronics", saved.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_WithParent_ShouldSetParentCategory() {
        // Arrange
        UUID parentId = UUID.randomUUID();
        Category parentCategory = new Category();
        parentCategory.setId(parentId);
        parentCategory.setName("Parent Category");
        
        testCategory.setParent(parentCategory);
        
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        // Act
        Category saved = categoryService.createCategory(testCategory);
        
        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getParent());
        assertEquals("Parent Category", saved.getParent().getName());
    }

    @Test
    void updateCategory_WithValidData_ShouldReturnUpdatedCategory() {
        // Arrange
        Category updatedDetails = new Category();
        updatedDetails.setName("Updated Electronics");
        updatedDetails.setSlug("updated-electronics");
        
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Category updated = categoryService.updateCategory(testId, updatedDetails);
        
        // Assert
        assertNotNull(updated);
        assertEquals("Updated Electronics", updated.getName());
        assertEquals("updated-electronics", updated.getSlug());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void deleteCategory_WithValidId_ShouldDeleteCategory() {
        // Arrange
        when(categoryRepository.hasProducts(testId)).thenReturn(false);
        when(categoryRepository.findByParentId(testId)).thenReturn(Collections.emptyList());
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        
        // Act
        categoryService.deleteCategory(testId);
        
        // Assert
        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    void deleteCategory_WithProducts_ShouldThrowException() {
        // Arrange
        when(categoryRepository.hasProducts(testId)).thenReturn(true);
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            categoryService.deleteCategory(testId);
        });
    }

    @Test
    void deleteCategory_WithSubcategories_ShouldThrowException() {
        // Arrange
        when(categoryRepository.hasProducts(testId)).thenReturn(false);
        when(categoryRepository.findByParentId(testId)).thenReturn(Collections.singletonList(new Category()));
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            categoryService.deleteCategory(testId);
        });
    }

    @Test
    void toggleCategoryStatus_ShouldUpdateActiveStatus() {
        // Arrange
        boolean newStatus = false;
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Category updated = categoryService.toggleCategoryStatus(testId, newStatus);
        
        // Assert
        assertNotNull(updated);
        assertEquals(newStatus, updated.isActive());
        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    void existsByName_ShouldReturnTrueWhenNameExists() {
        // Arrange
        String name = "Electronics";
        when(categoryRepository.existsByName(name)).thenReturn(true);
        
        // Act
        boolean exists = categoryService.existsByName(name);
        
        // Assert
        assertTrue(exists);
        verify(categoryRepository, times(1)).existsByName(name);
    }

    @Test
    void existsBySlug_ShouldReturnTrueWhenSlugExists() {
        // Arrange
        String slug = "electronics";
        when(categoryRepository.existsBySlug(slug)).thenReturn(true);
        
        // Act
        boolean exists = categoryService.existsBySlug(slug);
        
        // Assert
        assertTrue(exists);
        verify(categoryRepository, times(1)).existsBySlug(slug);
    }
}
