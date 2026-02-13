package com.erp.enterprise.repository.sales;

import com.erp.enterprise.entity.sales.SalesOrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Sales Order Item Repository
 *
 * Explanation:
 * - Manages individual line items in sales orders
 * - Used for product sales analysis
 */
@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {

    @Override
    @EntityGraph(attributePaths = {"product"})
    @org.springframework.lang.NonNull
    List<SalesOrderItem> findAll();

    // Find items by sales order
    List<SalesOrderItem> findBySalesOrderId(Long salesOrderId);

    // Find items by product (for sales analysis)
    List<SalesOrderItem> findByProductId(Long productId);
}