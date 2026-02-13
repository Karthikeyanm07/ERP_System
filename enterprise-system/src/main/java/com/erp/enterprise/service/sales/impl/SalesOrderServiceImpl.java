package com.erp.enterprise.service.sales.impl;

import com.erp.enterprise.dto.sales.SalesOrderCreateRequest;
import com.erp.enterprise.dto.sales.SalesOrderDTO;
import com.erp.enterprise.dto.sales.SalesOrderItemDTO;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.entity.inventory.Product;
import com.erp.enterprise.entity.inventory.Warehouse;
import com.erp.enterprise.entity.sales.*;
import com.erp.enterprise.exception.*;
import com.erp.enterprise.repository.hr.UserRepository;
import com.erp.enterprise.repository.inventory.*;
import com.erp.enterprise.repository.sales.*;
import com.erp.enterprise.service.common.SequenceGeneratorService;
import com.erp.enterprise.service.inventory.StockMovementService;
import com.erp.enterprise.service.inventory.StockService;
import com.erp.enterprise.service.sales.*;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sales Order Service Implementation
 *
 * Business Logic:
 * - Creates sales orders from customers
 * - Validates stock availability
 * - Status flow: PENDING → CONFIRMED → SHIPPED → DELIVERED
 * - Reduces stock when confirmed
 * - Creates stock movements for audit trail
 * - Prevents overselling
 */
