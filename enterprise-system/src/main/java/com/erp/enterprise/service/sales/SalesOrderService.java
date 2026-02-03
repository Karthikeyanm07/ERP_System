package com.erp.enterprise.service.sales;

import com.erp.enterprise.dto.sales.SalesOrderCreateRequest;
import com.erp.enterprise.dto.sales.SalesOrderDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Sales Order Service Interface
 *
 * Explanation:
 * - Manages customer orders
 * - Validates stock availability
 * - Reduces inventory when confirmed
 * - Links to invoice generation
 */
public interface SalesOrderService {

    SalesOrderDTO createSalesOrder(SalesOrderCreateRequest request);
    SalesOrderDTO getSalesOrderById(Long id);
    SalesOrderDTO getSalesOrderByNumber(String orderNumber);
    List<SalesOrderDTO> getAllSalesOrders();
    List<SalesOrderDTO> getSalesOrdersByCustomer(Long customerId);
    List<SalesOrderDTO> getSalesOrdersByWarehouse(Long warehouseId);
    List<SalesOrderDTO> getSalesOrdersByStatus(String status);
    List<SalesOrderDTO> getSalesOrdersByDateRange(LocalDate startDate, LocalDate endDate);
    List<SalesOrderDTO> getRecentSalesOrders();

    SalesOrderDTO confirmSalesOrder(Long id);
    SalesOrderDTO shipSalesOrder(Long id);
    SalesOrderDTO deliverSalesOrder(Long id);
    SalesOrderDTO cancelSalesOrder(Long id);

    void deleteSalesOrder(Long id);
}