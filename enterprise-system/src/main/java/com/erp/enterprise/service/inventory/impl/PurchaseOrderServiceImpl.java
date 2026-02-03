package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.*;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.entity.inventory.*;
import com.erp.enterprise.exception.*;
import com.erp.enterprise.repository.hr.UserRepository;
import com.erp.enterprise.repository.inventory.*;
import com.erp.enterprise.service.inventory.*;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Purchase Order Service Implementation
 *
 * Business Logic:
 * - Creates purchase orders to suppliers
 * - Status flow: PENDING → APPROVED → RECEIVED
 * - Updates stock when order is received
 * - Creates stock movements for audit trail
 */
@Service
@Transactional
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final StockMovementService stockMovementService;

    @Autowired
    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository,
                                    SupplierRepository supplierRepository,
                                    WarehouseRepository warehouseRepository,
                                    ProductRepository productRepository,
                                    UserRepository userRepository,
                                    StockService stockService,
                                    StockMovementService stockMovementService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
        this.stockMovementService = stockMovementService;
    }

    @Override
    public PurchaseOrderDTO createPurchaseOrder(PurchaseOrderCreateRequest request) {
        // Check if PO number exists
        if (purchaseOrderRepository.existsByPoNumber(request.getPoNumber())) {
            throw new DuplicateResourceException("PurchaseOrder", "poNumber", request.getPoNumber());
        }

        // Validate supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier", "id", request.getSupplierId()));

        // Validate warehouse
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse", "id", request.getWarehouseId()));

        // Validate user
        User user = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", request.getCreatedById()));

        // Validate items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(
                    "Purchase order must have at least one item",
                    "NO_ITEMS");
        }

        // Create purchase order
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPoNumber(request.getPoNumber());
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setWarehouse(warehouse);
        purchaseOrder.setOrderDate(request.getOrderDate());
        purchaseOrder.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        purchaseOrder.setStatus("PENDING");
        purchaseOrder.setCreatedBy(user);

        // Create items
        for (PurchaseOrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", itemDTO.getProductId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.calculateTotalPrice();

            purchaseOrder.addItem(item);
        }

        // Calculate total
        purchaseOrder.calculateTotal();

        // Save
        PurchaseOrder savedPO = purchaseOrderRepository.save(purchaseOrder);
        return DtoMapper.toPurchaseOrderDTO(savedPO);
    }

    @Override
    public PurchaseOrderDTO getPurchaseOrderById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        return DtoMapper.toPurchaseOrderDTO(purchaseOrder);
    }

    @Override
    public PurchaseOrderDTO getPurchaseOrderByNumber(String poNumber) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PurchaseOrder", "poNumber", poNumber));

        return DtoMapper.toPurchaseOrderDTO(purchaseOrder);
    }

    @Override
    public List<PurchaseOrderDTO> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll().stream()
                .map(DtoMapper::toPurchaseOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderDTO> getPurchaseOrdersBySupplier(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier", "id", supplierId);
        }

        return purchaseOrderRepository.findBySupplierId(supplierId).stream()
                .map(DtoMapper::toPurchaseOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderDTO> getPurchaseOrdersByWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        return purchaseOrderRepository.findByWarehouseId(warehouseId).stream()
                .map(DtoMapper::toPurchaseOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderDTO> getPurchaseOrdersByStatus(String status) {
        if (!isValidPOStatus(status)) {
            throw new BusinessException("Invalid purchase order status: " + status, "INVALID_STATUS");
        }

        return purchaseOrderRepository.findByStatusOrderByOrderDateDesc(status).stream()
                .map(DtoMapper::toPurchaseOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderDTO> getPurchaseOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date", "INVALID_DATE_RANGE");
        }

        return purchaseOrderRepository.findByOrderDateBetweenOrderByOrderDateDesc(startDate, endDate).stream()
                .map(DtoMapper::toPurchaseOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderDTO> getRecentPurchaseOrders() {
        return purchaseOrderRepository.findRecentPurchaseOrders().stream()
                .limit(20)
                .map(DtoMapper::toPurchaseOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseOrderDTO approvePurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (!"PENDING".equals(purchaseOrder.getStatus())) {
            throw new BusinessException(
                    "Can only approve pending purchase orders",
                    "INVALID_STATUS_TRANSITION");
        }

        purchaseOrder.setStatus("APPROVED");
        PurchaseOrder updated = purchaseOrderRepository.save(purchaseOrder);

        return DtoMapper.toPurchaseOrderDTO(updated);
    }

    @Override
    public PurchaseOrderDTO receivePurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if (!"APPROVED".equals(purchaseOrder.getStatus())) {
            throw new BusinessException(
                    "Can only receive approved purchase orders",
                    "INVALID_STATUS_TRANSITION");
        }

        // Update stock for each item
        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            // Update stock
            stockService.updateStock(
                    item.getProduct().getId(),
                    purchaseOrder.getWarehouse().getId(),
                    item.getQuantity()
            );

            // Create stock movement record
            stockMovementService.createStockMovement(
                    item.getProduct().getId(),
                    purchaseOrder.getWarehouse().getId(),
                    "IN",
                    item.getQuantity(),
                    "PURCHASE_ORDER",
                    purchaseOrder.getId(),
                    "Received from PO: " + purchaseOrder.getPoNumber(),
                    purchaseOrder.getCreatedBy().getId()
            );
        }

        // Update PO status
        purchaseOrder.setStatus("RECEIVED");
        PurchaseOrder updated = purchaseOrderRepository.save(purchaseOrder);

        return DtoMapper.toPurchaseOrderDTO(updated);
    }

    @Override
    public PurchaseOrderDTO cancelPurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if ("RECEIVED".equals(purchaseOrder.getStatus())) {
            throw new BusinessException(
                    "Cannot cancel received purchase orders",
                    "CANNOT_CANCEL");
        }

        purchaseOrder.setStatus("CANCELLED");
        PurchaseOrder updated = purchaseOrderRepository.save(purchaseOrder);

        return DtoMapper.toPurchaseOrderDTO(updated);
    }

    @Override
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));

        if ("RECEIVED".equals(purchaseOrder.getStatus())) {
            throw new BusinessException(
                    "Cannot delete received purchase orders",
                    "CANNOT_DELETE");
        }

        purchaseOrderRepository.delete(purchaseOrder);
    }

    private boolean isValidPOStatus(String status) {
        return status != null &&
                (status.equals("PENDING") ||
                        status.equals("APPROVED") ||
                        status.equals("RECEIVED") ||
                        status.equals("CANCELLED"));
    }
}