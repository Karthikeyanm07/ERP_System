package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.WarehouseDTO;
import com.erp.enterprise.entity.hr.Employee;
import com.erp.enterprise.entity.inventory.Warehouse;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.hr.EmployeeRepository;
import com.erp.enterprise.repository.inventory.WarehouseRepository;
import com.erp.enterprise.service.inventory.WarehouseService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public WarehouseServiceImpl(WarehouseRepository warehouseRepository,
                                EmployeeRepository employeeRepository) {
        this.warehouseRepository = warehouseRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public WarehouseDTO createWarehouse(WarehouseDTO warehouseDTO) {
        Warehouse warehouse = DtoMapper.toWarehouseEntity(warehouseDTO);

        if (warehouseDTO.getManagerId() != null) {
            Employee manager = employeeRepository.findById(warehouseDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", warehouseDTO.getManagerId()));
            warehouse.setManager(manager);
        }

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return DtoMapper.toWarehouseDTO(savedWarehouse);
    }

    @Override
    public WarehouseDTO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        return DtoMapper.toWarehouseDTO(warehouse);
    }

    @Override
    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(DtoMapper::toWarehouseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WarehouseDTO> getActiveWarehouses() {
        return warehouseRepository.findByIsActive(true).stream()
                .map(DtoMapper::toWarehouseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseDTO updateWarehouse(Long id, WarehouseDTO warehouseDTO) {
        Warehouse existingWarehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

        existingWarehouse.setName(warehouseDTO.getName());
        existingWarehouse.setLocation(warehouseDTO.getLocation());
        existingWarehouse.setIsActive(warehouseDTO.getIsActive());

        if (warehouseDTO.getManagerId() != null) {
            Employee manager = employeeRepository.findById(warehouseDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", warehouseDTO.getManagerId()));
            existingWarehouse.setManager(manager);
        } else {
            existingWarehouse.setManager(null);
        }

        Warehouse updatedWarehouse = warehouseRepository.save(existingWarehouse);
        return DtoMapper.toWarehouseDTO(updatedWarehouse);
    }

    @Override
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

        warehouseRepository.delete(warehouse);
    }
}