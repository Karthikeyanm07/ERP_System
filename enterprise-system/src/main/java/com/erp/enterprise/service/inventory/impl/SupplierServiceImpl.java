package com.erp.enterprise.service.inventory.impl;

import com.erp.enterprise.dto.inventory.SupplierDTO;
import com.erp.enterprise.entity.inventory.Supplier;
import com.erp.enterprise.exception.DuplicateResourceException;
import com.erp.enterprise.exception.ResourceNotFoundException;
import com.erp.enterprise.repository.inventory.SupplierRepository;
import com.erp.enterprise.service.inventory.SupplierService;
import com.erp.enterprise.util.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        if (supplierRepository.existsBySupplierCode(supplierDTO.getSupplierCode())) {
            throw new DuplicateResourceException(
                    "Supplier", "supplierCode", supplierDTO.getSupplierCode());
        }

        Supplier supplier = DtoMapper.toSupplierEntity(supplierDTO);
        Supplier savedSupplier = supplierRepository.save(supplier);

        return DtoMapper.toSupplierDTO(savedSupplier);
    }

    @Override
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return DtoMapper.toSupplierDTO(supplier);
    }

    @Override
    public SupplierDTO getSupplierByCode(String supplierCode) {
        Supplier supplier = supplierRepository.findBySupplierCode(supplierCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier", "supplierCode", supplierCode));
        return DtoMapper.toSupplierDTO(supplier);
    }

    @Override
    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(DtoMapper::toSupplierDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDTO> getActiveSuppliers() {
        return supplierRepository.findByIsActive(true).stream()
                .map(DtoMapper::toSupplierDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierDTO> searchSuppliers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSuppliers();
        }

        return supplierRepository.searchSuppliers(keyword.trim()).stream()
                .map(DtoMapper::toSupplierDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        if (!existingSupplier.getSupplierCode().equals(supplierDTO.getSupplierCode()) &&
                supplierRepository.existsBySupplierCode(supplierDTO.getSupplierCode())) {
            throw new DuplicateResourceException(
                    "Supplier", "supplierCode", supplierDTO.getSupplierCode());
        }

        existingSupplier.setSupplierCode(supplierDTO.getSupplierCode());
        existingSupplier.setName(supplierDTO.getName());
        existingSupplier.setContactPerson(supplierDTO.getContactPerson());
        existingSupplier.setEmail(supplierDTO.getEmail());
        existingSupplier.setPhone(supplierDTO.getPhone());
        existingSupplier.setAddress(supplierDTO.getAddress());
        existingSupplier.setCity(supplierDTO.getCity());
        existingSupplier.setCountry(supplierDTO.getCountry());
        existingSupplier.setIsActive(supplierDTO.getIsActive());

        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        return DtoMapper.toSupplierDTO(updatedSupplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        supplierRepository.delete(supplier);
    }
}