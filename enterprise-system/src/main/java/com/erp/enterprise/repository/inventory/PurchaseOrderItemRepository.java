package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Purchase Order Item Repository
 */
@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    // Find items by purchase order
    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);

    // Find items by product
    List<PurchaseOrderItem> findByProductId(Long productId);
}