package com.ecommerce.product.service;

import com.ecommerce.product.model.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    
    List<Category> getAllCategories();
    
    Category getCategoryById(UUID id);
    
    Category getCategoryBySlug(String slug);
    
    List<Category> getRootCategories();
    
    List<Category> getSubCategories(UUID parentId);
    
    Category createCategory(Category category);
    
    Category updateCategory(UUID id, Category categoryDetails);
    
    void deleteCategory(UUID id);
    
    Category toggleCategoryStatus(UUID id, boolean active);
    
    boolean existsByName(String name);
    
    boolean existsBySlug(String slug);
}
