package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Category Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Override
    @EntityGraph(attributePaths = {"parentCategory"})
    @org.springframework.lang.NonNull
    List<Category> findAll();

    // Check if category name exists
    boolean existsByName(String name);

    // Find category by name
    Optional<Category> findByName(String name);

    // Find categories by parent
    List<Category> findByParentCategoryId(Long parentCategoryId);

    // Find top-level categories (no parent)
    List<Category> findByParentCategoryIsNull();
}