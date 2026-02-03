package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Stock Movement Repository
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    // Find movements by product
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Find movements by warehouse
    List<StockMovement> findByWarehouseIdOrderByCreatedAtDesc(Long warehouseId);

    // Find movements by type
    List<StockMovement> findByMovementTypeOrderByCreatedAtDesc(String movementType);

    // Find movements by product and warehouse
    List<StockMovement> findByProductIdAndWarehouseIdOrderByCreatedAtDesc(
            Long productId, Long warehouseId);

    // Find movements in date range
    @Query("SELECT sm FROM StockMovement sm WHERE " +
            "DATE(sm.createdAt) BETWEEN :startDate AND :endDate " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find movements by reference
    List<StockMovement> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}