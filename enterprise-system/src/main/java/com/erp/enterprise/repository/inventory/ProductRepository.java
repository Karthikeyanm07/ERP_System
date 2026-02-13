package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    @EntityGraph(attributePaths = {"category"})
    @org.springframework.lang.NonNull
    List<Product> findAll();

    // Check if product code exists
    boolean existsByProductCode(String productCode);

    // Find product by code
    Optional<Product> findByProductCode(String productCode);

    // Find products by category
    List<Product> findByCategoryId(Long categoryId);

    // Find active products
    List<Product> findByIsActive(Boolean isActive);

    // Search products
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.productCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    // Find products below reorder level
    @Query("SELECT p FROM Product p WHERE p.id IN " +
            "(SELECT s.product.id FROM Stock s " +
            "GROUP BY s.product.id " +
            "HAVING SUM(s.quantity) <= p.reorderLevel)")
    List<Product> findProductsBelowReorderLevel();
}