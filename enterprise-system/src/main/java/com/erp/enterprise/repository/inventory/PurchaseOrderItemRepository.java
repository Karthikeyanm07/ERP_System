package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.PurchaseOrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Purchase Order Item Repository
 */
@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    @Override
    @EntityGraph(attributePaths = {"product"})
    @org.springframework.lang.NonNull
    List<PurchaseOrderItem> findAll();

    // Find items by purchase order
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);

    // Find items by product
    List<PurchaseOrderItem> findByProductId(Long productId);
}