@Service
@Transactional
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final StockMovementService stockMovementService;
    private final InvoiceService invoiceService;
    private final SequenceGeneratorService sequenceGenerator;

    @Autowired
    public SalesOrderServiceImpl(SalesOrderRepository salesOrderRepository,
                                 CustomerRepository customerRepository,
                                 WarehouseRepository warehouseRepository,
                                 ProductRepository productRepository,
                                 UserRepository userRepository,
                                 StockRepository stockRepository,
                                 StockService stockService,
                                 StockMovementService stockMovementService,
                                 InvoiceService invoiceService,
                                 SequenceGeneratorService sequenceGenerator) {
        this.salesOrderRepository = salesOrderRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.stockMovementService = stockMovementService;
        this.invoiceService = invoiceService;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public SalesOrderDTO createSalesOrder(SalesOrderCreateRequest request) {
        /**
         * Business Logic:
         * 1. Validate order number uniqueness
         * 2. Validate customer, warehouse, user
         * 3. Check stock availability for all items
         * 4. Calculate totals
         * 5. Create order in PENDING status
         */

        // Auto-generate order number if blank
        String orderNumber = request.getOrderNumber();
        if (orderNumber == null || orderNumber.isBlank()) {
            orderNumber = sequenceGenerator.nextSalesOrderNumber();
        }
        if (salesOrderRepository.existsByOrderNumber(orderNumber)) {
            throw new DuplicateResourceException(
                    "SalesOrder", "orderNumber", orderNumber);
        }

        // Validate customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", request.getCustomerId()));

        // Validate warehouse
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse", "id", request.getWarehouseId()));

        // Validate user — default to user 1 if not provided
        Long userId = request.getCreatedById() != null ? request.getCreatedById() : 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", userId));

        // Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Sales order must have at least one item", "NO_ITEMS");
        }

        // Create sales order
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderNumber(orderNumber);
        salesOrder.setCustomer(customer);
        salesOrder.setWarehouse(warehouse);
        salesOrder.setOrderDate(request.getOrderDate());
        salesOrder.setDeliveryDate(request.getDeliveryDate());
        salesOrder.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        salesOrder.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        salesOrder.setStatus("PENDING");
        salesOrder.setCreatedBy(user);
        salesOrder.setNotes(request.getNotes());

        // Create items and validate stock
        List<Long> productIds = request.getItems().stream()
                .map(SalesOrderItemDTO::getProductId)
                .collect(Collectors.toList());
        
        Map<Long, Integer> stockMap = getStockMap(productIds);

        for (SalesOrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", itemDTO.getProductId()));

            // Business Logic: Check TOTAL stock availability across ALL warehouses
            Integer totalStock = stockMap.getOrDefault(product.getId(), 0);

            if (totalStock < itemDTO.getQuantity()) {
                throw new BusinessException(
                        String.format("Insufficient stock for product %s. Available: %d, Required: %d",
                                product.getName(),
                                totalStock,
                                itemDTO.getQuantity()),
                        "INSUFFICIENT_STOCK"
                );
            }

            SalesOrderItem item = new SalesOrderItem();
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.calculateTotalPrice();

            salesOrder.addItem(item);
        }

        // Calculate totals
        salesOrder.calculateTotals();

        // Save
        SalesOrder savedSO = salesOrderRepository.save(salesOrder);
        return DtoMapper.toSalesOrderDTO(savedSO);
    }

    private Map<Long, Integer> getStockMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Object[]> results = stockRepository.getTotalStockForProducts(productIds);
        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> ((Number) r[1]).intValue(),
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public SalesOrderDTO getSalesOrderById(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));

        return DtoMapper.toSalesOrderDTO(salesOrder);
    }

    @Override
    public SalesOrderDTO getSalesOrderByNumber(String orderNumber) {
        SalesOrder salesOrder = salesOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SalesOrder", "orderNumber", orderNumber));

        return DtoMapper.toSalesOrderDTO(salesOrder);
    }

    @Override
    public List<SalesOrderDTO> getAllSalesOrders() {
        return salesOrderRepository.findAll().stream()
                .map(DtoMapper::toSalesOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesOrderDTO> getSalesOrdersByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return salesOrderRepository.findByCustomerIdOrderByOrderDateDesc(customerId).stream()
                .map(DtoMapper::toSalesOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesOrderDTO> getSalesOrdersByWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        return salesOrderRepository.findByWarehouseIdOrderByOrderDateDesc(warehouseId).stream()
                .map(DtoMapper::toSalesOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesOrderDTO> getSalesOrdersByStatus(String status) {
        if (!isValidSOStatus(status)) {
            throw new BusinessException("Invalid sales order status: " + status, "INVALID_STATUS");
        }

        return salesOrderRepository.findByStatusOrderByOrderDateDesc(status).stream()
                .map(DtoMapper::toSalesOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesOrderDTO> getSalesOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date", "INVALID_DATE_RANGE");
        }

        return salesOrderRepository.findByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate).stream()
                .map(DtoMapper::toSalesOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SalesOrderDTO> getRecentSalesOrders() {
        return salesOrderRepository.findRecentSalesOrders().stream()
                .limit(20)
                .map(DtoMapper::toSalesOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SalesOrderDTO confirmSalesOrder(Long id) {
        /**
         * Business Logic:
         * - Changes status from PENDING to CONFIRMED
         * - Reduces stock for all items
         * - Creates stock movement records (OUT)
         * - This is when inventory is actually reserved
         */

        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));

        if (!"PENDING".equals(salesOrder.getStatus())) {
            throw new BusinessException(
                    "Can only confirm pending sales orders",
                    "INVALID_STATUS_TRANSITION");
        }

        // Reduce stock for each item
        for (SalesOrderItem item : salesOrder.getItems()) {
            // Update stock (negative quantity = reduction)
            stockService.updateStock(
                    item.getProduct().getId(),
                    salesOrder.getWarehouse().getId(),
                    -item.getQuantity()
            );

            // Create stock movement record
            stockMovementService.createStockMovement(
                    item.getProduct().getId(),
                    salesOrder.getWarehouse().getId(),
                    "OUT",
                    item.getQuantity(),
                    "SALES_ORDER",
                    salesOrder.getId(),
                    "Sold via SO: " + salesOrder.getOrderNumber(),
                    salesOrder.getCreatedBy().getId()
            );
        }

        // Update status
        salesOrder.setStatus("CONFIRMED");
        SalesOrder updated = salesOrderRepository.save(salesOrder);

        return DtoMapper.toSalesOrderDTO(updated);
    }

    @Override
    public SalesOrderDTO shipSalesOrder(Long id) {
        /**
         * Business Logic:
         * - Changes status from CONFIRMED to SHIPPED
         * - Indicates order has left the warehouse
         */

        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));

        if (!"CONFIRMED".equals(salesOrder.getStatus())) {
            throw new BusinessException(
                    "Can only ship confirmed sales orders",
                    "INVALID_STATUS_TRANSITION");
        }

        salesOrder.setStatus("SHIPPED");
        SalesOrder updated = salesOrderRepository.save(salesOrder);

        return DtoMapper.toSalesOrderDTO(updated);
    }

    @Override
    public SalesOrderDTO deliverSalesOrder(Long id) {
        /**
         * Business Logic:
         * - Changes status from SHIPPED to DELIVERED
         * - Indicates customer received the order
         * - AUTO: Generates invoice for the delivered order
         */

        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));

        if (!"SHIPPED".equals(salesOrder.getStatus())) {
            throw new BusinessException(
                    "Can only deliver shipped sales orders",
                    "INVALID_STATUS_TRANSITION");
        }

        salesOrder.setStatus("DELIVERED");
        SalesOrder updated = salesOrderRepository.save(salesOrder);

        // Phase 1: Auto-generate invoice on delivery
        invoiceService.autoCreateInvoiceForDeliveredOrder(updated);

        return DtoMapper.toSalesOrderDTO(updated);
    }

    @Override
    public SalesOrderDTO cancelSalesOrder(Long id) {
        /**
         * Business Logic:
         * - Can cancel PENDING or CONFIRMED orders
         * - If CONFIRMED, stock needs to be added back
         * - Cannot cancel SHIPPED or DELIVERED orders
         */

        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));

        if ("SHIPPED".equals(salesOrder.getStatus()) || "DELIVERED".equals(salesOrder.getStatus())) {
            throw new BusinessException(
                    "Cannot cancel shipped or delivered sales orders",
                    "CANNOT_CANCEL");
        }

        // If order was confirmed, add stock back
        if ("CONFIRMED".equals(salesOrder.getStatus())) {
            for (SalesOrderItem item : salesOrder.getItems()) {
                // Add stock back
                stockService.updateStock(
                        item.getProduct().getId(),
                        salesOrder.getWarehouse().getId(),
                        item.getQuantity()
                );

                // Create stock movement record
                stockMovementService.createStockMovement(
                        item.getProduct().getId(),
                        salesOrder.getWarehouse().getId(),
                        "IN",
                        item.getQuantity(),
                        "SALES_ORDER",
                        salesOrder.getId(),
                        "Cancelled SO: " + salesOrder.getOrderNumber(),
                        salesOrder.getCreatedBy().getId()
                );
            }
        }

        salesOrder.setStatus("CANCELLED");
        SalesOrder updated = salesOrderRepository.save(salesOrder);

        return DtoMapper.toSalesOrderDTO(updated);
    }

    @Override
    public void deleteSalesOrder(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", "id", id));

        if ("CONFIRMED".equals(salesOrder.getStatus()) ||
                "SHIPPED".equals(salesOrder.getStatus()) ||
                "DELIVERED".equals(salesOrder.getStatus())) {
            throw new BusinessException(
                    "Cannot delete confirmed, shipped, or delivered sales orders",
                    "CANNOT_DELETE");
        }

        salesOrderRepository.delete(salesOrder);
    }

    private boolean isValidSOStatus(String status) {
        return status != null &&
                (status.equals("PENDING") ||
                        status.equals("CONFIRMED") ||
                        status.equals("SHIPPED") ||
                        status.equals("DELIVERED") ||
                        status.equals("CANCELLED"));
    }
}