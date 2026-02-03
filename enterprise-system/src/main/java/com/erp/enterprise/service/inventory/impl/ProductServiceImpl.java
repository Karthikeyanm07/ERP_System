package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.ProductDTO;
import com.erp.enterprise.entity.inventory.*;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.inventory.*;
import com.erp.enterprise.service.inventory.ProductService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockRepository = stockRepository;
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
        return productRepository.findAll().stream()
                .map(product -> {
                    ProductDTO dto = DtoMapper.toProductDTO(product);
                    dto.setTotalStock(stockRepository.getTotalStockForProduct(product.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByIsActive(true).stream()
                .map(product -> {
                    ProductDTO dto = DtoMapper.toProductDTO(product);
                    dto.setTotalStock(stockRepository.getTotalStockForProduct(product.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }

        return productRepository.findByCategoryId(categoryId).stream()
                .map(product -> {
                    ProductDTO dto = DtoMapper.toProductDTO(product);
                    dto.setTotalStock(stockRepository.getTotalStockForProduct(product.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }

        return productRepository.searchProducts(keyword.trim()).stream()
                .map(product -> {
                    ProductDTO dto = DtoMapper.toProductDTO(product);
                    dto.setTotalStock(stockRepository.getTotalStockForProduct(product.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsBelowReorderLevel() {
        return productRepository.findProductsBelowReorderLevel().stream()
                .map(product -> {
                    ProductDTO dto = DtoMapper.toProductDTO(product);
                    dto.setTotalStock(stockRepository.getTotalStockForProduct(product.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
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

        productRepository.delete(product);
    }
}