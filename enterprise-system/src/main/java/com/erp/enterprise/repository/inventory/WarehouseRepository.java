package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.Warehouse;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Warehouse Repository
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    @Override
    @EntityGraph(attributePaths = {"manager"})
    @org.springframework.lang.NonNull
    List<Warehouse> findAll();

    // Check if warehouse name exists
    boolean existsByName(String name);

    // Find active warehouses
    List<Warehouse> findByIsActive(Boolean isActive);

    // Find warehouses by manager
    List<Warehouse> findByManagerId(Long managerId);
}