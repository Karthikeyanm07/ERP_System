package com.erp.enterprise.service.inventory;

import com.erp.enterprise.dto.inventory.CategoryDTO;
import java.util.List;

public interface CategoryService {

    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO getCategoryById(Long id);
    List<CategoryDTO> getAllCategories();
    List<CategoryDTO> getTopLevelCategories();
    List<CategoryDTO> getChildCategories(Long parentCategoryId);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategory(Long id);
}