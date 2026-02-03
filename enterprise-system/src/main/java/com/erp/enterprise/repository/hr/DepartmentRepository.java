package com.erp.enterprise.repository.hr;

import com.erp.enterprise.entity.hr.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repository for Department
// Business Logic: JpaRepository provides basic CRUD (save, findById, findAll, delete, etc.)
// We add custom query methods as needed
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Custom query method - Spring automatically implements this
    // Business Logic: Check if department name already exists (for duplicate prevention)
    boolean existsByName(String name);

    // Find department by name
    Optional<Department> findByName(String name);

    // Spring Data JPA automatically creates SQL like:
    // SELECT * FROM departments WHERE name = ?
}