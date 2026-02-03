package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Stock Repository
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // Find stock by product and warehouse
    Optional<Stock> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    // Check if stock exists for product and warehouse
    boolean existsByProductIdAndWarehouseId(Long productId, Long warehouseId);

    // Find all stock for a product (across all warehouses)
    List<Stock> findByProductId(Long productId);

    // Find all stock in a warehouse
    List<Stock> findByWarehouseId(Long warehouseId);

    // Get total stock quantity for a product
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Stock s WHERE s.product.id = :productId")
    Integer getTotalStockForProduct(@Param("productId") Long productId);

    // Find products with low stock (below reorder level)
    @Query("SELECT s FROM Stock s WHERE s.product.reorderLevel >= " +
            "(SELECT COALESCE(SUM(st.quantity), 0) FROM Stock st WHERE st.product.id = s.product.id)")
    List<Stock> findLowStockItems();
}