package com.erp.enterprise.repository.sales;

import com.erp.enterprise.entity.sales.SalesOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Sales Order Repository
 *
 * Explanation:
 * - Manages sales orders from customers
 * - Queries for order tracking and reporting
 * - Status-based filtering for workflow
 */
@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    // Check if order number exists
    boolean existsByOrderNumber(String orderNumber);

    // Find by order number
    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    // Find orders by customer
    List<SalesOrder> findByCustomerIdOrderByOrderDateDesc(Long customerId);

    // Find orders by warehouse
    List<SalesOrder> findByWarehouseIdOrderByOrderDateDesc(Long warehouseId);

    // Optimized listing methods with EntityGraph to prevent N+1 queries
    @Override
    @EntityGraph(attributePaths = {"items", "customer", "warehouse"})
    @org.springframework.lang.NonNull
    List<SalesOrder> findAll();

    // Find orders by status
    @EntityGraph(attributePaths = {"items", "customer", "warehouse"})
    List<SalesOrder> findByStatusOrderByOrderDateDesc(String status);

    // Find orders in date range
    @EntityGraph(attributePaths = {"items", "customer", "warehouse"})
    List<SalesOrder> findByOrderDateBetweenOrderByOrderDateDesc(
            LocalDate startDate, LocalDate endDate);

    // Find orders by user
    List<SalesOrder> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    // Get recent orders
    @EntityGraph(attributePaths = {"items", "customer", "warehouse"})
    @Query("SELECT so FROM SalesOrder so ORDER BY so.orderDate DESC, so.createdAt DESC")
    List<SalesOrder> findRecentSalesOrders();
}