package com.erp.enterprise.repository.inventory;

import com.erp.enterprise.entity.inventory.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Supplier Repository
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Check if supplier code exists
    boolean existsBySupplierCode(String supplierCode);

    // Find supplier by code
    Optional<Supplier> findBySupplierCode(String supplierCode);

    // Find active suppliers
    List<Supplier> findByIsActive(Boolean isActive);

    // Search suppliers
    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Supplier> searchSuppliers(@Param("keyword") String keyword);
}