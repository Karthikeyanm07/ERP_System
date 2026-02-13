package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.ProductDTO;
import com.erp.enterprise.entity.inventory.*;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.inventory.*;
import com.erp.enterprise.repository.sales.SalesOrderItemRepository;
import com.erp.enterprise.service.inventory.ProductService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              StockRepository stockRepository,
                              StockMovementRepository stockMovementRepository,
                              PurchaseOrderItemRepository purchaseOrderItemRepository,
                              SalesOrderItemRepository salesOrderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        if (productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new DuplicateResourceException(
                    "Product", "productCode", productDTO.getProductCode());
        }

        Product product = DtoMapper.toProductEntity(productDTO);

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", productDTO.getCategoryId()));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        ProductDTO resultDTO = DtoMapper.toProductDTO(savedProduct);
        resultDTO.setTotalStock(0);  // New product has no stock

        return resultDTO;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        ProductDTO productDTO = DtoMapper.toProductDTO(product);
        productDTO.setTotalStock(stockRepository.getTotalStockForProduct(id));

        return productDTO;
    }

    @Override
    public ProductDTO getProductByCode(String productCode) {
        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productCode", productCode));

        ProductDTO productDTO = DtoMapper.toProductDTO(product);
        productDTO.setTotalStock(stockRepository.getTotalStockForProduct(product.getId()));

        return productDTO;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return mapToDtosWithStock(products);
    }

    @Override
    public List<ProductDTO> getActiveProducts() {
        List<Product> products = productRepository.findByIsActive(true);
        return mapToDtosWithStock(products);
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }

        List<Product> products = productRepository.findByCategoryId(categoryId);
        return mapToDtosWithStock(products);
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }

        List<Product> products = productRepository.searchProducts(keyword.trim());
        return mapToDtosWithStock(products);
    }

    @Override
    public List<ProductDTO> getProductsBelowReorderLevel() {
        List<Product> products = productRepository.findProductsBelowReorderLevel();
        return mapToDtosWithStock(products);
    }

    /**
     * Maps product entities to DTOs and fetches stock levels in bulk to prevent N+1 queries.
     */
    private List<ProductDTO> mapToDtosWithStock(List<Product> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        Map<Long, Integer> stockMap = getStockMap(productIds);

        return products.stream()
                .map(product -> {
                    ProductDTO dto = DtoMapper.toProductDTO(product);
                    dto.setTotalStock(stockMap.getOrDefault(product.getId(), 0));
                    return dto;
                })
                .collect(Collectors.toList());
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
                        (existing, replacement) -> existing // Should not happen with GROUP BY
                ));
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!existingProduct.getProductCode().equals(productDTO.getProductCode()) &&
                productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new DuplicateResourceException(
                    "Product", "productCode", productDTO.getProductCode());
        }

        existingProduct.setProductCode(productDTO.getProductCode());
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setUnit(productDTO.getUnit());
        existingProduct.setUnitPrice(productDTO.getUnitPrice());
        existingProduct.setReorderLevel(productDTO.getReorderLevel());
        existingProduct.setIsActive(productDTO.getIsActive());

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category", "id", productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        ProductDTO resultDTO = DtoMapper.toProductDTO(updatedProduct);
        resultDTO.setTotalStock(stockRepository.getTotalStockForProduct(id));

        return resultDTO;
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Business Logic: Check for Transactional History
        if (!purchaseOrderItemRepository.findByProductId(id).isEmpty() ||
            !salesOrderItemRepository.findByProductId(id).isEmpty()) {
            throw new com.erp.enterprise.exception.BusinessException(
                "Cannot delete product with purchase or sales history. Deactivate it instead to preserve records.",
                "PRODUCT_HAS_HISTORY"
            );
        }

        // Clean up inventory data (Stock and Movements) before deletion
        stockRepository.deleteByProductId(id);
        stockMovementRepository.deleteByProductId(id);

        productRepository.delete(product);
    }
}