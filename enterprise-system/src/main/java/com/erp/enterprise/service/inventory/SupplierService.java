package com.erp.enterprise.service.inventory;

import com.erp.enterprise.dto.inventory.SupplierDTO;
import java.util.List;

public interface SupplierService {

    SupplierDTO createSupplier(SupplierDTO supplierDTO);
    SupplierDTO getSupplierById(Long id);
    SupplierDTO getSupplierByCode(String supplierCode);
    List<SupplierDTO> getAllSuppliers();
    List<SupplierDTO> getActiveSuppliers();
    List<SupplierDTO> searchSuppliers(String keyword);
    SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO);
    void deleteSupplier(Long id);
}