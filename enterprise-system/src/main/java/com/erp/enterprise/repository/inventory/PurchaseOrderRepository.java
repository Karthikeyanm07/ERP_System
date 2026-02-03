package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Purchase Order Repository
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // Check if PO number exists
    boolean existsByPoNumber(String poNumber);

    // Find by PO number
    Optional<PurchaseOrder> findByPoNumber(String poNumber);

    // Find by supplier
    List<PurchaseOrder> findBySupplierId(Long supplierId);

    // Find by warehouse
    List<PurchaseOrder> findByWarehouseId(Long warehouseId);

    // Find by status
    List<PurchaseOrder> findByStatusOrderByOrderDateDesc(String status);

    // Find by date range
    List<PurchaseOrder> findByOrderDateBetweenOrderByOrderDateDesc(
            LocalDate startDate, LocalDate endDate);

    // Find by created by
    List<PurchaseOrder> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    // Get recent purchase orders
    @Query("SELECT po FROM PurchaseOrder po ORDER BY po.orderDate DESC, po.createdAt DESC")
    List<PurchaseOrder> findRecentPurchaseOrders();
}