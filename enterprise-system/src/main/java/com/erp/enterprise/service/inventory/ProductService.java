package com.erp.enterprise.service.inventory;

import com.erp.enterprise.dto.inventory.ProductDTO;
import java.util.List;

public interface ProductService {

    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    ProductDTO getProductByCode(String productCode);
    List<ProductDTO> getAllProducts();
    List<ProductDTO> getActiveProducts();
    List<ProductDTO> getProductsByCategory(Long categoryId);
    List<ProductDTO> searchProducts(String keyword);
    List<ProductDTO> getProductsBelowReorderLevel();
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
}