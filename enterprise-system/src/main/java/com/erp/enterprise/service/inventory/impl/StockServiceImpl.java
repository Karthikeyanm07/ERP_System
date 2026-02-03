package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.StockDTO;
import com.erp.enterprise.entity.inventory.*;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.inventory.ProductRepository;
import com.erp.enterprise.repository.inventory.StockRepository;
import com.erp.enterprise.repository.inventory.WarehouseRepository;
import com.erp.enterprise.service.inventory.StockService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stock Service Implementation
 *
 * Business Logic:
 * - Manages current stock levels
 * - One record per product-warehouse combination
 * - Updated by stock movements
 * - Tracks low stock alerts
 */
@Service
@Transactional
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    @Autowired
    public StockServiceImpl(StockRepository stockRepository,
                            ProductRepository productRepository,
                            WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public StockDTO getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "id", id));

        return DtoMapper.toStockDTO(stock);
    }

    @Override
    public StockDTO getStockByProductAndWarehouse(Long productId, Long warehouseId) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock",
                        "product and warehouse",
                        "Product ID: " + productId + ", Warehouse ID: " + warehouseId));

        return DtoMapper.toStockDTO(stock);
    }

    @Override
    public List<StockDTO> getStockByProduct(Long productId) {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<Stock> stockList = stockRepository.findByProductId(productId);

        return stockList.stream()
                .map(DtoMapper::toStockDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockDTO> getStockByWarehouse(Long warehouseId) {
        // Validate warehouse exists
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse", "id", warehouseId);
        }

        List<Stock> stockList = stockRepository.findByWarehouseId(warehouseId);

        return stockList.stream()
                .map(DtoMapper::toStockDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockDTO> getAllStock() {
        List<Stock> stockList = stockRepository.findAll();

        return stockList.stream()
                .map(DtoMapper::toStockDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockDTO> getLowStockItems() {
        List<Stock> lowStockItems = stockRepository.findLowStockItems();

        return lowStockItems.stream()
                .map(DtoMapper::toStockDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStock(Long productId, Long warehouseId, Integer quantityChange) {
        // Find or create stock record
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> {
                    // Create new stock record if doesn't exist
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

                    Warehouse warehouse = warehouseRepository.findById(warehouseId)
                            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

                    Stock newStock = new Stock();
                    newStock.setProduct(product);
                    newStock.setWarehouse(warehouse);
                    newStock.setQuantity(0);
                    return newStock;
                });

        // Update quantity
        stock.setQuantity(stock.getQuantity() + quantityChange);
        stock.setLastUpdated(LocalDateTime.now());

        // Business Logic: Quantity cannot be negative
        if (stock.getQuantity() < 0) {
            stock.setQuantity(0);
        }

        stockRepository.save(stock);
    }

    @Override
    public StockDTO initializeStock(Long productId, Long warehouseId) {
        // Check if stock already exists
        if (stockRepository.existsByProductIdAndWarehouseId(productId, warehouseId)) {
            return getStockByProductAndWarehouse(productId, warehouseId);
        }

        // Validate product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Validate warehouse
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        // Create stock record
        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setWarehouse(warehouse);
        stock.setQuantity(0);
        stock.setLastUpdated(LocalDateTime.now());

        Stock savedStock = stockRepository.save(stock);
        return DtoMapper.toStockDTO(savedStock);
    }
}