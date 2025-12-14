package com.ecommerce.product.service.impl;

import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getSubCategories(UUID parentId) {
        return categoryRepository.findSubCategories(parentId);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Set parent category if parentId is provided
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = getCategoryById(category.getParent().getId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(UUID id, Category categoryDetails) {
        Category category = getCategoryById(id);
        
        // Update category fields
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setImageUrl(categoryDetails.getImageUrl());
        category.setSlug(categoryDetails.getSlug());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setActive(categoryDetails.isActive());
        
        // Update parent category if needed
        if (categoryDetails.getParent() != null && categoryDetails.getParent().getId() != null) {
            if (!categoryDetails.getParent().getId().equals(id)) { // Prevent circular reference
                Category parent = getCategoryById(categoryDetails.getParent().getId());
                category.setParent(parent);
            }
        } else {
            category.setParent(null);
        }
        
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = getCategoryById(id);
        
        // Check if category has any products
        if (categoryRepository.hasProducts(id)) {
            throw new IllegalStateException("Cannot delete category with existing products");
        }
        
        // Check if category has subcategories
        List<Category> subCategories = categoryRepository.findByParentId(id);
        if (!subCategories.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public Category toggleCategoryStatus(UUID id, boolean active) {
        Category category = getCategoryById(id);
        category.setActive(active);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }
}
