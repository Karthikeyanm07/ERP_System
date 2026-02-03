package com.erp.enterprise.service.inventory;

import com.erp.enterprise.dto.inventory.PurchaseOrderCreateRequest;
import com.erp.enterprise.dto.inventory.PurchaseOrderDTO;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderDTO createPurchaseOrder(PurchaseOrderCreateRequest request);
    PurchaseOrderDTO getPurchaseOrderById(Long id);
    PurchaseOrderDTO getPurchaseOrderByNumber(String poNumber);
    List<PurchaseOrderDTO> getAllPurchaseOrders();
    List<PurchaseOrderDTO> getPurchaseOrdersBySupplier(Long supplierId);
    List<PurchaseOrderDTO> getPurchaseOrdersByWarehouse(Long warehouseId);
    List<PurchaseOrderDTO> getPurchaseOrdersByStatus(String status);
    List<PurchaseOrderDTO> getPurchaseOrdersByDateRange(LocalDate startDate, LocalDate endDate);
    List<PurchaseOrderDTO> getRecentPurchaseOrders();

    PurchaseOrderDTO approvePurchaseOrder(Long id);
    PurchaseOrderDTO receivePurchaseOrder(Long id);
    PurchaseOrderDTO cancelPurchaseOrder(Long id);

    void deletePurchaseOrder(Long id);
}