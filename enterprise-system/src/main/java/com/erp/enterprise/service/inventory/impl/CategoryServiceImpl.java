package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.CategoryDTO;
import com.erp.enterprise.entity.inventory.*;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.inventory.CategoryRepository;
import com.erp.enterprise.service.inventory.CategoryService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new DuplicateResourceException("Category", "name", categoryDTO.getName());
        }

        Category category = DtoMapper.toCategoryEntity(categoryDTO);

        if (categoryDTO.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", categoryDTO.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        return DtoMapper.toCategoryDTO(savedCategory);
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return DtoMapper.toCategoryDTO(category);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(DtoMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getTopLevelCategories() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(DtoMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getChildCategories(Long parentCategoryId) {
        if (!categoryRepository.existsById(parentCategoryId)) {
            throw new ResourceNotFoundException("Category", "id", parentCategoryId);
        }

        return categoryRepository.findByParentCategoryId(parentCategoryId).stream()
                .map(DtoMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!existingCategory.getName().equals(categoryDTO.getName()) &&
                categoryRepository.existsByName(categoryDTO.getName())) {
            throw new DuplicateResourceException("Category", "name", categoryDTO.getName());
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        if (categoryDTO.getParentCategoryId() != null) {
            if (categoryDTO.getParentCategoryId().equals(id)) {
                throw new BusinessException(
                        "Category cannot be its own parent",
                        "CIRCULAR_PARENT_REFERENCE");
            }

            Category parentCategory = categoryRepository.findById(categoryDTO.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", categoryDTO.getParentCategoryId()));
            existingCategory.setParentCategory(parentCategory);
        } else {
            existingCategory.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        return DtoMapper.toCategoryDTO(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        List<Category> childCategories = categoryRepository.findByParentCategoryId(id);
        if (!childCategories.isEmpty()) {
            throw new BusinessException(
                    "Cannot delete category with child categories",
                    "CATEGORY_HAS_CHILDREN");
        }

        categoryRepository.delete(category);
    }
}