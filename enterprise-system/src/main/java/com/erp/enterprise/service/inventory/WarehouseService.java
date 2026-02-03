package com.erp.enterprise.service.inventory;

import com.erp.enterprise.dto.inventory.WarehouseDTO;
import java.util.List;

public interface WarehouseService {

    WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO);
    WarehouseDTO getWarehouseById(Long id);
    List<WarehouseDTO> getAllWarehouses();
    List<WarehouseDTO> getActiveWarehouses();
    WarehouseDTO updateWarehouse(Long id, WarehouseDTO warehouseDTO);
    void deleteWarehouse(Long id);
}