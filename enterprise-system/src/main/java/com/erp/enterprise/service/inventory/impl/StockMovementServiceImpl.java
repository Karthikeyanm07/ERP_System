package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.StockMovementDTO;
import com.erp.enterprise.entity.inventory.*;
import com.erp.enterprise.entity.hr.User;
import com.erp.enterprise.exception.BusinessException;
import com.erp.enterprise.exception.ResourceNotFoundException;
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
 * Stock Movement Service Implementation
 *
 * Business Logic:
 * - Creates immutable audit trail of all stock changes
 * - Types: IN (receipt), OUT (issue), TRANSFER, ADJUSTMENT
 * - Links to source transactions
 */
@Service
@Transactional
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    @Autowired
    public StockMovementServiceImpl(StockMovementRepository stockMovementRepository,
                                    ProductRepository productRepository,
                                    WarehouseRepository warehouseRepository,
                                    UserRepository userRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public StockMovementDTO getStockMovementById(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMovement", "id", id));

        return DtoMapper.toStockMovementDTO(movement);
    }

    @Override
    public List<StockMovementDTO> getAllStockMovements() {
        return stockMovementRepository.findAll().stream()
                .map(DtoMapper::toStockMovementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getStockMovementsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(DtoMapper::toStockMovementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getStockMovementsByWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        return stockMovementRepository.findByWarehouseIdOrderByCreatedAtDesc(warehouseId).stream()
                .map(DtoMapper::toStockMovementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getStockMovementsByType(String movementType) {
        if (!isValidMovementType(movementType)) {
            throw new BusinessException("Invalid movement type: " + movementType, "INVALID_TYPE");
        }

        return stockMovementRepository.findByMovementTypeOrderByCreatedAtDesc(movementType).stream()
                .map(DtoMapper::toStockMovementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getStockMovementsByProductAndWarehouse(Long productId, Long warehouseId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        return stockMovementRepository.findByProductIdAndWarehouseIdOrderByCreatedAtDesc(
                        productId, warehouseId).stream()
                .map(DtoMapper::toStockMovementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovementDTO> getStockMovementsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date", "INVALID_DATE_RANGE");
        }

        return stockMovementRepository.findByDateRange(startDate, endDate).stream()
                .map(DtoMapper::toStockMovementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StockMovementDTO createStockMovement(
            Long productId,
            Long warehouseId,
            String movementType,
            Integer quantity,
            String referenceType,
            Long referenceId,
            String remarks,
            Long createdById) {

        // Validate product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Validate warehouse
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        // Validate user
        User user = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createdById));

        // Validate movement type
        if (!isValidMovementType(movementType)) {
            throw new BusinessException("Invalid movement type: " + movementType, "INVALID_TYPE");
        }

        // Create movement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setRemarks(remarks);
        movement.setCreatedBy(user);

        StockMovement savedMovement = stockMovementRepository.save(movement);
        return DtoMapper.toStockMovementDTO(savedMovement);
    }

    private boolean isValidMovementType(String movementType) {
        return movementType != null &&
                (movementType.equals("IN") ||
                        movementType.equals("OUT") ||
                        movementType.equals("TRANSFER") ||
                        movementType.equals("ADJUSTMENT"));
    }
}